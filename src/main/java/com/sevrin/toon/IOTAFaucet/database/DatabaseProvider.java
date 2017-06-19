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

    StoredTransaction getTransaction(String transactionId);

    /**
     * [startTimestamp, stopTimestamp[
     *
     * @param startTimestamp
     * @param stopTimestamp
     * @return
     */
    Iterable<StoredTransaction> getTransactionsWithinRange(long startTimestamp, long stopTimestamp);

    Iterable<StoredTransaction> getTransactionsSinceLastBundle();

    StoredTransaction addTransaction(String walletAddress, long amount);


    StoredBundle getResponsibleBundle(long timestamp);

    StoredBundle getCurrentBundle();

    StoredBundle getBundleByTransaction(String transaction);

    StoredBundle getBundleByProcessorId(String processorId);

    StoredBundle getBundleByBundleId(long bundleId);

    Iterable<StoredBundle> getStoppedBundles(boolean send, boolean confirmed);

    Iterable<ProcessorTransaction> getProcessorTransactions(String processor);

    Integer getLastConfirmedBundleId();

    boolean saveProcessorTransaction(Iterable<ProcessorTransaction> processorTransactions);

    ProcessorTransaction getProcessorTransaction(String processor, String transactionId);

    //prevTransactionId is nullable for first
    ProcessorTransaction getNextProcessorTransaction(String processor, String prevTransactionId);

    boolean confirmBundle(long bundleIndex);

    boolean startBundle(long bundleIndex, String startTx, String startState, String branch, String trunk);

    boolean updateCurrentInBundle(long bundleIndex, String previousTx, String nextTx);

    boolean updateProcessorTransactionTrytes(String processorId, String processorTransactionid, String oldTrytes, String newTrytes, String newState);
    boolean setHashedProcessorTransactionState(String processorId, String processorTransactionId, String oldState, String hashedState);

    //also sets the 'StoredBundle#lastTransaction' for bump spamming
    boolean stopBundle(long bundleIndex, String lastTransaction);

    boolean sendBundle(long bundleIndex);

    ProcessorTransaction getTransactionWithLastBranch(String processorId);

    boolean setLastSpammed(long bundleIndex, String previousTransaction, String currentTransaction);

    boolean allBundlesAreConfirmed();

}
