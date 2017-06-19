package com.sevrin.toon.IOTAFaucet.backend;

import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.ProcessorTransaction;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;

import java.util.concurrent.TimeUnit;

/**
 * Created by toonsev on 6/18/2017.
 */
public class RestartBundleLoop implements Runnable {
    private DatabaseProvider databaseProvider;
    private IotaProvider iotaProvider;

    private static final long SPAM_INTERVAL = TimeUnit.SECONDS.toMillis(30);

    public RestartBundleLoop(IotaProvider iotaProvider, DatabaseProvider databaseProvider) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void run() {
        for (StoredBundle storedBundle : databaseProvider.getStoppedBundles(true, false)) {
            ProcessorTransaction lastTransaction = databaseProvider.getTransactionWithLastBranch(storedBundle.getProcessor());
            if (!verifyTransaction(storedBundle.getBranch(), storedBundle.getSent()))
                handleBadTransaction(false);
            else if (!verifyTransaction(storedBundle.getBranch(), storedBundle.getSent()))
                handleBadTransaction(false);
            else if (!verifyTransaction(lastTransaction.getLastBranch(), storedBundle.getSent()))
                handleBadTransaction(true);
            else if (storedBundle.getSent() + SPAM_INTERVAL < System.currentTimeMillis())
                spamToTop(storedBundle);
        }
    }

    private void spamToTop(StoredBundle storedBundle) {
        String lastTransaction = storedBundle.getLastTransaction();
        String newSpam = getNewSpammedTransaction();//TODO: Implement this
        if (newSpam != null)
            databaseProvider.setLastSpammed(storedBundle.getBundleId(), storedBundle.getLastSpammed());
    }

    private String getNewSpammedTransaction() {
        return null;//TODO: Implement this
    }

    private void handleBadTransaction(boolean top) {
        System.out.println("NOT IMPLEMENTED: Handling of a bad transaction");
    }

    private boolean verifyTransaction(String hash, long sentTime) {
        System.out.println("NOT IMPLEMENTED: Verifying branch/trunk: " + hash);
        return true;//TODO Reimplement this
    }
}
