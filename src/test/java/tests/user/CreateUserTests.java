package tests.user;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import model.login.LoginRequest;
import model.login.LoginResponse;
import model.user.dao.CustomerAddressDao;
import model.user.dao.CustomerDao;
import model.user.dto.*;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import tests.BaseTest;
import utils.DbUtils;
import utils.JsonUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static utils.Endpoints.*;

public class CreateUserTests extends BaseTest {
    private static final String MSG_CREATE_USER_SUCCESS = "Customer created";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";


    @Test
    void verifyCreateUserSuccessfulSimple() {
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
        UserResponse userResponse = responseUser.as(UserResponse.class);
        createdCustomerIds.add(userResponse.getId());

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
    void verifyCreateUserSuccessfulUsingModelAndVerifyInDb() throws IOException {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        UserAddressRequest userAddressRequest =
                new UserAddressRequest("123", "Main St", "Ward 1", "District 1", "Thu Duc", "Ho Chi Minh", "70000", "VN");

        List<UserAddressRequest> userAddressRequestList = new ArrayList<>();
        userAddressRequestList.add(userAddressRequest);
        UserRequest userRequest = new UserRequest("Jos", "Doe", "Smith", "01-23-2000", email, phone, userAddressRequestList);

        LoginRequest loginData = JsonUtils.readJson("src/test/resources/data/login/valid-login.json", LoginRequest.class);
        LoginRequest loginRequest = new LoginRequest(loginData.getUsername(), loginData.getPassword());
        Response responseLogin = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post(LOGIN_ENDPOINT);
        System.out.println(responseLogin.asString());
        LoginResponse loginResponse = responseLogin.as(LoginResponse.class);
        String token = String.format("Bearer %s", loginResponse.getToken());

        LocalDateTime timeBeforeCreateUserDb = LocalDateTime.now();
        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        UserResponse userResponse = responseUser.as(UserResponse.class);
        createdCustomerIds.add(userResponse.getId());

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(userResponse.getId()), is(true));
        assertThat(userResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));
        //4. Verify by access to DB
        CustomerDao customerDao = DbUtils.getCustomerById(userResponse.getId());
        assertThatJson(customerDao).whenIgnoringPaths("$..id", "$..createdAt", "$..updatedAt", "$..customerId").isEqualTo(userRequest);

        LocalDateTime timeAfterCreateUserDb = LocalDateTime.now();
        for (CustomerAddressDao addressResponse : customerDao.getAddresses()) {
            assertThat(addressResponse.getCreatedAt().isAfter(timeBeforeCreateUserDb), is(true));
            assertThat(addressResponse.getCreatedAt().isBefore(timeAfterCreateUserDb), is(true));
            assertThat(addressResponse.getUpdatedAt().isAfter(timeBeforeCreateUserDb), is(true));
            assertThat(addressResponse.getUpdatedAt().isBefore(timeAfterCreateUserDb), is(true));
            assertThat(addressResponse.getCustomerId(), equalTo(UUID.fromString(userResponse.getId())));
        }
        assertThat(customerDao.getCreatedAt().isAfter(timeBeforeCreateUserDb), is(true));
        assertThat(customerDao.getCreatedAt().isBefore(timeAfterCreateUserDb), is(true));
        assertThat(customerDao.getUpdatedAt().isAfter(timeBeforeCreateUserDb), is(true));
        assertThat(customerDao.getUpdatedAt().isBefore(timeAfterCreateUserDb), is(true));

//        5. Clean Up data
        RestAssured.given().log().all()
                .header(AUTHORIZATION, token)
                .delete(String.format(USER_ENDPOINT_WITH_PATH_PARAM, userResponse.getId()));
    }

    @Test
    void verifyCreateUserSuccessfulUsingModelGetDefault() {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        UserRequest userRequest = UserRequest.getDefault();
        System.out.println(userRequest.toString());
        userRequest.setEmail(email);
        userRequest.setPhone(phone);
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
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        UserResponse userResponse = responseUser.as(UserResponse.class);
        createdCustomerIds.add(userResponse.getId());

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
    void verifyCreateUserSuccessfulWithTwoAddress() {
        Faker faker = new Faker();
        String email = faker.internet().emailAddress();
        String phone = faker.phoneNumber().subscriberNumber(10);

        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(email);
        userRequest.setPhone(phone);
        UserAddressRequest userAddressRequest1 = UserAddressRequest.getDefault();
        UserAddressRequest userAddressRequest2 = UserAddressRequest.getDefault();
        userAddressRequest2.setStreetNumber("456");
        userRequest.setAddresses(List.of(userAddressRequest1, userAddressRequest2));

        LocalDateTime timeBeforeCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        UserResponse userResponse = responseUser.as(UserResponse.class);
        createdCustomerIds.add(userResponse.getId());

        //1 Verify status code
        assertThat(responseUser.statusCode(), equalTo(200));
        //2 Verify headers
        assertThat(responseUser.header(HttpHeaders.CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        //3 Verify body
        //3.1 Verify schema -> do it in a separate test case
        //3.2 Verify response
        assertThat(StringUtils.isNotBlank(userResponse.getId()), is(true));
        assertThat(userResponse.getMessage(), equalTo(MSG_CREATE_USER_SUCCESS));

        //4. Verify response from get equal to body of create user
        Response responseGetUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .get(String.format("%s/%s", USER_ENDPOINT, userResponse.getId()));
        CustomerResponse customerResponse = responseGetUser.as(CustomerResponse.class);
        System.out.println(responseGetUser.asString());
        assertThat(responseGetUser.statusCode(), equalTo(200));
        assertThatJson(responseGetUser.asString())
                .whenIgnoringPaths("$..id", "$..createdAt", "$..updatedAt", "$..customerId")
                .isEqualTo(userRequest);

        assertThat(customerResponse.getId(), equalTo(userResponse.getId()));
        LocalDateTime timeAfterCreateUser = LocalDateTime.now(ZoneId.of("Z"));
        for (CustomerAddressResponse addressResponse : customerResponse.getAddresses()) {
            LocalDateTime addressCreatedAt = LocalDateTime.parse(addressResponse.getCreatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            assertThat(addressCreatedAt.isAfter(timeBeforeCreateUser), is(true));
            assertThat(addressCreatedAt.isBefore(timeAfterCreateUser), is(true));
            LocalDateTime addressUpdatedAt = LocalDateTime.parse(addressResponse.getUpdatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            assertThat(addressUpdatedAt.isAfter(timeBeforeCreateUser), is(true));
            assertThat(addressUpdatedAt.isBefore(timeAfterCreateUser), is(true));
            assertThat(addressResponse.getCustomerId(), equalTo(userResponse.getId()));
        }
        LocalDateTime userCreatedAt = LocalDateTime.parse(customerResponse.getCreatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        assertThat(userCreatedAt.isAfter(timeBeforeCreateUser), is(true));
        assertThat(userCreatedAt.isBefore(timeAfterCreateUser), is(true));
        LocalDateTime userUpdatedAt = LocalDateTime.parse(customerResponse.getUpdatedAt(), DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        assertThat(userUpdatedAt.isAfter(timeBeforeCreateUser), is(true));
        assertThat(userUpdatedAt.isBefore(timeAfterCreateUser), is(true));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data/user/create-user-fields-validation.csv", numLinesToSkip = 1)
    void verifyCreateUserFailed(String field, String value, String errorMessage) {
        UserRequest userRequest = UserRequest.getDefault();
        System.out.println(userRequest.toString());
        switch (field) {
            case "/firstName" -> userRequest.setFirstName(value);
            case "/lastName" -> userRequest.setLastName(value);
            case "/middleName" -> userRequest.setMiddleName(value);
            case "/birthday" -> userRequest.setBirthday(value);
            case "/email" -> userRequest.setEmail(value);
            case "/phone" -> userRequest.setPhone(value);
            case "/addresses/0/streetNumber" -> userRequest.getAddresses().get(0).setStreetNumber(value);
            case "/addresses/0/street" -> userRequest.getAddresses().get(0).setStreet(value);
            case "/addresses/0/ward" -> userRequest.getAddresses().get(0).setWard(value);
            case "/addresses/0/district" -> userRequest.getAddresses().get(0).setDistrict(value);
            case "/addresses/0/city" -> userRequest.getAddresses().get(0).setCity(value);
            case "/addresses/0/state" -> userRequest.getAddresses().get(0).setState(value);
            case "/addresses/0/zip" -> userRequest.getAddresses().get(0).setZip(value);
            case "/addresses/0/country" -> userRequest.getAddresses().get(0).setCountry(value);
        }
        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());
        UserErrorResponse userErrorResponse = responseUser.as(UserErrorResponse.class);
        assertThat(responseUser.statusCode(), equalTo(400));
        System.out.println(userErrorResponse.getMessage());
        assertThat(userErrorResponse.getMessage()).isEqualToIgnoringWhitespace(errorMessage);
    }

    @ParameterizedTest
    @MethodSource("createUserValidationProvider")
    void verifyCreateUserFailedUsingMethodSource(UserRequest userRequest, String field, String errorMessage) {
        Response responseUser = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest)
                .post(USER_ENDPOINT);
        System.out.println(responseUser.asString());

        //Verify Status Code
        assertThat(responseUser.statusCode(), equalTo(400));
        //Verify Header if needs

        //Verify body
        UserErrorResponse userErrorResponse = responseUser.as(UserErrorResponse.class);
        System.out.println(userErrorResponse.getMessage());
        assertThat(userErrorResponse.getField()).isEqualToIgnoringWhitespace(field);
        assertThat(userErrorResponse.getMessage()).isEqualToIgnoringWhitespace(errorMessage);
    }

    static Stream<Arguments> createUserValidationProvider() {
        List<Arguments> argumentsList = new ArrayList<>();
        UserRequest firstNameEmpty = UserRequest.getDefault();
        firstNameEmpty.setFirstName("");
        argumentsList.add(Arguments.arguments(firstNameEmpty, "/firstName", "must NOT have fewer than 1 characters"));
        UserRequest lastNameEmpty = UserRequest.getDefault();
        lastNameEmpty.setLastName("");
        argumentsList.add(Arguments.arguments(lastNameEmpty, "/lastName", "must NOT have fewer than 1 characters"));
        UserRequest birthdayEmpty = UserRequest.getDefault();
        birthdayEmpty.setBirthday("");
        argumentsList.add(Arguments.arguments(birthdayEmpty, "/birthday", "must match pattern \"^\\d{2}-\\d{2}-\\d{4}$\""));
        UserRequest emailEmpty = UserRequest.getDefault();
        emailEmpty.setEmail("");
        argumentsList.add(Arguments.arguments(emailEmpty, "/email", "must match format \"email\""));
        UserRequest phoneEmpty = UserRequest.getDefault();
        phoneEmpty.setPhone("");
        argumentsList.add(Arguments.arguments(phoneEmpty, "/phone", "must match pattern \"^\\d{10,11}$\""));
        UserRequest zipEmpty = UserRequest.getDefault();
        UserAddressRequest userAddressRequest = UserAddressRequest.getDefault();
        userAddressRequest.setZip("");
        zipEmpty.setAddresses(List.of(userAddressRequest));
        argumentsList.add(Arguments.arguments(zipEmpty, "/addresses/0/zip", "must match pattern \"^\\d{5}(?:-\\d{4})?$\""));
        return argumentsList.stream();
    }


}
