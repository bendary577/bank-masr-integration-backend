package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
public class MenuItemMapController {
    @Autowired
    private MenuItemsMapRepo menuItemsMapRepo;

    @Autowired
    MongoTemplate mongoTemplate;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping(value = "/addMenuItem")
    @ResponseBody
    public HashMap<String, Object> addMenuItem(@RequestBody MenuItemMap menuItem) {
        HashMap<String, Object> response = new HashMap<>();

        // check existence of account name
        if (menuItemsMapRepo.existsMenuItemByFirstNameAndDeleted(menuItem.getFirstName(), false)){
            response.put("message", "Menu item name already exits.");
            response.put("success", false);
            return response;
        }

        // create new account and user
        menuItem = mongoTemplate.save(menuItem);

        response.put("message", "Account added successfully.");
        response.put("success", true);
        response.put("menuItem", menuItem);

        return response;
    }



}
