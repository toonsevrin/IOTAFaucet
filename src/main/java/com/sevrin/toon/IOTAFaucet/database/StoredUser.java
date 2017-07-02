package com.sevrin.toon.IOTAFaucet.database;

import org.mongodb.morphia.annotations.*;

/**
 * Created by toonsev on 6/19/2017.
 */
@Entity("users")
@Indexes({
        @Index(value = "totalTokensReceived", fields = @Field("totalTokensReceived"))}
)
public class StoredUser {
    @Id
    private String address;


    @Property("totalTokensReceived")
    private Long totalTokensReceived;


    public StoredUser(String address) {
        this.address = address;
    }

    public StoredUser() {}

    public String getAddress() {
        return address;
    }
}
