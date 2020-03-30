package de.wacodis.codede.sentinel;

import de.wacodis.codede.sentinel.exception.HttpConnectionException;
import de.wacodis.observer.decode.DecodingException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for requesting CODE-DE Orthorectified images of the Sentinal satellites.
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */
@Component
public class CodeDeOpenSearchRequestor implements InitializingBean {

    private final static Logger LOG = LoggerFactory.getLogger(CodeDeOpenSearchRequestor.class);

    private CloseableHttpClient httpClient;

    private DocumentBuilderFactory factory;

    @Autowired
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Performs a query with the given paramerters.
     *
     * @param params all necessary parameters for the OpenSearch request
     * @return metadata for the found satellite images
     * @throws DecodingException
     * @throws HttpConnectionException
     */
    public List<CodeDeProductsMetadata> request(CodeDeRequestParams params) throws DecodingException, HttpConnectionException {
        try {
            CodeDeResponseResolver resolver = new CodeDeResponseResolver();
            int pages = 1;
            List<CodeDeProductsMetadata> productsMetadata = new ArrayList<>();    // result
            for (int k = 1; k <= pages; k++) {
                LOG.info("Building connection parameters for the " + k + ". GET-request");
                String getRequestUrl = CodeDeOpenSearchRequestorBuilder.buildGetRequestUrl(params, k);
                LOG.info("Request CODE-DE API: {}", getRequestUrl);

                Document responseDoc = sendOpenSearchRequest(getRequestUrl);
                if (responseDoc == null) {
                    throw new DecodingException("Creation of response document failed");
                } else {
                    LOG.debug("CODE-DE response document: {}", responseDoc.getTextContent());
                }

                if (k == 1) {
                    pages = resolver.getNumberOfPages(responseDoc);
                }
                NodeList nodeList = resolver.getEntryNodes(responseDoc);

                for (int i = 0; i < nodeList.getLength(); i++) {
                    CodeDeProductsMetadata metadataObject = new CodeDeProductsMetadata();
                    Node node = nodeList.item(i);

                    String downloadLink = resolver.getDownloadLink(node);
                    float cloudCoverage = resolver.getCloudCoverage(node);
                    String identifier = resolver.getIdentifier(node);
                    List<DateTime> timeFrame = resolver.getTimeFrame(node);
                    List<Float> bbox = resolver.getBbox(node);

                    metadataObject.setDownloadLink(downloadLink);
                    metadataObject.setCloudCover(cloudCoverage);
                    metadataObject.setDatasetId(identifier);
                    metadataObject.setStartDate(timeFrame.get(0));
                    metadataObject.setEndDate(timeFrame.get(1));
                    metadataObject.setBbox(bbox.get(0), bbox.get(1), bbox.get(2), bbox.get(3));
                    productsMetadata.add(metadataObject);
                }

            }
            return productsMetadata;
        } catch (XPathExpressionException e) {
            throw new DecodingException("Could not process OpenSearch response", e);
        } catch (IOException e) {
            throw new HttpConnectionException("Connection to server failed", e);
        }
    }

    /**
     * Delivers the content of the GET response.
     *
     * @param getRequestUrl string containing the URL of the GET request
     * @return content of the GET response as an Inputstream
     * @throws ClientProtocolException
     * @throws IOException
     */
    private Document sendOpenSearchRequest(String getRequestUrl) throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(getRequestUrl);

        ResponseHandler<Document> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    return builder.parse(entity.getContent());
                } catch (SAXException | ParserConfigurationException ex) {
                    LOG.warn(ex.getMessage());
                    throw new IOException("Could not parse XML document", ex);
                }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
        Document responseDoc = httpClient.execute(httpGet, responseHandler);
        return responseDoc;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }
}