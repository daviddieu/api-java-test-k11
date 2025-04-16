package model.user.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private List<UserAddressRequest> addresses;

    public static UserRequest getDefault() {
        return UserRequest.builder()
                .firstName("Jos")
                .lastName("Doe")
                .middleName("Smith")
                .birthday("01-23-2000")
                .email("auto_api@abc.com")
                .phone("01234567890")
                .addresses(List.of(UserAddressRequest.getDefault()))
                .build();
    }

    public static UserRequest updateUser() {
        return UserRequest.builder()
                .firstName("Jose")
                .lastName("Doee")
                .middleName("Smithe")
                .birthday("01-23-2001")
                .email("auto_api_1@abc.com")
                .phone("01234567890123")
                .addresses(List.of(UserAddressRequest.getDefault()))
                .build();
    }
}
