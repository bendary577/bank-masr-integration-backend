package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.MenuItemMap;
import com.sun.supplierpoc.models.Order;
import com.sun.supplierpoc.repositories.MenuItemsMapRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;


@RestController
public class OrderController {
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping(value = "/opera/addOrder")
    @ResponseBody
    public HashMap<String, Object> addOrder(@RequestBody Order order) {
        HashMap<String, Object> response = new HashMap<>();


        // create new account and user
        order = mongoTemplate.save(order);

        response.put("message", "Order added successfully.");
        response.put("success", true);
        response.put("order", order);

        return response;
    }

    @RequestMapping(value = "/opera/getOrders")
    @ResponseBody
    public List<Order> getOrders(@RequestParam("offset") int offset,
                                 @RequestParam("size") int size,
                                 @RequestParam("columnIndex") int columnIndex,
                                 @RequestParam("isAscending") boolean isAscending
        ){

        List<Order> allOrders = orderRepo.findAll();
        int totalLength = allOrders.size();
        //int endIndex = offset+size > totalLength ? totalLength : offset+size;
        List <Order> orders = allOrders.subList(offset,totalLength);
//        if(columnIndex==3){
//            orders.sort()
//        }
        return orders;
    }
}
