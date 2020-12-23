package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
@XmlAccessorType(XmlAccessType.FIELD)

public class PosApi_MenuItem {
    @XmlElement(name = "Condiments")

    private Condiments condiments ;
    @XmlElement(name = "MenuItem")
    private MenuItem menuItem ;
    public Condiments getCondiments() {
        return condiments;
    }

    public void setCondiments(Condiments condiments) {
        this.condiments = condiments;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }
}
