package com.sevrin.toon.IOTAFaucet;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.sevrin.toon.IOTAFaucet.backend.Backend;
import com.sevrin.toon.IOTAFaucet.backend.FaucetConfig;
import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.database.MongoDatabaseProvider;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
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
        IotaProvider iotaProvider = new IotaProvider(iotaAPI, getEnv("SEED"));
        Backend backend = new Backend(iotaProvider, databaseProvider, new FaucetConfigImpl());
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

    private static class FaucetConfigImpl implements FaucetConfig {
        @Override
        public long getBroadcastLoopIntervalInMillis() {
            return TimeUnit.SECONDS.toMillis(15);
        }

        @Override
        public long getConfirmLoopIntervalInMillis() {
            return TimeUnit.SECONDS.toMillis(5);
        }

        @Override
        public long getRestartLoopIntervalInMillis() {
            return TimeUnit.SECONDS.toMillis(30);
        }

        @Override
        public long getSendingLoopIntervalInMillis() {
            return TimeUnit.SECONDS.toMillis(5);
        }

        @Override
        public long getStartLoopIntervalInMillis() {
            return TimeUnit.SECONDS.toMillis(5);
        }

        @Override
        public long getTokenReceiveIntervalInMillis() {
            return TimeUnit.SECONDS.toMillis(1);
        }

        @Override
        public long getPayoutPerRequest() {
            return 10;
        }
    }
}
