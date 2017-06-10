package com.sevrin.toon.IOTAFaucet;

import jota.IotaAPI;

import java.net.URI;

/**
 * Created by toonsev on 6/10/2017.
 */
public class Bootstrap {
    public static void main(String[] args) {
        URI nodeAddress = URI.create(getEnv("iotaUri"));
        IotaAPI iotaAPI = new IotaAPI.Builder().protocol(nodeAddress.getScheme()).host(nodeAddress.getHost()).port(nodeAddress.getPort() + "").build();
        IOTAFaucet iotaFaucet = new IOTAFaucet(iotaAPI);
        Frontend.setup(iotaFaucet);
    }

    public static String getEnv(String env) {
        String value = System.getenv(env);
        if (value == null || value.equals("")) {
            System.out.println("Env " + env + " not found. Terminating.");
            System.exit(-1);
        }
        return value;
    }
}
