package de.wacodis.codede.sentinel;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class CodeDeFinderApiRequestBuilderTest {

    private static CodeDeRequestParams params;
    private static final String SATELLITE = "Sentinel2";
    private static final String INSTRUMENT = "MSI";
    private static final String PRODUCT_TYPE = "L2A";
    private static final String PROCESSING_LEVEL = "LEVEL2A";
    private static final String SENSOR_MODE = null;
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String COMPLETION_DATE = "2020-01-17T00:00:00Z";
    private static final Float[] BBOX = new Float[]{6.96f, 50.9f, 8.02f, 51.2f};
    private static final Float[] CLOUD_COVER = new Float[]{0.f, 40.f};

    @BeforeAll
    static void setup() {
        params = new CodeDeRequestParams(SATELLITE, INSTRUMENT, PRODUCT_TYPE, PROCESSING_LEVEL,
                DateTime.parse(START_DATE, CodeDeFinderApiRequestBuilder.FORMATTER),
                DateTime.parse(COMPLETION_DATE, CodeDeFinderApiRequestBuilder.FORMATTER), BBOX, SENSOR_MODE, CLOUD_COVER);
    }

    @Test
    void testBuilder() {
        CodeDeFinderApiRequestBuilder requestorBuilder = new CodeDeFinderApiRequestBuilder();
        String actualRequestUrl = requestorBuilder.buildGetRequestUrl(params, 1);
        String expectedRequestUrl = "https://finder.code-de.org/resto/api/collections/Sentinel2/search.json?" +
                "instrument=MSI" + "&" +
                "productType=L2A" + "&" +
                "processingLevel=LEVEL2A" + "&" +
                "startDate=2019-01-01T00:00:00Z" + "&" +
                "completionDate=2020-01-17T00:00:00Z" + "&" +
                "geometry=POLYGON((6.96+50.9,6.96+51.2,8.02+51.2,8.02+50.9,6.96+50.9))" + "&" +
                "maxRecords=50" + "&" +
                "cloudCover=[0,40]";

        Assertions.assertEquals(expectedRequestUrl, actualRequestUrl);
    }

}