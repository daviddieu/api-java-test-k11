package service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginResponse;
import model.login.LoginResquest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LoginService {

    public static String getToken(){
        LoginResquest loginResquest = LoginResquest.getDefault();
        Response responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginResquest)
                .post("/api/login");
        assertThat(responseLogin.statusCode(), equalTo(200));
        LoginResponse loginResponse = responseLogin.as(LoginResponse.class);
        return String.format("Bearer %s",loginResponse.getToken());
    }

//    private static String getToken() {
//        if (tokenCreated == null) {
//            return login();
//        } else {
//            long timeout = (long) (Long.parseLong(loginResponse.getTimeout()) * 0.8);
//            if (LocalDateTime.now().isAfter(tokenCreated.plusSeconds(timeout))) {
//                return login();
//            }
//        }
//        return String.format("Bearer %s", loginResponse.getToken());
//    }
//
//    private static String login() {
//        LoginRequest loginRequest = LoginRequest.getDefault();
//        tokenCreated = LocalDateTime.now();
//
//        Response response = io.restassured.RestAssured.given()
//                .log().all()
//                .contentType(io.restassured.http.ContentType.JSON)
//                .body(loginRequest)
//                .post("/api/login");
//
//        assertThat(response.statusCode()).isEqualTo(200);
//
//        loginResponse = response.as(LoginResponse.class);
//        return String.format("Bearer %s", loginResponse.getToken());
//    }
}
