package com.sevrin.toon.IOTAFaucet.database;

import org.mongodb.morphia.annotations.*;

/**
 * Created by toonsev on 6/19/2017.
 */
@Entity("usageRecords")
@Indexes({
        @Index(value = "ipAddress", fields = @Field("ipAddress")),

        @Index(value = "walletAddress", fields = @Field("walletAddress"))}
)
public class StoredUsage {
    @Id
    private String id;
    @Property("ipAddress")
    private String ipAddress;

    @Property("walletAddress")
    private String walletAddress;

    @Property("lastUsage")
    private Long lastUsage;

    public StoredUsage(String ipAddress, String walletAddress) {
        this.ipAddress = ipAddress;
        this.walletAddress = walletAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public Long getLastUsage() {
        return lastUsage;
    }
}
