package com.sevrin.toon.IOTAFaucet.backend;

import com.google.gson.Gson;
import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.ProcessorTransaction;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.database.StoredTransaction;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import jota.dto.response.GetTransactionsToApproveResponse;
import jota.model.Transaction;
import jota.model.Transfer;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Definitely IOTA related bugs in here...
 * Created by toonsev on 6/17/2017.
 */
public class StartLoop implements Runnable {
    private static final String TAG = "WALLET9TRANSFER999999999999";

    private IotaProvider iotaProvider;
    private DatabaseProvider databaseProvider;


    public StartLoop(IotaProvider iotaProvider, DatabaseProvider databaseProvider) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void run() {
        try {
            if (databaseProvider.allBundlesAreConfirmed()) {
                Integer lastIndex = databaseProvider.getLastKnownAddressIndex(iotaProvider.getSeed());
                Map<StoredTransaction, String> addressesByStoredTransaction = StreamSupport.stream(databaseProvider.getTransactionsSinceLastBundle().spliterator(), false)
                        .collect(Collectors.toMap(transaction -> transaction, transaction -> transaction.getWalletAddress()));
                if (addressesByStoredTransaction == null || addressesByStoredTransaction.isEmpty()) {
                    System.out.println("No transactions made yet, not starting a bundle.");
                    return;
                }
                if (lastIndex == null) {
                    lastIndex = iotaProvider.getFirstAddressWithFunds();
                    if (lastIndex == null)
                        throw new RuntimeException("NO ADDRESS FOUND WITH FUNDS!");
                }
                String lastAddress = iotaProvider.getAddress(lastIndex);

                int nextIndex = iotaProvider.getAvailableAddressIndex(lastIndex);
                String nextAddress = iotaProvider.getAddress(nextIndex);

                String processorId = UUID.randomUUID().toString();

                List<String> allTrytes = iotaProvider.prepareTransaction(addressesByStoredTransaction.keySet(), lastAddress, nextAddress);

                Set<ProcessorTransaction> processorTransactions = new HashSet<>();
                final GetTransactionsToApproveResponse txsToApprove = iotaProvider.getNewBranchAndTrunkTransactions();
                for (int i = 0; i < allTrytes.size(); i++) {
                    String trytes = allTrytes.get(i);
                    System.out.println("Looping over trytes: " + trytes);
                    Transaction transaction = new Transaction(trytes);

                    transaction.setBranchTransaction(txsToApprove.getBranchTransaction());

                    processorTransactions.add(fromTrytes(processorId, transaction.getCurrentIndex(), transaction.toTrytes()));
                }
                boolean saved = databaseProvider.saveProcessorTransaction(processorTransactions);
                if (saved) {
                    System.out.println("saved processor " + processorId + " for bundle with address " + lastAddress + " remainder will be sent to " + nextAddress);
                    startBundle(processorId, txsToApprove.getBranchTransaction(), txsToApprove.getTrunkTransaction(), new Transaction(allTrytes.get(0)).getLastIndex());
                } else
                    System.out.println("Failed to save processor :(");

            } else
                System.out.println("Startloop: bundles still in process");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //todo check if this method is safe with multiple processes
    private void startBundle(String processorId, String branch, String trunk, long lastBundleIndex) {
        StoredBundle bundle = databaseProvider.getLastConfirmedBundle();
        long bundleId = bundle == null ? 0 : bundle.getBundleId() + 1;//increment last id by one, or set it to 0 if this is first bundle
        ProcessorTransaction first = databaseProvider.getNextProcessorTransaction(processorId, lastBundleIndex + 1);
        System.out.println("first: " + first);
        boolean started = databaseProvider.startBundle(bundleId, processorId, first.getUniqueId(), branch, trunk);
        if (started) {
            System.out.println("Started bundle " + bundleId);
        } else
            System.out.println("Failed to start bundle:" + bundleId);

    }

    private ProcessorTransaction fromTrytes(String processorId, long bundleIndex, String trytes) {
        return new ProcessorTransaction(trytes, bundleIndex, iotaProvider.trytesToStateMatrix(trytes), processorId);
    }


    private Transfer fromStoredTransaction(StoredTransaction storedTransaction) {
        return new Transfer(storedTransaction.getWalletAddress(), storedTransaction.getAmount(), TAG, TAG);
    }
}
