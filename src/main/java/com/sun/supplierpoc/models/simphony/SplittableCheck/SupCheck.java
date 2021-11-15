package com.sun.supplierpoc.models.simphony.SplittableCheck;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class SupCheck implements Serializable {

    private String checkValue;
    private boolean paid;
    private int splitNumber;

}
