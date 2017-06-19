package com.sevrin.toon.IOTAFaucet.backend;

import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;

import java.util.stream.StreamSupport;

/**
 * Created by toonsev on 6/17/2017.
 */
public class SendingLoop implements Runnable {
    private IotaProvider iotaProvider;
    private DatabaseProvider databaseProvider;


    public SendingLoop(IotaProvider iotaProvider, DatabaseProvider databaseProvider) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void run() {
        for (StoredBundle storedBundle : databaseProvider.getStoppedBundles(false, false)) {
            try {
                String processor = storedBundle.getProcessor();
                String[] trytes = fetchHashedTrytes(processor);
                if (trytes == null)
                    throw new RuntimeException("Sending loop: received null trytes");
                if (trytes.length == 0)
                    throw new RuntimeException("Tried to broadcast bundle without transactions");
                iotaProvider.broadcastAndStore(trytes);
                System.out.println("Broadcasted a bundle to IOTA ledger.");
                if (databaseProvider.sendBundle(storedBundle.getBundleId()))
                    System.out.println("Bundle tagged as sent.");
                else
                    System.out.println("Failed to tag bundle as sent. Maybe another process did this already?");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String[] fetchHashedTrytes(String processor) {
        return StreamSupport.stream(databaseProvider.getProcessorTransactions(processor).spliterator(), false)
                .map(processorTransaction -> {
                    String trytes = processorTransaction.getHashedTrytes();
                    if (trytes == null)
                        throw new RuntimeException("Sending loop found transaction with unhashed trytes!");
                    return trytes;
                }).toArray(String[]::new);
    }
}
