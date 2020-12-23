package com.sun.supplierpoc.models.simphony;

import org.w3c.dom.stylesheets.LinkStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
@XmlRootElement(name = "ArrayOfDbMenuItemDefinition")
public class ArrayOfDbMenuItemDefinition {
    @XmlElement(name="DbMenuItemDefinition")
    private ArrayList<DbMenuItemDefinition> DbMenuItemDefinition;

    public ArrayList<com.sun.supplierpoc.models.simphony.DbMenuItemDefinition> getDbMenuItemDefinition() {
        return DbMenuItemDefinition;
    }

    public void setDbMenuItemDefinition(ArrayList<com.sun.supplierpoc.models.simphony.DbMenuItemDefinition> dbMenuItemDefinition) {
        DbMenuItemDefinition = dbMenuItemDefinition;
    }
}
