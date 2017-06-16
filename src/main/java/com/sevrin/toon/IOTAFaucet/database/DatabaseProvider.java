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

    StoredTransaction addTransaction(String walletAddress, long amount);

    StoredTransaction getNextSortedTransaction(long minTimestamp, long maxTimestamp, String prevTransactionId);

    StoredBundle getResponsibleBundle(long timestamp);

    StoredBundle getCurrentBundle();

    StoredBundle getBundleByTransaction(String transaction);

    StoredBundle getBundleByBundleId(long bundleId);

    Iterable<StoredBundle> getStoppedBundles(boolean send, boolean confirmed);

    boolean confirmBundle(long bundleIndex);

    boolean startBundle(long bundleIndex, String startTx, String startState);

    boolean updateCurrentInBundle(String previousTx, String nextTx, String nextTxState);

    boolean stopBundle(String lastTransaction);

    boolean sendBundle(long bundleIndex);

}
