package countries;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;


public class CountriesTests {

    @BeforeAll
    static void setUpEnv() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

//    @Test
//    void verifyGetCountrySchema(){
//        RestAssured.get("/api/v1/countries")
//                .then().log().all()
//                .statusCode(200).assertThat().body(matchesJsonSchemaInClasspath("data/get-country/get-country-json-schema.json"));
//    }
//
//    @Test
//    void verifyGetCountrySchemaDetails(){
//        RestAssured.get("/api/v1/countries/VN")
//                .then().log().all()
//                .statusCode(200);
//    }

//    @Test
//    void verifyGetCountryApiResponseCorrectData() {
//        String expectedResponseBody = """
//                [{\"name\":\"Viet Nam\",\"code\":\"VN\"},{\"name\":\"USA\",\"code\":\"US\"},{\"name\":\"Canada\",\"code\":\"CA\"},{\"name\":\"UK\",\"code\":\"GB\"},{\"name\":\"France\",\"code\":\"FR\"},{\"name\":\"Japan\",\"code\":\"JP\"},{\"name\":\"India\",\"code\":\"IN\"},{\"name\":\"China\",\"code\":\"CN\"},{\"name\":\"Brazil\",\"code\":\"BR\"}]
//                """;
//        String expectedResponseBodyWrongOrder = """
//                [{\"name\":\"USA\",\"code\":\"US\"},{\"name\":\"Viet Nam\",\"code\":\"VN\"},{\"name\":\"Canada\",\"code\":\"CA\"},{\"name\":\"UK\",\"code\":\"GB\"},{\"name\":\"France\",\"code\":\"FR\"},{\"name\":\"Japan\",\"code\":\"JP\"},{\"name\":\"India\",\"code\":\"IN\"},{\"name\":\"China\",\"code\":\"CN\"},{\"name\":\"Brazil\",\"code\":\"BR\"}]
//                """;
//        String expectedResponseBodyExtraFields = """
//                [{\"name\":\"USA\",\"code\":\"US\"},{\"name\":\"Viet Nam\",\"code\":\"VN\"},{\"name\":\"Canada\",\"code\":\"CA\"},{\"name\":\"UK\",\"code\":\"GB\"},{\"name\":\"France\",\"code\":\"FR\"},{\"name\":\"Japan\",\"code\":\"JP\"},{\"name\":\"India\",\"code\":\"IN\"},{\"name\":\"China\",\"code\":\"CN\"},{\"name\":\"Brazil\",\"code\":\"BR\"}]
//                """;
//        Response response = RestAssured.given().get("/api/v1/countries");
//        //1 status code
//        assertThat(response.statusCode(), equalTo(200));
//        //2 headers
//        assertThat(response.header("X-Powered-By"), equalTo("Express"));
//        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
//        //3 body
//        System.out.println(response.asString());
//        assertThatJson(response.asString()).isEqualTo(expectedResponseBody);
//        //Wrong order
//        //assertThatJson(response.asString()).isEqualTo(expectedResponseBodyWrongOrder);
//        assertThatJson(response.asString()).when(IGNORING_ARRAY_ORDER).isEqualTo(expectedResponseBodyWrongOrder);
//        //Extra fields
//        //assertThatJson(response.asString()).isEqualTo(expectedResponseBodyWrongOrder);
//        // assertThatJson(response.asString()).when(IGNORING_EXTRA_FIELDS).isEqualTo(expectedResponseBodyExtraFields);
//        assertThatJson(response.asString()).when(IGNORING_EXTRA_FIELDS, IGNORING_ARRAY_ORDER).isEqualTo(expectedResponseBodyExtraFields);
//    }

    @Test
    void verifyGetCountrySchema() {
        RestAssured.get("/api/v1/countries")
                .then().log().all()
                .statusCode(200).assertThat().body(matchesJsonSchemaInClasspath("data/get-country/get-country-json-schema.json"));
    }

    @Test
    void verifyGetCountryCorrectDataAndIgnore() {
        String expectedResponse = """
                [{"name":"Viet Nam","code":"VN"},{"name":"USA","code":"US"},{"name":"Canada","code":"CA"},{"name":"UK","code":"GB"},{"name":"France","code":"FR"},{"name":"Japan","code":"JP"},{"name":"India","code":"IN"},{"name":"China","code":"CN"},{"name":"Brazil","code":"BR"}]
                """;
        String expectedResponseOrder = """
                [{"name":"USA","code":"US"},{"name":"Viet Nam","code":"VN"},{"name":"Canada","code":"CA"},{"name":"UK","code":"GB"},{"name":"France","code":"FR"},{"name":"Japan","code":"JP"},{"name":"India","code":"IN"},{"name":"China","code":"CN"},{"name":"Brazil","code":"BR"}]
                """;
        String expectedResponseExtraFields = """
                [{"name":"Viet Nam","code":"VN"},{"name":"Canada","code":"CA"},{"name":"UK","code":"GB"},{"name":"France","code":"FR"},{"name":"Japan","code":"JP"},{"name":"India","code":"IN"},{"name":"China","code":"CN"},{"name":"Brazil","code":"BR"}]
                """;
        Response response = RestAssured.get("/api/v1/countries");

        //0 .Verify response code
        assertThat(response.statusCode(), equalTo(200));
        //1. Verify Header
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        assertThat(response.header("Content-Type"), containsString("application/json"));
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        //2. Verify Body
        System.out.println(response.asString());
        assertThatJson(response.asString()).isEqualTo(expectedResponse);
        // Wrong order
        assertThatJson(response.asString()).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expectedResponseOrder);
        // Extra field
        assertThatJson(response.asString()).when(Option.IGNORING_EXTRA_ARRAY_ITEMS,Option.IGNORING_ARRAY_ORDER).isEqualTo(expectedResponseOrder);
    }
}
