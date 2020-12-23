package com.sun.supplierpoc.models.simphony.response;



import com.sun.supplierpoc.models.simphony.DbMenuItemDefinition;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ArrayOfDbMenuItemDefinition")
public class ArrayOfDbMenuItemDefinition {
    @XmlElement(name="DbMenuItemDefinition")
    private List<DbMenuItemDefinition> DbMenuItemDefinition;

    public List<DbMenuItemDefinition> getDbMenuItemDefinition() {
        return DbMenuItemDefinition;
    }

    public void setDbMenuItemDefinition(List<DbMenuItemDefinition> dbMenuItemDefinition) {
        DbMenuItemDefinition = dbMenuItemDefinition;
    }
}
