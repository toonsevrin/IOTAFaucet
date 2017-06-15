package com.sevrin.toon.IOTAFaucet.web;


import com.sevrin.toon.IOTAFaucet.FaucetConfig;
import com.sevrin.toon.IOTAFaucet.database.DatabaseProvider;
import com.sevrin.toon.IOTAFaucet.iota.IotaProvider;
import com.sevrin.toon.IOTAFaucet.User;
import jota.model.Transaction;

import java.util.concurrent.TimeUnit;

/**
 * Created by toonsev on 6/10/2017.
 */
public class Backend {
    public static final String VERSION = "v0.0.1";

    private final IotaProvider iotaProvider;
    private final DatabaseProvider databaseProvider;
    private final long interval;
    private final long balanceIncPerRequest;
    private final long minPayoutBalance;

    public Backend(IotaProvider iotaProvider, DatabaseProvider databaseProvider, FaucetConfig faucetConfig) {
        this.iotaProvider = iotaProvider;
        this.databaseProvider = databaseProvider;
        this.interval = faucetConfig.getIntervalInMillis();
        this.balanceIncPerRequest = faucetConfig.getBalanceIncPerRequest();
        this.minPayoutBalance = faucetConfig.getMinPayoutBalance();
    }

    public RequestTransactionResponse requestTransaction(User user) {
        try {
            Long tokensReceivedTime = databaseProvider.getLastTokensReceived(user);
            if (tokensReceivedTime != null) {
                long difference = tokensReceivedTime + interval - System.currentTimeMillis();
                if (difference > 0)
                    return new RequestTransactionResponse(-2, "You will need to wait " + getTimeToWait(difference) + ".");
            }
            databaseProvider.setTokensReceived(user);
            databaseProvider.incBalance(user.getWalletAddress(), 1);
            long balance = databaseProvider.getBalance(user.getWalletAddress());
            if (balance >= minPayoutBalance) {
                return new RequestTransactionResponse(iotaProvider.prepareTransaction(user.getWalletAddress(), balance), balance + " iota will now be payed out to your address.");
            } else
                return new RequestTransactionResponse("You received " + balanceIncPerRequest + " iota. " + (minPayoutBalance - balanceIncPerRequest) + " more required for a payout");
        } catch (Exception e) {
            e.printStackTrace();
            return new RequestTransactionResponse(-1, "A backend error occured while trying to process your request.");
        }
    }

    public ConfirmTransactionResponse confirmTransaction(String trytes) {
        try {
            Transaction transaction = new Transaction(trytes);
            String address = transaction.getAddress();
            if (address == null)
                return new ConfirmTransactionResponse(-2, "address missing in transaction");
            long amount = transaction.getValue();
            if (amount <= 0 || amount < minPayoutBalance)
                return new ConfirmTransactionResponse(-2, "This transaction is not large enough");
            if (amount > databaseProvider.getBalance(transaction.getAddress()))
                return new ConfirmTransactionResponse(-3, "You don't have this amount of balance!");
            databaseProvider.incBalance(address, -amount);
            iotaProvider.finalizeTransaction(trytes);
            return new ConfirmTransactionResponse(amount + " iota has been send to " + address + ".");
        } catch (Exception e) {
            e.printStackTrace();
            return new ConfirmTransactionResponse(-1, "A backend error occured while trying to process your request.");
        }
    }


    private String getTimeToWait(long millis) {
        return String.format("%02dhours, %02dminutes and %02dseconds",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
