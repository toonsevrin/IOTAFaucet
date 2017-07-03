package com.sevrin.toon.IOTAFaucet.backend;

import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.ProcessorTransaction;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import jota.model.Transaction;

/**
 * TODO: PROTECT AGAINST THIRD PARTY REATTACH: IF THE NEGATIVE TRANSACTION ADDRESS NO LONGER HAS SUFFICIENT FUNDS, CONSIDER THE TX CONFIRMED.
 * Created by toonsev on 6/17/2017.
 */
public class ConfirmLoop implements Runnable {
    private IotaProvider iotaProvider;
    private DatabaseProvider databaseProvider;

    public ConfirmLoop(IotaProvider iotaProvider, DatabaseProvider databaseProvider) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void run() {
        for (StoredBundle storedBundle : databaseProvider.getStoppedBundles(true, false)) {
            try {
                ProcessorTransaction transaction = databaseProvider.getProcessorTransactions(storedBundle.getProcessor()).iterator().next();
                String hash = new Transaction(transaction.getHashedTrytes()).getHash();
                if (iotaProvider.isBundleConfirmed(new String[]{hash})) {
                    if(databaseProvider.confirmBundle(storedBundle.getBundleId()))
                        System.out.println("Confirmed bundle " + storedBundle.getBundleId());
                    else
                        System.out.println("Failed to confirm bundle" + storedBundle.getBundleId() + " probably confirmed already.");
                }else
                    System.out.println("Check previous bundle(" + storedBundle.getBundleId() + ")'s confirmation state but it's not confirmed yet.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
