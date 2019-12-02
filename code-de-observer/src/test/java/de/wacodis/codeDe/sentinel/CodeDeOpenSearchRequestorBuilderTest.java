package de.wacodis.codeDe.sentinel;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeDeOpenSearchRequestorBuilderTest {

    private static CodeDeRequestParams params;

    @BeforeAll
    static void setup(){
        String parentIdentifier = "S2_MSI_L2A";
        DateTime startDate = DateTime.parse("2019-10-01T00:00:00Z", CodeDeOpenSearchRequestorBuilder.FORMATTER);
        DateTime endDate = DateTime.parse("2019-10-31T00:00:00Z", CodeDeOpenSearchRequestorBuilder.FORMATTER);
        List<Float> bbox = new ArrayList<Float>();
        bbox.add(6.96f);
        bbox.add(50.9f);
        bbox.add(8.02f);
        bbox.add(51.2f);
        List<Byte> cloudCover = new ArrayList<Byte>();
        cloudCover.add((byte) 0);
        cloudCover.add((byte) 40);
        params = new CodeDeRequestParams(parentIdentifier, startDate, endDate, bbox, cloudCover);
    }

    @Test
    void testBuilder(){
        CodeDeOpenSearchRequestorBuilder requestorBuilder = new CodeDeOpenSearchRequestorBuilder();
        String actualRequestUrl = requestorBuilder.buildGetRequestUrl(params);
        String expectedRequestUrl = "https://catalog.code-de.org/opensearch/request/?" +
                "httpAccept=application/atom%2Bxml&" +
                "parentIdentifier=EOP:CODE-DE:S2_MSI_L2A&" +
                "startDate=2019-10-01T00:00:00Z&" +
                "endDate=2019-10-31T00:00:00Z&" +
                "bbox=6.96,50.9,8.02,51.2&" +
                "cloudCover=[0,40]";
        Assertions.assertEquals(expectedRequestUrl, actualRequestUrl);
    }


}