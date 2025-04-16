package tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import service.LoginService;
import utils.Endpoints;
import utils.RestAssuredUtils;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

public class BaseTest {
    public static String token;
    public static List<String> createdCustomerIds = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        RestAssuredUtils.setUp();
    }

    @BeforeEach
    void getUserToken() {
        token = LoginService.getToken();
    }

    @AfterAll
    static void cleanUpCreatedUser() {
        if (createdCustomerIds.size() != 0) {
            for (String id : createdCustomerIds) {
                RestAssured.given().log().all()
                        .header(AUTHORIZATION, token)
                        .delete(String.format(Endpoints.USER_ENDPOINT_WITH_PATH_PARAM, id));
            }
        }
    }
}
