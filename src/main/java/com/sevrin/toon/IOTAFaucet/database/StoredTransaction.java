package com.sevrin.toon.IOTAFaucet.database;

import org.mongodb.morphia.annotations.*;

/**
 * Created by toonsev on 6/16/2017.
 */
@Entity("transactions")
@Indexes({
        @Index(value = "created", fields = @Field("created")),
        @Index(value = "walletAddress", fields = @Field("walletAddress"))}
)
public class StoredTransaction {
    @Id
    private String transactionId;
    @Property("created")
    private Long created;
    @Property("walletAddress")
    private String walletAddress;
    @Property("amount")
    private long amount;


    public StoredTransaction() {
    }

    public StoredTransaction(Long created, String walletAddress, long amount) {
        this.created = created;
        this.walletAddress = walletAddress;
        this.amount = amount;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }


    public String getTransactionId() {
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
