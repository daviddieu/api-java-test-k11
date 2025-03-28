package tests.login;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginResponse;
import model.login.LoginResponseError;
import model.login.LoginRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.StringUtils;
import utils.RestAssuredUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class LoginTests {

    @BeforeAll
    static void setup() {
        RestAssuredUtils.setUp();
    }

    @Test
    void verifyUserLoginSuccessful() {
        LoginRequest loginRequest = new LoginRequest("staff", "1234567890");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/login");

        //1 Verify status code
        assertThat(response.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        LoginResponse loginResponse = response.as(LoginResponse.class);
        assertThat(StringUtils.isNotBlank(loginResponse.getToken()), is(true));
        assertThat(Integer.parseInt(loginResponse.getTimeout()), equalTo(120000));
        System.out.println(response.asString());
    }

    @Test
    void verifyUserLoginFail() {
        LoginRequest loginRequest = new LoginRequest("", "1234567890");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/login");

        //1 Verify status code
        assertThat(response.statusCode(), equalTo(401));
        //2 Verify headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        LoginResponseError loginResponse = response.as(LoginResponseError.class);
        assertThat(loginResponse.getMessage(), equalTo("Invalid credentials"));
        System.out.println(response.asString());
    }

    @ParameterizedTest
    @CsvSource({
            "staffs, 1234567890",
            "staff, 12345678901",
            "'', 1234567890",
            ", 1234567890",
            "staff, ''",
            "staff, ",
    })
    void verifyUserLoginFail(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/login");

        //1 Verify status code
        assertThat(response.statusCode(), equalTo(401));
        //2 Verify headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        LoginResponseError loginResponse = response.as(LoginResponseError.class);
//        assertThat(loginResponse.getMessage(), equalTo("Invalid credentials"));
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(loginResponse.getMessage()).isEqualTo("Invalid credentials");
        softAssertions.assertAll();
        System.out.println(response.asString());
    }


}
