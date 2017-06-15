package com.sevrin.toon.IOTAFaucet;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.MongoDatabaseProvider;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import com.sevrin.toon.IOTAFaucet.web.Frontend;
import jota.IotaAPI;

import java.net.URI;

/**
 * Created by toonsev on 6/10/2017.
 */
public class Bootstrap {
    public static void main(String[] args) {
        URI nodeAddress = URI.create(getEnv("IOTA_URI"));

        IotaAPI iotaAPI = new IotaAPI.Builder().protocol(nodeAddress.getScheme()).host(nodeAddress.getHost()).port(nodeAddress.getPort() + "").build();
        DatabaseProvider databaseProvider = loadDatabaseProvider();
        IotaProvider iotaProvider = new IotaProvider(iotaAPI, "");
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

    public static MongoDatabaseProvider loadDatabaseProvider() {
        MongoClientURI uri = new MongoClientURI(getEnv("MONGO_URI"));
        String databaseName = getEnv("DB_NAME");
        return new MongoDatabaseProvider(new MongoClient(uri), databaseName);
    }
}
