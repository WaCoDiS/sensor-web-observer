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
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CodeDeOpenSearchRequestor {



    final static Logger LOG = LoggerFactory.getLogger(CodeDeOpenSearchRequestor.class);

    public static CodeDeProductsMetadata request(CodeDeRequestParams params) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        LOG.debug("Start building connection parameters for GET-request");
        String getRequestUrl = null;
        LOG.debug("Start GET-request");
        InputStream getResponse = sendOpenSearchRequest(getRequestUrl);
        LOG.debug("Analyze InputStream");

        // create xml-Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        Document getResponseDoc = docBuilder.parse(getResponse);

        // analyze xml-Document
        CodeDeResponseResolver resolver = new CodeDeResponseResolver();
        List<String> downloadLinks = resolver.getDownloadLink(getResponseDoc);

        CodeDeProductsMetadata metadata = new CodeDeProductsMetadata();
        //metadata.setParentIdentifier();



        return metadata;
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
