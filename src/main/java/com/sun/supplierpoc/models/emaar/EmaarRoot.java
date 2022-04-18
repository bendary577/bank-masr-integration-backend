package com.sun.supplierpoc.models.emaar;

import org.codehaus.jackson.annotate.JsonProperty;

public class EmaarRoot {
    @JsonProperty("SalesDataCollection")
    public SalesDataCollection salesDataCollection;
}