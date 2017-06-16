package com.sevrin.toon.IOTAFaucet.database;

/**
 * Created by toonsev on 6/16/2017.
 */
public class CurrentTransaction {
    private long index;
    private String state;

    public CurrentTransaction(long index, String state) {
        this.index = index;
        this.state = state;
    }

    public long getIndex() {
        return index;
    }

    public String getState() {
        return state;
    }
}
