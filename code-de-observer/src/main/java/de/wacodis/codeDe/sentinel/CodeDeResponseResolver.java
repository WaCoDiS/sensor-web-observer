package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import de.wacodis.sentinel.apihub.decode.SimpleNamespaceContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 *
 *  Helper class to resolve required product metadata from the OpenSearch Response
 *
 *
 *@author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 *@author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */
public class CodeDeResponseResolver {

    private static final String ENTRY_TAG = "entry";
    private static final String LINK_TAG = "link";
    private static final String TITLE_ATTRIBUTE = "title";
    private static final String TITLE_VALUE = "Download";
    private static final String HYPER_REFERENCE = "href";
    private static final int ITEMS_PER_PAGE = 50;
    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);
    private final XPath xpath;
    public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    DocumentBuilder db;

    public CodeDeResponseResolver() throws ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();

        // enables namespaces with the xpath-library
        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
        Map<String, String> prefMap = new HashMap<String, String>(){
            {
                // default namespace
                put("a", "http://www.w3.org/2005/Atom");
                // namespaces used in the xml documents
                put("opt", "http://www.opengis.net/opt/2.1");
                put("om", "http://www.opengis.net/om/2.0");
                put("georss", "http://www.georss.org/georss");
                put("dc", "http://purl.org/dc/elements/1.1/");
                put("eop", "http://www.opengis.net/eop/2.1");
                put("os", "http://a9.com/-/spec/opensearch/1.1/");
            }
        };
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);
    }


    /**
     *  Delivers the xml-Document from an InputStream
     * @param getResponse link to the xml document
     * @return xml document
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public Document getDocument(InputStream getResponse) throws IOException, ParserConfigurationException, SAXException {
        LOG.debug("Analyze InputStream");
        return db.parse(getResponse);
    }

    /**
     * Delivers the single entry nodes from the requested sentinel products (all <entry>-tags)
     * @param responseDoc requested document
     * @return entry nodes as NodeList
     * @throws XPathExpressionException
     */
    public NodeList getEntryNodes(Document responseDoc) throws XPathExpressionException {
        String xPathString="/a:feed/a:entry";
        XPathExpression expression = xpath.compile(xPathString);
        return (NodeList) expression.evaluate(responseDoc, XPathConstants.NODESET);
    }

    /**
     * Delivers the Downloadlink of one specific sentinel product (<entry>-Tag)
     *
     * @param entryNode One specific sentinel Product which corresponds to an <entry>-Tag
     * @return URL as String
     * @throws XPathExpressionException
     */
    public String getDownloadLink(Node entryNode) throws XPathExpressionException {
        Document newDocument = db.newDocument();
        Node importedNode = newDocument.importNode(entryNode, true);
        newDocument.appendChild(importedNode);
        String xPathString="/a:entry/a:link[@title=\"Download\"]/@href";
        XPathExpression expression = this.xpath.compile(xPathString);
        String downloadLink = (String) expression.evaluate(newDocument, XPathConstants.STRING);
        return downloadLink;
    }

    /**
     * Returns the cloud coverage percentage of the product
     *
     * @param entryNode the xml document which contains the metadata of one sentinel product
     * @return float number (percentage)
     * @throws XPathExpressionException
     */
    public float getCloudCoverage(Node entryNode) throws XPathExpressionException {
        Document newDocument = db.newDocument();
        Node importedNode = newDocument.importNode(entryNode, true);
        newDocument.appendChild(importedNode);
        String xpathString="/a:entry/opt:EarthObservation/om:result/opt:EarthObservationResult/opt:cloudCoverPercentage";
        XPathExpression expression = this.xpath.compile(xpathString);
        String resultCloudCoverage = (String)expression.evaluate(newDocument, XPathConstants.STRING);
        float cloudCoverage = Float.parseFloat(resultCloudCoverage);
        return cloudCoverage;
    }


    /**
     *  Returns the identifier/datasetID of the sentinel layer
     *
     * @param entryNode the xml document which contains the metadata of one sentinel product
     * @return identifier of a sentinel layer
     */
    public String getIdentifier(Node entryNode) throws XPathExpressionException {
        Document newDocument = db.newDocument();
        Node importedNode = newDocument.importNode(entryNode, true);
        newDocument.appendChild(importedNode);
        String xpathString = "a:entry/opt:EarthObservation/eop:metaDataProperty/eop:EarthObservationMetaData/eop:identifier";
        XPathExpression expression = this.xpath.compile(xpathString);
        String parentIdentifier = (String) expression.evaluate(newDocument, XPathConstants.STRING);
        return parentIdentifier;
    }

    /**
     * Returns the Date of recording as a list of two dates which are equal
     *
     * @param entryNode One specific sentinel Product which corresponds to an <entry>-Tag
     * @return DateTime list which contains the start and enddate
     * @throws XPathExpressionException
     */
    public List<DateTime> getTimeFrame(Node entryNode) throws XPathExpressionException {
        List<DateTime> result = new ArrayList<DateTime>();
        Document newDocument = db.newDocument();
        Node importedNode = newDocument.importNode(entryNode, true);
        newDocument.appendChild(importedNode);
        String xPathString="/a:entry/dc:date";
        XPathExpression expression= this.xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(newDocument, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] timeFrame = nodeList.item(i).getTextContent().split("/");
            for (String s : timeFrame) {
                DateTime date = DateTime.parse(s, FORMATTER);
                result.add(date);
            }
        }

        return result;
    }

    /**
     * Returns the Bounding Box of the product
     *
     * @param  entryNode One specific sentinel Product which corresponds to an <entry>-Tag
     * @return Bounding Box of the sentinel product - Schema [minLat, minLon, maxLat, maxLon]
     * @throws XPathExpressionException
     */
    public List<Float> getBbox(Node entryNode) throws XPathExpressionException {
        ArrayList<Float> bbox= new ArrayList<>();
        Document newDocument = db.newDocument();
        Node importedNode = newDocument.importNode(entryNode, true);
        newDocument.appendChild(importedNode);
        String xPathString="/a:entry/georss:box";
        XPathExpression expression= this.xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(newDocument, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] bboxCoordinates = nodeList.item(i).getTextContent().split(" ");
            for (String bboxCoordinate : bboxCoordinates) {
                bbox.add(Float.parseFloat(bboxCoordinate));
            }
        }

        return bbox;
    }

    /**
     * Delivers the number of pages of the product.
     *
     * @param  responseDoc requested document
     * @return number of pages (int)
     * @throws XPathExpressionException
     */
    public int getNumberOfPages(Document responseDoc) throws XPathExpressionException {
        String xPathString="/a:feed/os:totalResults";
        XPathExpression expression = this.xpath.compile(xPathString);
        int totalResults = (int)((double) expression.evaluate(responseDoc, XPathConstants.NUMBER));
        return numberOfPagesCalculation(totalResults);
    }

    /**
     * Calculates the number of pages. Each page contains 50 <entry>-Tags
     *
     * @param totalResults number of total <entry>-Tags
     * @return number of pages (int)
     */
    private int numberOfPagesCalculation(int totalResults){
        int modulo = totalResults%ITEMS_PER_PAGE;
        int nop = (totalResults-modulo)/ITEMS_PER_PAGE;
        if (modulo > 0)
            nop += 1;
        return nop;
    }
}
