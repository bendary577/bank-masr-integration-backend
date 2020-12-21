package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;
@XmlAccessorType(XmlAccessType.FIELD)
public class ppComboMeals {

    @XmlElementWrapper(name="SimphonyPosApi_ComboMeal",nillable=true,required = true)

    private List<SimphonyPosApi_ComboMeal> SimphonyPosApi_ComboMeal;

    public List<SimphonyPosApi_ComboMeal> getSimphonyPosApi_ComboMeal() {
        return SimphonyPosApi_ComboMeal;
    }

    public void setSimphonyPosApi_ComboMeal(List<SimphonyPosApi_ComboMeal> simphonyPosApi_ComboMeal) {
        SimphonyPosApi_ComboMeal = simphonyPosApi_ComboMeal;
    }
}
