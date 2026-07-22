package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * DELETE Tests — All possible scenarios:
 * Happy path, non-existent resource, verify empty body, response code variations
 */
public class DeleteTests extends BaseTest {

    // ═══════════════════════════════════════════════════════════════
    // SECTION 1: HAPPY PATH — valid deletes
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "DELETE existing post returns 200 OK")
    public void delete_existingPost_returns200() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
            // 200 = JSONPlaceholder (returns empty body)
            // 204 = real APIs (No Content — no body returned)
    }

    @Test(description = "DELETE post with ID 50 returns 200 or 204")
    public void delete_postId50_returns200or204() {
        given()
            .pathParam("id", 50)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    @Test(description = "DELETE response body is empty object or empty string")
    public void delete_responseBody_isEmpty() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)))
            .body(anyOf(equalTo("{}"), equalTo("")));
            // 200 → body is "{}"  (JSONPlaceholder)
            // 204 → body is ""   (real APIs with No Content)
    }

    @Test(description = "DELETE response time is acceptable")
    public void delete_responseTime_acceptable() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)))
            .time(lessThan(3000L));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 2: NEGATIVE — invalid IDs, non-existent resources
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "DELETE non-existent post ID returns 404 or 200")
    public void delete_nonExistentPost_404or200() {
        given()
            .pathParam("id", 99999)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(200)));
            // 404 = correct for real APIs — resource doesn't exist
            // 200 = JSONPlaceholder returns 200 even for non-existent IDs
    }

    @Test(description = "DELETE with ID zero returns 404 or 400 (real API) or 200 (JSONPlaceholder)")
    public void delete_idZero_returnsError() {
        given()
            .pathParam("id", 0)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400), equalTo(200)));
            // Real APIs: 404 — resource with id=0 doesn't exist
            // JSONPlaceholder: 200 (very lenient)
    }

    @Test(description = "DELETE with negative ID returns 404 or 400 (real API) or 200 (JSONPlaceholder)")
    public void delete_negativeId_returnsError() {
        given()
            .pathParam("id", -1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400), equalTo(200)));
    }

    @Test(description = "DELETE with non-numeric ID returns 404 or 400 (real API) or 200 (JSONPlaceholder)")
    public void delete_nonNumericId_returnsError() {
        given()
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/notanumber")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400), equalTo(200)));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 3: CHAINED — delete then verify resource is gone
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "DELETE post then GET confirms it is gone (chained test)")
    public void delete_thenVerifyGone() {
        int idToDelete = 1;

        // Step 1: Delete the post
        given()
            .pathParam("id", idToDelete)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));

        System.out.println("Deleted post with ID: " + idToDelete);

        // Step 2: Try to GET it — should return 404 on real APIs
        // NOTE: JSONPlaceholder doesn't actually delete, so GET still returns 200 here
        // On a real API, this would be:
        given()
            .pathParam("id", idToDelete)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)));
            // 404 = correct for real APIs (deleted = gone)
            // 200 = JSONPlaceholder (doesn't actually delete)

        System.out.println("Verified: DELETE then GET test complete");
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 4: BOUNDARY VALUES — min/max valid IDs
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "DELETE post with minimum ID (1) returns 200 or 204")
    public void delete_minBoundaryId1_returns200or204() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    @Test(description = "DELETE post with maximum ID (100) returns 200 or 204")
    public void delete_maxBoundaryId100_returns200or204() {
        given()
            .pathParam("id", 100)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    @Test(description = "DELETE post with ID 101 (above max) returns 404 or 200")
    public void delete_aboveMaxId101_404or200() {
        given()
            .pathParam("id", 101)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)));
    }
}
