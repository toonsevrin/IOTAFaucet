package com.sevrin.toon.IOTAFaucet;

/**
 * Created by toonsev on 6/15/2017.
 */
public interface FaucetConfig {
    long getMinPayoutBalance();
    long getIntervalInMillis();
    long getBalanceIncPerRequest();
}
