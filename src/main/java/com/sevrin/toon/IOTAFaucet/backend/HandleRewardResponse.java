package com.sevrin.toon.IOTAFaucet.backend;

import com.sevrin.toon.IOTAFaucet.database.StoredTransaction;

/**
 * Created by toonsev on 6/16/2017.
 */
public class HandleRewardResponse {
    private boolean success;
    private StoredTransaction transaction;
    private Integer errorCode;
    private String message;

    public HandleRewardResponse(StoredTransaction transaction, String message) {
        this.transaction = transaction;
        this.message = message;
    }

    public HandleRewardResponse(Integer errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public StoredTransaction getTransaction() {
        return transaction;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
