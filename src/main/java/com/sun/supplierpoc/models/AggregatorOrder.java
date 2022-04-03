package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.aggregtor.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminOrder;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AggregatorOrder implements Serializable {

    @Id
    private ObjectId _id = ObjectId.get();
    private List<SingleOrder> orderMeals;
    private TalabatAdminOrder talabatAdminOrder;
    private FoodicsOrder foodicsOrder;
    private RestOrder restOrder;

    private String aggregatorName;

    @DBRef
    private Account account;
    private Date creationDate;
    private String orderStatus;
    private String reason;
    private boolean deleted = false;

    public AggregatorOrder() {
    }

    public AggregatorOrder(List<SingleOrder> orderMeals) {
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

    public String getAggregatorName() {
        return aggregatorName;
    }

    public void setAggregatorName(String aggregatorName) {
        this.aggregatorName = aggregatorName;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
