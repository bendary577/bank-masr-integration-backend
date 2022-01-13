package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.application.AppUserController;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import com.sun.supplierpoc.models.requests.CreateVoucherRequest;
import com.sun.supplierpoc.models.requests.UpdateVoucherRequest;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.simphony.VoucherRepository;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private Conversions conversions;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private AppUserController appUserController;


    public Response getAllVoucher(Account account) {

        Response response = new Response();

        try {
            ArrayList<Voucher> vouchers =
                    voucherRepository.findAllByAccountIdAndDeleted(account.getId(), false);

            response.setStatus(true);
            response.setData(vouchers);

        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Can't get voucher list due to the error: " + e.getMessage());
        }
        return response;
    }

    public Response addVoucher(Account account, CreateVoucherRequest voucherRequest) {

        Response response = new Response();

        if (voucherRepository.existsByAccountIdAndNameAndDeleted(account.getId(), voucherRequest.getName(), false)) {
            response.setStatus(false);
            response.setMessage("This Voucher Is Exist With This Name.");
        } else {

            try {

                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                ArrayList<SimphonyDiscount> simphonyDiscountList = generalSettings.getDiscountRates();

                SimphonyDiscount simphonyDiscount = conversions.checkSimphonyDiscountExistence(simphonyDiscountList,
                        voucherRequest.getSimphonyDiscountId());

                String code = createCode(voucherRequest.getName());

                Voucher voucher = new Voucher();
                voucher.setName(voucherRequest.getName());
                voucher.setStartDate(voucherRequest.getStartDate());
                voucher.setEndDate(voucherRequest.getEndDate());
                voucher.setSimphonyDiscount(simphonyDiscount);
                voucher.setVoucherCode(code);
                voucher.setRedeemQuota(voucherRequest.getRedeemQuota());
                voucher.setCreationDate(new Date());
                voucher.setAccountId(account.getId());
                voucher.setDeleted(false);

                voucherRepository.save(voucher);

                response.setMessage("Voucher saved successfully.");
                response.setStatus(true);
                response.setData(voucher);
            } catch (Exception e) {
                response.setStatus(false);
                response.setMessage(e.getMessage());
            }
        }
        return response;
    }

    public Response updateVoucher(Account account, UpdateVoucherRequest voucherRequest) {

        Response response = new Response();

        try {

            Optional<Voucher> voucherOptional = voucherRepository.findById(voucherRequest.getId());

            if (voucherOptional.isPresent()) {

                Voucher voucher = voucherOptional.get();

                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                ArrayList<SimphonyDiscount> simphonyDiscountList = generalSettings.getDiscountRates();

                if (!voucherRequest.getName().equals(voucher.getName()) && voucherRepository.existsByAccountIdAndNameAndDeleted(account.getId(), voucherRequest.getName(), false)) {
                    response.setStatus(false);
                    response.setMessage("This Voucher Is Exist With This Name.");
                } else {

                    SimphonyDiscount simphonyDiscount = conversions.checkSimphonyDiscountExistence(simphonyDiscountList,
                            voucherRequest.getSimphonyDiscount().getDiscountId());

                    String code = createCode(voucherRequest.getName());


                    voucher.setName(voucherRequest.getName());
                    voucher.setStartDate(voucherRequest.getStartDate());
                    voucher.setEndDate(voucherRequest.getEndDate());
                    voucher.setSimphonyDiscount(simphonyDiscount);
                    voucher.setVoucherCode(code);
                    voucher.setRedeemQuota(voucherRequest.getRedeemQuota());
                    voucher.setCreationDate(new Date());
                    voucher.setAccountId(account.getId());
                    voucher.setLastUpdate(new Date());
                    voucher.setDeleted(false);

                    voucherRepository.save(voucher);

                    response.setMessage("Voucher updated successfully.");
                    response.setStatus(true);

                }
            } else {
                response.setStatus(false);
                response.setMessage("This Voucher Is Not Exist.");
            }
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Can't Update Voucher Deu To Error: "  + e.getMessage());
        }

        return response;
    }


    public String createCode(String name) {
        String code;
        try {
            Random random = new Random();
            code = name.replaceAll(" ", "");
            if (code.length() > 17) {
                code = code.substring(0, 17);
            } else {
                code = code + code.substring(0, 3);
            }
            code = (code + random.nextInt(1000)).replace(".", "");
            code = Base64UrlCodec.BASE64URL.encode(code);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return code;
    }

}
