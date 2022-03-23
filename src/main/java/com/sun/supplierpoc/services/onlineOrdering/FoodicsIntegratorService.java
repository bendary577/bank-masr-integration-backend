package com.sun.supplierpoc.services.onlineOrdering;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.aggregtor.BranchMapping;
import com.sun.supplierpoc.models.configurations.AggregatorConfiguration;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.*;
import com.sun.supplierpoc.models.aggregtor.login.Token;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.services.restTemplate.TalabatAdminWebService;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class FoodicsIntegratorService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private TalabatAdminWebService talabatAdminWebService;

    public Response syncFoodicsOrders(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {
            ArrayList<String> branches = new ArrayList<>();
            for (BranchMapping branch : aggregatorConfiguration.getBranchMappings()) {
                branches.add(branch.getTalabatBranchId());
            }

            TalabatAggregatorOrder talabatOrder = talabatRestService.getOrders(token, branches);

            if (talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null) {

                response.setMessage("Sync Talabat Orders Successfully");
                response.setData(talabatOrder);

            } else {
                response.setStatus(false);
                response.setMessage("Login To Talabat Failed Due To : " + talabatOrder.getMessage());
            }
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    public Response syncFoodicsBranchOrders(Account account, String branch) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatAggregatorOrder talabatOrder = talabatRestService.getOrders(token, branch);

            if (talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null) {

                response.setMessage("Sync Talabat Orders Successfully");
                response.setData(talabatOrder);

            } else {
                response.setStatus(false);
                response.setMessage("Login To Talabat Failed Due To : " + talabatOrder.getMessage());
            }
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    public Response getOrderDetails(Account account, RestOrder order) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatAggregatorOrder talabatOrderDetails = talabatRestService.getOrderById(order, token);
            response.setData(talabatOrderDetails);
            response.setStatus(true);
            response.setMessage("Success");
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

}
