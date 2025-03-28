package tests.user;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginRequest;
import model.login.LoginResponse;
import model.user.*;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import tests.BaseTest;
import utils.JsonUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static utils.Endpoints.LOGIN_ENDPOINT;
import static utils.Endpoints.USER_ENDPOINT;

public class CreateUserTests extends BaseTest {
    private static final String MSG_CREATE_USER_SUCCESS = "Customer created";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @Test
    void verifyCreateUserSuccessfulSimple() {
        Random random = new Random();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        long currentTimeMillis = System.currentTimeMillis();
        String email = String.format("auto_%s@gmail.com", timestamp);
        String phone = String.format("09%d", currentTimeMillis % 1000000000);
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

        LoginRequest loginRequest = new LoginRequest("staff", "1234567890");
        Response responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
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
        CreateUserResponse createUserResponse = responseUser.as(CreateUserResponse.class);

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(createUserResponse.getId()), is(true));
        assertThat(createUserResponse.getMessage(), equalTo("Customer created"));
    }

    @Test
    void verifyCreateUserSuccessfulUsingModel() throws IOException {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        CreatUserAddressRequest creatUserAddressRequest =
                new CreatUserAddressRequest("123", "Main St", "Ward 1", "District 1", "Thu Duc", "Ho Chi Minh", "70000", "VN");

        List<CreatUserAddressRequest> creatUserAddressRequestList = new ArrayList<>();
        creatUserAddressRequestList.add(creatUserAddressRequest);
        CreatUserRequest creatUserRequest = new CreatUserRequest("Jos", "Doe", "Smith", "01-23-2000", email, phone, creatUserAddressRequestList);

        LoginRequest loginData = JsonUtils.readJson("src/test/resources/data/login/valid-login.json", LoginRequest.class);
        LoginRequest loginRequest = new LoginRequest(loginData.getUsername(), loginData.getPassword());
        Response responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post(LOGIN_ENDPOINT);
        System.out.println(responseLogin.asString());
        LoginResponse loginResponse = responseLogin.as(LoginResponse.class);
        String token = String.format("Bearer %s", loginResponse.getToken());

        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(creatUserRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        CreateUserResponse createUserResponse = responseUser.as(CreateUserResponse.class);


        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(createUserResponse.getId()), is(true));
        assertThat(createUserResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));
    }

    @Test
    void verifyCreateUserSuccessfulUsingModelGetDefault() {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        CreatUserRequest creatUserRequest = CreatUserRequest.getDefault();
        System.out.println(creatUserRequest.toString());
        creatUserRequest.setEmail(email);
        creatUserRequest.setPhone(phone);
        LoginRequest loginData = LoginRequest.getDefault();

        LoginResponse responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginData)
                .post(LOGIN_ENDPOINT)
                .as(LoginResponse.class);
        String token = String.format("Bearer %s", responseLogin.getToken());

        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(creatUserRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        CreateUserResponse createUserResponse = responseUser.as(CreateUserResponse.class);

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(createUserResponse.getId()), is(true));
        assertThat(createUserResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));
    }

    @Test
    void verifyCreateUserSuccessfulWithTwoAddress() {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        CreatUserRequest creatUserRequest = CreatUserRequest.getDefault();
        creatUserRequest.setEmail(email);
        creatUserRequest.setPhone(phone);
        CreatUserAddressRequest creatUserAddressRequest1 = CreatUserAddressRequest.getDefault();
        CreatUserAddressRequest creatUserAddressRequest2 = CreatUserAddressRequest.getDefault();
        creatUserAddressRequest2.setStreetNumber("456");
        creatUserRequest.setAddresses(List.of(creatUserAddressRequest1, creatUserAddressRequest2));

        LocalDateTime timeBeforeCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(creatUserRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        CreateUserResponse createUserResponse = responseUser.as(CreateUserResponse.class);

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(createUserResponse.getId()), is(true));
        assertThat(createUserResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));

        //4. Verify response from get equal to body of create user
        Response responseGetUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .get(String.format("%s/%s", USER_ENDPOINT, createUserResponse.getId()));
        GetCustomerResponse getCustomerResponse = responseGetUser.as(GetCustomerResponse.class);
        System.out.println(responseGetUser.asString());
        assertThat(responseGetUser.statusCode(), equalTo(200));
        assertThatJson(responseGetUser.asString())
                .whenIgnoringPaths("$..id", "$..createdAt", "$..updatedAt", "$..customerId")
                .isEqualTo(creatUserRequest);

        assertThat(getCustomerResponse.getId(), equalTo(createUserResponse.getId()));
        LocalDateTime timeAfterCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        for (GetCustomerAddressResponse addressResponse : getCustomerResponse.getAddresses()) {
            LocalDateTime addressCreatedAt = LocalDateTime.parse(addressResponse.getCreatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            assertThat(addressCreatedAt.isAfter(timeBeforeCreateUser), is(true));
            assertThat(addressCreatedAt.isBefore(timeAfterCreateUser), is(true));
            LocalDateTime addressUpdatedAt = LocalDateTime.parse(addressResponse.getUpdatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            assertThat(addressUpdatedAt.isAfter(timeBeforeCreateUser), is(true));
            assertThat(addressUpdatedAt.isBefore(timeAfterCreateUser), is(true));
            assertThat(addressResponse.getCustomerId(), equalTo(createUserResponse.getId()));
        }
        LocalDateTime userCreatedAt = LocalDateTime.parse(getCustomerResponse.getCreatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        assertThat(userCreatedAt.isAfter(timeBeforeCreateUser), is(true));
        assertThat(userCreatedAt.isBefore(timeAfterCreateUser), is(true));
        LocalDateTime userUpdatedAt = LocalDateTime.parse(getCustomerResponse.getUpdatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        assertThat(userUpdatedAt.isAfter(timeBeforeCreateUser), is(true));
        assertThat(userUpdatedAt.isBefore(timeAfterCreateUser), is(true));
    }
}
