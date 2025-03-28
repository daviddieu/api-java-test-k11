package service;

import io.restassured.response.Response;
import model.login.LoginResponse;
import model.login.LoginRequest;
import org.hamcrest.Matchers;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;

public class LoginService {
    private static LocalDateTime tokenCreated;
    private static LoginResponse loginResponse;

    public static String getToken() {
        if (tokenCreated == null) {
            return login();
        } else {
            long timeout = (long) (Long.parseLong(loginResponse.getTimeout()) * 0.8);
            if (LocalDateTime.now().isAfter(tokenCreated.plusSeconds(timeout))) {
                return login();
            }
        }
        return String.format("Bearer %s", loginResponse.getToken());
    }

    private static String login() {
        LoginRequest loginRequest = LoginRequest.getDefault();
        tokenCreated = LocalDateTime.now();

        Response response = io.restassured.RestAssured.given()
                .log().all()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(loginRequest)
                .post("/api/login");

        assertThat(response.statusCode(), Matchers.equalTo(200));

        loginResponse = response.as(LoginResponse.class);
        return String.format("Bearer %s", loginResponse.getToken());
    }
}
