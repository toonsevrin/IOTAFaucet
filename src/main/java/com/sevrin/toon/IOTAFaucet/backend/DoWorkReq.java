package com.sevrin.toon.IOTAFaucet.backend;

/**
 * Created by toonsev on 6/16/2017.
 */
public class DoWorkReq {
    private String processorId;
    private String processorTransactionUniqueId;
    private String state;

    public DoWorkReq(String processorId, String processorTransactionUniqueId, String state) {
        this.processorId = processorId;
        this.processorTransactionUniqueId = processorTransactionUniqueId;
        this.state = state;
    }

    public String getProcessorId() {
        return processorId;
    }

    public String getProcessorTransactionUniqueId() {
        return processorTransactionUniqueId;
    }

    public String getState() {
        return state;
    }

    public int getMinWeightMagnitude(){
        return 15;
    }
}
