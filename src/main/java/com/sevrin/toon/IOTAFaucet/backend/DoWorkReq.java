package com.sevrin.toon.IOTAFaucet.backend;

/**
 * Created by toonsev on 6/16/2017.
 */
public class DoWorkReq {
    private String processorId;
    private String processorTransactionUniqueId;
    private String state;
    private String trytes;
    private int minWeightMagnitude;

    public DoWorkReq(String processorId, String processorTransactionUniqueId, String trytes, String state, int minWeightMagnitude) {
        this.processorId = processorId;
        this.processorTransactionUniqueId = processorTransactionUniqueId;
        this.trytes = trytes;
        this.state = state;
        this.minWeightMagnitude = minWeightMagnitude;
    }

    public String getTrytes() {
        return trytes;
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
        return minWeightMagnitude;
    }
}
