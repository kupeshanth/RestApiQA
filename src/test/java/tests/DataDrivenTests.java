package tests;

import base.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class DataDrivenTests extends BaseTest {

    // DATA PROVIDER — supplies rows of test data to the test method below
    // Each Object[] is one test run. Here we have 5 valid post IDs.
    //
    // Real-world problem: you might load this data from a CSV, Excel, or database
    // instead of hardcoding it. But for learning, hardcoded is fine.

    @DataProvider(name = "validPostIds")
    public Object[][] validPostIds() {
        return new Object[][] {
            { 1  },
            { 2  },
            { 5  },
            { 50 },
            { 100 }
        };
    }

    // This @Test runs 5 times — once per row in validPostIds
    // TestNG shows each run separately in the report: getPostById[1], [2], [5]...
    //
    // Real-world problem: POST IDs in your system might not be sequential (1,2,3).
    // They could be UUIDs. Your DataProvider would then contain UUIDs instead.

    @Test(dataProvider = "validPostIds",
          description = "GET each post by ID — data-driven with 5 different IDs")
    public void getPostById(int postId) {
        given()
            .pathParam("id", postId)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(postId))      // id in response must match what we requested
            .body("title", not(emptyString()))
            .body("userId", greaterThan(0));
    }


    // DATA PROVIDER — invalid IDs that should return 404
    // Real-world problem: negative testing (testing what SHOULD fail) is
    // just as important as positive testing. QAs who skip this miss real bugs.

    @DataProvider(name = "invalidPostIds")
    public Object[][] invalidPostIds() {
        return new Object[][] {
            { 0    },   // zero — no resource has id=0
            { -1   },   // negative number
            { 99999 }   // very large id that doesn't exist
        };
    }

    @Test(dataProvider = "invalidPostIds",
          description = "GET with invalid IDs returns 404 — negative testing")
    public void getPostByInvalidId(int invalidId) {
        // Real-world problem: some poorly built APIs return 200 with an empty body
        // instead of 404 when a resource doesn't exist. This test catches that.
        given()
            .pathParam("id", invalidId)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(404);
    }


    // DATA PROVIDER — multiple fields for POST testing
    // Each row has: title, body, userId, expectedStatus
    //
    // Real-world problem: testing different combinations of input data
    // without writing a separate test for each combination.

    @DataProvider(name = "postBodyVariants")
    public Object[][] postBodyVariants() {
        return new Object[][] {
            // title,             body,            userId, expectedStatus
            { "Normal Post",    "Normal body",       1,    201 },
            { "Another Post",   "Another body",      2,    201 },
            { "Third Post",     "Third body",        3,    201 }
        };
    }

    @Test(dataProvider = "postBodyVariants",
          description = "POST with different data variants — all should return 201")
    public void createPostWithVariants(String title, String body, int userId, int expectedStatus) {
        String requestBody = String.format(
            "{ \"title\": \"%s\", \"body\": \"%s\", \"userId\": %d }",
            title, body, userId
        );

        given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(expectedStatus)
            .body("title", equalTo(title))
            .body("userId", equalTo(userId));
    }
}
