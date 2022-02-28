package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.application.AppUserController;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import com.sun.supplierpoc.models.requests.VoucherRequest;
import com.sun.supplierpoc.models.simphony.redeemVoucher.UniqueVoucher;
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



    public Response getVoucherById(Account account, String voucherId) {
        Response response = new Response();

        try {
            Voucher vouchers = voucherRepository.getByIdAndAccountId(voucherId, account.getId());

            response.setStatus(true);
            response.setData(vouchers);

        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Can't get voucher list due to the error: " + e.getMessage());
        }
        return response;
    }

    public Response getAllVoucher(Account account) {

        Response response = new Response();

        try {
            ArrayList<Voucher> vouchers =
                    voucherRepository.findAllByAccountId(account.getId());

            response.setStatus(true);
            response.setData(vouchers);

        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage("Can't get voucher list due to the error: " + e.getMessage());
        }
        return response;
    }

    public Response addVoucher(Account account, VoucherRequest voucherRequest) {

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

                List<UniqueVoucher> uniqueVouchers = createUniqueVoucher(voucherRequest.getName(),
                        voucherRequest.getUniqueVouchers(), 1);

                Voucher voucher = new Voucher();
                voucher.setName(voucherRequest.getName());
                voucher.setStartDate(voucherRequest.getStartDate());
                voucher.setEndDate(voucherRequest.getEndDate());
                voucher.setRedemption(voucherRequest.getRedemption());
                voucher.setSimphonyDiscount(simphonyDiscount);
                voucher.setUniqueVouchers(uniqueVouchers);
                voucher.setCreationDate(new Date());
                voucher.setAccountId(account.getId());
                voucher.setDeleted(false);

                voucherRepository.save(voucher);

                response.setMessage("Voucher added successfully.");
                response.setStatus(true);
                response.setData(voucher);
            } catch (Exception e) {
                response.setStatus(false);
                response.setMessage(e.getMessage());
            }
        }
        return response;
    }

    public Response updateVoucher(Account account, VoucherRequest voucherRequest) {

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
                } else if(voucher.getUniqueVouchers().size() > voucherRequest.getUniqueVouchers()) {
                    response.setStatus(false);
                    response.setMessage("Invalid unique vouchers number.");
                } else if(voucher.getRedemption() > voucherRequest.getRedemption()) {
                    response.setStatus(false);
                    response.setMessage("Invalid number of redemption.");
                }else{

                    SimphonyDiscount simphonyDiscount = conversions.checkSimphonyDiscountExistence(simphonyDiscountList,
                            voucherRequest.getSimphonyDiscountId());

                    if(voucher.getUniqueVouchers().size() < voucherRequest.getUniqueVouchers()){
                        List<UniqueVoucher> uniqueVouchers = createUniqueVoucher(voucherRequest.getName(),
                                voucherRequest.getUniqueVouchers(), voucher.getUniqueVouchers().size() + 1);
                        voucher.getUniqueVouchers().addAll(uniqueVouchers);
                    }
                    if(voucher.getRedemption() < voucherRequest.getRedemption()) {
                        voucher.getUniqueVouchers().stream()
                                .forEach(uniqueVoucher1 -> uniqueVoucher1.setStatus(Constants.VALID_VOUCHER));
                    }
                    voucher.setRedemption(voucherRequest.getRedemption());

                    voucher.setName(voucherRequest.getName());
                    voucher.setStartDate(voucherRequest.getStartDate());
                    voucher.setEndDate(voucherRequest.getEndDate());
                    voucher.setSimphonyDiscount(simphonyDiscount);
                    voucher.setLastUpdate(new Date());
                    voucher.setDeleted(voucherRequest.isDeleted());

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

    private List<UniqueVoucher> createUniqueVoucher(String name, int uniqueVouchers, int counter) {

        List<UniqueVoucher> uniqueVouchersList = new ArrayList<>();
        UniqueVoucher uniqueVoucher = new UniqueVoucher();
        for(int i = counter; i <= uniqueVouchers; i++){

            uniqueVoucher = new UniqueVoucher();

            uniqueVoucher.setId(String.valueOf(i));
            uniqueVoucher.setCode(createCode(name));
            uniqueVoucher.setNumOfRedemption(0);
            uniqueVoucher.setStatus(Constants.VALID_VOUCHER);

            uniqueVouchersList.add(uniqueVoucher);
        }

        return uniqueVouchersList;
    }
    public Response markVoucherDeleted(Account account, List<Voucher> voucherRequests) {

        Response response = new Response();

        try{
                for(Voucher tempVoucher :voucherRequests){
                    Voucher voucher = voucherRepository.findById(tempVoucher.getId()).get();
                    voucher.setDeleted(!voucher.isDeleted());
                    voucherRepository.save(voucher);
                }

                response.setStatus(true);
                response.setMessage("Success.");
                return response;

        }catch(Exception e){
            response.setStatus(false);
            response.setMessage("Cant complete operation due to the error: " + e.getMessage());
            return response;
        }
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
