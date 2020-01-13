package de.wacodis.codeDe.sentinel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CodeDeOpenSearchRequestorIT {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static CodeDeRequestParams params;

    @BeforeAll
    static void setUp() {
        params = new CodeDeRequestParams();
        params.setParentIdentifier("S2_MSI_L2A");
        params.setStartDate(DateTime.parse("2019-10-01T00:00:00Z", FORMATTER));
        params.setEndDate(DateTime.parse("2019-10-31T00:00:00Z", FORMATTER));
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
    void request() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        // expected metadata object
        CodeDeProductsMetadata expectedMetadataObject = new CodeDeProductsMetadata();
        expectedMetadataObject.setDownloadLink("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T32ULB_20191012T135838.SAFE.zip");
        expectedMetadataObject.setCloudCover(29.141719f);
        expectedMetadataObject.setDatasetId("EOP:CODE-DE:S2_MSI_L2A:/S2B_MSIL2A_20191012T103029_N0213_R108_T32ULB_20191012T135838");
        expectedMetadataObject.setStartDate(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
        expectedMetadataObject.setEndDate(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
        expectedMetadataObject.setBbox(50.4368368428495f, 6.589443020581445f, 51.44399790803582f, 7.729300891794365f );

        List<CodeDeProductsMetadata> metadataList = CodeDeOpenSearchRequestor.request(params);
        CodeDeProductsMetadata actualMetadataObject = metadataList.get(0);

        Assertions.assertEquals(expectedMetadataObject.getDownloadLink(), actualMetadataObject.getDownloadLink());
        Assertions.assertEquals(expectedMetadataObject.getCloudCover(), actualMetadataObject.getCloudCover());
        Assertions.assertEquals(expectedMetadataObject.getDatasetId(), actualMetadataObject.getDatasetId());
        Assertions.assertEquals(expectedMetadataObject.getStartDate(), actualMetadataObject.getStartDate());
        Assertions.assertEquals(expectedMetadataObject.getEndDate(), actualMetadataObject.getEndDate());
        Assertions.assertEquals(expectedMetadataObject.getAreaOfInterest(), actualMetadataObject.getAreaOfInterest());
    }


}