import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.jvm.hotspot.utilities.Assert;

import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.path.json.JsonPath.from;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

/**
 * Created by andrewfowler on 1/30/17.
 */
public class ExampleTest {

    @BeforeClass
    public static void setupURL()
    {
        // here we setup the default URL and API base path to use throughout the tests
        RestAssured.baseURI = "http://api.fixer.io";
        RestAssured.basePath = "";
    }

    @Test
    public void testEuroVisibleByDefault()
    {
        when(). get("/latest").
        then(). body("base", equalTo("EUR")).
                assertThat().body(matchesJsonSchemaInClasspath("conversion-schema.json"));
    }

    @Test
    public void testRequestedCurrencyIsReturned()
    {
        given(). queryParam("base", "USD").
        when().  get("/latest").
        then().  body("base", equalTo("USD")).
                 assertThat().body(matchesJsonSchemaInClasspath("conversion-schema.json"));
    }

    @Test
    public void test31ConversionsReturned()
    {
        given(). queryParam("base", "USD").
        when().  get("/latest").
        then().  body("rates.size()", is(31)).
                 assertThat().body(matchesJsonSchemaInClasspath("conversion-schema.json"));
    }

    @Test
    public void testEuroConversionsIsPositive()
    {
        given(). queryParam("base", "USD").
        when().  get("/latest").
        then().  body("rates.EUR", is(greaterThan(0.0f))).
                 assertThat().body(matchesJsonSchemaInClasspath("conversion-schema.json"));
    }

    @Test
    public void testEuroConversionsIsPositive_ViaExtraction()
    {
        Response response =
            given(). queryParam("base", "USD").
            when().  get("/latest").
            then().  extract().response();

        ResponseBody body = response.body();
        Float euro = body.path("rates.EUR");
        Assert.that(euro > 0.0f, "Expected EUR conversion to be > 0 but was: " + euro);
    }

    @Test
    public void testQueryingDKKtoGbpUsd(){
        Response resp =
            given() .queryParam("base", "DKK")
                    .queryParam("symbols", "GBP,USD")
            .when() .get("/latest")
            .then() .extract().response();

        assertThat(true, is(true));

        assertThat(resp.body().path("base").toString(), is(equalTo("DKK")));

        HashMap rates = resp.body().path("rates");

        Assert.that(rates.containsKey("GBP"), "Expected response to contain GBP");
        Assert.that(rates.containsKey("USD"), "Expected response to contain USD");
    }

    @Test
    public void DkkConversion_SchemaValidation() {
        given() .queryParam("base", "DKK")
                .queryParam("symbols", "GBP,USD")
                .when() .get("/latest")
                .then() .assertThat().body(matchesJsonSchemaInClasspath("conversion-schema.json"));
    }

    @Test
    public void DkkConversionToMany_SchemaValidation() {
        given() .queryParam("base", "DKK")
                .queryParam("symbols", "AUD,BGN,BRL,CAD,GBP,USD")
                .when() .get("/latest")
                .then() .assertThat().body(matchesJsonSchemaInClasspath("conversion-schema.json"));
    }
}
