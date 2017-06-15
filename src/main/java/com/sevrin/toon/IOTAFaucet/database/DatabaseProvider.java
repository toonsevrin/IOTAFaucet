package com.sevrin.toon.IOTAFaucet.database;

import com.sevrin.toon.IOTAFaucet.User;

/**
 * Created by toonsev on 6/12/2017.
 */
public interface DatabaseProvider {
    Long getLastTokensReceived(User user);

    void setTokensReceived(User user);

    long getBalance(String walletAddress);

    void incBalance(String walletAddress, long balance);

    Integer getLastKnownAddressIndex(String seed);

    void setLastKnownAddressIndex(String seed, int index);
}
