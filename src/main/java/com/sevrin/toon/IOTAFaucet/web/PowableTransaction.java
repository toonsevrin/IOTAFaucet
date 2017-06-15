package com.sevrin.toon.IOTAFaucet.web;

/**
 * Created by toonsev on 6/12/2017.
 */
public class PowableTransaction {
    String transactionTrytes;
    String branchTransaction;
    String trunkTransaction;

    public PowableTransaction(String transactionTrytes, String branchTransaction, String trunkTransaction) {
        this.transactionTrytes = transactionTrytes;
        this.branchTransaction = branchTransaction;
        this.trunkTransaction = trunkTransaction;
    }

    public String getTransactionTrytes() {
        return transactionTrytes;
    }

    public String getBranchTransaction() {
        return branchTransaction;
    }

    public String getTrunkTransaction() {
        return trunkTransaction;
    }

    public int getMinWeightMagnitude(){
        return 15;
    }
}
