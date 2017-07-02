package com.sevrin.toon.IOTAFaucet.backend;

import org.bson.types.ObjectId;

/**
 * Created by toonsev on 6/16/2017.
 */
public class DoWorkRes {
    private String processorId;
    private String processorTransactionUniqueId;
    private String hash;

    public DoWorkRes() {}

    public DoWorkRes(String processorId, String processorTransactionUniqueId, String hash) {
        this.processorId = processorId;
        this.processorTransactionUniqueId = processorTransactionUniqueId;
        this.hash = hash;
    }

    public String getProcessorId() {
        return processorId;
    }

    public ObjectId getProcessorTransactionUniqueId() {
        return new ObjectId(processorTransactionUniqueId);
    }

    public String getHash() {
        return hash;
    }
}
