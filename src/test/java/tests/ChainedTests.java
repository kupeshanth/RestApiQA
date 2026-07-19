package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ChainedTests extends BaseTest {

    // CHAINED TEST: POST → GET → DELETE
    //
    // Real-world scenario: QA verifying a full user story:
    //   "As a user I can create a post, view it, and delete it"
    //
    // This is called an End-to-End (E2E) API test — it covers a complete workflow.
    //
    // REAL PROBLEM YOU'LL FACE: Test data pollution
    //   If your POST actually creates data in the database and your DELETE fails,
    //   the test data stays in the system forever. This is called "dirty test data".
    //   Solution: always clean up in @AfterMethod, or use a dedicated test environment.

    @Test(description = "Full workflow: create a post, verify it, then delete it")
    public void createReadDeletePost() {

        // ── STEP 1: POST — Create a new post ──────────────────────────────────
        // Real-world problem: if the POST returns 500, the test stops here.
        // This immediately tells you the CREATE feature is broken.

        String newPostBody =
            "{ \"title\": \"E2E Test Post\", \"body\": \"Created in chain test\", \"userId\": 1 }";

        int createdId = given()
            .spec(requestSpec)
            .body(newPostBody)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .body("title", equalTo("E2E Test Post"))
            .extract().path("id");    // ← capture the server-generated ID

        System.out.println(">>> Created post with ID: " + createdId);

        // ── STEP 2: GET — Read back the post we just created ──────────────────
        // Real-world problem: sometimes POST returns 201 but data isn't actually
        // saved (caching issue, async processing). A GET immediately after catches this.
        // JSONPlaceholder simulates creation so it won't find our new post —
        // but in a real API this GET would return our created resource.

        given()
            .pathParam("id", 1)   // using id=1 since JSONPlaceholder doesn't persist
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(1));

        System.out.println(">>> Successfully read post ID: " + createdId);

        // ── STEP 3: DELETE — Remove the post ──────────────────────────────────
        // Real-world problem: if DELETE returns 404, the resource was never created.
        // If it returns 403, the user doesn't have permission to delete.

        given()
            .pathParam("id", 1)   // using id=1 since JSONPlaceholder doesn't persist
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200);

        System.out.println(">>> Successfully deleted post ID: " + createdId);

        // ── STEP 4: Assert the chain completed ────────────────────────────────
        // This is extra safety — confirm the ID we captured was a valid number
        Assert.assertTrue(createdId > 0, "Created post ID should be a positive number");
        System.out.println(">>> Full E2E chain passed!");
    }


    // CHAINED TEST: Extract multiple values from a response
    //
    // Real-world scenario: You need to verify relationships between fields.
    // E.g. verify that userId in a post matches an actual user in the system.

    @Test(description = "Extract and cross-validate response fields")
    public void extractAndValidateFields() {

        Response response = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();   // ← get the whole response object

        // Extract individual fields
        int    postId  = response.path("id");
        int    userId  = response.path("userId");
        String title   = response.path("title");
        String body    = response.path("body");

        System.out.println("Post ID : " + postId);
        System.out.println("User ID : " + userId);
        System.out.println("Title   : " + title);

        // Cross-validate using TestNG Assert
        // Real-world problem: these fail when backend returns wrong types
        // e.g. userId comes back as a String "1" instead of integer 1
        Assert.assertEquals(postId, 1,  "Post ID should be 1");
        Assert.assertEquals(userId, 1,  "User ID should be 1");
        Assert.assertFalse(title.isEmpty(), "Title should not be empty");
        Assert.assertFalse(body.isEmpty(),  "Body should not be empty");

        // Real-world cross-field validation:
        // Assert the userId in this post belongs to a valid user
        // In a real project: GET /users/{userId} and assert 200
        System.out.println(">>> All field extractions and validations passed!");
    }
}
