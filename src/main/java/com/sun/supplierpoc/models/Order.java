package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.simphony.response.CondimentResponse;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    @Id
    private ObjectId _id = ObjectId.get();
    private List<SingleOrder> orderMeals;
    private boolean deleted = false;

    public Order() {
    }

    public Order(List<SingleOrder> orderMeals) {
        this.orderMeals = orderMeals;
    }

    public ObjectId getId() {
        return _id;
    }

    public List<SingleOrder> getOrderMeals() {
        return orderMeals;
    }

    public void setOrderMeals(List<SingleOrder> orderMeals) {
        this.orderMeals = orderMeals;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
