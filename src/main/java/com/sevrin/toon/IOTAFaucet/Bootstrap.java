package com.sevrin.toon.IOTAFaucet;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.MongoDatabaseProvider;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import com.sevrin.toon.IOTAFaucet.web.Backend;
import com.sevrin.toon.IOTAFaucet.web.Frontend;
import jota.IotaAPI;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by toonsev on 6/10/2017.
 */
public class Bootstrap {
    public static void main(String[] args) {
        URI nodeAddress = URI.create(getEnv("IOTA_URI"));

        IotaAPI iotaAPI = new IotaAPI.Builder().protocol(nodeAddress.getScheme()).host(nodeAddress.getHost()).port(nodeAddress.getPort() + "").build();
        DatabaseProvider databaseProvider = loadDatabaseProvider();
        IotaProvider iotaProvider = new IotaProvider(iotaAPI, "");
        Backend backend = new Backend(iotaProvider, databaseProvider, new FaucetConfig() {
            @Override
            public long getMinPayoutBalance() {
                return 20;
            }

            @Override
            public long getIntervalInMillis() {
                return TimeUnit.MINUTES.toMillis(1);
            }

            @Override
            public long getBalanceIncPerRequest() {
                return 10;
            }
        });
        Frontend.setup(backend);
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
