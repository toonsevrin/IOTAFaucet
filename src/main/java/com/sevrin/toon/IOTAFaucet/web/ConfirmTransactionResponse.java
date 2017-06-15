package com.sevrin.toon.IOTAFaucet.web;

/**
 * Created by toonsev on 6/15/2017.
 */
public class ConfirmTransactionResponse {
    private boolean success;
    private Integer errorCode;
    private String message;

    public ConfirmTransactionResponse(String message) {
        success = true;
        this.message = message;
    }

    public ConfirmTransactionResponse(Integer errorCode, String message) {
        this.success = false;
        this.errorCode = errorCode;
        this.message = message;
    }
}
