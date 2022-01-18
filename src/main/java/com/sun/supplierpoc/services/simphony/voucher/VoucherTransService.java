package com.sun.supplierpoc.services.simphony.voucher;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.*;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import com.sun.supplierpoc.models.voucher.VoucherTransaction;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.repositories.simphony.VoucherRepository;
import com.sun.supplierpoc.repositories.simphony.VoucherTransRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VoucherTransService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private Conversions conversions;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;


    public HashMap createTransaction(TransactionType transactionType, Transactions voucherTransaction, Account account, GeneralSettings generalSettings, RevenueCenter revenueCenter) {
        HashMap response = new HashMap();
        Voucher voucher = voucherRepository.findByVoucherCode(voucherTransaction.getCode());
        try {

            Date today = new Date();

            if (voucher == null) {
                response.put("message", "No voucher for this code.");
            } else if (!voucher.getAccountId().equals(account.getId())) {
                response.put("message", "This voucher does not belong to this account loyalty program.");
            } else if (voucher.isDeleted()) {
                response.put("message", "This voucher is deleted.");
            } else if (voucher.getStartDate().getTime() > today.getTime()) {
                response.put("message", "This voucher doesn't start yet.");
            } else if (voucher.getEndDate().getTime() < today.getTime()) {
                response.put("message", "This voucher has been ended.");
            } else if (voucher.getNumberOfRedemption() >= voucher.getRedeemQuota()) {
                response.put("message", "This voucher has been exceeded the redemption quota.");
            } else if (transactionRepo.existsByCheckNumberAndRevenueCentreIdAndStatus(
                    voucherTransaction.getCheckNumber(), voucherTransaction.getRevenueCentreId(), Constants.SUCCESS)) {
                response.put("message", "Can't use an voucher for the same check twice.");
            } else {

                double amount = voucherTransaction.getTotalAmount();


                // get discount rate using discount id
                ArrayList<SimphonyDiscount> discounts = generalSettings.getDiscountRates();
                SimphonyDiscount simphonyDiscount = conversions.checkSimphonyDiscountExistence(discounts, voucher.getSimphonyDiscount().getDiscountId());
                if (simphonyDiscount.getDiscountId() == 0) {
                    response.put("message", "Invalid discount found for this voucher.");
                    response.put("isSuccess", false);
                    return response;
                }
                double discount = simphonyDiscount.getDiscountRate();
                double amountAfterDiscount = amount - (amount * (discount / 100));
                voucherTransaction.setDiscountRate(discount);
                voucherTransaction.setAfterDiscount(amountAfterDiscount);
                voucherTransaction.setAccountId(account.getId());
                voucherTransaction.setTransactionTypeId(transactionType.getId());
                voucherTransaction.setVoucherId(voucher.getId());
                voucherTransaction.setTransactionDate(new Date());
                voucherTransaction.setStatus(Constants.SUCCESS);

                transactionRepo.save(voucherTransaction);
                generalSettings.getSimphonyQuota().setUsedTransactionQuota(generalSettings.getSimphonyQuota().getUsedTransactionQuota() + 1);
                generalSettingsRepo.save(generalSettings);

                response.put("isSuccess", true);
                response.put("message", "Discount added successfully.");
                response.put("discountId", voucher.getSimphonyDiscount().getDiscountId());
                return response;
            }
            response.put("isSuccess", false);
        } catch (Exception e) {
            response.put("isSuccess", false);
            response.put("message", "This is an error occurs, Please contact support team.");
        }
        voucherTransaction.setAccountId(account.getId());
        voucherTransaction.setTransactionTypeId(transactionType.getId());
        if (voucher != null) {
            voucherTransaction.setVoucherId(voucher.getId());
        }
        voucherTransaction.setTransactionDate(new Date());
        voucherTransaction.setStatus(Constants.FAILED);
        voucherTransaction.setReason(response.get("message").toString());
        transactionRepo.save(voucherTransaction);
        return response;
    }

    public HashMap getVoucherTransStatistics(String voucherId, Account account) {

        HashMap statistic = new HashMap();
        try {
            double totalSpend = 0;
            int totalTransactions = 0;
            int failedTransactionCount = 0;
            int succeedTransactionCount = 0;

            totalTransactions= transactionRepo.countAllByVoucherIdAndAccountId(voucherId, account.getId());
//            totalSpend = transactionRepo.countToTalAmountByVoucherIdAndAccountIdAndStatus(voucherId, account.getId(), Constants.SUCCESS);
            succeedTransactionCount = transactionRepo.countAllByVoucherIdAndAccountIdAndStatus(voucherId, account.getId(), Constants.SUCCESS);
            failedTransactionCount = transactionRepo.countAllByVoucherIdAndAccountIdAndStatus(voucherId, account.getId(), Constants.FAILED);

            statistic.put("totalSpend", totalSpend);
            statistic.put("totalTransactions", totalTransactions);
            statistic.put("succeedTransactionCount", succeedTransactionCount);
            statistic.put("failedTransactionCount", failedTransactionCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statistic;
    }

    public HashMap getTotalVoucherTransactions(int page, int size, String voucherId, Account account) {


        HashMap statistic = new HashMap();
        try {
            List<Transactions> transactions;

            Pageable paging = PageRequest.of(page - 1, size);

            transactions = transactionRepo.findByVoucherIdAndAccountId(voucherId, account.getId(), paging);

            statistic.put("transactions", transactions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statistic;
    }

}
