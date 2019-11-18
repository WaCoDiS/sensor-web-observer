package sentinel;

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
}
