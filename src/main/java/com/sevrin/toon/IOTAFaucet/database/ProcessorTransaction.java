package com.sevrin.toon.IOTAFaucet.database;

import org.mongodb.morphia.annotations.*;

/**
 * Created by toonsev on 6/17/2017.
 */
@Entity("processorTransaction")
@Indexes({
        @Index(value = "processorId", fields = @Field("processorId")),
        @Index(value = "transactionId", fields = @Field("transactionId"))}
)
public class ProcessorTransaction {
    @Id
    private String uniqueId;
    @Property("processorId")
    private String processorId;
    @Property("trytes")
    private String trytes;
    @Property("state")
    private String state;
    @Property("hashedState")
    private String hashedState;
    //linked transaction if a transaction exists, note that this does not exist on the remainer transaction

    @Property("transactionId")
    private String transactionId;
    @Property("lastBranch")
    private String lastBranch;//make sure this is set and saved.
    @Property("branchLastUpdated")
    private Long branchLastUpdated;

    @Property("minWeightMagnitude")
    private int minWeightMagnitude = 15;

    public ProcessorTransaction() {
    }

    public ProcessorTransaction(String trytes, String state, String processorId, String transactionId) {
        this.trytes = trytes;
        this.state = state;
        this.transactionId = transactionId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public String getTrytes() {
        return trytes;
    }

    public String getState() {
        return state;
    }

    public String getHashedState() {
        return hashedState;
    }

    public String getLastBranch() {
        return lastBranch;
    }

    public void setLastBranch(String lastBranch) {
        this.lastBranch = lastBranch;
    }

    public void setTrytes(String trytes) {
        this.trytes = trytes;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getMinWeightMagnitude() {
        return minWeightMagnitude;
    }

    public Long getBranchLastUpdated() {
        return branchLastUpdated;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
