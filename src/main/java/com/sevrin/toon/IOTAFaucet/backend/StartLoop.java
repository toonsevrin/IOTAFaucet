package com.sevrin.toon.IOTAFaucet.backend;

import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.ProcessorTransaction;
import com.sevrin.toon.IOTAFaucet.database.StoredTransaction;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import jota.dto.response.GetTransactionsToApproveResponse;
import jota.model.Transaction;
import jota.model.Transfer;

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

                if (lastIndex == null) {
                    lastIndex = iotaProvider.getFirstAddressWithFunds();
                    if (lastIndex == null)
                        throw new RuntimeException("NO ADDRESS FOUND WITH FUNDS!");
                }
                String lastAddress = iotaProvider.getAddress(lastIndex);
                int nextIndex = iotaProvider.getAvailableAddressIndex(lastIndex);
                String nextAddress = iotaProvider.getAddress(nextIndex);

                String processorId = UUID.randomUUID().toString();

                Map<String, StoredTransaction> storedTransactionByAddress = StreamSupport.stream(databaseProvider.getTransactionsSinceLastBundle().spliterator(), false)
                        .collect(Collectors.toMap(transaction -> transaction.getWalletAddress(), transaction -> transaction));


                List<String> allTrytes = iotaProvider.prepareTransaction(storedTransactionByAddress.values(), lastAddress, nextAddress);

                Set<ProcessorTransaction> processorTransactions = new HashSet<>();
                final GetTransactionsToApproveResponse txsToApprove = iotaProvider.getNewBranchAndTrunkTransactions();
                String previousTx = null;
                for (int i = 0; i < allTrytes.size(); i++) {
                    String trytes = allTrytes.get(i);
                    Transaction transaction = new Transaction(trytes);
                    transaction.setBranchTransaction(txsToApprove.getBranchTransaction());
                    transaction.setTrunkTransaction(previousTx == null ? txsToApprove.getTrunkTransaction() : previousTx);
                    previousTx = transaction.getHash();//TODO: THIS HASH IS PROBABLY OUTDATED!

                    StoredTransaction storedTrans = storedTransactionByAddress.get(transaction.getAddress());
                    String transactionId = storedTrans != null ? storedTrans.getTransactionId() : null;
                    processorTransactions.add(fromTrytes(processorId, transaction.toTrytes(), transactionId));
                }
                boolean saved = databaseProvider.saveProcessorTransaction(processorTransactions);
                if (saved) {
                    System.out.println("saved processor " + processorId + " for bundle with address " + lastAddress + " remainder will be sent to " + nextAddress);
                    startBundle(processorId, txsToApprove.getBranchTransaction(), txsToApprove.getTrunkTransaction());
                } else
                    System.out.println("Failed to save processor :(");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBundle(String processorId, String branch, String trunk) {
        Integer id = databaseProvider.getLastConfirmedBundleId();
        id = id == null ? 0 : id + 1;//increment last id by one, or set it to 0 if this is first bundle
        ProcessorTransaction first = databaseProvider.getNextProcessorTransaction(processorId, null);
        boolean started = databaseProvider.startBundle(id, first.getUniqueId(), first.getState(), branch, trunk);
        if (started) {
            System.out.println("Started bundle " + id);
        } else
            System.out.println("Failed to start bundle:" + id);

    }

    private ProcessorTransaction fromTrytes(String processorId, String trytes, String transactionId) {
        return new ProcessorTransaction(trytes, getState(trytes), transactionId, processorId);
    }

    private String getState(String trytes) {
        return "";//todo: implement
    }

    private Transfer fromStoredTransaction(StoredTransaction storedTransaction) {
        return new Transfer(storedTransaction.getWalletAddress(), storedTransaction.getAmount(), TAG, TAG);
    }
}
