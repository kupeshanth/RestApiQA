package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * GET Tests — All possible scenarios:
 * Happy path, negative, edge cases, headers, schema, soft assertions, extraction
 */
public class GetTests extends BaseTest {

    // ═══════════════════════════════════════════════════════════════
    // SECTION 1: HAPPY PATH — valid inputs, expected success
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET all posts returns 200 with 100 items and correct structure")
    public void getAllPosts_returns200_with100Items() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()",        equalTo(100))       // exact count
            .body("[0].id",        notNullValue())     // first item has id
            .body("[0].title",     not(emptyString())) // first item has title
            .body("[0].userId",    notNullValue())     // first item has userId
            .contentType("application/json")
            .time(lessThan(3000L));                    // under 3 seconds
    }

    @Test(description = "GET single post by valid ID returns 200 with correct data")
    public void getPostById_validId_returns200() {
        int postId = 1;
        given()
            .pathParam("id", postId)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",     equalTo(postId))
            .body("title",  not(emptyString()))
            .body("body",   not(emptyString()))
            .body("userId", equalTo(1));
    }

    @Test(description = "GET post at maximum valid ID returns 200")
    public void getPostById_maxValidId_returns200() {
        given()
            .pathParam("id", 100)           // 100 = last valid post
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(100));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 2: QUERY PARAMETERS — filtering
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET posts filtered by userId=1 returns only user 1's posts")
    public void getPostsByUserId_returns10Posts() {
        given()
            .queryParam("userId", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()",  equalTo(10))
            .body("userId",  everyItem(equalTo(1)));   // every post has userId=1
    }

    @Test(description = "GET posts filtered by userId=2 returns only user 2's posts")
    public void getPostsByUserId2_returnsCorrectPosts() {
        given()
            .queryParam("userId", 2)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()",  equalTo(10))
            .body("userId",  everyItem(equalTo(2)));
    }

    @Test(description = "GET posts with multiple query params filters correctly")
    public void getPostsWithMultipleQueryParams() {
        given()
            .queryParam("userId", 1)
            .queryParam("id", 1)           // both filters applied
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()",  equalTo(1))
            .body("[0].id",  equalTo(1))
            .body("[0].userId", equalTo(1));
    }

    @Test(description = "GET posts with non-existent userId returns empty array")
    public void getPostsByNonExistentUserId_returnsEmptyArray() {
        given()
            .queryParam("userId", 99999)   // no user with this ID
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));   // empty array — not 404
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 3: NEGATIVE — invalid inputs, expected errors
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET non-existent post ID returns 404")
    public void getPost_nonExistentId_returns404() {
        given()
            .pathParam("id", 99999)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(404);
    }

    @Test(description = "GET post with ID zero returns 404")
    public void getPost_idZero_returns404() {
        given()
            .pathParam("id", 0)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400)));
    }

    @Test(description = "GET post with negative ID returns 404 or 400")
    public void getPost_negativeId_returnsError() {
        given()
            .pathParam("id", -1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400)));
    }

    @Test(description = "GET post with non-numeric ID returns 404 or 400")
    public void getPost_nonNumericId_returnsError() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/abc")  // string ID
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(400)));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 4: RESPONSE HEADERS — what the server sends back
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET response has Content-Type: application/json header")
    public void getPost_hasJsonContentTypeHeader() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"));
    }

    @Test(description = "GET response has Content-Type header present")
    public void getPost_hasRequiredHeaders() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/1")
        .then()
            .statusCode(200)
            .header("Content-Type", notNullValue());
            // Note: Content-Length may not be present when Transfer-Encoding: chunked is used
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 5: RESPONSE TIME — performance check
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET all posts responds in under 3 seconds")
    public void getAllPosts_responseTimeUnder3Seconds() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .time(lessThan(3000L));
    }

    @Test(description = "GET single post responds in under 2 seconds")
    public void getPostById_responseTimeUnder2Seconds() {
        given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .time(lessThan(2000L));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 6: JSON SCHEMA VALIDATION — contract tests
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET single post response matches JSON schema contract")
    public void getPost_matchesJsonSchema() {
        given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
        // WHAT THIS CHECKS:
        // - All required fields exist: id, title, body, userId
        // - id is integer (not string)
        // - title is string
        // - body is string
        // - userId is integer
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 7: VALUE EXTRACTION — capture and reuse values
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET post extracts title and validates it is not blank")
    public void getPost_extractTitle_notBlank() {
        String title = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().path("title");

        System.out.println("Extracted title: " + title);
        Assert.assertFalse(title.isEmpty(), "Title should not be empty");
        Assert.assertTrue(title.length() > 3,  "Title should be longer than 3 chars");
        Assert.assertFalse(title.isBlank(),     "Title should not be blank/whitespace");
    }

    @Test(description = "GET post extracts userId and verifies it is a positive integer")
    public void getPost_extractUserId_isPositive() {
        int userId = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().path("userId");

        System.out.println("Extracted userId: " + userId);
        Assert.assertTrue(userId > 0, "userId should be a positive integer");
        Assert.assertTrue(userId <= 10, "userId should be <= 10 for JSONPlaceholder");
    }

    @Test(description = "GET full response object and validate multiple fields")
    public void getPost_extractFullResponse_validateAllFields() {
        Response response = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();

        // Print for debugging (remove in production)
        System.out.println("Full response: " + response.asPrettyString());

        // Extract individual fields
        int    id     = response.path("id");
        int    userId = response.path("userId");
        String title  = response.path("title");
        String body   = response.path("body");

        // Validate each field
        Assert.assertEquals(id, 1, "ID should be 1");
        Assert.assertTrue(userId > 0, "userId should be positive");
        Assert.assertNotNull(title, "title should not be null");
        Assert.assertNotNull(body, "body should not be null");
        Assert.assertFalse(title.isEmpty(), "title should not be empty");
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 8: SOFT ASSERTIONS — validate all fields in one run
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "Validate all post fields with soft assertions — all failures reported")
    public void getPost_softAssertAllFields() {
        Response response = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();

        SoftAssert soft = new SoftAssert();

        // All these run even if one fails — all failures reported at once
        soft.assertEquals((int) response.path("id"),     1,   "id mismatch");
        soft.assertEquals((int) response.path("userId"), 1,   "userId mismatch");
        soft.assertNotNull(response.path("title"),             "title is null");
        soft.assertNotNull(response.path("body"),              "body is null");
        soft.assertFalse(((String) response.path("title")).isEmpty(), "title is empty");
        soft.assertFalse(((String) response.path("body")).isEmpty(),  "body is empty");
        soft.assertEquals(response.statusCode(), 200,          "wrong status code");

        soft.assertAll();   // MANDATORY — never skip this line
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 9: ARRAY ASSERTIONS — validating list responses
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET all posts — each post has all required fields")
    public void getAllPosts_everyItemHasRequiredFields() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("id",     everyItem(notNullValue()))
            .body("title",  everyItem(not(emptyString())))
            .body("body",   everyItem(not(emptyString())))
            .body("userId", everyItem(notNullValue()))
            .body("userId", everyItem(greaterThan(0)));
    }

    @Test(description = "GET all posts — userId values are within expected range 1-10")
    public void getAllPosts_userIdWithinRange() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("userId", everyItem(greaterThanOrEqualTo(1)))
            .body("userId", everyItem(lessThanOrEqualTo(10)));
    }

    @Test(description = "GET all posts — IDs are unique (no duplicates)")
    public void getAllPosts_idsAreUnique() {
        Response response = given()
            .when().get(ApiConstants.POSTS_ENDPOINT)
            .then().statusCode(200).extract().response();

        java.util.List<Integer> ids = response.path("id");
        long uniqueCount = ids.stream().distinct().count();
        Assert.assertEquals(uniqueCount, ids.size(), "IDs should be unique — found duplicates");
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 10: BOUNDARY VALUES — edge of valid range
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "GET post with ID 1 — minimum valid boundary")
    public void getPost_minBoundaryId1_returns200() {
        given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));
    }

    @Test(description = "GET post with ID 100 — maximum valid boundary")
    public void getPost_maxBoundaryId100_returns200() {
        given()
            .pathParam("id", 100)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(100));
    }

    @Test(description = "GET post with ID 101 — just above maximum boundary returns 404")
    public void getPost_aboveMaxBoundaryId101_returns404() {
        given()
            .pathParam("id", 101)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(404);
    }
}
