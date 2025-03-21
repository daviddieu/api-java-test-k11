package model.user;

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
    private List<AddressRequest> addresses;

    public static UserRequest getDefault() {
        return UserRequest.builder()
                .firstName("Jos")
                .lastName("Doe")
                .middleName("Smith")
                .birthday("01-23-2000")
                .email("auto_api@abc.com")
                .phone("01234567890")
                .addresses(List.of(AddressRequest.getDefault()))
                .build();
    }
}
