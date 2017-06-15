package com.sevrin.toon.IOTAFaucet.iota;

import com.sevrin.toon.IOTAFaucet.web.PowableTransaction;
import jota.IotaAPI;
import jota.dto.response.GetTransactionsToApproveResponse;
import jota.error.*;
import jota.model.Transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toonsev on 6/10/2017.
 */
public class IotaProvider {
    private static final String TAG = "LATESTPAYMENT99999999999999";
    private String seed;
    private IotaAPI iotaAPI;

    public IotaProvider(IotaAPI iotaAPI, String seed) {
        this.iotaAPI = iotaAPI;
        this.seed = seed;
    }

    public PowableTransaction prepareTransaction(String address, long amount) throws InvalidSecurityLevelException, InvalidAddressException, InvalidTransferException, NotEnoughBalanceException {
        List<Transfer> transfers = new ArrayList<>();
        Transfer transfer = new Transfer(address, amount, "LATESTPAYMENT", TAG);
        transfers.add(transfer);
        List<String> trytes = iotaAPI.prepareTransfers(seed, 2, transfers, null, null);
        GetTransactionsToApproveResponse transactionsToApprove = iotaAPI.getTransactionsToApprove(2);
        return new PowableTransaction(trytes.get(0), transactionsToApprove.getBranchTransaction(), transactionsToApprove.getTrunkTransaction());
    }
    public String getNewAttachedAddress(int lastKnownIndex) throws InvalidAddressException, InvalidSecurityLevelException {
        String address = iotaAPI.getNewAddress(seed, 2, lastKnownIndex, false, 0, true).getAddresses().get(0);
        return address;
    }

    public void finalizeTransaction(String finalizedTransactionTrytes) throws BroadcastAndStoreException {
        iotaAPI.broadcastAndStore(finalizedTransactionTrytes);
    }
}
