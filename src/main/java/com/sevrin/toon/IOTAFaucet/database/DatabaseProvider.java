package com.sevrin.toon.IOTAFaucet.database;

import com.sevrin.toon.IOTAFaucet.User;
import org.bson.types.ObjectId;

/**
 * Created by toonsev on 6/12/2017.
 */
public interface DatabaseProvider {
    Long getLastTokensReceived(User user);

    boolean setTokensReceived(User user, long amount);


    Integer getLastKnownAddressIndex(String seed);

    boolean setLastKnownAddressIndex(String seed, long index);

    StoredBundle getLastBundle();

    /**
     *
     * @param minIndex exclusive
     * @param maxIndex inclusive
     * @return
     */
    Iterable<StoredTransaction> getTransactionsUpToIndex(long minIndex, long maxIndex);

    Long getTransactionIndexOfLatestTransaction();

    StoredTransaction addTransaction(String walletAddress, long amount);


    StoredBundle getCurrentBundle();

    Iterable<StoredBundle> getStoppedBundles(boolean send, boolean confirmed);

    Iterable<ProcessorTransaction> getProcessorTransactions(String processorId);

    StoredBundle getLastConfirmedBundle();

    boolean saveProcessorTransaction(Iterable<ProcessorTransaction> processorTransactions);

    ProcessorTransaction getProcessorTransaction(String processorId, ObjectId processorTransactionId);

    //prevTransactionId is nullable for first
    ProcessorTransaction getNextProcessorTransaction(String processorId, Long prevTransactionBundleIndex);

    boolean confirmBundle(long bundleIndex);

    boolean startBundle(long bundleIndex, String processorId, ObjectId startTx, String branch, String trunk, long lastTransactionIndex, long nextAddressIndex);

    boolean updateCurrentInBundle(long bundleIndex, ObjectId previousTx, String prevTransactionHash, ObjectId nextTx);

    boolean updateProcessorTransactionTrytes(String processorId, ObjectId processorTransactionid, String oldTrytes, String newTrytes, String newState);

    boolean setHashedProcessorTransactionTrytes(String processorId, ObjectId processorTransactionId, String oldState, String hashedTrytes);

    //also sets the 'StoredBundle#lastTransaction' for bump spamming
    boolean stopBundle(long bundleIndex, ObjectId lastTransaction);

    boolean sendBundle(long bundleIndex);

    ProcessorTransaction getTopTransaction(String processorId);

    boolean setLastSpammed(long bundleId, Long lastLastSpammed);

    long getNewTransactionIndex();

    boolean allBundlesAreConfirmed();

}
