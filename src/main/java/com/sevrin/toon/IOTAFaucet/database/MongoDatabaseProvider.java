package com.sevrin.toon.IOTAFaucet.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sevrin.toon.IOTAFaucet.User;
import org.apache.commons.collections.SortedBag;
import org.apache.commons.lang.SystemUtils;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.InsertOptions;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.*;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Updates.set;

/**
 * Created by toonsev on 6/15/2017.
 */
public class MongoDatabaseProvider implements DatabaseProvider {
    private final Morphia morphia = new Morphia();
    private final Datastore datastore;

    private MongoCollection<Document> usageCollection;
    private MongoCollection<Document> bundlesCollection;
    private MongoCollection<Document> processorTransactionsCollection;
    private MongoCollection<Document> transactionsCollection;
    private MongoCollection<Document> addressCacheCollection;

    public MongoDatabaseProvider(MongoClient mongoClient, String databaseName) {
        morphia.mapPackage("com.sevrin.toon.IOTAFaucet.database");
        this.datastore = morphia.createDatastore(mongoClient, databaseName);
        this.datastore.ensureIndexes();


        MongoDatabase db = mongoClient.getDatabase(databaseName);
        this.usageCollection = db.getCollection("usage");
        usageCollection.createIndex(new Document("ip", 1));
        usageCollection.createIndex(new Document("address", 1));

        this.bundlesCollection = db.getCollection("bundles");
        bundlesCollection.createIndex(new Document("bundleId", 1));
        bundlesCollection.createIndex(new Document("processorId", 1));
        bundlesCollection.createIndex(new Document("currentTransaction", 1));

        this.transactionsCollection = db.getCollection("transactions");

        this.processorTransactionsCollection = db.getCollection("processorTransactions");
        processorTransactionsCollection.createIndex(new Document("transactionId", 1));
        processorTransactionsCollection.createIndex(new Document("processorId", 1));

        this.addressCacheCollection = db.getCollection("addressCache");

    }

    //addressCache
    @Override
    public Integer getLastKnownAddressIndex(String seed) {
        Document document = addressCacheCollection.find(new Document("_id", seed)).first();
        if (document == null || !document.containsKey("lastIndex"))
            return null;
        return document.getInteger("lastIndex");
    }

    @Override
    public void setLastKnownAddressIndex(String seed, long index) {
        addressCacheCollection.updateOne(new Document("_id", seed), set("lastIndex", index), new UpdateOptions().upsert(true));
    }


    @Override
    public Iterable<StoredTransaction> getTransactionsSinceLastBundle() {
        StoredBundle lastBundle = getLastBundle();
        long start = lastBundle == null ? 0 : lastBundle.getStarted();
        return datastore.createQuery(StoredTransaction.class).field("created").greaterThan(start);
    }

    @Override
    public StoredTransaction addTransaction(String walletAddress, long amount) {
        StoredTransaction transaction = new StoredTransaction(System.currentTimeMillis(), walletAddress, amount);
        datastore.save(transaction);
        return transaction;
    }

    @Override
    public StoredBundle getCurrentBundle() {
        List<StoredBundle> bundles = datastore.createQuery(StoredBundle.class)
                .field("stopped").doesNotExist()
                .order(Sort.descending("orderId"))
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
        List<StoredBundle> bundles = datastore.createQuery(StoredBundle.class).order(Sort.descending("orderId")).asList(new FindOptions().limit(1));
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
    public ProcessorTransaction getProcessorTransaction(String processorId, String processorTransactionId) {
        return datastore.createQuery(ProcessorTransaction.class).field("processorId").equal(processorId).field("_id").equal(processorTransactionId).get();
    }

    @Override
    public ProcessorTransaction getNextProcessorTransaction(String processorId, String prevProcessorTransactionId) {
        List<ProcessorTransaction> transactions = datastore.createQuery(ProcessorTransaction.class).field("processorId").equal(processorId)
                .field("_id").greaterThan(prevProcessorTransactionId)
                .order(Sort.ascending("_id"))
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
    public boolean startBundle(long bundleId, String processorId, String startTx, String branch, String trunk) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId).field("started").doesNotExist().field("processorId").doesNotExist();
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("started", System.currentTimeMillis())
                .set("currentTransaction", startTx)
                .set("branch", branch)
                .set("trunk", trunk)
                .set("processorId", processorId);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean updateCurrentInBundle(long bundleId, String previousTx, String nextTx) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId).field("currentTransaction").equal(previousTx);
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("currentTransaction", nextTx);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean updateProcessorTransactionTrytes(String processorId, String processorTransactionId, String oldTrytes, String newTrytes, String newState) {
        Query<ProcessorTransaction> query = datastore.createQuery(ProcessorTransaction.class).field("_id").equal(processorTransactionId)
                .field("processorId").equal(processorId)
                .field("trytes").equal(oldTrytes);
        UpdateOperations<ProcessorTransaction> updateOperations = datastore.createUpdateOperations(ProcessorTransaction.class)
                .set("trytes", newTrytes)
                .set("state", newState);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean setHashedProcessorTransactionTrytes(String processorId, String processorTransactionId, String oldState, String hashedTrytes) {
        Query<ProcessorTransaction> query = datastore.createQuery(ProcessorTransaction.class).field("_id").equal(processorTransactionId)
                .field("processorId").equal(processorId)
                .field("state").equal(oldState);
        UpdateOperations<ProcessorTransaction> updateOperations = datastore.createUpdateOperations(ProcessorTransaction.class)
                .set("hashedTrytes", hashedTrytes);
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean stopBundle(long bundleId, String lastTransaction) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId)
                .field("stopped").doesNotExist()
                .field("currentTransaction").equal(lastTransaction);
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("currentTransaction", null)
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
    public ProcessorTransaction getTransactionWithLastBranch(String processorId) {
        return datastore.createQuery(ProcessorTransaction.class).field("processorId").equal(processorId).field("lastBranch").exists().get();
    }


    @Override
    public boolean setLastSpammed(long bundleId, long lastSpammed) {
        Query<StoredBundle> query = datastore.createQuery(StoredBundle.class).field("bundleId").equal(bundleId)
                .field("lastSpammed").equal(lastSpammed);
        UpdateOperations<StoredBundle> updateOperations = datastore.createUpdateOperations(StoredBundle.class)
                .set("lastSpammed", System.currentTimeMillis());
        return datastore.update(query, updateOperations).getUpdatedCount() > 0;
    }

    @Override
    public boolean allBundlesAreConfirmed() {
        return datastore.createQuery(StoredBundle.class).field("confirmed").doesNotExist().get() == null;//if a storedbundle exists, this will return false
    }

    @Override
    public Long getLastTokensReceived(User user) {
        Long addressLastUsage = getLastUsageFromDoc(usageCollection.find(new Document("address", user.getWalletAddress())).first());
        Long ipLastUsage = getLastUsageFromDoc(usageCollection.find(new Document("ip", user.getIpAddress())).first());
        if (addressLastUsage != null && ipLastUsage != null)//return the largest of the two, the most recent usage
            return addressLastUsage > ipLastUsage ? addressLastUsage : ipLastUsage;
        if (addressLastUsage != null)
            return addressLastUsage;
        return ipLastUsage;
    }

    private Long getLastUsageFromDoc(Document usageDoc) {
        if (usageDoc != null && usageDoc.containsKey("lastUsage"))
            return usageDoc.getLong("lastUsage");
        return null;
    }

    @Override
    public void setTokensReceived(User user) {
        usageCollection.updateOne(new Document("address", user.getWalletAddress()), set("lastUsage", System.currentTimeMillis()), new UpdateOptions().upsert(true));
        usageCollection.updateOne(new Document("ip", user.getIpAddress()), set("lastUsage", System.currentTimeMillis()), new UpdateOptions().upsert(true));
    }
}
