package main.java.com.autoverwaltung.validation;

import java.time.Year;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ValidationService {
    
    private static final Logger LOG = Logger.getLogger(ValidationService.class);
    
    @Incoming("auto-validation-request")
    @Outgoing("auto-validation-response")
    public Multi<ValidationResponse> validateAutos(Multi<ValidationRequest> requests) {
        return requests
            .onItem().transform(request -> {
                boolean valid = validateAuto(request.kennzeichen(), request.baujahr());
                LOG.debug("Auto-Validation: " + request.kennzeichen() + " -> " + valid);
                return new ValidationResponse(request.id(), valid);
            });
    }
    
    private boolean validateAuto(String kennzeichen, int baujahr) {
        // Prüfe ob das Kennzeichen dem deutschen Format entspricht
        boolean kennzeichenValid = kennzeichen != null && 
                                   kennzeichen.matches("[A-ZÄÖÜ]{1,3}-[A-Z]{1,2} [0-9]{1,4}");
        
        // Prüfe ob das Baujahr plausibel ist (nicht in der Zukunft und nicht zu alt)
        int currentYear = Year.now().getValue();
        boolean baujahrValid = baujahr >= 1900 && baujahr <= currentYear;
        
        LOG.info("Validiere Auto: " + kennzeichen + " (Baujahr: " + baujahr + ")");
        LOG.info("Kennzeichen gültig: " + kennzeichenValid);
        LOG.info("Baujahr gültig: " + baujahrValid);
        
        return kennzeichenValid && baujahrValid;
    }
}