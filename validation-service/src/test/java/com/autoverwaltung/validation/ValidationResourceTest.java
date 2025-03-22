package com.autoverwaltung.validation;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class ValidationResourceTest {
    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/validation")
          .then()
             .statusCode(200)
             .body(is("Validierungsservice läuft!"));    }

}