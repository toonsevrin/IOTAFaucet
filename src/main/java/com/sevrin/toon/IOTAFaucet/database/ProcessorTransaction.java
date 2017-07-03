package com.sevrin.toon.IOTAFaucet.database;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by toonsev on 6/17/2017.
 */
@Entity("processorTransactions")
@Indexes({
        @Index(value = "processorId", fields = @Field("processorId"))
}
)
public class ProcessorTransaction {
    @Id
    private ObjectId uniqueId;

    @Property("processorId")
    private String processorId;
    @Property("trytes")
    private String trytes;
    @Property("state")
    private String state;
    @Property("hashedTrytes")
    private String hashedTrytes;
    //linked transaction if a transaction exists, note that this does not exist on the remainer transaction

    @Property("lastBranch")
    private String lastBranch;//make sure this is set and saved.
    @Property("branchLastUpdated")
    private Long branchLastUpdated;

    @Property("minWeightMagnitude")
    private int minWeightMagnitude = 15;
    @Property("bundleIndex")
    private long bundleIndex;//THIS IS THE INDEX WITHIN THE BUNDLE EG. THE CURRENTINDEX


    public ProcessorTransaction() {
    }

    public ProcessorTransaction(String trytes,  long bundleIndex, String state, String processorId) {
        this.trytes = trytes;
        this.bundleIndex = bundleIndex;
        this.state = state;
        this.processorId = processorId;
    }

    public long getIndexInBundle() {
        return bundleIndex;
    }
    public ObjectId getUniqueId() {
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

    public String getHashedTrytes() {
        return hashedTrytes;
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

}
