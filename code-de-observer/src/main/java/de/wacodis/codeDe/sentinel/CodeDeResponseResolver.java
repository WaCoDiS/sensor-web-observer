package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import de.wacodis.sentinel.apihub.decode.SimpleNamespaceContext;

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

public class CodeDeResponseResolver {

    private static final String ENTRY_TAG = "entry";
    private static final String LINK_TAG = "link";
    private static final String TITLE_ATTRIBUTE = "title";
    private static final String TITLE_VALUE = "Download";
    private static final String HYPER_REFERENCE = "href";
    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);
    private final XPath xpath;

    public CodeDeResponseResolver(){
        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
    }

    public List<String> getDownloadLink(Document xmlDoc) throws ParserConfigurationException, XPathExpressionException {
        LOG.debug("Resolve TypeName out of GetCapabilities Document");
        List<String> donwloadLinks = new ArrayList<String>();
        String xPathString="/feed/entry/link[@title=\"Download\"]/@href";
        XPathExpression expression = this.xpath.compile(xPathString);
        NodeList result = (NodeList)expression.evaluate(xmlDoc, XPathConstants.NODESET);
        NodeList downloadLinkNodes = (NodeList) result;

        for (int i = 0; i < downloadLinkNodes.getLength(); i++) {
            Node node = downloadLinkNodes.item(i);
            donwloadLinks.add(node.getNodeValue());
        }

            return donwloadLinks;
    }

    public List<String> getMetaDataLinks(Document xmlDoc) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        // request metadatalinks
        List<String> metadataLinks = new ArrayList<String>();
        String xPathStringMetadata="/feed/entry/link[@title=\"O&M 1.1 metadata\"]/@href";
        XPathExpression expressionMetadata = this.xpath.compile(xPathStringMetadata);
        NodeList resultMetadata = (NodeList)expressionMetadata.evaluate(xmlDoc, XPathConstants.NODESET);
        NodeList metadataLinkNodes = (NodeList) resultMetadata;
        for (int i = 0; i < metadataLinkNodes.getLength(); i++) {
            String metadataLink = metadataLinkNodes.item(i).getNodeValue();
            metadataLinks.add(metadataLink.replace("httpAccept=application/gml+xml&", ""));

        }

        return metadataLinks;
    }


    public float getCloudCoverage(Document xmlDoc) throws XPathExpressionException {

        Map<String, String> prefMap = new HashMap<String, String>(){
            {
                put("opt", "http://www.opengis.net/opt/2.1");
                put("om", "http://www.opengis.net/om/2.0");
            }
        };
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);

        String xPathStringCloudCoverage="/feed/entry/opt:EarthObservation/om:result/opt:EarthObservationResult/opt:cloudCoverPercentage";
        XPathExpression expressionCloudCoverage = this.xpath.compile(xPathStringCloudCoverage);
        String resultCloudCoverage = (String)expressionCloudCoverage.evaluate(xmlDoc, XPathConstants.STRING);
        float cloudCoverage = Float.parseFloat(resultCloudCoverage);
        return cloudCoverage;
    }




    public String getParentIdentifier(Document xmlDoc){
        String parentIdentifier = null;
        return parentIdentifier;
    }

    public List<DateTime> getTimeFrame(Document xmlDoc) {
        List<DateTime> timeFrame = new ArrayList<DateTime>();
        return timeFrame;
    }

    public List<Float> getBbox(Document xmlDoc) {
        List<Float> bbox = new ArrayList<Float>();
        return bbox;
    }
}
