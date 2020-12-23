package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ArrayOfDbMenuItemPrice")
public class ArrayOfDbMenuItemPrice {
    @XmlElement(name="DbMenuItemPrice")
    private List<DbMenuItemPrice> DbMenuItemPrice;

    public List<DbMenuItemPrice> getDbMenuItemPrice() {
        return DbMenuItemPrice;
    }

    public void setDbMenuItemPrice(List<DbMenuItemPrice> dbMenuItemPrice) {
        DbMenuItemPrice = dbMenuItemPrice;
    }
}