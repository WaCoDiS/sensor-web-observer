package de.wacodis.codeDe.sentinel;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CodeDeOpenSearchRequestorBuilderTest {

    private static CodeDeRequestParams params;

    @BeforeClass
    public static void setup(){
        String parentIdentifier = "S2_MSI_L2A";
        DateTime startDate = DateTime.parse("2019-01-01T00:00:00Z", CodeDeOpenSearchRequestorBuilder.FORMATTER);
        DateTime endDate = DateTime.parse("2020-01-17T00:00:00Z", CodeDeOpenSearchRequestorBuilder.FORMATTER);
        List<Float> bbox = new ArrayList<Float>();
        bbox.add(6.96f);
        bbox.add(50.9f);
        bbox.add(8.02f);
        bbox.add(51.2f);
        List<Float> cloudCover = new ArrayList<Float>();
        cloudCover.add(0.f);
        cloudCover.add(40.f);
        params = new CodeDeRequestParams(parentIdentifier, startDate, endDate, bbox, cloudCover);
    }

    @Test
    public void testBuilder(){
        CodeDeOpenSearchRequestorBuilder requestorBuilder = new CodeDeOpenSearchRequestorBuilder();
        String actualRequestUrl = requestorBuilder.buildGetRequestUrl(params, 1);
        String expectedRequestUrl = "https://catalog.code-de.org/opensearch/request/?" +
                "parentIdentifier=EOP:CODE-DE:S2_MSI_L2A&" +
                "startDate=2019-01-01T00:00:00Z&" +
                "endDate=2020-01-17T00:00:00Z&" +
                "bbox=6.96,50.9,8.02,51.2&" +
                "cloudCover=[0.0,40.0]&" +
                "maximumRecords=50&" +
                "recordSchema=om&" +
                "startPage=1";
        Assert.assertEquals(expectedRequestUrl, actualRequestUrl);
    }

}