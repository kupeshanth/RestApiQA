package tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import base.BaseTest;
import io.restassured.http.ContentType;
import utils.ApiConstants;

public class PutTests extends BaseTest {

    // TEST 1: Full update with PUT
    // PUT = replace the ENTIRE resource with new data
    // You must send ALL fields, not just the ones you want to change
    // Sends: PUT /posts/1 with complete new data
    // Expects: 200 OK, response reflects the new data
    @Test(description = "PUT replaces the full post and returns 200")
    public void updatePostWithPut() {
        String fullReplacement =
                "{ \"id\": 1, \"title\": \"Replaced Title\", \"body\": \"Replaced body\", \"userId\": 1 }";

        given()
            .contentType(ContentType.JSON)
            .body(fullReplacement)
            .pathParam("id", 1)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")   // PUT /posts/1
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("title", equalTo("Replaced Title"))   // ← correct value
            .body("body", equalTo("Replaced body"));
    }

    // TEST 2: Partial update with PATCH
    // PATCH = update only the fields you send, leave the rest unchanged
    // You send ONLY what you want to change
    // Sends: PATCH /posts/1 with only the title
    // Expects: 200 OK, title is updated
    @Test(description = "PATCH updates only the title field and returns 200")
    public void updatePostTitleWithPatch() {
        String partialUpdate = "{ \"title\": \"Only Title Updated\" }";

        given()
            .contentType(ContentType.JSON)
            .body(partialUpdate)
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")  // PATCH /posts/1
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("title", equalTo("Only Title Updated"));
    }
}
