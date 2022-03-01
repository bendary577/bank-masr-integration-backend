package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.simphony.response.CondimentResponse;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.TalabatRest.TalabatAdminOrder;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {

    @Id
    private ObjectId _id = ObjectId.get();
    private List<SingleOrder> orderMeals;
    private TalabatAdminOrder talabatAdminOrder;
    private FoodicsOrder foodicsOrder;
    private RestOrder restOrder;

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

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public TalabatAdminOrder getTalabatAdminOrder() {
        return talabatAdminOrder;
    }

    public void setTalabatAdminOrder(TalabatAdminOrder talabatAdminOrder) {
        this.talabatAdminOrder = talabatAdminOrder;
    }

    public FoodicsOrder getFoodicsOrder() {
        return foodicsOrder;
    }

    public void setFoodicsOrder(FoodicsOrder foodicsOrder) {
        this.foodicsOrder = foodicsOrder;
    }

    public RestOrder getRestOrder() {
        return restOrder;
    }

    public void setRestOrder(RestOrder restOrder) {
        this.restOrder = restOrder;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
