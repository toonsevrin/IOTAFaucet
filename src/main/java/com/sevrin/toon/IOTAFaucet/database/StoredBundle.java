package com.sevrin.toon.IOTAFaucet.database;


/**
 * Created by toonsev on 6/16/2017.
 */
public class StoredBundle {
    private String uniqueId;
    private long bundleId;
    private Long started;
    private Long stopped;
    private String processor;
    private String currentTransaction;
    private Long sent;
    private Long confirmed;

    private String branch;
    private String trunk;
    private String lastTransaction;
    private Long lastSpammed;

    public StoredBundle() {}

    public StoredBundle(long bundleId) {
        this.bundleId = bundleId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public long getBundleId() {
        return bundleId;
    }

    public Long getStarted() {
        return started;
    }


    public String getProcessor() {
        return processor;
    }

    public String getCurrentTransaction() {
        return currentTransaction;
    }

    public Long getSent() {
        return sent;
    }

    public String getLastTransaction() {
        return lastTransaction;
    }

    public Long getStopped() {
        return stopped;
    }

    public String getBranch() {
        return branch;
    }

    public String getTrunk() {
        return trunk;
    }

    public Long getLastSpammed() {
        return lastSpammed;
    }

    public Long getConfirmed() {
        return confirmed;
    }
}