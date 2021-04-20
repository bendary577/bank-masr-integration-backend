package com.sun.supplierpoc.models.applications;

public class SimphonyQuota {

    private int transactionQuota;

    private int usedTransactionQuota;

    public int getTransactionQuota() {
        return transactionQuota;
    }

    public void setTransactionQuota(int transactionQuota) {
        this.transactionQuota = transactionQuota;
    }

    public int getUsedTransactionQuota() {
        return usedTransactionQuota;
    }

    public void setUsedTransactionQuota(int usedTransactionQuota) {
        this.usedTransactionQuota = usedTransactionQuota;
    }
}
