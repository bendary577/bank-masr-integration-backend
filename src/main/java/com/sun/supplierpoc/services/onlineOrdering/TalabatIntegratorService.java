package com.sun.supplierpoc.services.onlineOrdering;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.TalabatAdminAccount;
import com.sun.supplierpoc.models.configurations.TalabatConfiguration;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccount;
import com.sun.supplierpoc.models.talabat.*;
import com.sun.supplierpoc.models.talabat.TalabatRest.*;
import com.sun.supplierpoc.models.talabat.foodics.*;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.repositories.applications.FoodicsOrderRepo;
import com.sun.supplierpoc.repositories.applications.FoodicsProductRepo;
import com.sun.supplierpoc.services.restTemplate.TalabatAdminWebService;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TalabatIntegratorService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private FoodicsProductRepo foodicsProductRepo;

    @Autowired
    private FoodicsOrderRepo foodicsOrderRepo;

    @Autowired
    private TalabatAdminWebService talabatAdminWebService;

    public Response syncFoodicsOrders(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatOrder talabatOrder = talabatRestService.getOrders(token);

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
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatOrder talabatOrder = talabatRestService.getOrders(token, branch);

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

    public Response testTalabatRest(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        TalabatAdminAccount talabatAdminAccount = talabatConfiguration.getTalabatAdminAccounts().get(0);

        com.sun.supplierpoc.models.talabat.branchAdmin.Token token = talabatAdminWebService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatAdminOrder talabatOrder = talabatAdminWebService.acceptService(token);
            //            if (talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null) {
//
//                List<RestOrder> receivedOrders = talabatOrder.getOrders().stream()
//                        .filter(restOrder -> restOrder.getOrder_status().equals("ACCEPTED"))
//                        .collect(Collectors.toList());
//
////                List<RestOrder> receivedOrders = List.of(talabatOrder.getOrders().get(0));
//                try {
//                    List<TalabatOrder> talabatOrderList = new ArrayList<>();
//                    TalabatOrder talabatOrderDetails = new TalabatOrder();
//
//                    FoodicsLoginBody foodicsLoginBody = new FoodicsLoginBody();
//                    //talabatRestService.LoginToFoodics();
//
//                    for (RestOrder restOrder : receivedOrders) {
//                        talabatOrderDetails = talabatRestService.getOrderById(restOrder, token);
//
//                        FoodicsOrder foodicsOrder = parseOrderParametersToFoodics(talabatOrderDetails, generalSettings);
//
//                        if (foodicsOrder != null) {
//                            talabatOrderList.add(talabatOrderDetails);
//                        } else {
//
//                        }
//                        foodicsOrder = talabatRestService.sendOrderToFoodics(foodicsOrder, foodicsLoginBody, generalSettings, foodicsAccount);
//                        talabatOrderDetails.setOrders(List.of(restOrder));
//
//                        if (foodicsOrder.isCallStatus()) {
//                            talabatOrderList.add(talabatOrderDetails);
//                            foodicsOrderRepo.save(foodicsOrder);
//                        }
//
//                    }
//
//                    if (talabatOrderList.size() > 0) {
//                        orderRepo.saveAll(talabatOrderList);
//                        response.setMessage("Send Talabat Orders Successfully");
//                        response.setData(talabatOrderList.get(0));
//                    } else {
//                        response.setMessage("Send Talabat Orders Successfully");
//                        response.setData(new TalabatOrder());
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                response.setStatus(false);
//                response.setMessage("Login To Talabat Failed Due To : " + talabatOrder.getMessage());
//            }
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    public Response getOrderDetails(Account account, RestOrder order) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatOrder talabatOrderDetails = talabatRestService.getOrderById(order, token);
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
