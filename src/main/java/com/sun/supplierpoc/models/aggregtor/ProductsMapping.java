package com.sun.supplierpoc.models.aggregtor;

import com.sun.supplierpoc.models.aggregtor.TalabatRest.Modifier;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProductsMapping {

    private String name = "";
    private String foodIcsProductId = "";
    private String talabatProductId = "";
    private ArrayList<ModifierMapping> modifiers = new ArrayList<>();
    private String type;
    public boolean combo;
    public String SKU;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFoodIcsProductId() {
        return foodIcsProductId;
    }

    public void setFoodIcsProductId(String foodIcsProductId) {
        this.foodIcsProductId = foodIcsProductId;
    }

    public String getTalabatProductId() {
        return talabatProductId;
    }

    public void setTalabatProductId(String talabatProductId) {
        this.talabatProductId = talabatProductId;
    }

    public ArrayList<ModifierMapping> getModifiers() {
        return modifiers;
    }

    public void setModifiers(ArrayList<ModifierMapping> modifiers) {
        this.modifiers = modifiers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCombo() {
        return combo;
    }

    public void setCombo(boolean combo) {
        this.combo = combo;
    }

    public ArrayList<ModifierMapping> filterModifier(ArrayList<ModifierMapping> modifierMappings, ArrayList<Modifier> modifiers){
        ArrayList<ModifierMapping> firstFilteredModifiers = new ArrayList<>();
        ArrayList<ModifierMapping> newModifiers = new ArrayList<>();

        for (ModifierMapping modifierMapping : modifierMappings) {
            // check if first id exists in item's extras (modifiers)
            if(modifiers.stream().filter(tempModifier -> tempModifier.getProductId().equals(modifierMapping.getTalabatProductId()))
                    .collect(Collectors.toList()).stream().findFirst().orElse(null) != null){
                firstFilteredModifiers.add(modifierMapping);
            }
        }

        for (ModifierMapping modifierMapping : firstFilteredModifiers) {
            // check if second id exists in item's extras (modifiers)
            if(modifierMapping.getTalabatSecProductId().equals("")){
                newModifiers.add(modifierMapping);
            }else {
                if(modifiers.stream().filter(tempModifier -> tempModifier.getProductId().equals(modifierMapping.getTalabatSecProductId()))
                        .collect(Collectors.toList()).stream().findFirst().orElse(null) != null){
                    newModifiers.add(modifierMapping);
                }
            }
        }

        return newModifiers;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }
}
