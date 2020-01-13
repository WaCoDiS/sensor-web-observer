package de.wacodis.codeDe.sentinel;

import de.wacodis.sentinel.apihub.decode.SimpleNamespaceContext;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
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
 * This class is responsible for requesting CODE-DE Orthorectified images of the Sentinal satellites.
 *
 *@author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 *@author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeOpenSearchRequestor {
    


    final static Logger LOG = LoggerFactory.getLogger(CodeDeOpenSearchRequestor.class);

    /**
     * Performs a query with the given paramerters.
     *
     * @param params all necessary parameters for the OpenSearch request
     * @return metadata for the found satellite images
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */

    public static List<CodeDeProductsMetadata> request(CodeDeRequestParams params) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {


        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath = factory.newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Map<String, String> prefMap = new HashMap<String, String>(){
            {
                put("a", "http://www.w3.org/2005/Atom");
            }
        };
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefMap);
        xpath.setNamespaceContext(namespaces);


        LOG.debug("Start building connection parameters for GET-request");
        String getRequestUrl = CodeDeOpenSearchRequestorBuilder.buildGetRequestUrl(params);
        LOG.debug("Start GET-request");
        Document getResponseDoc = getDocument(getRequestUrl);
        String xPathString="/a:feed/a:entry";
        XPathExpression expression = xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(getResponseDoc, XPathConstants.NODESET);

        // prepare loop
        List<CodeDeProductsMetadata> productsMetadata = new ArrayList<CodeDeProductsMetadata>();    // result

        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        // analyze xml-Document
        CodeDeResponseResolver resolver = new CodeDeResponseResolver();
        for(int i = 0; i < nodeList.getLength(); i++){
           CodeDeProductsMetadata metadataObject = new CodeDeProductsMetadata();
           Node node = nodeList.item(i);
           Document newDocument = docBuilder.newDocument();
           Node importedNode = newDocument.importNode(node, true);
           newDocument.appendChild(importedNode);
           String downloadLink = resolver.getDownloadLink(newDocument);
           String metadataLink = resolver.getMetaDataLink(newDocument);
           Document metadataDocument = getDocument(metadataLink);
           float cloudCoverage = resolver.getCloudCoverage(metadataDocument);
           String identifier = resolver.getIdentifier(metadataDocument);
           List<DateTime> timeFrame = resolver.getTimeFrame(newDocument);
           List<Float> bbox = resolver.getBbox(newDocument);

           metadataObject.setDownloadLink(downloadLink);
           metadataObject.setCloudCover(cloudCoverage);
           metadataObject.setDatasetId(identifier);
           metadataObject.setStartDate(timeFrame.get(0));
           metadataObject.setEndDate(timeFrame.get(1));
           metadataObject.setBbox(bbox.get(0), bbox.get(1), bbox.get(2), bbox.get(3));

           productsMetadata.add(metadataObject);
        }

        return productsMetadata;
    }

    /**
     *  Delivers the content of the GET response.
     * @param getRequestUrl string containing the URL of the GET request
     * @return content of the GET response as an Inputstream
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static InputStream sendOpenSearchRequest(String getRequestUrl) throws ClientProtocolException, IOException {

        // contact http-client
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(getRequestUrl);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity(); // fill http-Object (status, parameters, content)
        InputStream httpcontent = entity.getContent(); // ask for content
        return httpcontent;
    }

    /**
     *  Delivers the xml-Document from an url
     * @param url link to the xml document
     * @return xml document
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document getDocument(String url) throws IOException, ParserConfigurationException, SAXException {
        InputStream getResponse = sendOpenSearchRequest(url);
        LOG.debug("Analyze InputStream");

        // create xml-Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        dbf.setNamespaceAware(true);

        return docBuilder.parse(getResponse);
    }

}