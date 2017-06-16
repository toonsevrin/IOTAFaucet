package com.sevrin.toon.IOTAFaucet.database;

/**
 * Created by toonsev on 6/16/2017.
 */
public class StoredTransaction {
    private String transactionId;
    private Long created;
    private Long started;
    private Long stopped;
    private String originalTrytes;
    private String originalStateTrytes;
    private String hashedTrytes;
    private String walletAddress;
    private long amount;


    private int minWeightMagnitude = 15;


    public StoredTransaction() {}

    public void setCreated(Long created) {
        this.created = created;
    }

    public void setStarted(Long started) {
        this.started = started;
    }

    public void setStopped(Long stopped) {
        this.stopped = stopped;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public void setOriginalTrytes(String originalTrytes) {
        this.originalTrytes = originalTrytes;
    }

    public void setHashedTrytes(String hashedTrytes) {
        this.hashedTrytes = hashedTrytes;
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

    public Long getStarted() {
        return started;
    }

    public String getOriginalStateTrytes() {
        return originalStateTrytes;
    }

    public Long getStopped() {
        return stopped;
    }

    public String getOriginalTrytes() {
        return originalTrytes;
    }

    public String getHashedTrytes() {
        return hashedTrytes;
    }
}
