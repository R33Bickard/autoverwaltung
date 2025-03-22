package com.autoverwaltung.validation;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class ValidationRequestDeserializer extends ObjectMapperDeserializer<ValidationRequest> {
    public ValidationRequestDeserializer() {
        super(ValidationRequest.class);
    }
}