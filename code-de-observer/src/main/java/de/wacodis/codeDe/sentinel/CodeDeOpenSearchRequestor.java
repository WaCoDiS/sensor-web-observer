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

import java.io.IOException;
import java.io.InputStream;

public class CodeDeOpenSearchRequestor {

    public static final String SERVICE_URL = "https://catalog.code-de.org/opensearch/request/?";
    public static final String HTTP_ACCEPT = "httpAccept=application/atom%2Bxml";
    public static final String PARENT_IDENTIFIER_PREFIX = "httpAccept=application/atom%2Bxml";

    final static Logger LOG = LoggerFactory.getLogger(CodeDeOpenSearchRequestor.class);

    public static CodeDeProductsMetadata request(CodeDeRequestParams params) throws IOException {
        String getRequestUrl = null;
        InputStream getResponse = sendOpenSearchRequest(getRequestUrl);
        CodeDeProductsMetadata metadata = new CodeDeProductsMetadata();
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

    public String buildGetRequestUrl(CodeDeRequestParams params){
        // parentIdentifier
        String parentIdentifier = CodeDeJob.PARENT_IDENTIFIER_KEY + "=" + params.getParentIdentifier();
        // dates
        String startDate = CodeDeJob.START_DATE_KEY + "=" + params.getStartDate().toString();
        String endDate = CodeDeJob.END_DATE_KEY + "=" + params.getEndDate().toString();
        // bbox
        String bboxContent = params.getBbox().toString();
        bboxContent.replace("[", "");
        bboxContent.replace("]", "");
        bboxContent.replace(" ", "");
        String bbox = CodeDeJob.BBOX_KEY + "=" + bboxContent;
        // cloud coverage
        String cloudCoverContent = params.getCloudCover().toString();
        cloudCoverContent.replace(" ", "");
        String cloudCover = CodeDeJob.CLOUD_COVER_KEY + cloudCoverContent;
        // put together
        String getRequestUrl = SERVICE_URL
                + HTTP_ACCEPT + "&"
                + parentIdentifier + "&"
                + startDate + "&"
                + endDate + "&"
                + bbox + "&"
                + cloudCover;
        return getRequestUrl;
    }
}
