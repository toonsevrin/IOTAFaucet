package com.sevrin.toon.IOTAFaucet.database;

/**
 * Created by toonsev on 6/17/2017.
 */
public class ProcessorTransaction {
    private String uniqueId;
    private String processorId;
    private String trytes;
    private String state;
    private String hashedTrytes;
    //linked transaction if a transaction exists, note that this does not exist on the remainer transaction
    private String transactionId;
    private String lastBranch;
    private Long branchLastUpdated;

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

    public Long getBranchLastUpdated() {
        return branchLastUpdated;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
