package com.sun.supplierpoc.models.simphony;


import com.sun.supplierpoc.models.simphony.masters.DbMenuItemMaster;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "ArrayOfDbMenuItemMaster")
public class ArrayOfDbMenuItemMaster {

    @XmlElement(name = "DbMenuItemMaster")
    public final List<DbMenuItemMaster> DbMenuItemMaster = new ArrayList<>();

    public List<DbMenuItemMaster> getDbMenuItemMaster() {
        return DbMenuItemMaster;
    }

}
