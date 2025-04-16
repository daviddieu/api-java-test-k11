package tests.country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.country.Country;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import model.country.CountryPagination;
import net.javacrumbs.jsonunit.core.Option;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import utils.RestAssuredUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static data.country.GetCountriesData.GET_ALL_COUNTRIES;
import static data.country.GetCountriesData.GET_ALL_COUNTRIES_PRIVATE;
import static data.country.GetCountriesDataGDP.GET_COUNTRIES_GDP;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountryTests {

    @BeforeAll
    static void setUpEnv() {
        RestAssuredUtils.setUp();
    }

    @Test
    void verifyGetCountriesV1Schema() {
        RestAssured.get("/api/v1/countries")
                .then().log().all()
                .statusCode(200).assertThat().body(matchesJsonSchemaInClasspath("data/country/get-country-json-schema.json"));
    }

    @Test
    void verifyGetCountryV1UsingIgnoreOption() {
        String expectedResponseBody = """
                [{\"name\":\"Viet Nam\",\"code\":\"VN\"},{\"name\":\"USA\",\"code\":\"US\"},{\"name\":\"Canada\",\"code\":\"CA\"},{\"name\":\"UK\",\"code\":\"GB\"},{\"name\":\"France\",\"code\":\"FR\"},{\"name\":\"Japan\",\"code\":\"JP\"},{\"name\":\"India\",\"code\":\"IN\"},{\"name\":\"China\",\"code\":\"CN\"},{\"name\":\"Brazil\",\"code\":\"BR\"}]
                """;
        String expectedResponseBodyWrongOrder = """
                [{\"name\":\"USA\",\"code\":\"US\"},{\"name\":\"Viet Nam\",\"code\":\"VN\"},{\"name\":\"Canada\",\"code\":\"CA\"},{\"name\":\"UK\",\"code\":\"GB\"},{\"name\":\"France\",\"code\":\"FR\"},{\"name\":\"Japan\",\"code\":\"JP\"},{\"name\":\"India\",\"code\":\"IN\"},{\"name\":\"China\",\"code\":\"CN\"},{\"name\":\"Brazil\",\"code\":\"BR\"}]
                """;
        String expectedResponseBodyExtraFields = """
                [{\"name\":\"USA\",\"code\":\"US\"},{\"name\":\"Viet Nam\",\"code\":\"VN\"},{\"name\":\"Canada\",\"code\":\"CA\"},{\"name\":\"UK\",\"code\":\"GB\"},{\"name\":\"France\",\"code\":\"FR\"},{\"name\":\"Japan\",\"code\":\"JP\"},{\"name\":\"India\",\"code\":\"IN\"},{\"name\":\"China\",\"code\":\"CN\"},{\"name\":\"Brazil\",\"code\":\"BR\"}]
                """;
        Response response = RestAssured.get("/api/v1/countries");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        assertThatJson(response.asString()).isEqualTo(expectedResponseBody);
        //4 verify if ResponseBody Wrong Order
        assertThatJson(response.asString()).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expectedResponseBodyWrongOrder);
        //5 verify if ResponseBody Extra Fields
        assertThatJson(response.asString()).when(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_ARRAY_ITEMS).isEqualTo(expectedResponseBodyExtraFields);
    }

    @Test
    void verifyGetCountryV1UsingIgnoreOptionAndDataDriven() {
        Response response = RestAssured.get("/api/v1/countries");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        assertThatJson(response.asString()).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(GET_ALL_COUNTRIES);
    }

    @Test
    void verifyGetCountryV1Details() {
        Response response = RestAssured.get("/api/v1/countries/VN");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        String expectData = """
                {
                    "name": "Viet Nam",
                    "code": "VN"
                }
                """;
        assertThat(response.asString(), jsonEquals(expectData));
    }

    @Test
    void verifyGetCountryV1DetailsUsingPathParam() {
        Response response = RestAssured.get("/api/v1/countries/{code}", "VN");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        String expectData = """
                {
                    "name": "Viet Nam",
                    "code": "VN"
                }
                """;
        assertThatJson(response.asString()).isEqualTo(expectData);
    }

    @Test
    void verifyGetCountryV1DetailsUsingPathParamAndObjectMapperAndForLoop() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> countries = mapper.readValue(GET_ALL_COUNTRIES, new TypeReference<List<Map<String, String>>>() {
        });

        for (Map<String, String> country : countries) {
            Response response = RestAssured.get("/api/v1/countries/{code}", country.get("code"));
            //1 status code
            assertThat(response.statusCode(), equalTo(200));
            //2 headers
            assertThat(response.header("X-Powered-By"), equalTo("Express"));
            assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
            //3 body
            System.out.println(response.asString());
            assertThatJson(response.asString()).isEqualTo(country);
            assertThat(response.asString(), jsonEquals(country));
        }
    }

    static Stream<Map<String, String>> countryProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> data = mapper.readValue(GET_ALL_COUNTRIES, new TypeReference<List<Map<String, String>>>() {
        });
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("countryProvider")
    void verifyGetCountryV1DetailsUsingPathParamAndParameterizedTestMethodSource(Map<String, String> country) {
        Response response = RestAssured.get("/api/v1/countries/{code}", country.get("code"));
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        assertThatJson(response.asString()).isEqualTo(country);
    }


    @Test
    void verifyGetCountriesV3ApiResponseCorrectDataWithParamFilter() {
        float gdp = 5000;
        Response response = RestAssured.given().log().all()
                .queryParam("gdp", gdp)
                .queryParam("operator", ">=")
                .get("/api/v3/countries");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        List<Map<String, String>> countries = response.as(new TypeRef<List<Map<String, String>>>() {
        });
        for (Map<String, String> country : countries) {
            assertThat(Float.parseFloat(country.get("gdp")), greaterThan(gdp));
        }
    }


    static Stream<?> gdpOperatorProvider() throws JsonProcessingException {
        List<Map<String, String>> inputs = new ArrayList<>();
        inputs.add(Map.of("gdp", "1868", "operator", ">"));
        inputs.add(Map.of("gdp", "1868", "operator", "<"));
        inputs.add(Map.of("gdp", "1868", "operator", ">="));
        inputs.add(Map.of("gdp", "1868", "operator", "<="));
        inputs.add(Map.of("gdp", "1868", "operator", "=="));
        inputs.add(Map.of("gdp", "1868", "operator", "!="));
        return inputs.stream();
    }

    //http://localhost:3000/api/v3/countries?gdp=1868&operator=%3E
    // URL encode, ki tu dac biet truoc khi gui di, java tu encode , ngon ngu khac -> tu encode, dung tool online

    @ParameterizedTest
    @MethodSource("gdpOperatorProvider")
    void verifyGetCountriesV3ApiResponseCorrectDataWithParamFilterParameterizedTestUsingFor(Map<String, String> queryParams) {
        Response response = RestAssured.given().log().all()
                .queryParams(queryParams)
                .get("/api/v3/countries");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        List<Map<String, String>> countries = response.as(new TypeRef<List<Map<String, String>>>() {
        });
        for (Map<String, String> country : countries) {
            if (">".equals(queryParams.get("operator"))) {
                assertThat(Float.parseFloat(country.get("gdp")), greaterThan(Float.parseFloat(queryParams.get("gdp"))));
            } else if ("<".equals(queryParams.get("operator"))) {
                assertThat(Float.parseFloat(country.get("gdp")), lessThan(Float.parseFloat(queryParams.get("gdp"))));
            } else if (">=".equals(queryParams.get("operator"))) {
                assertThat(Float.parseFloat(country.get("gdp")), greaterThanOrEqualTo(Float.parseFloat(queryParams.get("gdp"))));
            } else if ("<=".equals(queryParams.get("operator"))) {
                assertThat(Float.parseFloat(country.get("gdp")), lessThanOrEqualTo(Float.parseFloat(queryParams.get("gdp"))));
            } else if ("==".equals(queryParams.get("operator"))) {
                assertThat(Float.parseFloat(country.get("gdp")), equalTo(Float.parseFloat(queryParams.get("gdp"))));
            } else if ("!=".equals(queryParams.get("operator"))) {
                assertThat(Float.parseFloat(country.get("gdp")), not(equalTo(Float.parseFloat(queryParams.get("gdp")))));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("gdpOperatorProvider")
    void verifyGetCountriesV3ApiResponseCorrectDataWithParamFilterParameterizedTestUsingSwitchCase(Map<String, String> queryParams) {
        Response response = RestAssured.given().log().all()
                .queryParams(queryParams)
                .get("/api/v3/countries");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        List<Map<String, String>> countries = response.as(new TypeRef<List<Map<String, String>>>() {
        });
        for (Map<String, String> country : countries) {
            switch (queryParams.get("operator")) {
                case ">":
                    assertThat(Float.parseFloat(country.get("gdp")), greaterThan(Float.parseFloat(queryParams.get("gdp"))));
                    break;
                case "<":
                    assertThat(Float.parseFloat(country.get("gdp")), lessThan(Float.parseFloat(queryParams.get("gdp"))));
                    break;
                case ">=":
                    assertThat(Float.parseFloat(country.get("gdp")), greaterThanOrEqualTo(Float.parseFloat(queryParams.get("gdp"))));
                    break;
                case "<=":
                    assertThat(Float.parseFloat(country.get("gdp")), lessThanOrEqualTo(Float.parseFloat(queryParams.get("gdp"))));
                    break;
                case "!=":
                    assertThat(Float.parseFloat(country.get("gdp")), not(equalTo(Float.parseFloat(queryParams.get("gdp")))));
                    break;
                default:
                    assertThat(Float.parseFloat(country.get("gdp")), equalTo(Float.parseFloat(queryParams.get("gdp"))));
                    break;
            }
        }
    }

    @ParameterizedTest
    @MethodSource("gdpOperatorProvider")
    void verifyGetCountriesV3ApiResponseCorrectDataWithParamFilterParameterizedTestUsingMarcher(Map<String, String> queryParams) {
        Response response = RestAssured.given().log().all()
                .queryParams(queryParams)
                .get("/api/v3/countries");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        List<Map<String, String>> countries = response.as(new TypeRef<List<Map<String, String>>>() {
        });
        for (Map<String, String> country : countries) {
            Matcher<Float> swicher = switch (queryParams.get("operator")) {
                case ">" -> greaterThan(Float.parseFloat(queryParams.get("gdp")));
                case "<" -> lessThan(Float.parseFloat(queryParams.get("gdp")));
                case ">=" -> greaterThanOrEqualTo(Float.parseFloat(queryParams.get("gdp")));
                case "<=" -> lessThanOrEqualTo(Float.parseFloat(queryParams.get("gdp")));
                case "!=" -> not(equalTo(Float.parseFloat(queryParams.get("gdp"))));
                default -> equalTo(Float.parseFloat(queryParams.get("gdp")));
            };
            assertThat(Float.parseFloat(country.get("gdp")), swicher);
        }
    }

    static Stream<Map<String, String>> gdpOperatorDataDrivenProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> data = mapper.readValue(GET_COUNTRIES_GDP, new TypeReference<List<Map<String, String>>>() {
        });
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("gdpOperatorDataDrivenProvider")
    void verifyGetCountriesV3ApiResponseCorrectDataWithParamFilterParameterizedTestDataDrivenUsingMarcher(Map<String, String> queryParams) {
        Response response = RestAssured.given().log().all()
                .queryParams(queryParams)
                .get("/api/v3/countries");
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        List<Map<String, String>> countries = response.as(new TypeRef<List<Map<String, String>>>() {
        });
        for (Map<String, String> country : countries) {
            float actualGdp = Float.parseFloat(queryParams.get("gdp"));
            Matcher<Float> switcher = switch (queryParams.get("operator")) {
                case ">" -> greaterThan(actualGdp);
                case "<" -> lessThan(actualGdp);
                case ">=" -> greaterThanOrEqualTo(actualGdp);
                case "<=" -> lessThanOrEqualTo(actualGdp);
                case "!=" -> not(equalTo(actualGdp));
                default -> equalTo(actualGdp);
            };
            assertThat(Float.parseFloat(country.get("gdp")), switcher);
        }
    }

    @Test
    void verifyGetCountryV4ApiWithPagination() {
        int page = 1;
        int size = 4;

        // Verify first page
        Response response = RestAssured.given().log().all()
                .queryParam("page", page)
                .queryParam("size", size)
                .get("/api/v4/countries");

        CountryPagination countryPaginationFirstPage = response.as(CountryPagination.class);
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        assertThat(countryPaginationFirstPage.getData().size(), equalTo(size));

        // Verify Second page
        response = RestAssured.given().log().all()
                .queryParam("page", page + 1)
                .queryParam("size", size)
                .get("/api/v4/countries");

        CountryPagination countryPaginationSecondPage = response.as(CountryPagination.class);
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        assertThat(countryPaginationSecondPage.getData().size(), equalTo(size));

        // Verify data first page different with second page
        assertThat(countryPaginationSecondPage.getData().containsAll(countryPaginationFirstPage.getData()), is(false));

        List<model.country.Country> countryList = countryPaginationSecondPage.getData();

        // Verify last page
        int total = countryPaginationFirstPage.getTotal();
        int lastPage = total / size;
        if (total % size != 0) {
            lastPage++;
        }
        int sizeOfLastPage = total % size;
        if (sizeOfLastPage == 0) {
            sizeOfLastPage = size;
        }

        // Verify last page
        response = RestAssured.given().log().all()
                .queryParam("page", lastPage)
                .queryParam("size", size)
                .get("/api/v4/countries");

        CountryPagination countryPaginationLastPage = response.as(CountryPagination.class);
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        assertThat(countryPaginationLastPage.getData().size(), equalTo(sizeOfLastPage));
    }

    @Test
    void verifyGetCountryV4ApiWithPaginationEnhancement() {
        int page = 1;
        int size = 4;

        // Verify first page
        Response response = getCountries(page, size);

        CountryPagination countryPaginationFirstPage = response.as(CountryPagination.class);
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        assertThat(countryPaginationFirstPage.getData().size(), equalTo(size));

        // Verify Second page
        response = getCountries(page + 1, size);

        CountryPagination countryPaginationSecondPage = response.as(CountryPagination.class);
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        assertThat(countryPaginationSecondPage.getData().size(), equalTo(size));

        // Verify data first page different with second page
        assertThat(countryPaginationSecondPage.getData().containsAll(countryPaginationFirstPage.getData()), is(false));

        // Verify last page
        int total = countryPaginationFirstPage.getTotal();
        int lastPage = total / size;
        if (total % size != 0) {
            lastPage++;
        }
        int sizeOfLastPage = total % size;
        if (sizeOfLastPage == 0) {
            sizeOfLastPage = size;
        }

        // Verify last page
        response = getCountries(lastPage, size);

        CountryPagination countryPaginationLastPage = response.as(CountryPagination.class);
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        assertThat(countryPaginationLastPage.getData().size(), equalTo(sizeOfLastPage));
    }

    private static Response getCountries(int page, int size) {
        return RestAssured.given().log().all()
                .queryParam("page", page)
                .queryParam("size", size)
                .get("/api/v4/countries");
    }

    @BeforeEach
    void beforeEachTest() {
        System.out.println("Before each test execution");
    }


    @ParameterizedTest(name = "Run test with value: {0}")
    @ValueSource(ints = {1, 2, 3})
    void verifyParameterizedTest(int arg) {
        System.out.println("Start test :" + arg);
    }

    static List<Map<String, String>> countryProviderJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Đọc dữ liệu từ file JSON
        String json = new String(Files.readAllBytes(Paths.get("src/test/resources/data/country/get-countries-data.json")));
        List<Map<String, String>> data = mapper.readValue(json, new TypeReference<List<Map<String, String>>>() {
        });
        return data;
    }

    @ParameterizedTest
    @MethodSource("countryProviderJson")
    void verifyGetCountryV1DetailsUsingPathParamAndParameterizedTestMethodSourceJson(Map<String, String> country) {
        Response response = RestAssured.get("/api/v1/countries/{code}", country.get("code"));
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        assertThatJson(response.asString()).isEqualTo(country);
    }

    static Stream<Country> countryProviderPojo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        InputStream inputStream = CountryTests.class.getClassLoader()
                .getResourceAsStream("data/country/get-countries-data.json");
        if (inputStream == null) {
            throw new RuntimeException("File JSON not found!");
        }

        List<Country> data = mapper.readValue(inputStream, new TypeReference<List<Country>>() {
        });
        return data.stream();
    }

    @ParameterizedTest
    @MethodSource("countryProviderPojo")
    void verifyGetCountryV1DetailsUsingPathParamAndParameterizedTestMethodSourcePojo(Country country) {
        Response response = RestAssured.get("/api/v1/countries/{code}", country.getCode());
        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        System.out.println(response.asString());
        assertThatJson(response.asString()).isEqualTo(country);
    }

    @Test
    void verifyGetCountryV5CustomerHeader() {
        Response response = RestAssured.given().log().all()
                .header("api-key", "private")
                .get("/api/v5/countries");

        //1 status code
        assertThat(response.statusCode(), equalTo(200));
        //2 headers
        assertThat(response.header("X-Powered-By"), equalTo("Express"));
        assertThat(response.header("Content-Type"), equalTo("application/json; charset=utf-8"));
        //3 body
        //3.1 Verify Schema
        System.out.println(response.asString());
        assertThatJson(response.asString()).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(GET_ALL_COUNTRIES_PRIVATE);
    }
}

