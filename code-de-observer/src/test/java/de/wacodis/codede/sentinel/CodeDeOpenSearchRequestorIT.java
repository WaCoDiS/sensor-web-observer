package de.wacodis.codede.sentinel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CodeDeOpenSearchRequestorIT {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static CodeDeRequestParams params;
    private static CodeDeOpenSearchRequestor requestor;

    @BeforeClass
    public static void setUp() {
        params = new CodeDeRequestParams();
        requestor = new CodeDeOpenSearchRequestor();
        params.setParentIdentifier("S2_MSI_L2A");
        params.setStartDate(DateTime.parse("2019-01-01T00:00:00Z", FORMATTER));
        params.setEndDate(DateTime.parse("2020-01-17T00:00:00Z", FORMATTER));
        params.setBbox(new ArrayList<Float>(){
            {
                add(6.96f);
                add(50.9f);
                add(8.02f);
                add(51.2f);
            }
        });
        params.setCloudCover(new ArrayList<Float>(){            {
                add(0.0f);
                add(40.0f);
            }
        });
    }

    @Test
    public void request() throws Exception {
        // expected metadata object
        CodeDeProductsMetadata expectedMetadataObject = new CodeDeProductsMetadata();
        expectedMetadataObject.setDownloadLink("https://code-de.org/download/S2A_MSIL2A_20190120T103341_N0211_R108_T32ULB_20190120T131644.SAFE.zip");
        expectedMetadataObject.setCloudCover(2.175213f);
        expectedMetadataObject.setDatasetId("EOP:CODE-DE:S2_MSI_L2A:/S2A_MSIL2A_20190120T103341_N0211_R108_T32ULB_20190120T131644");
        expectedMetadataObject.setStartDate(DateTime.parse("2019-01-20T10:33:41.024Z", CodeDeResponseResolver.FORMATTER));
        expectedMetadataObject.setEndDate(DateTime.parse("2019-01-20T10:33:41.024Z", CodeDeResponseResolver.FORMATTER));
        expectedMetadataObject.setBbox(50.43685871745407f, 6.590688202965415f, 51.44399790803582f, 7.729300891794365f );

        List<CodeDeProductsMetadata> metadataList = requestor.request(params);
        CodeDeProductsMetadata actualMetadataObject = metadataList.get(0);

        Assert.assertEquals(expectedMetadataObject.getDownloadLink(), actualMetadataObject.getDownloadLink());
        Assert.assertEquals(expectedMetadataObject.getCloudCover(), actualMetadataObject.getCloudCover(), 0.001);
        Assert.assertEquals(expectedMetadataObject.getDatasetId(), actualMetadataObject.getDatasetId());
        Assert.assertEquals(expectedMetadataObject.getStartDate(), actualMetadataObject.getStartDate());
        Assert.assertEquals(expectedMetadataObject.getEndDate(), actualMetadataObject.getEndDate());
        Assert.assertEquals(expectedMetadataObject.getAreaOfInterest(), actualMetadataObject.getAreaOfInterest());
    }

}