package com.sevrin.toon.IOTAFaucet;


import jota.IotaAPI;
import jota.dto.response.GetNodeInfoResponse;
import jota.dto.response.SendTransferResponse;
import jota.error.*;
import jota.model.Transaction;
import jota.model.Transfer;
import jota.utils.IotaAPIUtils;
import jota.utils.TrytesConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toonsev on 6/10/2017.
 */
public class IOTAFaucet {
    public static final String VERSION = "v0.0.1";
    private IotaAPI iotaAPI;
    private static final String TEST_TAG = "JOTASPAMAAA9999999999999999";
    private static final String SEED = "TOONSTANKIOTA";

    public IOTAFaucet(IotaAPI iotaAPI) {
        this.iotaAPI = iotaAPI;

    }

    public void send(String address, int amount) throws InvalidSecurityLevelException, InvalidAddressException, InvalidTrytesException, InvalidTransferException, NotEnoughBalanceException, UnsuccessfulTransferException {
        List<Transfer> transfers = new ArrayList<>();
        Transfer transfer = new Transfer(address, amount, "FAUCET9PAYMENT", TEST_TAG);
        transfers.add(transfer);

        SendTransferResponse res = iotaAPI.sendTransfer(SEED, 2, 9, 15, transfers, null, null);
        if (!res.getSuccessfully()[0])
            throw new UnsuccessfulTransferException();

    }
}
