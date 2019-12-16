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
    private static CodeDeResponseResolver resolver;
    private static InputStream openSearchResponseStream;
    private static Document xmlDoc;

    @BeforeAll
    static void setup()  {
        // expected
        expectedDownloadLinks = new ArrayList<>();
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T32ULB_20191012T135838.SAFE.zip");
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T32UMB_20191012T135838.SAFE.zip");
        expectedDownloadLinks.add("https://code-de.org/download/S2B_MSIL2A_20191012T103029_N0213_R108_T31UGS_20191012T135838.SAFE.zip");

    }
    @Test
    void test() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        // actual
        openSearchResponseStream = this.getClass().getResourceAsStream("/catalog.code-de.org.txt");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        xmlDoc = db.parse(openSearchResponseStream);
        resolver = new CodeDeResponseResolver();
        List<String> actualDownloadLinks = resolver.getDownloadLink(xmlDoc);

        Assertions.assertEquals(expectedDownloadLinks, actualDownloadLinks);
    }
}