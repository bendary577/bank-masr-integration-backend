package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.*;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.Category;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import com.sun.supplierpoc.models.configurations.AggregatorConfiguration;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.models.aggregtor.*;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.*;
import com.sun.supplierpoc.models.aggregtor.foodics.*;
import com.sun.supplierpoc.models.aggregtor.login.Token;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.repositories.applications.ProductRepository;
import com.sun.supplierpoc.services.restTemplate.FoodicsWebServices;
import com.sun.supplierpoc.services.restTemplate.TalabatAdminWebService;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.bson.types.ObjectId;
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
    private TalabatAdminWebService talabatAdminWebService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FoodicsWebServices foodicsWebServices;


    public Response syncFoodicsOrders(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {
            ArrayList<String> branches = new ArrayList<>();
            for (BranchMapping branch : talabatConfiguration.getBranchMappings()) {
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
        AggregatorConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = talabatConfiguration.getFoodicsAccount();

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

    public Response getOrderDtails(Account account, RestOrder order) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = talabatConfiguration.getFoodicsAccount();

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

    public Response fetchProducts(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = talabatConfiguration.getFoodicsAccount();

        Product product = foodicsWebServices.fetchProducts(generalSettings, foodicsAccountData);

        try {
            productRepository.save(product);
            response.setData(product);
        } catch (Exception e) {
            response.setMessage("Can't save foodics product.");
            response.setStatus(false);
        }

        return response;
    }

    /////////////////////////////////// Talabat Order      ///////////////////////////////////
    public LinkedHashMap updateTalabatOrder(Account account, FoodicsOrder tempFoodicsOrder) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        try {

            AggregatorOrder order = orderRepo.findByFoodicsOrderId(tempFoodicsOrder.getId()).orElse(null);
//            AggregatorOrder order = orderRepo.findByIdAndDeleted("624eeb646a3b892304e51c6d", false).orElse(null);
            FoodicsOrder foodicsOrder = order.getFoodicsOrder();

            // Get account configuration
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
            ArrayList<BranchMapping> branches = aggregatorConfiguration.getBranchMappings();

            // ==> Get order's branch
            String branchId = order.getTalabatAdminOrder().globalEntityId + ";" + order.getTalabatAdminOrder().externalRestaurantId;
            BranchMapping branchMapping = branches.stream().
                    filter(branch -> branch.getTalabatBranchId().equals(branchId))
                    .collect(Collectors.toList()).stream().findFirst().orElse(null);

            String delivery_status = "";
            String status = "";

            if (foodicsOrder != null) {
                switch (tempFoodicsOrder.getDelivery_status()) {
                    case 1:
                        delivery_status = "sent to kitchen";
                        break;
                    case 2:
                        delivery_status = "ready";

                        // ==> Change status in talabat
                        talabatAdminWebService.readyForDeliveryOrder(account, order.getTalabatAdminOrder().id, branchMapping);
                        break;
                    case 3:
                        delivery_status = "assigned";
                        break;
                    case 4:
                        delivery_status = "en route";
                        break;
                    case 5:
                        delivery_status = "delivered";
                        break;
                    case 6:
                        delivery_status = "closed";
                        break;
                }

                switch (tempFoodicsOrder.getStatus()) {
                    case 1:
                        status = "Pending";
                        break;
                    case 2:
                        status = "Active";
                        break;
                    case 3:
                        status = "Declined";
                        break;
                    case 4:
                        status = "Closed";
                        break;
                    case 5:
                        status = "Returned";
                        break;
                    case 6:
                        status = "Void";
                        break;
                }

                foodicsOrder.setDelivery_status(tempFoodicsOrder.getDelivery_status());
                foodicsOrder.setStatus(tempFoodicsOrder.getStatus());

                order.setFoodicsOrder(foodicsOrder);

                orderRepo.save(order);

                response.put("message", "Order information was successfully updated.");
                response.put("status", "success");
                response.put("orderStatus", status);
                response.put("deliveryStatus", delivery_status);
            } else {
                response.put("message", "Order Not Found.");
                response.put("status", false);
            }

        } catch (Exception e) {
            response.put("message", "Can't save foodics order.");
            response.put("status", false);
        }
        return response;
    }

    /////////////////////////////////// Talabat Menu Items ///////////////////////////////////

    /*
     * This service fetch all talabat menu items ans save it in OSII middleware database
     * */
    public Response fetchTalabatProducts(Account account) {

        Response response = new Response();

        // Get account branches
        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        ArrayList<BranchMapping> branchMappings = generalSettings.getTalabatConfiguration().getBranchMappings();

        ArrayList<ProductsMapping> productsMappings = generalSettings.getTalabatConfiguration().getProductsMappings();

        BranchMapping branchMapping = branchMappings.get(2);

        TalabatMenu menu = talabatAdminWebService.getTalabatBranchMenuItems(account, branchMapping);

        ArrayList<com.sun.supplierpoc.models.aggregtor.branchAdmin.Category> categories = menu.menus.get(0).categories;

        ProductsMapping product;
        for (Category category : categories) {
            for (TalabatMenuItem item : category.items) {
                product = new ProductsMapping();

                product.setTalabatProductId(item.id);
                product.setName(item.name);
                product.setType("Product");
                productsMappings.add(product);
            }
        }

        generalSettings.getAggregatorConfiguration().setProductsMappings(productsMappings);
        try {
            generalSettingsRepo.save(generalSettings);
            response.setData(menu);
        } catch (Exception e) {
            response.setMessage("Can't save talabat product.");
            response.setStatus(false);
        }

        return response;
    }

    /*
     * This service update talabat product in case of any change occurs in foodics
     * */
    public LinkedHashMap updateTalabatProduct(Account account, FoodicsProduct foodicsProduct) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        Product product = new Product();

        try {
//            FoodicsProduct product = foodicsProductRepo.findById(foodicsProduct.getId()).orElse(null);

            if (product != null) {

//                foodicsProductRepo.save(foodicsProduct);

                response.put("message", "Product information was successfully updated.");
                response.put("status", "success");
                response.put("data", foodicsProduct);

            } else {

                response.put("message", "Product Not Found.");
                response.put("status", "failed");

            }
        } catch (Exception e) {
            response.put("message", "Can't save foodics product.");
            response.put("status", "failed");
        }

        return response;
    }

}
