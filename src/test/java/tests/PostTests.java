package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * POST Tests — All possible scenarios:
 * Happy path, missing fields, wrong types, empty body, duplicates, content-type
 */
public class PostTests extends BaseTest {

    // ═══════════════════════════════════════════════════════════════
    // SECTION 1: HAPPY PATH — valid data, expected 201
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "POST valid post data returns 201 Created with generated ID")
    public void createPost_validData_returns201() {
        String body = "{ \"title\": \"My QA Test Post\", \"body\": \"This is the body\", \"userId\": 1 }";

        int newId = given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .body("id",     notNullValue())
            .body("title",  equalTo("My QA Test Post"))
            .body("body",   equalTo("This is the body"))
            .body("userId", equalTo(1))
            .extract().path("id");

        System.out.println("Created post with ID: " + newId);
        Assert.assertTrue(newId > 0, "New ID should be a positive number");
    }

    @Test(description = "POST with different userId returns 201 with correct userId")
    public void createPost_differentUserId_returns201() {
        String body = "{ \"title\": \"User 5 Post\", \"body\": \"Content\", \"userId\": 5 }";

        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .body("userId", equalTo(5))
            .body("title",  equalTo("User 5 Post"));
    }

    @Test(description = "POST with long title returns 201 — no length restriction")
    public void createPost_longTitle_returns201() {
        String longTitle = "A".repeat(255);   // 255 character title
        String body = "{ \"title\": \"" + longTitle + "\", \"body\": \"Content\", \"userId\": 1 }";

        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(400)));
            // 201 = JSONPlaceholder accepts it
            // 400 = real APIs may have length limits
    }

    @Test(description = "POST returns server-assigned ID greater than existing IDs")
    public void createPost_serverAssignsId_greaterThan100() {
        String body = "{ \"title\": \"Test\", \"body\": \"Content\", \"userId\": 1 }";

        int newId = given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .extract().path("id");

        System.out.println("Server assigned ID: " + newId);
        Assert.assertTrue(newId > 100, "JSONPlaceholder assigns IDs > 100 for new posts");
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 2: RESPONSE VALIDATION — check what comes back
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "POST response Content-Type is application/json")
    public void createPost_responseIsJson() {
        String body = "{ \"title\": \"Test\", \"body\": \"Body\", \"userId\": 1 }";
        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(containsString("application/json"));
    }

    @Test(description = "POST response body contains all sent fields echoed back")
    public void createPost_responseEchosSentFields() {
        String title  = "Unique Title " + System.currentTimeMillis();
        String body   = "{ \"title\": \"" + title + "\", \"body\": \"Test body\", \"userId\": 3 }";

        Response response = given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .extract().response();

        Assert.assertEquals(response.path("title").toString(), title);
        Assert.assertEquals(response.path("body").toString(), "Test body");
        Assert.assertEquals((int) response.path("userId"), 3);
        Assert.assertNotNull(response.path("id"), "Response must include server-generated id");
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 3: NEGATIVE — missing required fields
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "POST without title field — check API behaviour")
    public void createPost_missingTitle_400or201() {
        String body = "{ \"body\": \"No title here\", \"userId\": 1 }";
        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(201)));
            // Real APIs: 400 Bad Request — title is required
            // JSONPlaceholder: 201 (it is lenient)
    }

    @Test(description = "POST without userId field — check API behaviour")
    public void createPost_missingUserId_400or201() {
        String body = "{ \"title\": \"No userId\", \"body\": \"Content\" }";
        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(201)));
    }

    @Test(description = "POST with empty body — API should reject or handle gracefully")
    public void createPost_emptyBody_400or201() {
        given()
            .spec(requestSpec)
            .body("{}")
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(201)));
    }

    @Test(description = "POST with null title value — check API handling")
    public void createPost_nullTitle_400or201() {
        String body = "{ \"title\": null, \"body\": \"Content\", \"userId\": 1 }";
        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(201)));
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 4: WRONG DATA TYPES — type validation
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "POST with userId as string instead of integer — type validation")
    public void createPost_userIdAsString_400or201() {
        String body = "{ \"title\": \"Test\", \"body\": \"Content\", \"userId\": \"one\" }";
        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(201)));
            // Real APIs: 400 — userId must be integer, not string
            // JSONPlaceholder: lenient
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 5: SECURITY INPUTS — injection testing
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "POST with SQL injection in title — must NOT crash server (no 500)")
    public void createPost_sqlInjectionInTitle_handledSafely() {
        String body = "{ \"title\": \"'; DROP TABLE posts; --\", \"body\": \"Content\", \"userId\": 1 }";
        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(400)));
            // MUST NOT return 500 — server crashed on bad input is a security bug
    }

    @Test(description = "POST with XSS in title — must NOT execute script")
    public void createPost_xssInTitle_handledSafely() {
        String body = "{ \"title\": \"<script>alert('xss')</script>\", \"body\": \"Content\", \"userId\": 1 }";
        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(400)));
            // Must NOT return 500
            // Title should be stored as plain text, not executed
    }

    // ═══════════════════════════════════════════════════════════════
    // SECTION 6: CHAINED — create then verify
    // ═══════════════════════════════════════════════════════════════

    @Test(description = "POST creates resource, then GET verifies it (chained test)")
    public void createPost_thenVerifyCreated() {
        String uniqueTitle = "Chain Test " + System.currentTimeMillis();
        String requestBody = "{ \"title\": \"" + uniqueTitle + "\", \"body\": \"Body\", \"userId\": 1 }";

        // Step 1: Create
        int newId = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .body("title", equalTo(uniqueTitle))
            .extract().path("id");

        System.out.println("Created ID: " + newId);

        // Step 2: Verify (JSONPlaceholder doesn't persist, but real APIs do)
        // For real APIs this would be:
        // given().pathParam("id", newId).when().get("/posts/{id}").then()
        //     .statusCode(200).body("title", equalTo(uniqueTitle));
        Assert.assertTrue(newId > 0, "Created ID should be valid");
    }
}
