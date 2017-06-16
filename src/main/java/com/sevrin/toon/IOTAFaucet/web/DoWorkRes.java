package com.sevrin.toon.IOTAFaucet.web;

/**
 * Created by toonsev on 6/16/2017.
 */
public class DoWorkRes {
    private String transactionId;
    private String hash;

    public DoWorkRes(String transactionId, String hash) {
        this.transactionId = transactionId;
        this.hash = hash;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getHash() {
        return hash;
    }
}
