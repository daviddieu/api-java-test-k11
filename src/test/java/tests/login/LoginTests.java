package tests.login;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginResponse;
import model.login.LoginResponseError;
import model.login.LoginResquest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.StringUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class LoginTests {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifyUserLoginSuccessful() {
        LoginResquest loginResquest = new LoginResquest("staff", "1234567890");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginResquest)
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
        assertThat(loginResponse.getTimeout(), equalTo("120000"));
        System.out.println(response.asString());
    }

    @Test
    void verifyUserLoginFail() {
        LoginResquest loginResquest = new LoginResquest("", "1234567890");

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginResquest)
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
        LoginResquest loginResquest = new LoginResquest(username, password);

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginResquest)
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
        System.out.println(response.asString());
    }


}
