package de.wacodis.codeDe.sentinel;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeDeResponseResolverTest {


    private static ArrayList<String> expectedDownloadLinks;
    private static ArrayList<Float> cloudCoverage;
    private static CodeDeResponseResolver resolver;
    private static Document xmlDoc;
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder db;


    @BeforeAll
    static void setup() throws ParserConfigurationException {

        resolver = new CodeDeResponseResolver();
        db = dbf.newDocumentBuilder();

    }
    @Test
    void testGetDownloadLink() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        // expected download links
        expectedDownloadLinks = new ArrayList<>();
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T32ULB_20191012T135838.SAFE.zip");
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T32UMB_20191012T135838.SAFE.zip");
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T31UGS_20191012T135838.SAFE.zip");

        // actual downloadLinks
        InputStream openSearchResponseStream = this.getClass().getResourceAsStream("/catalog.code-de.org.txt");
        xmlDoc = db.parse(openSearchResponseStream);
        List<String> actualDownloadLinks = resolver.getDownloadLink(xmlDoc);

        Assertions.assertEquals(expectedDownloadLinks, actualDownloadLinks);
    }
    @Test
    void testGetCloudCoverage() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        // expected cloud coverage
        float expectedCloudCoverage = 29.141719f;

        // actual cloud coverage
        InputStream cloudCoverageDoc1 = this.getClass().getResourceAsStream("/metadata_1_picture.txt");
        xmlDoc = db.parse(cloudCoverageDoc1);
        resolver = new CodeDeResponseResolver();
        float actualCloudCoverage = resolver.getCloudCoverage(xmlDoc);

        Assertions.assertEquals(expectedCloudCoverage, actualCloudCoverage);
    }

    @Test
    void testGetBbox() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        // expected cloud coverage
        List<List<Float>> expectedbbox = new ArrayList<List<Float>>();
        List<Float> firstPicture = new ArrayList<Float>(){
            {
                add(50.4368368428495f);
                add(6.589443020581445f);
                add(51.44399790803582f);
                add(7.729300891794365f);
            }
        };
        List<Float> secondPicture = new ArrayList<Float>(){
            {
                add(50.455265339610506f);
                add(7.560547850091258f);
                add(51.451098078258774f);
                add(9.140457957691327f);
            }
        };
        List<Float> thirdPicture = new ArrayList<Float>(){
            {
                add(50.38212225093181f);
                add(6.577303273387585f);
                add(51.38158890366703f);
                add(7.450589699013681f);
            }
        };
        expectedbbox.add(firstPicture);
        expectedbbox.add(secondPicture);
        expectedbbox.add(thirdPicture);
        // actual cloud coverage
        InputStream bboxDoc1 = this.getClass().getResourceAsStream("/catalog.code-de.org.txt");
        xmlDoc = db.parse(bboxDoc1);
        resolver = new CodeDeResponseResolver();
        List<List<Float>> actualBBox = resolver.getBbox(xmlDoc);

        Assertions.assertEquals(expectedbbox, actualBBox);
    }
    @Test
    void testGetTimeFrame() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        // expected cloud coverage
        List<List<DateTime>> expectedTimeFrames= new ArrayList<List<DateTime>>();
        List<DateTime> firstPicture = new ArrayList<DateTime>(){
            {
                add(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
                add(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
            }
        };
        List<DateTime> secondPicture = new ArrayList<DateTime>(){
            {
                add(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
                add(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
            }
        };
        List<DateTime> thirdPicture = new ArrayList<DateTime>(){
            {
                add(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
                add(DateTime.parse("2019-10-12T10:30:29.024Z", CodeDeResponseResolver.FORMATTER));
            }
        };
        expectedTimeFrames.add(firstPicture);
        expectedTimeFrames.add(secondPicture);
        expectedTimeFrames.add(thirdPicture);
        // actual cloud coverage
        InputStream document = this.getClass().getResourceAsStream("/catalog.code-de.org.txt");
        xmlDoc = db.parse(document);
        resolver = new CodeDeResponseResolver();
        List<List<DateTime>> actualTimeFrames = resolver.getTimeFrame(xmlDoc);
        Assertions.assertEquals(expectedTimeFrames, actualTimeFrames);
    }
}