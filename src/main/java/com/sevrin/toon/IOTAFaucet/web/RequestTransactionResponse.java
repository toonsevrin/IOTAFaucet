package com.sevrin.toon.IOTAFaucet.web;

/**
 * Created by toonsev on 6/15/2017.
 */
public class RequestTransactionResponse {
    private boolean success;
    private PowableTransaction transaction;
    private Integer errorCode;
    private String message;

    public RequestTransactionResponse(String message) {
        this.message = message;
        this.success = true;
    }

    public RequestTransactionResponse(PowableTransaction transaction, String message) {
        success = true;
        this.transaction = transaction;
    }

    public RequestTransactionResponse(int errorCode, String message) {
        success = false;
        this.errorCode = errorCode;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }


    /**
     * Will only return a value when enough increments have been made
     */
    public PowableTransaction getTransaction() {
        return transaction;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

}
