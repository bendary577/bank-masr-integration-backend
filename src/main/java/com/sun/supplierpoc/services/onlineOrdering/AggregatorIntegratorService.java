package com.sun.supplierpoc.services.onlineOrdering;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Order;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.AggregatorConfiguration;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccount;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.talabat.foodics.Product;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.repositories.applications.FoodicsProductRepo;
import com.sun.supplierpoc.services.restTemplate.FoodicsWebServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Service
public class AggregatorIntegratorService {

    @Autowired
    private FoodicsWebServices foodicsWebServices;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private FoodicsProductRepo foodicsProductRepo;

    @Autowired
    private OrderRepo orderRepo;

    public Response fetchProducts(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = aggregatorConfiguration.getFoodicsAccount();

        response = foodicsWebServices.fetchProducts(generalSettings, foodicsAccount);

        ArrayList<Product> foodicsProducts = response.getFoodicsProducts();

        try {
            foodicsProductRepo.saveAll(foodicsProducts);
        } catch (Exception e) {
            response.setMessage("Can't save foodics product.");
            response.setStatus(false);
        }

        return response;
    }

    public LinkedHashMap updateFoodicsProduct(Account account, Product foodicsProduct) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        try {
            Product product = foodicsProductRepo.findById(foodicsProduct.getId()).orElse(null);

            if (product != null) {

                foodicsProductRepo.save(foodicsProduct);

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

    public LinkedHashMap updateFoodicsOrder(Account account, FoodicsOrder tempFoodicsOrder) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        try {

            Order order = orderRepo.findByFoodicsOrderId(tempFoodicsOrder.getId()).orElse(null);

            FoodicsOrder foodicsOrder = order.getFoodicsOrder();

            String delivery_status = "";
            String status = "";

            if (foodicsOrder != null) {


                switch (tempFoodicsOrder.getDelivery_status()) {
                    case 1:
                        delivery_status = "sent to kitchen";
                        break;
                    case 2:
                        delivery_status = "ready";
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

}
