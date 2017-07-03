package com.sevrin.toon.IOTAFaucet.backend;

import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.ProcessorTransaction;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import io.reactivex.schedulers.Schedulers;
import jota.error.*;
import jota.model.Transaction;
import org.bson.types.ObjectId;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by toonsev on 6/18/2017.
 */
public class RestartBundleLoop implements Runnable {
    private DatabaseProvider databaseProvider;
    private IotaProvider iotaProvider;
    private boolean curling = false;
    private static final long SPAM_INTERVAL = TimeUnit.SECONDS.toMillis(30);

    public RestartBundleLoop(IotaProvider iotaProvider, DatabaseProvider databaseProvider) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void run() {
        for (StoredBundle storedBundle : databaseProvider.getStoppedBundles(true, false))
            if (storedBundle.getSent() + SPAM_INTERVAL < System.currentTimeMillis())
                spamToTop(storedBundle);
    }

    private void spamToTop(StoredBundle storedBundle) {
        if (isCurling())
            return;
        Schedulers.io().scheduleDirect(() -> {//we don't want to hold up the restart task
            try {
                setCurling(true);
                ObjectId lastTransactionId = storedBundle.getLastTransaction();
                if (lastTransactionId == null) {
                    System.out.println("RestartLoop: No last transaction?");
                    return;
                }
                ProcessorTransaction lastTransaction = databaseProvider.getProcessorTransaction(storedBundle.getProcessor(), lastTransactionId);
                String newSpam = getNewSpammedTransaction(new Transaction(lastTransaction.getHashedTrytes()).getHash());
                if (newSpam != null)
                    databaseProvider.setLastSpammed(storedBundle.getBundleId(), storedBundle.getLastSpammed());
                else
                    System.out.println("RestartLoop: NewSpam returned null?");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                setCurling(false);
            }
        });
    }


    private synchronized boolean isCurling() {
        return curling;
    }

    private synchronized void setCurling(boolean curling) {
        this.curling = curling;
    }

    private String getNewSpammedTransaction(String trunk) {
        try {
            String spamTransaction = iotaProvider.spamTransaction(trunk, IotaProvider.RECOMMENDED_MIN_WEIGHT_MAGNITUDE);
            System.out.println("RestartBundle: Sent spam tx: " + spamTransaction);
            return spamTransaction;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
