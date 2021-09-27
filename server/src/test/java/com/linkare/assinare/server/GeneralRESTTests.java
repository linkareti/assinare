package com.linkare.assinare.server;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import com.linkare.assinare.server.test.Profiles;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

/**
 *
 * @author bnazare
 */
@QuarkusTest
@TestProfile(Profiles.Default.class)
public class GeneralRESTTests {

    @Test
    public void testRTE() throws Exception {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
        .when()
            .post("/automated-tests/rte")
        .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .body("code", is(ErrorCode.GENERAL_ERROR.name()))
            .body("message", is("Erro Genérico"));
    }
    
    @Path("/automated-tests/rte")
    public static class RTEResource {

        @GET
        public String throwRuntimeException() {
            throw new IllegalStateException("test generated error");
        }

    }

}
