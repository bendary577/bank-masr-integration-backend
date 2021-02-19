package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ArrayOfDbMenuItemClass")
public class ArrayOfDbMenuItemClass {

    @XmlElement(name = "DbMenuItemClass")
    private List<DbMenuItemClass> DbMenuItemClass ;

    public List<com.sun.supplierpoc.models.simphony.DbMenuItemClass> getDbMenuItemClass() {
        return DbMenuItemClass;
    }

    public void setDbMenuItemClass(List<com.sun.supplierpoc.models.simphony.DbMenuItemClass> dbMenuItemClass) {
        DbMenuItemClass = dbMenuItemClass;
    }
}
