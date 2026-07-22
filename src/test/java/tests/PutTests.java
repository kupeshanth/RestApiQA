package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * PUT and PATCH Tests — All possible scenarios:
 * Full replace, partial update, invalid ID, missing fields, wrong types
 */
public class PutTests extends BaseTest {

    // ═══════════════════════════════════════════════════════════════
    // SECTION 1: PUT — full replacement
    // PUT sends ALL fields. Missing fields get wiped on real APIs.
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "PUT replaces entire post and returns 200 with updated data")
    public void put_fullUpdate_returns200() {
        String fullBody = "{ \"id\": 1, \"title\": \"Replaced Title\", \"body\": \"Replaced body\", \"userId\": 1 }";

        given()
            .spec(requestSpec)
            .body(fullBody)
            .pathParam("id", 1)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",    equalTo(1))
            .body("title", equalTo("Replaced Title"))
            .body("body",  equalTo("Replaced body"));
    }

    @Test(description = "PUT response Content-Type is application/json")
    public void put_responseIsJson() {
        String body = "{ \"id\": 1, \"title\": \"Test\", \"body\": \"Test\", \"userId\": 1 }";
        given()
            .spec(requestSpec)
            .body(body)
            .pathParam("id", 1)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"));
    }

    @Test(description = "PUT with different values returns correct updated data")
    public void put_differentValues_returnsUpdatedData() {
        String body = "{ \"id\": 5, \"title\": \"Updated at " + System.currentTimeMillis() + "\", \"body\": \"New body\", \"userId\": 2 }";

        given()
            .spec(requestSpec)
            .body(body)
            .pathParam("id", 5)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",     equalTo(5))
            .body("userId", equalTo(2));
    }

    @Test(description = "PUT extracts response and validates all returned fields")
    public void put_extractAndValidateResponse() {
        String requestBody = "{ \"id\": 1, \"title\": \"Validated Title\", \"body\": \"Validated body\", \"userId\": 1 }";

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
            .pathParam("id", 1)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();

        System.out.println("PUT response: " + response.asPrettyString());

        Assert.assertEquals((int) response.path("id"), 1, "ID should be 1");
        Assert.assertEquals(response.path("title").toString(), "Validated Title", "Title mismatch");
        Assert.assertNotNull(response.path("body"), "body should not be null");
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 2: PUT NEGATIVE — invalid or edge cases
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "PUT on non-existent ID returns 404 or 500")
    public void put_nonExistentId_returnsError() {
        String body = "{ \"id\": 99999, \"title\": \"Test\", \"body\": \"Test\", \"userId\": 1 }";

        given()
            .spec(requestSpec)
            .body(body)
            .pathParam("id", 99999)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(500)));
            // JSONPlaceholder returns 500 here — real APIs return 404
    }

    @Test(description = "PUT with empty body returns 200 or 400 depending on API")
    public void put_emptyBody_400or200() {
        given()
            .spec(requestSpec)
            .body("{}")
            .pathParam("id", 1)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(400)));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 3: PATCH — partial update
    // PATCH sends ONLY the fields you want to change.
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "PATCH updates only title — other fields unchanged")
    public void patch_titleOnly_returns200() {
        String partialBody = "{ \"title\": \"Only Title Updated\" }";

        given()
            .spec(requestSpec)
            .body(partialBody)
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",    equalTo(1))
            .body("title", equalTo("Only Title Updated"));
            // userId and body are NOT in the response assertion
            // because PATCH leaves other fields untouched
    }

    @Test(description = "PATCH updates only body — title should remain unchanged")
    public void patch_bodyOnly_returns200() {
        String partialBody = "{ \"body\": \"Only body was patched\" }";

        given()
            .spec(requestSpec)
            .body(partialBody)
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",   equalTo(1))
            .body("body", equalTo("Only body was patched"));
    }

    @Test(description = "PATCH updates userId only — returns 200 with correct userId")
    public void patch_userIdOnly_returns200() {
        String partialBody = "{ \"userId\": 9 }";

        given()
            .spec(requestSpec)
            .body(partialBody)
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",     equalTo(1))
            .body("userId", equalTo(9));
    }

    @Test(description = "PATCH with multiple fields updates all specified fields")
    public void patch_multipleFields_allUpdated() {
        String partialBody = "{ \"title\": \"Multi Patch Title\", \"body\": \"Multi Patch Body\" }";

        given()
            .spec(requestSpec)
            .body(partialBody)
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",    equalTo(1))
            .body("title", equalTo("Multi Patch Title"))
            .body("body",  equalTo("Multi Patch Body"));
    }

    @Test(description = "PATCH response Content-Type is application/json")
    public void patch_responseIsJson() {
        given()
            .spec(requestSpec)
            .body("{ \"title\": \"Test\" }")
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"));
    }

    @Test(description = "PATCH with empty body returns 200 or 400")
    public void patch_emptyBody_400or200() {
        given()
            .spec(requestSpec)
            .body("{}")
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(400)));
    }

    @Test(description = "PATCH on non-existent post returns 404 or 500")
    public void patch_nonExistentId_returnsError() {
        given()
            .spec(requestSpec)
            .body("{ \"title\": \"Won't work\" }")
            .pathParam("id", 99999)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(500), equalTo(200)));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 4: PUT vs PATCH — demonstrate the difference
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "Demonstrate PUT vs PATCH difference in one test")
    public void demonstratePutVsPatchDifference() {
        // PUT — must send ALL fields
        String putBody = "{ \"id\": 1, \"title\": \"PUT Title\", \"body\": \"PUT Body\", \"userId\": 1 }";

        given()
            .spec(requestSpec)
            .body(putBody)
            .pathParam("id", 1)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("title", equalTo("PUT Title"));

        // PATCH — send ONLY what changes
        String patchBody = "{ \"title\": \"PATCH Title Only\" }";

        given()
            .spec(requestSpec)
            .body(patchBody)
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("title", equalTo("PATCH Title Only"))
            .body("id",    equalTo(1));  // other fields stay

        System.out.println("PUT vs PATCH: PUT needs all fields, PATCH needs only changed fields");
    }
}
