package tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import service.LoginService;
import utils.RestAssuredUtils;

public class BaseTest {
    protected String token;

    @BeforeAll
    static void setUp() {
        RestAssuredUtils.setUp();
    }

    @BeforeEach
    void getUserToken() {
        token = LoginService.getToken();
    }
}
