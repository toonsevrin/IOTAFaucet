package com.sevrin.toon.IOTAFaucet.database;


/**
 * Created by toonsev on 6/16/2017.
 */
public class StoredBundle {
    private String uniqueId;
    private long bundleIndex;
    private Long started;
    private Long stopped;
    private String currentTransaction;
    private String currentState;
    private Boolean send;
    private Boolean confirmed;

    public StoredBundle() {}

    public StoredBundle(long bundleIndex) {
        this.bundleIndex = bundleIndex;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public long getBundleIndex() {
        return bundleIndex;
    }

    public Long getStarted() {
        return started;
    }

    public Long getStopped() {
        return stopped;
    }

    public Boolean getSend() {
        return send;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }
}