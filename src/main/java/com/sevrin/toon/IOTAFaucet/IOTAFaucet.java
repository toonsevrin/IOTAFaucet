package com.sevrin.toon.IOTAFaucet;


import jota.IotaAPI;

/**
 * Created by toonsev on 6/10/2017.
 */
public class IOTAFaucet {
    public static final String VERSION = "v0.0.1";
    private IotaAPI iotaAPI;

    public IOTAFaucet(IotaAPI iotaAPI) {
        this.iotaAPI = iotaAPI;
    }
}
