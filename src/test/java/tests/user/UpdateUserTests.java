package tests.user;

import io.restassured.response.Response;
import model.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import tests.BaseTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static service.UserService.responseCreateUser;
import static service.UserService.responseUpdateUser;

public class UpdateUserTests extends BaseTest {

    @Test
    void verifyUpdateUserSuccessfully(){
        //1 . Create User
        Response responseCreateUser = responseCreateUser(token);
        UserResponse userCreateResponse = responseCreateUser.as(UserResponse.class);
        createdCustomerIds.add(userCreateResponse.getId());
        // 2. Update User
        Response responseUpdateUser = responseUpdateUser(token,userCreateResponse.getId());
        UserResponse userUpdateResponse = responseUpdateUser.as(UserResponse.class);
        assertThat(userUpdateResponse.getId(),equalTo(userCreateResponse.getId()));
        assertThat(userUpdateResponse.getMessage(),equalTo("Customer updated"));
    }
}
