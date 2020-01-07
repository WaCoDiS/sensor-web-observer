package de.wacodis.codeDe.sentinel;

import de.wacodis.sentinel.apihub.decode.SimpleNamespaceContext;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CodeDeResponseResolverTest {
    
    private static ArrayList<String> expectedDownloadLinks;
    private static CodeDeResponseResolver resolver;
    private static Document xmlDoc;
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder db;
    private static XPath xpath;

    @BeforeAll
    static void setup() throws ParserConfigurationException {

        resolver = new CodeDeResponseResolver();
        dbf.setNamespaceAware(true);
        //dbf.setCoalescing(true);
        db = dbf.newDocumentBuilder();
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        Map<String, String> prefMap = new HashMap<String, String>(){
            {
                put("a", "http://www.w3.org/2005/Atom");
            }
        };
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);


    }
    @Test
    void testGetDownloadLink() throws IOException, SAXException, XPathExpressionException {

        // expected download links
        expectedDownloadLinks = new ArrayList<>();
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T32ULB_20191012T135838.SAFE.zip");
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T32UMB_20191012T135838.SAFE.zip");
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T31UGS_20191012T135838.SAFE.zip");

        // actual downloadLinks
        InputStream openSearchResponseStream = this.getClass().getResourceAsStream("/catalog.code-de.org.xml");
        xmlDoc = db.parse(openSearchResponseStream);
        String xPathString="/a:feed/a:entry";
        XPathExpression expression = xpath.compile(xPathString);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDoc, XPathConstants.NODESET);

        List<String> actualDownloadLinks = new ArrayList<>();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            Document newDocument = db.newDocument();
            Node importedNode = newDocument.importNode(node, true);
            newDocument.appendChild(importedNode);
            String downloadLink = resolver.getDownloadLink(newDocument);
            actualDownloadLinks.add(downloadLink);
        }
        Assertions.assertEquals(expectedDownloadLinks, actualDownloadLinks);
    }

    @Test
    void testGetMetadataLink() throws IOException, SAXException, XPathExpressionException {

        // expected download links
        expectedDownloadLinks = new ArrayList<>();
        expectedDownloadLinks.add("https://catalog.code-de.org/opensearch/request/?httpAccept=application/gml%2Bxml&amp;parentIdentifier=EOP:CODE-DE:S2_MSI_L2A&amp;uid=EOP:CODE-DE:S2_MSI_L2A:/S2B_MSIL2A_20191012T103029_N0213_R108_T32ULB_20191012T135838&amp;recordSchema=om");
        expectedDownloadLinks.add("https://catalog.code-de.org/opensearch/request/?httpAccept=application/gml%2Bxml&amp;parentIdentifier=EOP:CODE-DE:S2_MSI_L2A&amp;uid=EOP:CODE-DE:S2_MSI_L2A:/S2B_MSIL2A_20191012T103029_N0213_R108_T32UMB_20191012T135838&amp;recordSchema=om");
        expectedDownloadLinks.add("https://catalog.code-de.org/opensearch/request/?httpAccept=application/gml%2Bxml&amp;parentIdentifier=EOP:CODE-DE:S2_MSI_L2A&amp;uid=EOP:CODE-DE:S2_MSI_L2A:/S2B_MSIL2A_20191012T103029_N0213_R108_T31UGS_20191012T135838&amp;recordSchema=om");

        // actual metadataLinks
        InputStream openSearchResponseStream = this.getClass().getResourceAsStream("/catalog.code-de.org.xml");
        xmlDoc = db.parse(openSearchResponseStream);
        String xPathString="/a:feed/a:entry";
        XPathExpression expression = xpath.compile(xPathString);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDoc, XPathConstants.NODESET);

        List<String> actualMetadataLinks = new ArrayList<>();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            Document newDocument = db.newDocument();
            Node importedNode = newDocument.importNode(node, true);
            newDocument.appendChild(importedNode);
            String metadataLink = resolver.getMetaDataLink(newDocument);
            String metadataLinkModified = metadataLink.replaceAll("&", "&amp;");
            actualMetadataLinks.add(metadataLinkModified);

        }
        Assertions.assertEquals(expectedDownloadLinks, actualMetadataLinks);

    }

    @Test
    void testGetCloudCoverage() throws IOException, SAXException, XPathExpressionException {
        // expected cloud coverage
        float expectedCloudCoverage = 29.141719f;
        // actual cloud coverage
        InputStream cloudCoverageDoc1 = this.getClass().getResourceAsStream("/metadata_1_picture.xml");
        xmlDoc = db.parse(cloudCoverageDoc1);
        resolver = new CodeDeResponseResolver();
        float actualCloudCoverage = resolver.getCloudCoverage(xmlDoc);

        Assertions.assertEquals(expectedCloudCoverage, actualCloudCoverage);
    }

    @Test
    void testGetParentIdentifier() throws IOException, SAXException, XPathExpressionException {
        // expected parent identifier
        String expectedParentIdentifier = "EOP:CODE-DE:S2_MSI_L2A";
        // actual parent identifier
        InputStream parentIdentifierDoc1 = this.getClass().getResourceAsStream("/metadata_1_picture.xml");
        xmlDoc = db.parse(parentIdentifierDoc1);
        resolver = new CodeDeResponseResolver();
        String actualParentIdentifier = resolver.getParentIdentifier(xmlDoc);

        Assertions.assertEquals(expectedParentIdentifier, actualParentIdentifier);
    }

    @Test
    void testGetTimeFrame() throws IOException, SAXException, XPathExpressionException {

        // expected cloud coverage
        List<List<DateTime>> expectedTimeFrames = new ArrayList<>();
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
        InputStream document = this.getClass().getResourceAsStream("/catalog.code-de.org.xml");
        xmlDoc = db.parse(document);
        String xPathString="/a:feed/a:entry";
        XPathExpression expression = xpath.compile(xPathString);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDoc, XPathConstants.NODESET);

        List<List<DateTime>> actualTimeFrames = new ArrayList<>();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            Document newDocument = db.newDocument();
            Node importedNode = newDocument.importNode(node, true);
            newDocument.appendChild(importedNode);
            List<DateTime> timeFrame = resolver.getTimeFrame(newDocument);
            actualTimeFrames.add(timeFrame);
        }

        Assertions.assertEquals(expectedTimeFrames, actualTimeFrames);
    }

    @Test
    void testGetBbox() throws IOException, SAXException, XPathExpressionException {

        // expected cloud coverage
        List<List<Float>> expectedbbox = new ArrayList<>();
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
        InputStream bboxDoc1 = this.getClass().getResourceAsStream("/catalog.code-de.org.xml");
        xmlDoc = db.parse(bboxDoc1);
        String xPathString="/a:feed/a:entry";
        XPathExpression expression = xpath.compile(xPathString);
        NodeList nodeList = (NodeList) expression.evaluate(xmlDoc, XPathConstants.NODESET);

        List<List<Float>> actualBbox = new ArrayList<>();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            Document newDocument = db.newDocument();
            Node importedNode = newDocument.importNode(node, true);
            newDocument.appendChild(importedNode);
            List<Float> bbox = resolver.getBbox(newDocument);
            actualBbox.add(bbox);
        }

        Assertions.assertEquals(expectedbbox, actualBbox);
    }

}