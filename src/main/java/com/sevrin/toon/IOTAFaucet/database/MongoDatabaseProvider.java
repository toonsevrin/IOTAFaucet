package com.sevrin.toon.IOTAFaucet.database;

import com.mongodb.MongoClient;
import com.sevrin.toon.IOTAFaucet.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.*;
import org.mongodb.morphia.query.*;
import org.mongodb.morphia.query.Query;

import javax.management.*;
import java.util.List;


/**
 * Created by toonsev on 6/15/2017.
 */
public class MongoDatabaseProvider implements DatabaseProvider {
    private final Morphia morphia = new Morphia();
    private final Datastore datastore;


    public MongoDatabaseProvider(MongoClient mongoClient, String databaseName) {
        morphia.mapPackage("com.sevrin.toon.IOTAFaucet.database");
        this.datastore = morphia.createDatastore(mongoClient, databaseName);
        this.datastore.ensureIndexes();

    }

    //addressCache
    @Override
    public Integer getLastKnownAddressIndex(String seed) {
        CachedSeed cachedSeed = datastore.get(CachedSeed.class, seed);
        return cachedSeed == null ? null : cachedSeed.getLastIndex();
    }

    @Override
    public boolean setLastKnownAddressIndex(String seed, long index) {
        Query<CachedSeed> query = datastore.createQuery(CachedSeed.class).field("_id").equal(seed);
        UpdateOperations<CachedSeed> updateOperations = datastore.createUpdateOperations(CachedSeed.class)
                .set("lastIndex", index);
        return datastore.update(query, updateOperations, new UpdateOptions().upsert(true)).getUpdatedCount() > 0;
    }


    @Override
    public Iterable<StoredTransaction> getTransactionsUpToIndex(long minIndex, long maxIndex) {
        return datastore.createQuery(StoredTransaction.class).field("insertionIndex").greaterThan(minIndex).field("insertionIndex").lessThanOrEq(maxIndex);
    }

    @Override
    public Long getTransactionIndexOfLatestTransaction() {
        List<StoredTransaction> transactions = datastore.createQuery(StoredTransaction.class)
                .order(Sort.descending("insertionIndex"))
                .asList(new FindOptions().limit(1));
        return transactions.isEmpty() ? null : transactions.get(0).getInsertionIndex();
    }


    @Override
    public StoredTransaction addTransaction(String walletAddress, long amount) {
        StoredTransaction transaction = new StoredTransaction(System.currentTimeMillis(), getNewTransactionIndex(), walletAddress, amount);
        datastore.save(transaction);
        return transaction;
    }

    @Override
    public StoredBundle getCurrentBundle() {
        List<StoredBundle> bundles = datastore.createQuery(StoredBundle.class)
                .field("stopped").doesNotExist()
                .order(Sort.descending("bundleId"))
                .asList(new FindOptions().limit(1));
        return bundles.isEmpty() ? null : bundles.get(0);
    }

    @Override
    public Iterable<StoredBundle> getStoppedBundles(boolean send, boolean confirmed) {

        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class)
                .field("stopped").exists();
        query = send ? query.field("sent").exists() : query.field("sent").doesNotExist();
        query = confirmed ? query.field("confirmed").exists() : query.field("confirmed").doesNotExist();
        return query;
    }

    @Override
    public Iterable<ProcessorTransaction> getProcessorTransactions(String processorId) {
        return datastore.createQuery(ProcessorTransaction.class).field("processorId").equal(processorId);
    }

    @Override
    public StoredBundle getLastBundle() {
        List<StoredBundle> bundles = datastore.createQuery(StoredBundle.class).order(Sort.descending("bundleId")).asList(new FindOptions().limit(1));
        return bundles.isEmpty() ? null : bundles.get(0);
    }

    @Override
    public StoredBundle getLastConfirmedBundle() {
        List<StoredBundle> bundles = datastore.createQuery(StoredBundle.class).field("confirmed").exists().order(Sort.descending("confirmed")).asList(new FindOptions().limit(1));
        return bundles.isEmpty() ? null : bundles.get(0);
    }

    @Override
    public boolean saveProcessorTransaction(Iterable<ProcessorTransaction> processorTransactions) {
        Iterable result = datastore.save(processorTransactions);
        return result != null;
    }

    @Override
    public ProcessorTransaction getProcessorTransaction(String processorId, ObjectId processorTransactionId) {
        return datastore.createQuery(ProcessorTransaction.class).field("processorId").equal(processorId).field("_id").equal(processorTransactionId).get();
    }

    @Override
    public ProcessorTransaction getNextProcessorTransaction(String processorId, Long prevTransactionBundleIndex) {
        Query<ProcessorTransaction> processorTransactions = datastore.createQuery(ProcessorTransaction.class).field("processorId").equal(processorId);
        if (prevTransactionBundleIndex != null)
            processorTransactions = processorTransactions.field("bundleIndex").lessThan(prevTransactionBundleIndex);
        List<ProcessorTransaction> transactions = processorTransactions.order(Sort.descending("bundleIndex"))
                .asList(new FindOptions().limit(1));
        return transactions.isEmpty() ? null : transactions.get(0);
    }

    @Override
    public boolean confirmBundle(long bundleId) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId).field("confirmed").doesNotExist();
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class).set("confirmed", System.currentTimeMillis());
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean startBundle(long bundleId, String processorId, ObjectId startTx, String branch, String trunk, long lastTransactionIndex, long nextAddressIndex) {
        //Let's make sure a bundle is created with this bundleId first, because in the next query we are putting some more restrictions, and we don't want to upsert these
        datastore.update(datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId), datastore.createUpdateOperations(StoredBundle.class).set("bundleId", bundleId), new UpdateOptions().upsert(true));

        //TODO: Started time should be the last transaction of the previous bundle, lastTransactionTime
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId).field("started").doesNotExist().field("processorId").doesNotExist();
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("started", System.currentTimeMillis())
                .set("currentTransaction", startTx)
                .set("branch", branch)
                .set("trunk", trunk)
                .set("processorId", processorId)
                .set("lastTransactionIndex", lastTransactionIndex)
                .set("nextAddressIndex", nextAddressIndex);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean updateCurrentInBundle(long bundleId, ObjectId previousTx, String prevTransactionHash, ObjectId nextTx) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId).field("currentTransaction").equal(previousTx);
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("currentTransaction", nextTx)
                .set("prevTransactionHash", prevTransactionHash);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean updateProcessorTransactionTrytes(String processorId, ObjectId processorTransactionId, String oldTrytes, String newTrytes, String newState) {
        Query<ProcessorTransaction> query = datastore.createQuery(ProcessorTransaction.class).field("_id").equal(processorTransactionId)
                .field("processorId").equal(processorId)
                .field("trytes").equal(oldTrytes);
        UpdateOperations<ProcessorTransaction> updateOperations = datastore.createUpdateOperations(ProcessorTransaction.class)
                .set("trytes", newTrytes)
                .set("state", newState);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean setHashedProcessorTransactionTrytes(String processorId, ObjectId processorTransactionId, String oldState, String hashedTrytes) {
        Query<ProcessorTransaction> query = datastore.createQuery(ProcessorTransaction.class).field("_id").equal(processorTransactionId)
                .field("processorId").equal(processorId)
                .field("state").equal(oldState);
        UpdateOperations<ProcessorTransaction> updateOperations = datastore.createUpdateOperations(ProcessorTransaction.class)
                .set("hashedTrytes", hashedTrytes);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean stopBundle(long bundleId, ObjectId lastTransaction) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId)
                .field("stopped").doesNotExist()
                .field("currentTransaction").equal(lastTransaction);
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .unset("currentTransaction")
                .set("stopped", System.currentTimeMillis())
                .set("lastTransaction", lastTransaction);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean sendBundle(long bundleId) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId)
                .field("sent").doesNotExist();
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("sent", System.currentTimeMillis());
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public ProcessorTransaction getTopTransaction(String processorId) {
        return datastore.createQuery(ProcessorTransaction.class).field("processorId").equal(processorId).field("bundleIndex").equal(0).get();
    }


    @Override
    public boolean setLastSpammed(long bundleId, Long lastLastSpammed) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId);
        if (lastLastSpammed == null)
            query = query.field("lastSpammed").doesNotExist();
        else
            query = query.field("lastSpammed").equal(lastLastSpammed);
        ;
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("lastSpammed", System.currentTimeMillis());
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean allBundlesAreConfirmed() {
        return datastore.createQuery(StoredBundle.class).field("started").exists().field("confirmed").doesNotExist().get() == null;//if a storedbundle exists, this will return false
    }

    @Override
    public Long getLastTokensReceived(User user) {
        StoredUsage addressLastUsage = datastore.createQuery(StoredUsage.class).field("walletAddress").equal(user.getWalletAddress()).get();
        StoredUsage ipLastUsage = datastore.createQuery(StoredUsage.class).field("ipAddress").equal(user.getIpAddress()).get();
        if (addressLastUsage != null && ipLastUsage != null)//return the largest of the two, the most recent usage
            return addressLastUsage.getLastUsage() > ipLastUsage.getLastUsage() ? addressLastUsage.getLastUsage() : ipLastUsage.getLastUsage();
        if (addressLastUsage != null)
            return addressLastUsage.getLastUsage();
        return ipLastUsage != null ? ipLastUsage.getLastUsage() : null;
    }

    @Override
    public boolean setTokensReceived(User user, long amount) {
        Query<StoredUser> query = datastore.createQuery(StoredUser.class).field("_id").equal(user.getWalletAddress());
        UpdateOperations<StoredUser> updateOperations = datastore.createUpdateOperations(StoredUser.class)
                .inc("totalTokensReceived", amount);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public long getNewTransactionIndex() {
        Query<TransactionIndex> query = datastore.createQuery(TransactionIndex.class);
        UpdateOperations<TransactionIndex> updateOperations = datastore.createUpdateOperations(TransactionIndex.class).inc("currentIndex");
        return datastore.findAndModify(query, updateOperations, new FindAndModifyOptions().returnNew(true).upsert(true)).getCurrentIndex();
    }
}
