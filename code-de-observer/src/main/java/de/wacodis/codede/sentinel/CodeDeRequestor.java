package de.wacodis.codede.sentinel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.wacodis.codede.sentinel.exception.HttpConnectionException;
import de.wacodis.codede.sentinel.exception.ParsingException;
import de.wacodis.observer.decode.DecodingException;
import de.wacodis.observer.http.JsonNodeResponseHandler;
import de.wacodis.observer.http.XmlDocResponseHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.print.DocFlavor;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for requesting CODE-DE Orthorectified images of the Sentinal satellites.
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 * @author <a href="mailto:sebastian.drost@hs-bochum.de">Sebastian Drost</a>
 */
@Component
public class CodeDeRequestor implements InitializingBean, DisposableBean {

    private final static Logger LOG = LoggerFactory.getLogger(CodeDeRequestor.class);

    private CloseableHttpClient httpClient;
    private DocumentBuilderFactory factory;
    private CodeDeFinderApiRequestBuilder requestBuilder;

    @Autowired
    public void setRequestBuilder(CodeDeFinderApiRequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    @Autowired
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Performs a query with the given paramerters.
     *
     * @param params all necessary parameters for the OpenSearch request
     * @return metadata for the found satellite images
     * @throws ParsingException
     * @throws HttpConnectionException
     */
    public List<CodeDeProductsMetadata> request(CodeDeRequestParams params) throws ParsingException, HttpConnectionException {
        try {
            CodeDeResponseJsonResolver resolver = new CodeDeResponseJsonResolver();
            int pages = 1;
            List<CodeDeProductsMetadata> metadataList = new ArrayList<>();    // result
            for (int k = 1; k <= pages; k++) {
                LOG.info("Building connection parameters for the " + k + ". GET-request");
                String getRequestUrl = requestBuilder.buildGetRequestUrl(params, k);
                LOG.info("Request CODE-DE API: {}", getRequestUrl);

                JsonNode responseBody = sendOpenSearchRequest(getRequestUrl);

                if (responseBody == null) {
                    throw new ParsingException("Creation of response document failed");
                }

                if (k == 1) {
                    pages = resolver.getNumberOfPages(responseBody);
                }
                ArrayNode features = resolver.resolveFeatures(responseBody);
                features.forEach(f -> {
                    CodeDeProductsMetadata metadata = null;
                    try {
                        metadata = resolver.resolveMetadata(f);
                    } catch (ParsingException ex) {
                        LOG.error("Could not resolve metadata for feature. Cause: {}", ex.getMessage());
                        LOG.debug(String.format("Could not resolve metadata for feature: {}.", f), ex);
                    }
                    metadataList.add(metadata);
                });
            }
            return metadataList;
        } catch (IOException e) {
            throw new HttpConnectionException("Connection to server failed", e);
        }
    }

    /**
     * Delivers the content of the GET response.
     *
     * @param requestUrl string containing the URL of the GET request
     * @return content of the GET response as an Inputstream
     * @throws ClientProtocolException
     * @throws IOException
     */
    private JsonNode sendOpenSearchRequest(String requestUrl) throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(requestUrl);
        return httpClient.execute(httpGet, new JsonNodeResponseHandler());
    }

    @Override
    public void afterPropertiesSet() {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    @Override
    public void destroy() throws Exception {
        httpClient.close();
    }
}