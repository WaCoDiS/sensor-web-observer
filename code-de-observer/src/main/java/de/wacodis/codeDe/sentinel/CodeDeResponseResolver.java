package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import de.wacodis.sentinel.apihub.decode.SimpleNamespaceContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
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
    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);
    private final XPath xpath;
    public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public CodeDeResponseResolver(){
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
            }
        };
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);
    }

    /**
     * Delivers the Downloadlink of one specfic sentinel product (<entry>-Tag)
     *
     * @param entryNode One spezific sentinel Product which corresponds to an <entry>-Tag
     * @return URL as String
     * @throws XPathExpressionException
     */
    public String getDownloadLink(Document entryNode) throws XPathExpressionException {
        LOG.debug("Resolve DownloadLink out of one entry Node of the OpenSearch Response Document");
        String xPathString="/a:entry/a:link[@title=\"Download\"]/@href";
        XPathExpression expression = this.xpath.compile(xPathString);
        String downloadLink = (String) expression.evaluate(entryNode, XPathConstants.STRING);
        return downloadLink;
    }

    /**
     * Delivers the URL which links to a seperate XML-Document which contains the metadata of one specfic sentinel product
     *
     * @param entryNode <entry>-Tag of the OpenSearch Response and consists one sentinel product
     * @return URL as String
     * @throws XPathExpressionException
     */
    public String getMetaDataLink(Document entryNode) throws XPathExpressionException {
        LOG.debug("Resolve MetadataLink out of one entry Node the OpenSearch Response Document");
        String xPathString="/a:entry/a:link[@title=\"O&M 1.1 metadata\"]/@href";
        XPathExpression expression = this.xpath.compile(xPathString);
        String metadataLink = (String) expression.evaluate(entryNode, XPathConstants.STRING);
        return metadataLink;
    }

    /**
     * Returns the cloud coverage percentage of the product
     *
     * @param xmlDoc the xml document which contains the metadata of one sentinel product
     * @return float number (percentage)
     * @throws XPathExpressionException
     */
    public float getCloudCoverage(Document xmlDoc) throws XPathExpressionException {
        String xPathStringCloudCoverage="/a:feed/a:entry/opt:EarthObservation/om:result/opt:EarthObservationResult/opt:cloudCoverPercentage";
        XPathExpression expressionCloudCoverage = this.xpath.compile(xPathStringCloudCoverage);
        String resultCloudCoverage = (String)expressionCloudCoverage.evaluate(xmlDoc, XPathConstants.STRING);
        float cloudCoverage = Float.parseFloat(resultCloudCoverage);
        return cloudCoverage;
    }


    /**
     *  Returns the identifier of the sentinel layer
     *
     * @param xmlDoc the xml document which contains the metadata of one sentinel product
     * @return identifier of a sentinel layer
     */
    public String getParentIdentifier(Document xmlDoc){
        String parentIdentifier = null;
        return parentIdentifier;
    }

    /**
     * Returns the Date of recording as a list of two dates which are the same
     *
     * @param entryNode One specific sentinel Product which corresponds to an <entry>-Tag
     * @return DateTime list which contains the start and enddate
     * @throws XPathExpressionException
     */
    public List<DateTime> getTimeFrame(Document entryNode) throws XPathExpressionException {
        List<DateTime> result = new ArrayList<DateTime>();
        String xPathString="/a:entry/dc:date";
        XPathExpression expression= this.xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(entryNode, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] timeFrame = nodeList.item(i).getTextContent().split("/");
            for(int k = 0; k < timeFrame.length; k++){
                DateTime date = DateTime.parse(timeFrame[k], FORMATTER);
                result.add(date);
            }
        }

        return result;
    }

    /**
     * Returns the Bounding Box
     *
     * @param  entryNode One specific sentinel Product which corresponds to an <entry>-Tag
     * @return Bounding Box of the sentinel product - Schema [minLat, minLon, maxLat, maxLon]
     * @throws XPathExpressionException
     */
    public List<Float> getBbox(Document entryNode) throws XPathExpressionException {
        ArrayList<Float> bbox= new ArrayList<Float>();
        String xPathString="/a:entry/georss:box";
        XPathExpression expression= this.xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(entryNode, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            String[] bboxCoordinates = nodeList.item(i).getTextContent().split(" ");
            for(int k = 0; k < bboxCoordinates.length; k++){
                bbox.add(Float.parseFloat(bboxCoordinates[k]));
            }
        }

        return bbox;
    }
}
