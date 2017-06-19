package com.sevrin.toon.IOTAFaucet.backend;

/**
 * Created by toonsev on 6/19/2017.
 */
public interface FaucetConfig {
    long getBroadcastLoopIntervalInMillis();

    long getConfirmLoopIntervalInMillis();

    long getRestartLoopIntervalInMillis();

    long getSendingLoopIntervalInMillis();

    long getStartLoopIntervalInMillis();

    long getTokenReceiveIntervalInMillis();

    long getPayoutPerRequest();

}
