package com.sevrin.toon.IOTAFaucet.web;

/**
 * Created by toonsev on 6/16/2017.
 */
public class DoWorkReq {
    private long bundleIndex;
    private long transactionIndex;
    private String state;

    public DoWorkReq(long bundleIndex, long transactionIndex, String state) {
        this.bundleIndex = bundleIndex;
        this.transactionIndex = transactionIndex;
        this.state = state;
    }

    public long getBundleIndex() {
        return bundleIndex;
    }

    public long getTransactionIndex() {
        return transactionIndex;
    }

    public String getState() {
        return state;
    }

    public int getMinWeightMagnitude(){
        return 15;
    }
}
