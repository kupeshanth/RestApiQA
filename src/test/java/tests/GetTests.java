package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class GetTests extends BaseTest {

    // TEST 1: GET all posts
    // Sends: GET https://jsonplaceholder.typicode.com/posts
    // Expects: 200 status, array of 100 posts
    @Test(description = "GET all posts returns 200 with 100 items")
    public void getAllPosts() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()", equalTo(100))          // array has 100 items
            .body("[0].id", equalTo(1))            // first item id is 1
            .body("[0].title", not(emptyString())) // first item title is not empty
            .contentType("application/json")       // response is JSON
            .time(lessThan(3000L));                // responded in under 3 seconds
    }

    // TEST 2: GET one post by ID (path parameter)
    // Sends: GET /posts/1
    // Expects: 200 status, post with id=1
    @Test(description = "GET single post by ID returns correct data")
    public void getPostById() {
        given()
            .pathParam("id", 1)                    // {id} in URL will be replaced by 1
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")  // → GET /posts/1
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("title", not(emptyString()))
            .body("body", not(emptyString()))
            .body("userId", equalTo(1));
    }

    // TEST 3: GET posts filtered by userId (query parameter)
    // Sends: GET /posts?userId=1
    // Expects: 10 posts, all belonging to user 1
    @Test(description = "GET posts filtered by userId returns only that user's posts")
    public void getPostsByUserId() {
        given()
            .queryParam("userId", 1)               // adds ?userId=1 to the URL
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()", equalTo(10))           // user 1 has exactly 10 posts
            .body("userId", everyItem(equalTo(1))); // every post has userId = 1
    }

    // TEST 4: GET a post that does not exist
    // Sends: GET /posts/99999
    // Expects: 404 Not Found
    @Test(description = "GET non-existent post returns 404")
    public void getNonExistentPost() {
        given()
            .pathParam("id", 99999)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(404);
    }

    // TEST 5: Validate the response structure using JSON Schema
    // Sends: GET /posts/1
    // Expects: response matches the schema defined in post-schema.json
    @Test(description = "GET single post response matches JSON schema")
    public void getPostMatchesSchema() {
        given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
    }

    // TEST 6: Soft assertions — collect ALL failures, not just the first
    //
    // Real-world problem with normal assertions:
    //   Imagine a response has 5 wrong fields. Normal assertions stop at field 1.
    //   You fix field 1, run again, see field 2 fail, fix, run again...
    //   That's 5 separate test runs to find 5 bugs.
    //
    // With SoftAssert: one run shows ALL 5 failures at once.
    // This is critical when raising defects — you report everything in one ticket.
    //
    // COMMON MISTAKE: forgetting softAssert.assertAll() at the end.
    //   If you forget it, ALL failures are silently swallowed — test always passes.
    //   Always end with assertAll(). Always.

    @Test(description = "Soft assertions — validate all fields of a post at once")
    public void validatePostFieldsWithSoftAssert() {
        Response response = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();

        // Extract all fields from the response
        int    id     = response.path("id");
        int    userId = response.path("userId");
        String title  = response.path("title");
        String body   = response.path("body");

        SoftAssert softAssert = new SoftAssert();

        // All these assertions run even if one fails
        softAssert.assertEquals(id, 1,              "id should be 1");
        softAssert.assertEquals(userId, 1,          "userId should be 1");
        softAssert.assertFalse(title.isEmpty(),     "title should not be empty");
        softAssert.assertFalse(body.isEmpty(),      "body should not be empty");
        softAssert.assertTrue(title.length() > 3,  "title should be longer than 3 chars");
        softAssert.assertTrue(id > 0,              "id should be a positive number");

        // THIS LINE IS MANDATORY — without it, failures above are never reported
        softAssert.assertAll();
    }
}
