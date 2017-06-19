package com.sevrin.toon.IOTAFaucet.database;

import com.sevrin.toon.IOTAFaucet.User;

/**
 * Created by toonsev on 6/12/2017.
 */
public interface DatabaseProvider {
    Long getLastTokensReceived(User user);

    void setTokensReceived(User user);


    Integer getLastKnownAddressIndex(String seed);

    void setLastKnownAddressIndex(String seed, long index);

    StoredBundle getLastBundle();

    Iterable<StoredTransaction> getTransactionsSinceLastBundle();

    StoredTransaction addTransaction(String walletAddress, long amount);


    StoredBundle getCurrentBundle();

    Iterable<StoredBundle> getStoppedBundles(boolean send, boolean confirmed);

    Iterable<ProcessorTransaction> getProcessorTransactions(String processorId);

    StoredBundle getLastConfirmedBundle();

    boolean saveProcessorTransaction(Iterable<ProcessorTransaction> processorTransactions);

    ProcessorTransaction getProcessorTransaction(String processorId, String processorTransactionId);

    //prevTransactionId is nullable for first
    ProcessorTransaction getNextProcessorTransaction(String processorId, String prevProcessorTransactionId);

    boolean confirmBundle(long bundleIndex);

    boolean startBundle(long bundleIndex, String processorId, String startTx, String branch, String trunk);

    boolean updateCurrentInBundle(long bundleIndex, String previousTx, String nextTx);

    boolean updateProcessorTransactionTrytes(String processorId, String processorTransactionid, String oldTrytes, String newTrytes, String newState);

    boolean setHashedProcessorTransactionState(String processorId, String processorTransactionId, String oldState, String hashedState);

    //also sets the 'StoredBundle#lastTransaction' for bump spamming
    boolean stopBundle(long bundleIndex, String lastTransaction);

    boolean sendBundle(long bundleIndex);

    ProcessorTransaction getTransactionWithLastBranch(String processorId);

    boolean setLastSpammed(long bundleId, long lastSpammed);

    boolean allBundlesAreConfirmed();

}
