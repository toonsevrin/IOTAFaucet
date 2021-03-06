package com.sevrin.toon.IOTAFaucet.backend;

import com.google.gson.Gson;
import com.sevrin.toon.IOTAFaucet.User;
import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.ProcessorTransaction;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.database.StoredTransaction;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import jota.model.Transaction;
import jota.utils.Converter;
import jota.utils.InputValidator;

import java.util.concurrent.TimeUnit;

/**
 * Created by toonsev on 6/18/2017.
 */
public class Backend {
    public static final String VERSION = "v0.0.1";

    private final Scheduler scheduler = Schedulers.io();
    private IotaProvider iotaProvider;
    private DatabaseProvider databaseProvider;
    private FaucetConfig faucetConfig;

    private BroadcastingLoop broadcastingLoop;
    private ConfirmLoop confirmLoop;
    private RestartBundleLoop restartBundleLoop;
    private SendingLoop sendingLoop;
    private StartLoop startLoop;

    public Backend(IotaProvider iotaProvider, DatabaseProvider databaseProvider, FaucetConfig faucetConfig) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
        this.faucetConfig = faucetConfig;

        this.broadcastingLoop = new BroadcastingLoop(iotaProvider, databaseProvider);
        this.confirmLoop = new ConfirmLoop(iotaProvider, databaseProvider);
        this.restartBundleLoop = new RestartBundleLoop(iotaProvider, databaseProvider);
        this.sendingLoop = new SendingLoop(iotaProvider, databaseProvider);
        this.startLoop = new StartLoop(iotaProvider, databaseProvider);
        scheduler.schedulePeriodicallyDirect(broadcastingLoop, 0, faucetConfig.getBroadcastLoopIntervalInMillis(), TimeUnit.MILLISECONDS);
        scheduler.schedulePeriodicallyDirect(confirmLoop, 0, faucetConfig.getConfirmLoopIntervalInMillis(), TimeUnit.MILLISECONDS);
        scheduler.schedulePeriodicallyDirect(restartBundleLoop, 0, faucetConfig.getRestartLoopIntervalInMillis(), TimeUnit.MILLISECONDS);
        scheduler.schedulePeriodicallyDirect(sendingLoop, 0, faucetConfig.getSendingLoopIntervalInMillis(), TimeUnit.MILLISECONDS);
        scheduler.schedulePeriodicallyDirect(startLoop, 0, faucetConfig.getStartLoopIntervalInMillis(), TimeUnit.MILLISECONDS);
    }

    public Observable<DoWorkReq> getWorkObservable(Observable<DoWorkRes> responseObservable) {
        responseObservable.observeOn(Schedulers.io()).subscribe((doWorkRes) -> handleDoWorkResponse(doWorkRes));
        return broadcastingLoop.getDoWorkReqSubject();
    }

    public void handleDoWorkResponse(DoWorkRes doWorkRes) {
        if (doWorkRes.getHash() == null || doWorkRes.getProcessorTransactionUniqueId() == null || doWorkRes.getProcessorId() == null) {
            System.out.println("Received doworkres with bad params");
            return;//this is a bad request...
        }
        StoredBundle bundle = databaseProvider.getCurrentBundle();
        if (bundle == null || bundle.getProcessor() == null || bundle.getCurrentTransaction() == null || bundle.getStopped() != null) {
            System.out.println("Received doworkres while none should be done");
        }
        if (!bundle.getCurrentTransaction().equals(doWorkRes.getProcessorTransactionUniqueId())) {
            System.out.println("Received doworkres with wrong processorTransactionId");
            return;
        }
        if (!bundle.getProcessor().equals(doWorkRes.getProcessorId())) {
            System.out.println("Received doworkres with wrong processorTransactionId");
            return;
        }
        ProcessorTransaction transaction = databaseProvider.getProcessorTransaction(bundle.getProcessor(), bundle.getCurrentTransaction());

        int[] validatedState = iotaProvider.validateTransaction(doWorkRes.getHash(), transaction.getState(), transaction.getMinWeightMagnitude());
        if (validatedState == null) {
            System.out.println("Received doworkres with invalid hash");
            return;
        }
        String hashedTrytes = Converter.trytes(iotaProvider.getTritssWithHash(Converter.trits(transaction.getTrytes()), Converter.trits(doWorkRes.getHash())));
        System.out.println("Trytes: " + hashedTrytes);
        boolean updatedHash = databaseProvider.setHashedProcessorTransactionTrytes(transaction.getProcessorId(), transaction.getUniqueId(), transaction.getState(), hashedTrytes);
        if (updatedHash) {
            System.out.println("Saved transaction hash " + doWorkRes.getHash() + "to database.");
            ProcessorTransaction nextTransaction = databaseProvider.getNextProcessorTransaction(bundle.getProcessor(), transaction.getIndexInBundle());
            System.out.println(new Gson().toJson(nextTransaction));
            if (nextTransaction == null) {
                boolean stopped = databaseProvider.stopBundle(bundle.getBundleId(), bundle.getCurrentTransaction());
                if (stopped)
                    System.out.println("Succesfully stopped bundle " + bundle.getBundleId() + " after receiving the last hash.");
                else
                    System.out.println("Failed to stop bundle, maybe it was already stopped?");
            } else {
                boolean nextTransactionUpdated = databaseProvider.updateCurrentInBundle(bundle.getBundleId(), transaction.getUniqueId(), new Transaction(hashedTrytes).getHash(), nextTransaction.getUniqueId());
                if (nextTransactionUpdated) {
                    System.out.println("Next transaction saved, broadcasting it now...");
                    broadcastingLoop.run();
                } else
                    System.out.println("Failed to save next transaction, maybe one is saved already.");
            }
        } else
            System.out.println("Failed to save worked hash to database, maybe another person cut you to it?");
    }


    public HandleRewardResponse handleReward(User user) {
        try {
            Long tokensReceivedTime = databaseProvider.getLastTokensReceived(user);
            if (tokensReceivedTime != null) {
                long difference = tokensReceivedTime + faucetConfig.getTokenReceiveIntervalInMillis() - System.currentTimeMillis();
                if (difference > 0)
                    return new HandleRewardResponse(-2, "You will need to wait " + formatTimeToWait(difference) + ".");
            }
            long payout = getPayoutPerRequest();
            databaseProvider.setTokensReceived(user, payout);
            if (!InputValidator.isAddress(user.getWalletAddress()))
                return new HandleRewardResponse(-1, "This is not an address.");
            StoredTransaction transaction = databaseProvider.addTransaction(user.getWalletAddress(), payout);
            if (transaction == null)
                return new HandleRewardResponse(-3, "Received a null transaction from databaseProvider");
            return new HandleRewardResponse(transaction, "Your transaction is now pending. Stay on the site to confirm it!");
        } catch (Exception e) {
            e.printStackTrace();
            return new HandleRewardResponse(-1, "A backend error occured while trying to process your request.");
        }
    }

    private long getPayoutPerRequest() {
        //possibly some randomness here
        return faucetConfig.getPayoutPerRequest();
    }


    private static String formatTimeToWait(long millis) {
        return String.format("%02dhours, %02dminutes and %02dseconds",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
