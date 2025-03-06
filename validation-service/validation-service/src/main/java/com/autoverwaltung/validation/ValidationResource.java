package com.autoverwaltung.validation;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/validation")
public class ValidationResource {
    
    private final AtomicInteger validations = new AtomicInteger(0);
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Validierungsservice läuft! Bisher wurden " + validations + " Validierungen durchgeführt.";
    }
    
    @GET
    @Path("/rules")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidationRules getValidationRules() {
        return new ValidationRules(
            "Kennzeichen muss dem deutschen Format (z.B. 'M-AB 123') entsprechen",
            "Baujahr muss zwischen 1900 und " + Year.now().getValue() + " liegen"
        );
    }
    
    record ValidationRules(String kennzeichenRegel, String baujahrRegel) {}
}