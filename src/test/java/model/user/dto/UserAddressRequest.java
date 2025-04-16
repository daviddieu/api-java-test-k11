package model.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressRequest {
    private String streetNumber;
    private String street;
    private String ward;
    private String district;
    private String city;
    private String state;
    private String zip;
    private String country;

    public static UserAddressRequest getDefault(){
//        return new AddressRequest("123", "Main St", "Ward 1", "District 1", "Thu Duc", "Ho Chi Minh", "70000", "VN");
        return UserAddressRequest.builder()
                .streetNumber("123")
                .street("Main St")
                .ward("Ward 1")
                .district("District 1")
                .city("Thu Duc")
                .state("Ho Chi Minh")
                .zip("70000")
                .country("VN")
                .build();
    }
}
