package com.autoverwaltung.auto;

import java.util.Optional;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AutoService {
    
    private static final Logger LOG = Logger.getLogger(AutoService.class);
    
    @Transactional
    public Auto createAuto(Auto auto) {
        auto.persist();
        return auto;
    }
    
    @Incoming("auto-validation-response")
    @Transactional
    public void processValidation(ValidationResponse response) {
        LOG.debug("Validation Response: " + response);
        Optional<Auto> autoOptional = Auto.findByIdOptional(response.id());
        if (autoOptional.isEmpty()) {
            LOG.warn("Auto not found: " + response.id());
            return;
        }
        Auto auto = autoOptional.get();
        auto.validated = response.valid();
        LOG.info("Auto " + auto.id + " (" + auto.kennzeichen + ") validation status updated: " + auto.validated);
    }
}