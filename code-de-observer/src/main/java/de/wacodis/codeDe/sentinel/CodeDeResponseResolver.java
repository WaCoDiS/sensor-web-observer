package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
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
        /*Map<String, String> prefMap = new HashMap<String, String>() {
            {
                put("os", "http://a9.com/-/spec/opensearch/1.1/");
                put("a", "http://www.w3.org/2005/Atom");
            }
        };*/
        //SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        //xpath.setNamespaceContext(namespaces);
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

    public List<Byte> getCloudCover(Document xmlDoc) {
        List<Byte> cloudCover = new ArrayList<Byte>();
        return cloudCover;
    }
}
