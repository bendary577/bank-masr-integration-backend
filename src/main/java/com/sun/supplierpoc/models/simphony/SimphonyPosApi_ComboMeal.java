package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;
@XmlAccessorType(XmlAccessType.FIELD)

public class SimphonyPosApi_ComboMeal {
    private ComboMealMainItem ComboMealMainItem;
    private ComboMealMenuItem ComboMealMenuItem;
    private Number ComboMealObjectNum;
    @XmlElementWrapper(name="SimphonyPosApi_MenuItem")
    private List<PosApi_MenuItem> SimphonyPosApi_MenuItem;

    public ComboMealMainItem getComboMealMainItem() {
        return ComboMealMainItem;
    }

    public void setComboMealMainItem(ComboMealMainItem comboMealMainItem) {
        ComboMealMainItem = comboMealMainItem;
    }

    public ComboMealMenuItem getComboMealMenuItem() {
        return ComboMealMenuItem;
    }

    public void setComboMealMenuItem(ComboMealMenuItem comboMealMenuItem) {
        ComboMealMenuItem = comboMealMenuItem;
    }

    public Number getComboMealObjectNum() {
        return ComboMealObjectNum;
    }

    public void setComboMealObjectNum(Number comboMealObjectNum) {
        ComboMealObjectNum = comboMealObjectNum;
    }

    public List<PosApi_MenuItem> getSimphonyPosApi_MenuItem() {
        return SimphonyPosApi_MenuItem;
    }

    public void setSimphonyPosApi_MenuItem(List<PosApi_MenuItem> simphonyPosApi_MenuItem) {
        SimphonyPosApi_MenuItem = simphonyPosApi_MenuItem;
    }
}
