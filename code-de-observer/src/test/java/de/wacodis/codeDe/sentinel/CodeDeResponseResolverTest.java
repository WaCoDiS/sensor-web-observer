package de.wacodis.codeDe.sentinel;

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
        InputStream cloudCoverageDoc2 = this.getClass().getResourceAsStream("/metadata_2_picture");
        InputStream cloudCoverageDoc3 = this.getClass().getResourceAsStream("/metadata_3_picture");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        xmlDoc = db.parse(cloudCoverageDoc1);
        resolver = new CodeDeResponseResolver();
        float actualCloudCoverage = resolver.getCloudCoverage(xmlDoc);

        Assertions.assertEquals(expectedCloudCoverage, actualCloudCoverage);
    }
}