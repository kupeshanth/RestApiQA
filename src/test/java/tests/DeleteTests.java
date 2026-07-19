package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class DeleteTests extends BaseTest {

    // TEST 1: Delete a post by ID
    // Sends: DELETE /posts/1
    // Expects: 200 OK (some APIs return 204 No Content instead)
    @Test(description = "DELETE a post returns 200 OK")
    public void deletePost() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")  // DELETE /posts/1
        .then()
            .statusCode(200);
    }

    // TEST 2: Check that DELETE returns empty body
    // JSONPlaceholder returns {} after a delete
    // In real APIs this is often 204 No Content (empty body, no braces)
    @Test(description = "DELETE response body is empty object")
    public void deletePostReturnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
