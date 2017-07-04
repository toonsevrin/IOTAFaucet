package com.sevrin.toon.IOTAFaucet.database;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by toonsev on 6/16/2017.
 */
@Entity("transactions")
@Indexes({
        @Index(value = "created", fields = @Field("created")),
        @Index(value = "walletAddress", fields = @Field("walletAddress")),
        @Index(value = "insertionIndex", fields = @Field("insertionIndex"))}
)
public class StoredTransaction {
    @Id
    private ObjectId transactionId;
    @Property("created")
    private Long created;
    @Property("walletAddress")
    private String walletAddress;
    @Property("amount")
    private long amount;

    @Property("insertionIndex")
    private long insertionIndex;


    public StoredTransaction() {
    }

    public StoredTransaction(Long created, long insertionIndex, String walletAddress, long amount) {
        this.created = created;
        this.insertionIndex = insertionIndex;
        this.walletAddress = walletAddress;
        this.amount = amount;
    }

    public long getInsertionIndex() {
        return insertionIndex;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }


    public ObjectId getTransactionId() {
        return transactionId;
    }

    public Long getCreated() {
        return created;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public long getAmount() {
        return amount;
    }
}
