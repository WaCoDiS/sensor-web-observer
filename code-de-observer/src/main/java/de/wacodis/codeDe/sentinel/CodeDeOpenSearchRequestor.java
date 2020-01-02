package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import java.util.List;

public class CodeDeOpenSearchRequestor {

    private final XPath xpath = null;


    final static Logger LOG = LoggerFactory.getLogger(CodeDeOpenSearchRequestor.class);

    public static List<CodeDeProductsMetadata> request(CodeDeRequestParams params) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        List<CodeDeProductsMetadata> resultMetadata = new ArrayList<CodeDeProductsMetadata>();

        LOG.debug("Start building connection parameters for GET-request");
        String getRequestUrl = null;
        LOG.debug("Start GET-request");
        InputStream getResponse = sendOpenSearchRequest(getRequestUrl);
        LOG.debug("Analyze InputStream");

        // create xml-Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        dbf.setNamespaceAware(true);
        Document getResponseDoc = docBuilder.parse(getResponse);

        String xPathString="/feed/entry";
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expression = xpath.compile(xPathString);
        NodeList nodeList = (NodeList)expression.evaluate(getResponseDoc, XPathConstants.NODESET);

        List<CodeDeProductsMetadata> productsMetadata = new ArrayList<CodeDeProductsMetadata>();
        // analyze xml-Document
        CodeDeResponseResolver resolver = new CodeDeResponseResolver();
        for(int i = 0; i < nodeList.getLength(); i++){
           CodeDeProductsMetadata metadataObject = new CodeDeProductsMetadata();
           Node node = nodeList.item(i);
           String downloadLink = resolver.getDownloadLink(node);
           //String metadataLink = resolver.getMetaDataLinks(node);
           //getResponse = sendOpenSearchRequest(metadataLink);
        }


        //List<String> metadataLinks = resolver.getMetaDataLinks(getResponseDoc);
        // request links
        /*
        for (int i=0; i<metadataLinks.size(); i++) {
            CodeDeProductsMetadata metadata = new CodeDeProductsMetadata();
            getResponse = sendOpenSearchRequest(metadataLinks.get(i));
            Document getMetadataDoc = docBuilder.parse(getResponse);
            float cloudCoverage = resolver.getCloudCoverage(getMetadataDoc);
            metadata.setCloudCover(cloudCoverage);
            resultMetadata.add(metadata);
        }
        */

        return resultMetadata;
    }

    public static InputStream sendOpenSearchRequest(String getRequestUrl) throws ClientProtocolException, IOException {

        // contact http-client
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(getRequestUrl);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity(); // fill http-Object (status, parameters, content)
        InputStream httpcontent = entity.getContent(); // ask for content
        return httpcontent;
    }
}
