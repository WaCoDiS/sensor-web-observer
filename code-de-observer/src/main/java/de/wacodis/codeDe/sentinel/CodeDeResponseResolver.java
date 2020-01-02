package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import de.wacodis.sentinel.apihub.decode.SimpleNamespaceContext;

import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.*;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeDeResponseResolver {

    private static final String ENTRY_TAG = "entry";
    private static final String LINK_TAG = "link";
    private static final String TITLE_ATTRIBUTE = "title";
    private static final String TITLE_VALUE = "Download";
    private static final String HYPER_REFERENCE = "href";
    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);
    private final XPath xpath;
    public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public CodeDeResponseResolver(){
        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
        Map<String, String> prefMap = new HashMap<String, String>(){
            {
                put("a", "http://www.w3.org/2005/Atom");
                put("opt", "http://www.opengis.net/opt/2.1");
                put("om", "http://www.opengis.net/om/2.0");
                put("georss", "http://www.georss.org/georss");
                put("dc", "http://purl.org/dc/elements/1.1/");
            }
        };
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);
    }


    public String getDownloadLink(Node entryNode) throws XPathExpressionException {
        /*
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document newDocument = builder.newDocument();
        Node importedNode = newDocument.importNode(entryNode, true);
        newDocument.appendChild(importedNode);

        DOMSource domSource = new DOMSource(newDocument);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        System.out.println("XML IN String format is: \n" + writer.toString());
        */

        LOG.debug("Resolve DownloadLink out of the OpenSearch Response Document");
        String xPathString="/a:link[@title=\"Download\"]/@href";
        XPathExpression expression = this.xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(entryNode, XPathConstants.NODESET);
        String downloadLink = (String) expression.evaluate(entryNode, XPathConstants.STRING);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            downloadLink = node.getTextContent();
        }

        return downloadLink;
    }

    public List<String> getMetaDataLinks(Node entryNode) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        // request metadatalinks
        List<String> metadataLinks = new ArrayList<String>();
        String xPathStringMetadata="/a:link[@title=\"O&M 1.1 metadata\"]/@href";
        XPathExpression expressionMetadata = this.xpath.compile(xPathStringMetadata);
        NodeList nodeList = (NodeList)expressionMetadata.evaluate(entryNode, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String metadataLink = nodeList.item(i).getNodeValue();
            metadataLinks.add(metadataLink.replace("httpAccept=application/gml+xml&", ""));
        }

        return metadataLinks;
    }


    public float getCloudCoverage(Node entryNode) throws XPathExpressionException {
        String xPathStringCloudCoverage="/a:feed/a:entry/opt:EarthObservation/om:result/opt:EarthObservationResult/opt:cloudCoverPercentage";
        XPathExpression expressionCloudCoverage = this.xpath.compile(xPathStringCloudCoverage);
        String resultCloudCoverage = (String)expressionCloudCoverage.evaluate(entryNode, XPathConstants.STRING);
        float cloudCoverage = Float.parseFloat(resultCloudCoverage);
        return cloudCoverage;
    }






    public String getParentIdentifier(Document xmlDoc){
        String parentIdentifier = null;
        return parentIdentifier;
    }

    public List<List<DateTime>> getTimeFrame(Document xmlDoc) throws XPathExpressionException {
        List<List<DateTime>> timeFrames = new ArrayList<List<DateTime>>();
        String xPathString="/a:feed/a:entry/dc:date";
        XPathExpression expression= this.xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(xmlDoc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] timeFrame = nodeList.item(i).getTextContent().split("/");
            List<DateTime> timeStamp = new ArrayList<DateTime>();
            for(int k = 0; k < timeFrame.length; k++){
                DateTime date = DateTime.parse(timeFrame[k], FORMATTER);
                timeStamp.add(date);
            }
            timeFrames.add(timeStamp);
        }
        return timeFrames;
    }

    public List<List<Float>> getBbox(Document xmlDoc) throws XPathExpressionException {
        List<List<Float>> bboxForAll = new ArrayList<List<Float>>();
        String xPathString="/a:feed/a:entry/georss:box";
        XPathExpression expression= this.xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(xmlDoc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] bboxCoordinates = nodeList.item(i).getTextContent().split(" ");
            List<Float> bboxForOne = new ArrayList<Float>();
            for(int k = 0; k < bboxCoordinates.length; k++){
                bboxForOne.add(Float.parseFloat(bboxCoordinates[k]));
            }
            bboxForAll.add(bboxForOne);
        }
        return bboxForAll;
    }
}
