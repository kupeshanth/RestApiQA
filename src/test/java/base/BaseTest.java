package base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import utils.ApiConstants;

public class BaseTest {

    // Shared request specification — all test classes inherit this
    // Use it with: given().spec(requestSpec)
    protected RequestSpecification requestSpec;

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = ApiConstants.BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Build a reusable request spec with common headers
        // In real projects this is where you also add:
        //   - Authentication tokens
        //   - API version headers (e.g. "x-api-version: 2")
        //   - Correlation IDs for tracing
        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)     // all requests send JSON
            .setAccept(ContentType.JSON)           // all requests expect JSON back
            .build();
    }
}
