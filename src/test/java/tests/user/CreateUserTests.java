package tests.user;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginResponse;
import model.login.LoginResquest;
import model.user.AddressRequest;
import model.user.UserRequest;
import model.user.UserResponse;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import service.LoginService;
import utils.JsonUtils;
import utils.RestAssuredUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static utils.Endpoints.LOGIN_ENDPOINT;
import static utils.Endpoints.USER_ENDPOINT;

public class CreateUserTests {
    private static final String MSG_CREATE_USER_SUCCESS = "Customer created";
    static String token;

    @BeforeAll
    static void setUp() {
        RestAssuredUtils.setUp();
    }

    @BeforeEach
    void getUserToken() {
        token = LoginService.getToken();
    }

    @Test
    void verifyCreateUserSuccessfulSimple() {
        Random random = new Random();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        long currentTimeMillis = System.currentTimeMillis();
        String email = String.format("auto_%s@gmail.com", timestamp);
        String phone = String.format("09%d",currentTimeMillis% 1000000000);
        String body = String.format("""
                {
                    "firstName":"Jos",
                    "lastName": "Doe",
                    "middleName": "Smith",
                    "birthday": "01-23-2000",
                    "email": "%s",
                    "phone": "%s",
                    "addresses": [
                        {
                            "streetNumber": "123",
                            "street": "Main St",
                            "ward": "Ward 1",
                            "district": "District 1",
                            "city": "Thu Duc",
                            "state": "Ho Chi Minh",
                            "zip": "70000",
                            "country": "VN"
                        }
                    ]
                }
                """, email, phone);

        LoginResquest loginResquest = new LoginResquest("staff", "1234567890");
        Response responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginResquest)
                .post("/api/login");
        System.out.println(responseLogin.asString());
        LoginResponse loginResponse = responseLogin.as(LoginResponse.class);
        String token = String.format("Bearer %s", loginResponse.getToken());

        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(body)
                .post("/api/user");
        System.out.println(responseUser.asString());
        UserResponse userResponse = responseUser.as(UserResponse.class);

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(userResponse.getId()), is(true));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));
    }

    @Test
    void verifyCreateUserSuccessfulUsingModel() throws IOException {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        AddressRequest addressRequest =
                new AddressRequest("123", "Main St", "Ward 1", "District 1", "Thu Duc", "Ho Chi Minh", "70000", "VN");

        List<AddressRequest> addressRequestList = new ArrayList<>();
        addressRequestList.add(addressRequest);
        UserRequest userRequest = new UserRequest("Jos", "Doe", "Smith", "01-23-2000", email, phone, addressRequestList);

        LoginResquest loginData = JsonUtils.readJson("src/test/resources/data/login/valid-login.json", LoginResquest.class);
        LoginResquest loginResquest = new LoginResquest(loginData.getUsername(), loginData.getPassword());
        Response responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginResquest)
                .post(LOGIN_ENDPOINT);
        System.out.println(responseLogin.asString());
        LoginResponse loginResponse = responseLogin.as(LoginResponse.class);
        String token = String.format("Bearer %s", loginResponse.getToken());

        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        UserResponse userResponse = responseUser.as(UserResponse.class);


        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(userResponse.getId()), is(true));
        assertThat(userResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));
    }

    @Test
    void verifyCreateUserSuccessfulUsingModelGetDefault() throws IOException {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        UserRequest userRequest = UserRequest.getDefault();
        System.out.println(userRequest.toString());
        userRequest.setEmail(email);
        userRequest.setPhone(phone);
        LoginResquest loginData = LoginResquest.getDefault();

        LoginResponse responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginData)
                .post(LOGIN_ENDPOINT)
                .as(LoginResponse.class);
        String token = String.format("Bearer %s", responseLogin.getToken());

        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        UserResponse userResponse = responseUser.as(UserResponse.class);

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(userResponse.getId()), is(true));
        assertThat(userResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));
    }

    @Test
    void verifyCreateUserSuccessfulWithTwoAddress() throws IOException {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(email);
        userRequest.setPhone(phone);
        AddressRequest addressRequest1 = AddressRequest.getDefault();
        AddressRequest addressRequest2 = AddressRequest.getDefault();
        addressRequest2.setStreetNumber("456");
        userRequest.setAddresses(List.of(addressRequest1, addressRequest2));

        LoginResquest loginData = LoginResquest.getDefault();
        LoginResponse responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginData)
                .post(LOGIN_ENDPOINT)
                .as(LoginResponse.class);

        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        UserResponse userResponse = responseUser.as(UserResponse.class);

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(userResponse.getId()), is(true));
        assertThat(userResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));
    }
}
