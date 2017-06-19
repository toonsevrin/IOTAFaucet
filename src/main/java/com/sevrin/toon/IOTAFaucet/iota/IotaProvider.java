package com.sevrin.toon.IOTAFaucet.iota;

import com.sevrin.toon.IOTAFaucet.database.StoredTransaction;
import jota.IotaAPI;
import jota.dto.response.FindTransactionResponse;
import jota.dto.response.GetNewAddressResponse;
import jota.dto.response.GetTransactionsToApproveResponse;
import jota.error.*;
import jota.model.Input;
import jota.model.Transaction;
import jota.model.Transfer;
import jota.pow.ICurl;
import jota.pow.JCurl;
import jota.utils.Converter;
import jota.utils.IotaAPIUtils;
import jota.utils.StopWatch;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by toonsev on 6/10/2017.
 */
public class IotaProvider {
    private static final String TAG = "IOTA9TRANSFER99999999999999";
    private String seed;
    private IotaAPI iotaAPI;

    public IotaProvider(IotaAPI iotaAPI, String seed) {
        this.seed = seed;
    }

    public int[] validateTransaction(String hash, final String state, int minWeightMagnitude) {
        int[] hashTrits = Converter.trits(hash);
        int[] stateArray = Converter.trits(state);
        if (hashTrits.length != 243) {
            System.out.println("Received hash trits with length " + hashTrits.length);
            return null;
        }
        System.arraycopy(hashTrits, 0, stateArray, 0, 243);//maybe not use the realState for this, as we may want to keep it immutable

        ICurl curl = new JCurl();
        curl.setState(stateArray);
        curl.transform();
        int[] transformedState = curl.getState();
        boolean valid = validateState(transformedState, minWeightMagnitude);
        return valid ? transformedState : null;
    }

    private boolean validateState(final int[] state, final int minWeightMagnitude) {
        int zeros = 0;
        for (int i = 242; i >= 0; i--) {
            if (state[i] != 0)
                return zeros >= minWeightMagnitude ? true : false;//this check is not really necessary;
            zeros++;//++zeros?
            if (zeros >= minWeightMagnitude)
                return true;
        }
        return false;
    }

    public String getNewAttachedAddress(int lastKnownIndex) throws InvalidAddressException, InvalidSecurityLevelException {
        String address = iotaAPI.getNewAddress(seed, 2, lastKnownIndex, false, 0, true).getAddresses().get(0);
        return address;
    }

    public Integer getAvailableAddressIndex(Integer lastKnownIndex) throws InvalidAddressException {
        int i = lastKnownIndex == null ? -1 : lastKnownIndex;
        while (true) {
            i++;
            String newAddress = IotaAPIUtils.newAddress(seed, 2, i, false, null);
            if (iotaAPI.findTransactionsByAddresses(new String[]{newAddress}).getHashes().length == 0)
                return i;
        }
    }

    public String getAddress(int index) throws InvalidAddressException {
        return IotaAPIUtils.newAddress(seed, 2, index, false, null);
    }

    public Integer getFirstAddressWithFunds() throws InvalidAddressException, InvalidSecurityLevelException {
        return iotaAPI.getInputs(seed, 2, 0, 300, 1).getInput().get(0).getKeyIndex();
    }

    public void broadcastAndStore(String... finalizedTransactionTrytes) throws BroadcastAndStoreException {
        iotaAPI.broadcastAndStore(finalizedTransactionTrytes);
    }

    public boolean areHashesConfirmed(String[] hashes) {

        for (Transaction transaction : iotaAPI.getTransactionsObjects(hashes))
            if (!transaction.getPersistence())
                return false;
        return true;
    }

    //Only the transaction to the new address should be confirmed!
    public boolean isBundleConfirmed(List<String> transactionsTrytes) {
        String[] hashes = transactionsTrytes.stream()
                .map(transactionTrytes -> new Transaction(transactionTrytes))
                .map(transaction -> transaction.getHash())
                .toArray(String[]::new);
        for (Transaction transaction : iotaAPI.getTransactionsObjects(hashes))
            if (!transaction.getPersistence())
                return false;
        return true;
    }

    public String getNewBranchTransaction() {
        return iotaAPI.getTransactionsToApprove(3).getBranchTransaction();
    }

    public GetTransactionsToApproveResponse getNewBranchAndTrunkTransactions() {
        return iotaAPI.getTransactionsToApprove(3);
    }

    public List<String> prepareTransaction(Iterable<StoredTransaction> transactions, String currentAddress, String remainderAddress) throws InvalidAddressException, InvalidSecurityLevelException, NotEnoughBalanceException, InvalidTransferException {
        List<Transfer> transfers = StreamSupport.stream(transactions.spliterator(), false)
                .map(storedTransaction -> new Transfer(storedTransaction.getWalletAddress(), storedTransaction.getAmount(), "IOTAFAUCET9TRANSFER", TAG))
                .collect(Collectors.toList());
        long total = 0;
        for (Transfer transfer : transfers)
            total += transfer.getValue();
        List<Input> inputs = iotaAPI.getBalanceAndFormat(Arrays.asList(currentAddress), total, 0, 0, new StopWatch(), 2).getInput();
        return iotaAPI.prepareTransfers(seed, 2, transfers, remainderAddress, inputs);
    }

    public String getSeed() {
        return seed;
    }
}
