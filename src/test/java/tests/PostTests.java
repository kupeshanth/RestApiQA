package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PostTests extends BaseTest {

    private static final String NEW_POST =
            "{ \"title\": \"QA Practice Post\", \"body\": \"This is a test body\", \"userId\": 1 }";

    // Notice: no more .contentType(ContentType.JSON) needed — requestSpec handles it
    @Test(description = "POST a new post returns 201 Created with generated ID")
    public void createPost() {
        given()
            .spec(requestSpec)           // ← replaces contentType(ContentType.JSON)
            .body(NEW_POST)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("QA Practice Post"))
            .body("body", equalTo("This is a test body"))
            .body("userId", equalTo(1));
    }

    @Test(description = "POST response has application/json content type")
    public void createPostReturnsJsonContentType() {
        given()
            .spec(requestSpec)
            .body(NEW_POST)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(containsString("application/json"));
    }

    @Test(description = "POST with empty body returns 201 on JSONPlaceholder")
    public void createPostWithEmptyBody() {
        given()
            .spec(requestSpec)
            .body("{}")
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201);
    }
}
