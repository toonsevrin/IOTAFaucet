package com.sevrin.toon.IOTAFaucet.backend;

/**
 * Created by toonsev on 6/16/2017.
 */
public class DoWorkRes {
    private String processorId;
    private String processorTransactionUniqueId;
    private String hash;

    public DoWorkRes(String transactionId, String processorId, String hash) {
        this.processorTransactionUniqueId = processorTransactionUniqueId;
        this.processorId = processorId;
        this.hash = hash;
    }

    public String getProcessorId() {
        return processorId;
    }

    public String getProcessorTransactionUniqueId() {
        return processorTransactionUniqueId;
    }

    public String getHash() {
        return hash;
    }
}
