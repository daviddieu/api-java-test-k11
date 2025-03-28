package model.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCustomerResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private String createdAt;
    private String updatedAt;
    private List<GetCustomerAddressResponse> addresses;
}
