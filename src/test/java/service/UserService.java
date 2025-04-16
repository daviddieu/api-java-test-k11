package service;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import model.user.dto.UserRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.Endpoints.USER_ENDPOINT;
import static utils.Endpoints.USER_ENDPOINT_WITH_PATH_PARAM;

public class UserService {
    // Dùng cho POST
    private static Response responseUser(Method method, String token, UserRequest userRequest) {
        return responseUser(method, token, userRequest, null); // Gọi bản đầy đủ nhưng không truyền id
    }

    // Dùng cho PUT
    private static Response responseUser(Method method, String token, UserRequest userRequest, String id) {
        Faker faker = new Faker();
        userRequest.setEmail(faker.internet().emailAddress());
        userRequest.setPhone(faker.phoneNumber().subscriberNumber(10));

        String url;
        switch (method) {
            case POST -> url = USER_ENDPOINT;
            case PUT -> {
                if (id == null || id.isEmpty()) {
                    throw new IllegalArgumentException("ID is required for PUT method");
                }
                url = String.format(USER_ENDPOINT_WITH_PATH_PARAM, id);
            }
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        }

        RequestSpecification requestSpec = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(AUTHORIZATION, token)
                .body(userRequest);

        Response response = requestSpec.request(method.name(), url);
        System.out.println(response.asString());

        assertThat(response.statusCode(), equalTo(200));
        return response;
    }

    public static Response responseCreateUser(String token) {
        return responseUser(Method.POST, token, UserRequest.getDefault());
    }

    public static Response responseUpdateUser(String token,String id) {
        return responseUser(Method.PUT, token, UserRequest.updateUser(),id);
    }
}
