package com.sun.supplierpoc;

public class Conversions {
    public Conversions() {
    }

    public String transformColName(String columnName){
        return columnName.strip().toLowerCase().replace(' ', '_');
    }

}
