package com.sevrin.toon.IOTAFaucet.iota;

import org.junit.Before;

import static org.junit.Assert.assertFalse;

/**
 * Created by toonsev on 6/16/2017.
 */
public class IotaProviderTest {
    private IotaProvider provider;

    @Before
    public void setup() {
        provider = new IotaProvider(null, "");
    }
}
