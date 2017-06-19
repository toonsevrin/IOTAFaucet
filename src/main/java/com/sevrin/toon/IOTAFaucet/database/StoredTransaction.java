package com.sevrin.toon.IOTAFaucet.database;

/**
 * Created by toonsev on 6/16/2017.
 */
public class StoredTransaction {
    private String transactionId;
    private Long created;
    private String walletAddress;
    private long amount;


    private int minWeightMagnitude = 15;


    public StoredTransaction() {}



    public void setCreated(Long created) {
        this.created = created;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }


    public String getTransactionId() {
        return transactionId;
    }

    public Long getCreated() {
        return created;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public long getAmount() {
        return amount;
    }

    public int getMinWeightMagnitude() {
        return minWeightMagnitude;
    }
}
