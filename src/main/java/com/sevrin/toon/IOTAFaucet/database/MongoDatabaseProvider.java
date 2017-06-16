package com.sevrin.toon.IOTAFaucet.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sevrin.toon.IOTAFaucet.User;
import org.bson.Document;

import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by toonsev on 6/15/2017.
 */
public class MongoDatabaseProvider implements DatabaseProvider {
    private MongoCollection<Document> usageCollection;
    private MongoCollection<Document> accountCollection;
    private MongoCollection<Document> addressCacheCollection;

    public MongoDatabaseProvider(MongoClient mongoClient, String databaseName) {
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        this.accountCollection = db.getCollection("users");
        this.addressCacheCollection = db.getCollection("addressCache");
        this.usageCollection = db.getCollection("usage");
        usageCollection.createIndex(new Document("ip", 1));
        usageCollection.createIndex(new Document("address", 1));

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
    //accounts

    @Override
    public long getBalance(String walletAddress) {
        Document accountDoc = accountCollection.find(new Document("_id", walletAddress)).first();
        if (accountDoc == null || !accountDoc.containsKey("balance"))
            return 0;
        return accountDoc.getLong("balance");
    }

    @Override
    public void incBalance(String walletAddress, long balance) {
        accountCollection.updateOne(new Document("_id", walletAddress), inc("balance", balance), new UpdateOptions().upsert(true));
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
    public void setLastKnownAddressIndex(String seed, int index) {
        addressCacheCollection.updateOne(new Document("_id", seed), set("lastIndex", index), new UpdateOptions().upsert(true));
    }

    @Override
    public CurrentTransaction getCurrentTransaction() {
        return null;
    }
}
