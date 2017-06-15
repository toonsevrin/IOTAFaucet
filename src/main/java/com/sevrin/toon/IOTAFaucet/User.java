package com.sevrin.toon.IOTAFaucet;

/**
 * Created by toonsev on 6/12/2017.
 */
public class User {
    private String walletAddress;
    private String ipAddress;

    public User(String walletAddress, String ipAddress) {
        this.walletAddress = walletAddress;
        this.ipAddress = ipAddress;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
