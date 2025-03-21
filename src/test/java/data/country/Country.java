package data.country;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data  // Tự động tạo Getter, Setter, toString(), hashCode(), equals()
public class Country {

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;
}
