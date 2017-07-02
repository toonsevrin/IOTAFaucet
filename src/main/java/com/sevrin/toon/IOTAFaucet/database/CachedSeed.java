package com.sevrin.toon.IOTAFaucet.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by toonsev on 6/19/2017.
 */
@Entity("seeds")
public class CachedSeed {
    @Id
    private String seed;

    @Property("lastIndex")
    private Integer lastIndex;

    public CachedSeed() {
    }

    public CachedSeed(String seed, Integer lastIndex) {
        this.seed = seed;
        this.lastIndex = lastIndex;
    }

    public Integer getLastIndex() {
        return lastIndex;
    }

    public String getSeed() {
        return seed;
    }
}
