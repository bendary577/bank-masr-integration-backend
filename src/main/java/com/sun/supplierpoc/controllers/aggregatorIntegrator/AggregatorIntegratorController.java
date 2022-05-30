package com.sun.supplierpoc.controllers.aggregatorIntegrator;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.AggregatorOrder;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.aggregtor.*;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsBranch;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.onlineOrdering.AggregatorIntegratorService;
import com.sun.supplierpoc.services.restTemplate.FoodicsWebServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/aggregator")
public class AggregatorIntegratorController {
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private AggregatorIntegratorService aggregatorIntegratorService;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private FoodicsWebServices foodicsWebServices;

    @GetMapping("/products")
    public ResponseEntity<?> fetchProducts(Principal principal) {

        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if (user != null) {

            Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

            if (accountOptional.isPresent()) {

                Account account = accountOptional.get();

                response = aggregatorIntegratorService.fetchProducts(account);

                if (response.isStatus()) {
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            } else {
                response.setMessage(Constants.INVALID_ACCOUNT);
                response.setStatus(false);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } else {
            response.setMessage(Constants.INVALID_USER);
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> sendTalabatOrders(Principal principal) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response = aggregatorIntegratorService.sendTalabatOrdersToFoodics(account);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/storedOrders")
    public ResponseEntity<?> getstoredOrders(Principal principal,
                                             @RequestParam(name = "pageNumber") int pageNumber,
                                             @RequestParam(name = "limit") int limit) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response.setStatus(true);
            response.setMessage("");

            Pageable paging = PageRequest.of(pageNumber-1, limit);
            ArrayList<AggregatorOrder> aggregatorOrders = (ArrayList<AggregatorOrder>) orderRepo.findAllByAccountOrderByCreationDateDesc(account, paging);
            response.setData(aggregatorOrders);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping("/getOrdersCount")
    public int getOrdersCount(Principal principal) {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            int ordersCount = orderRepo.countAllByAccountId(account.getId());

            return ordersCount;
        }else{
            return 0;
        }
    }

    @GetMapping("/getMappedProducts")
    public ResponseEntity<?> getMappedProducts(Principal principal) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            ArrayList<ProductsMapping> unmappedProductsList = (ArrayList<ProductsMapping>) generalSettings.getAggregatorConfiguration().getProductsMappings()
                    .stream()
                    .filter(productsMapping -> !productsMapping.getFoodIcsProductId().equals("") || productsMapping.getModifiers().size() > 0 )
                    .collect(Collectors.toList());

            response.setStatus(true);
            response.setMessage("mapped foodics products returned successfully");
            response.setData(unmappedProductsList);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/getUnMappedProducts")
    public ResponseEntity<?> getUnMappedProducts(Principal principal) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            ArrayList<ProductsMapping> unmappedProductsList = (ArrayList<ProductsMapping>) generalSettings.getAggregatorConfiguration().getProductsMappings()
                    .stream()
                    .filter(productsMapping -> productsMapping.getFoodIcsProductId().equals("") && productsMapping.getModifiers().size() == 0 )
                    .collect(Collectors.toList());

            response.setStatus(true);
            response.setMessage("Unmapped foodics products returned successfully");
            response.setData(unmappedProductsList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/foodicsProducts")
    public ResponseEntity<?> foodicsProducts(Principal principal) {

        Response response = new Response();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            if(!generalSettings.getAggregatorConfiguration().isIntegrationStatus()){
                response.setStatus(false);
                response.setMessage("Please finish foodics integration process before starting getting foodics products");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }else{
                FoodicProductResponse foodicProductResponse = foodicsWebServices.fetchFoodicsProducts(generalSettings.getAggregatorConfiguration().getFoodicsAccountData());
                if(foodicProductResponse.isStatus()){
                    response.setStatus(true);
                    response.setMessage(foodicProductResponse.getMessage());
                    response.setData(foodicProductResponse);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }else{
                    response.setStatus(false);
                    response.setMessage(foodicProductResponse.getMessage());
                    response.setData(foodicProductResponse);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/foodicsProductsPaginated")
    public ResponseEntity<?> foodicsProductsPaginated(Principal principal, @RequestBody FoodicsGetPaginatedProductsRequest foodicsGetPaginatedProductsRequest) {

        Response response = new Response();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            if(!generalSettings.getAggregatorConfiguration().isIntegrationStatus()){
                response.setStatus(false);
                response.setMessage("Please finish foodics integration process before starting getting foodics products");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }else{
                FoodicProductResponse foodicProductResponse = foodicsWebServices.fetchFoodicsProductsPaginated(generalSettings.getAggregatorConfiguration().getFoodicsAccountData(), foodicsGetPaginatedProductsRequest.getRequestAPI());
                if(foodicProductResponse.isStatus()){
                    response.setStatus(true);
                    response.setMessage(foodicProductResponse.getMessage());
                    response.setData(foodicProductResponse);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }else{
                    response.setStatus(false);
                    response.setMessage(foodicProductResponse.getMessage());
                    response.setData(foodicProductResponse);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/getFoodicsAccessToken")
    public ResponseEntity<?> getFoodicsAccessToken(Principal principal,@RequestBody FoodicsAccessTokenRequest foodicsAccessTokenRequest) {
        Response response = new Response();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            FoodicsAccessToken foodicsAccessToken = foodicsWebServices.getFoodicsAccessToken(foodicsAccessTokenRequest);

            if(foodicsAccessToken.isStatus()){
                generalSettings.getAggregatorConfiguration().getFoodicsAccountData().setToken(foodicsAccessToken.getAccess_token());
                generalSettings.getAggregatorConfiguration().setIntegrationStatus(true);
                generalSettingsRepo.save(generalSettings);
                response.setStatus(true);
                response.setMessage("Foodics products returned successfully");
                response.setData(foodicsAccessToken);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else{
                response.setStatus(false);
                response.setMessage(foodicsAccessToken.getMessage());
                response.setData(foodicsAccessToken);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

    }

    @GetMapping("/getFoodicsBranches")
    public ResponseEntity<?> getFoodicsBranches(Principal principal) {

        Response response = new Response();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            if(!generalSettings.getAggregatorConfiguration().isIntegrationStatus()){
                response.setStatus(false);
                response.setMessage("Please finish foodics integration process before starting getting foodics products");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }else{
                List<FoodicsBranch> foodicsBranchList = foodicsWebServices.fetchFoodicsBranches(generalSettings.getAggregatorConfiguration().getFoodicsAccountData());
//                boolean newBranchFetched = false;
//                for (FoodicsBranch foodicsBranch : foodicsBranchList) {
//                    List<BranchMapping> resultFoodicsBranches = generalSettings.getAggregatorConfiguration().getBranchMappings().stream()
//                            .filter(branch -> branch.getName().equals(foodicsBranch.getName()))
//                            .collect(Collectors.toList());
//                    if(resultFoodicsBranches.isEmpty()){
//                        newBranchFetched = true;
//                        BranchMapping branchMapping = new BranchMapping();
//                        branchMapping.setName(foodicsBranch.getName());
//                        branchMapping.setFoodIcsBranchId(foodicsBranch.getId());
//                        branchMapping.setTalabatBranchId("");
//                        branchMapping.setPassword("");
//                        branchMapping.setUsername("");
//                        generalSettings.getAggregatorConfiguration().getBranchMappings().add(branchMapping);
//                    }
//                }
//                if(newBranchFetched){
//                    generalSettingsRepo.save(generalSettings);
//                }
                response.setStatus(true);
                response.setMessage("Branches mapping Returned successfully");
                response.setData(foodicsBranchList);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

    }
}
