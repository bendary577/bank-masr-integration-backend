package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.MenuItemMap;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository

public interface MenuItemsMapRepo extends MongoRepository<MenuItemMap, String>{
    Optional<MenuItemMap> findByIdAndDeleted(String menuItemId, boolean deleted);
    boolean existsMenuItemByFirstNameAndDeleted(String menuItemName, boolean deleted);
    List<MenuItemMap> findAll();
}
