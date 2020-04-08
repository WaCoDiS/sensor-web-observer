package de.wacodis.codede.sentinel;

import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class CodeDeOpenSearchRequestorIT {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static CodeDeRequestParams params;
    private static CodeDeRequestor requestor;

    @BeforeAll
    public static void setUp() throws Exception {

        requestor = new CodeDeRequestor();
        requestor.afterPropertiesSet();
        requestor.setHttpClient(HttpClients.createDefault());

//        params = new CodeDeRequestParams("Sentinel2",
//                DateTime.parse("2020-01-01T00:00:00Z", FORMATTER),
//                DateTime.parse("2020-02-01T00:00:00Z", FORMATTER),
//                new Float[]{0.0f, 50.0f});

//        params.setBbox(new ArrayList<Float>() {
//            {
//                add(7.023582486435771f);
//                add(50.94074249267579f);
//                add(7.555961664766073f);
//                add(51.29734038608149f);
//            }
//        });

    }

    @Test
    public void request() throws Exception {
        // expected metadata object
        CodeDeProductsMetadata expectedMetadataObject = new CodeDeProductsMetadata();
        expectedMetadataObject.setDownloadLink("https://code-de.org/download/S2B_MSIL2A_20200113T104309_N0213_R008_T31UGT_20200113T112959.SAFE.zip");
        expectedMetadataObject.setCloudCover(45.389987000000005f);
        expectedMetadataObject.setDatasetId("EOP:CODE-DE:S2_MSI_L2A:/S2B_MSIL2A_20200113T104309_N0213_R008_T31UGT_20200113T112959");
        expectedMetadataObject.setStartDate(DateTime.parse("2020-01-13T10:43:09.025Z", CodeDeResponseResolver.FORMATTER));
        expectedMetadataObject.setEndDate(DateTime.parse("2020-01-13T10:43:09.025Z", CodeDeResponseResolver.FORMATTER));
        expectedMetadataObject.setAreaOfInterest(Arrays.asList(51.27892518278613f, 5.870215764132042f, 52.31403780479302f, 7.539979680877824f));

        List<CodeDeProductsMetadata> metadataList = requestor.request(params);
        CodeDeProductsMetadata actualMetadataObject = metadataList.get(0);

        Assertions.assertEquals(expectedMetadataObject.getDownloadLink(), actualMetadataObject.getDownloadLink());
        Assertions.assertEquals(expectedMetadataObject.getCloudCover(), actualMetadataObject.getCloudCover(), 0.001);
        Assertions.assertEquals(expectedMetadataObject.getDatasetId(), actualMetadataObject.getDatasetId());
        Assertions.assertEquals(expectedMetadataObject.getStartDate(), actualMetadataObject.getStartDate());
        Assertions.assertEquals(expectedMetadataObject.getEndDate(), actualMetadataObject.getEndDate());
        Assertions.assertEquals(expectedMetadataObject.getAreaOfInterest(), actualMetadataObject.getAreaOfInterest());
    }

}