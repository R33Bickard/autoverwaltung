package com.autoverwaltung.auto;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/autos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AutoResource {
    
    private static final Logger LOG = Logger.getLogger(AutoResource.class);
    
    @Inject
    AutoService autoService;
    
    @Inject
    @Channel("auto-validation-request")
    Emitter<ValidationRequest> validationRequestEmitter;
    
    @GET
    public List<Auto> getAllAutos() {
        return Auto.listAll();
    }
    
    @GET
    @Path("/{id}")
    public Response getAuto(@PathParam("id") Long id) {
        Auto auto = Auto.findById(id);
        if (auto == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(auto).build();
    }
    
    @POST
    @Transactional
    public Response createAuto(Auto auto) {
        try {
            // Erst Auto speichern
            Auto created = autoService.createAuto(auto);
            LOG.info("Auto created: " + created.id);
            
            // Dann Validierungsanfrage senden (nach der Transaktion)
            sendValidationRequest(created);
            
            return Response.status(Status.CREATED).entity(created).build();
        } catch (Exception e) {
            LOG.error("Error creating auto", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Erstellen des Autos: " + e.getMessage())
                    .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateAuto(@PathParam("id") Long id, Auto auto) {
        Auto entity = Auto.findById(id);
        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        entity.marke = auto.marke;
        entity.modell = auto.modell;
        entity.baujahr = auto.baujahr;
        entity.kennzeichen = auto.kennzeichen;
        entity.farbe = auto.farbe;
        
        // Nach der Transaktion Validierungsanfrage senden
        sendValidationRequest(entity);
        
        return Response.ok(entity).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteAuto(@PathParam("id") Long id) {
        Auto entity = Auto.findById(id);
        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        entity.delete();
        return Response.noContent().build();
    }
    
    private void sendValidationRequest(Auto auto) {
        try {
            LOG.info("Sending validation request for auto: " + auto.id);
            ValidationRequest request = new ValidationRequest(auto.id, auto.kennzeichen, auto.baujahr);
            validationRequestEmitter.send(request).toCompletableFuture().join();
        } catch (Exception e) {
            LOG.error("Error sending validation request", e);
        }
    }
}