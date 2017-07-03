package com.sevrin.toon.IOTAFaucet.database;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by toonsev on 7/3/2017.
 */
@Entity("transactionIndices")
public class TransactionIndex {
    @Id
    private String id = "index";
    @Property("currentIndex")
    private long currentIndex;

    public long getCurrentIndex() {
        return currentIndex;
    }
}
