package com.sevrin.toon.IOTAFaucet.web;

import com.sevrin.toon.IOTAFaucet.FaucetConfig;
import com.sevrin.toon.IOTAFaucet.User;
import com.sevrin.toon.IOTAFaucet.beta.HandleRewardResponse;
import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.database.StoredTransaction;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import com.sevrin.toon.IOTAFaucet.web.DoWorkReq;
import com.sevrin.toon.IOTAFaucet.web.DoWorkRes;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import jota.error.BroadcastAndStoreException;
import jota.model.Transaction;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by toonsev on 6/16/2017.
 */
public class Backend {
    private DatabaseProvider databaseProvider;

    private IotaProvider iotaProvider;
    private final long interval;
    private final long payoutPerRequest;

    private final Subject<DoWorkReq> workReqStream = PublishSubject.<DoWorkReq>create().toSerialized();

    public Backend(IotaProvider iotaProvider, DatabaseProvider databaseProvider, FaucetConfig faucetConfig) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
        this.interval = faucetConfig.getIntervalInMillis();
        this.payoutPerRequest = faucetConfig.getPayoutPerRequest();
    }

    public HandleRewardResponse handleReward(User user) {
        try {
            Long tokensReceivedTime = databaseProvider.getLastTokensReceived(user);
            if (tokensReceivedTime != null) {
                long difference = tokensReceivedTime + interval - System.currentTimeMillis();
                if (difference > 0)
                    return new HandleRewardResponse(-2, "You will need to wait " + getTimeToWait(difference) + ".");
            }
            databaseProvider.setTokensReceived(user);
            StoredTransaction transaction = databaseProvider.addTransaction(user.getWalletAddress(), payoutPerRequest);
            if (transaction == null)
                return new HandleRewardResponse(-3, "Received a null transaction from databaseProvider");
            return new HandleRewardResponse(transaction, "Your transaction is now pending. Stay on the site to confirm it!");
        } catch (Exception e) {
            e.printStackTrace();
            return new HandleRewardResponse(-1, "A backend error occured while trying to process your request.");
        }
    }

    /**
     * Bidirectional stream for browser work
     *
     * @param responseObservable
     * @return
     */
    public Observable<DoWorkReq> getWorkObservable(Observable<DoWorkRes> responseObservable) {
        responseObservable.observeOn(Schedulers.io()).subscribe((doWorkRes) -> handleDoWorkResponse(doWorkRes));
        return workReqStream;
    }

    private void handleDoWorkResponse(DoWorkRes res) {
        StoredTransaction transaction = databaseProvider.getTransaction(res.getTransactionId());
        if (transaction == null) {
            System.out.println("Received a hash for a transaction which is not stored in the database");
            return;
        }
        if (transaction.getStarted() == null || transaction.getStopped() != null) {
            System.out.println("Received a hash for a transaction that has yet to finish or is already finished");
            return;
        }
        int[] transformedState = iotaProvider.validateTransaction(res.getHash(), transaction.getOriginalStateTrytes(), transaction.getMinWeightMagnitude());
        if (transformedState == null) {
            StoredBundle storedBundle = databaseProvider.getBundleByTransaction(transaction.getTransactionId());
            StoredBundle previousBundle = databaseProvider.getBundleByBundleId(storedBundle.getBundleIndex());
            long start = previousBundle.getStarted();
            StoredTransaction nextTransaction = databaseProvider.getNextSortedTransaction(start, storedBundle.getStarted(), transaction.getTransactionId());//create db method
            if (nextTransaction == null) {
                if (databaseProvider.stopBundle(transaction.getTransactionId())) {
                    updateStoppedBundles();
                }
            } else
                databaseProvider.updateCurrentInBundle(transaction.getTransactionId(), nextTransaction.getTransactionId(), nextTransaction.getOriginalStateTrytes());
        }
    }

    private void updateStoppedBundles() {
        //broadcast unsend bundles
        databaseProvider.getStoppedBundles(false, false).forEach(storedBundle -> {
            List<String> transactions = getHashTrytesList(storedBundle);
            if (transactions.isEmpty()) {
                System.out.println("An empty bundle is finished? This is no good!");
            } else {
                try {
                    iotaProvider.broadcastAndStore(transactions.toArray(new String[transactions.size()]));
                    databaseProvider.sendBundle(storedBundle.getBundleIndex());
                } catch (BroadcastAndStoreException e) {
                    e.printStackTrace();
                }
            }
        });

        //confirm unconfirmed bundles
        databaseProvider.getStoppedBundles(true, false).forEach(storedBundle -> {
            if (iotaProvider.isBundleConfirmed(getHashTrytesList(storedBundle)))
                databaseProvider.confirmBundle(storedBundle.getBundleIndex());
        });

        //TODO: if there are no more unconfirmed bundles, start working on the latest bundle.
    }

    private List<String> getHashTrytesList(StoredBundle bundle) {
        StoredBundle previousBundle = databaseProvider.getBundleByBundleId(bundle.getBundleIndex() - 1);
        long startTimestamp = previousBundle == null ? 0 : previousBundle.getStarted();
        return StreamSupport.stream(databaseProvider.getTransactionsWithinRange(startTimestamp, bundle.getStarted()).spliterator(), false)
                .map(storedTransaction -> storedTransaction.getHashedTrytes())
                .collect(Collectors.toList());
    }

    private String getTimeToWait(long millis) {
        return String.format("%02dhours, %02dminutes and %02dseconds",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
