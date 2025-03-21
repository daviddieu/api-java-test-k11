package model.login;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResquest {
    private String username;
    private String password;

    public static LoginResquest getDefault(){
        return LoginResquest.builder()
                .username("staff")
                .password("1234567890")
                .build();
    }
}
