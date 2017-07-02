package com.sevrin.toon.IOTAFaucet.database;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by toonsev on 6/16/2017.
 */
@Entity("bundles")
@Indexes({
        @Index(value = "bundleId", fields = @Field("bundleId")),
        @Index(value = "processorId", fields = @Field("processorId")),
        @Index(value = "currentTransaction", fields = @Field("currentTransaction")),
        @Index(value = "sent", fields = @Field("sent")),
        @Index(value = "confirmed", fields = @Field("confirmed")),
        @Index(value = "started", fields = @Field("started")),
        @Index(value = "stopped", fields = @Field("stopped"))}
)

public class StoredBundle {
    @Id
    private ObjectId uniqueId;
    @Property("bundleId")
    private long bundleId;
    @Property("processorId")
    private String processorId;
    @Property("currentTransaction")
    private ObjectId currentTransaction;
    @Property("prevTransactionHash")
    private String prevTransactionHash;
    @Property("started")
    private Long started;
    @Property("stopped")
    private Long stopped;
    @Property("sent")
    private Long sent;
    @Property("confirmed")
    private Long confirmed;

    @Property("branch")
    private String branch;
    @Property("trunk")
    private String trunk;
    //this is for transaction bump spamming
    @Property("lastTransaction")
    private String lastTransaction;
    @Property("lastSpammed")
    private Long lastSpammed;

    public StoredBundle() {
    }

    public StoredBundle(long bundleId) {
        this.bundleId = bundleId;
    }

    public ObjectId getUniqueId() {
        return uniqueId;
    }

    public long getBundleId() {
        return bundleId;
    }

    public Long getStarted() {
        return started;
    }

    public String getPrevTransactionHash() {
        return prevTransactionHash;
    }

    public String getProcessor() {
        return processorId;
    }

    public ObjectId getCurrentTransaction() {
        return currentTransaction;
    }

    public Long getSent() {
        return sent;
    }

    public String getLastTransaction() {
        return lastTransaction;
    }

    public Long getStopped() {
        return stopped;
    }

    public String getBranch() {
        return branch;
    }

    public String getTrunk() {
        return trunk;
    }

    public Long getLastSpammed() {
        return lastSpammed;
    }

    public Long getConfirmed() {
        return confirmed;
    }
}