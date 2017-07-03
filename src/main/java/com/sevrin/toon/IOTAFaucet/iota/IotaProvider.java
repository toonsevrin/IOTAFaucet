package com.sevrin.toon.IOTAFaucet.iota;

import com.google.gson.Gson;
import com.iota.curl.IotaCurlMiner;
import com.sevrin.toon.IOTAFaucet.database.StoredTransaction;
import jota.IotaAPI;
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
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static jota.pow.JCurl.HASH_LENGTH;

/**
 * Created by toonsev on 6/10/2017.
 */
public class IotaProvider {
    private static final String SPAM_ADDRESS = "999999999999999999999999999999999999999999999999999999999999999999999999999999999";
    private static final String TAG = "FAUCET9TRANSFER999999999999";
    private static final String SPAM_TAG = "FAUCET9SPAM9999999999999999";
    public static final int RECOMMENDED_MIN_WEIGHT_MAGNITUDE = 15;
    private String seed;
    private IotaAPI iotaAPI;

    public IotaProvider(IotaAPI iotaAPI, String seed) {
        this.iotaAPI = iotaAPI;
        this.seed = StringUtils.rightPad(seed, 81, "9");
    }


    public String spamTransaction(String trunk, int minWeightMagnitude) throws InvalidSecurityLevelException, InvalidAddressException, InvalidTransferException, NotEnoughBalanceException, ExecutionException, InterruptedException, BroadcastAndStoreException, InvalidTrytesException {

        String trytes = iotaAPI.prepareTransfers("", 2, Arrays.asList(new Transfer(SPAM_ADDRESS, 0, "SPAM", SPAM_TAG)), null, null).get(0);
        String sent = iotaAPI.attachToTangle(trunk, getNewBranchTransaction(), minWeightMagnitude, trytes).getTrytes()[0];
        iotaAPI.broadcastAndStore(sent);
        return sent;

    }

    public int[] getTritssWithHash(int[] originalTrits, int[] hash) {
        int[] toReturn = new int[originalTrits.length];//no sideeffects
        System.arraycopy(originalTrits, 0, toReturn, 0, originalTrits.length);
        System.arraycopy(hash, 0, toReturn, originalTrits.length - 243, 243);

        return toReturn;
    }

    public int[] validateTransaction(String hash, final String state, int minWeightMagnitude) {
        int[] hashTrits = Converter.trits(hash);
        int[] stateArray = Converter.trits(state);
        if (hashTrits.length != 243) {
            System.out.println("Received hash trits with length " + hashTrits.length);
            return null;
        }
        //
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
    public String searchConfirmedTransactionHash() throws NoNodeInfoException {
        return (searchConfirmedTransactionHash(getNewBranchTransaction()));
    }
    public String searchConfirmedTransactionHash(String txHash) throws NoNodeInfoException {
        if (iotaAPI.getLatestInclusion(new String[]{txHash}).getStates()[0])
            return txHash;
        String transaction = getNewBranchTransaction();
        List<Transaction> transactions = iotaAPI.getTransactionsObjects(new String[]{transaction});
        if (transactions == null || transactions.size() == 0 || transactions.get(0).getTrunkTransaction() == null || transactions.get(0).getTrunkTransaction().equals(SPAM_ADDRESS))
            return searchConfirmedTransactionHash(getNewBranchTransaction());
        return (transactions.get(0).getTrunkTransaction());
    }

    public void broadcastAndStore(String... finalizedTransactionTrytes) throws BroadcastAndStoreException {
        iotaAPI.broadcastAndStore(finalizedTransactionTrytes);
    }


    public boolean isBundleConfirmed(String[] hashes) {
        try {
            for (boolean included : iotaAPI.getLatestInclusion(hashes).getStates())
                if (!included)
                    return false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getNewBranchTransaction() {
        return iotaAPI.getTransactionsToApprove(3).getBranchTransaction();
    }

    public GetTransactionsToApproveResponse getNewBranchAndTrunkTransactions() {
        return iotaAPI.getTransactionsToApprove(3);
    }

    public List<String> prepareTransaction(Iterable<StoredTransaction> transactions, int currentAddressIndex, String currentAddress, String remainderAddress) throws InvalidAddressException, InvalidSecurityLevelException, NotEnoughBalanceException, InvalidTransferException {
        List<Transfer> transfers = StreamSupport.stream(transactions.spliterator(), false)
                .map(storedTransaction -> new Transfer(storedTransaction.getWalletAddress(), storedTransaction.getAmount(), "IOTAFAUCET9TRANSFER", TAG))
                .collect(Collectors.toList());
        long total = 0;
        for (Transfer transfer : transfers)
            total += transfer.getValue();
        List<Input> inputs = iotaAPI.getBalanceAndFormat(Arrays.asList(currentAddress), total, currentAddressIndex, 0, new StopWatch(), 2).getInput();
        return iotaAPI.prepareTransfers(seed, 2, transfers, remainderAddress, inputs, false);
    }

    public String trytesToStateMatrix(String trytes) {
        System.out.println(trytes);
        int[] trits = Converter.trits(trytes);
        ICurl curl = new JCurl();
        curl.absorb(trits, 0, trits.length - HASH_LENGTH);
        return Converter.trytes(curl.getState());
    }

    public String getSeed() {
        return seed;
    }
}
