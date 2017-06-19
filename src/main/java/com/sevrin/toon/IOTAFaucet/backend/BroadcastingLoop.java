package com.sevrin.toon.IOTAFaucet.backend;

import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.ProcessorTransaction;
import com.sevrin.toon.IOTAFaucet.database.StoredBundle;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import jota.model.Transaction;

import java.util.concurrent.TimeUnit;

/**
 * Created by toonsev on 6/17/2017.
 */
public class BroadcastingLoop implements Runnable {
    private IotaProvider iotaProvider;
    private DatabaseProvider databaseProvider;
    private static final long BRANCH_UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(60);

    private Subject<DoWorkReq> processorTransactionSubject = PublishSubject.<DoWorkReq>create().toSerialized();

    public BroadcastingLoop(IotaProvider iotaProvider, DatabaseProvider databaseProvider) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public void run() {
        StoredBundle bundle = databaseProvider.getCurrentBundle();
        if (bundle.getCurrentTransaction() != null) {
            ProcessorTransaction currentTransaction = databaseProvider.getProcessorTransaction(bundle.getProcessor(), bundle.getCurrentTransaction());
            if (currentTransaction.getLastBranch() != null) {
                if (currentTransaction.getBranchLastUpdated() == null || currentTransaction.getBranchLastUpdated() + BRANCH_UPDATE_INTERVAL < System.currentTimeMillis()) {
                    String oldTrytes = currentTransaction.getTrytes();
                    String branch = iotaProvider.getNewBranchTransaction();
                    Transaction transaction = new Transaction(oldTrytes);
                    transaction.setBranchTransaction(branch);
                    String newTrytes = transaction.toTrytes();
                    String newState = Utils.trytesToStateMatrix(newTrytes);
                    boolean updated = databaseProvider.updateProcessorTransactionTrytes(bundle.getProcessor(), currentTransaction.getTransactionId(), oldTrytes, newTrytes, newState);
                    if (updated) {
                        System.out.println("Sucessfully updated branch of last processorTransaction in bundle (these should be fresh)");
                        currentTransaction.setTrytes(newTrytes);
                        currentTransaction.setState(newState);
                    } else
                        System.out.println("Failed to update branch of last processorTransaction (maybe another process did this already?");
                } else
                    System.out.println("Waiting for more time to expire before the lastTransaction is regenerated, the branch is still fresh.");
            }
            System.out.println("Broadcasting processorTransaction " + currentTransaction.getTransactionId() + " to clients.");
            processorTransactionSubject.onNext(new DoWorkReq(currentTransaction.getProcessorId(), currentTransaction.getUniqueId(), currentTransaction.getState()));
        } else
            System.out.println("No current transaction to broadcast to clients.");
    }

    public Subject<DoWorkReq> getDoWorkReqSubject() {
        return processorTransactionSubject;
    }
}
