package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class DwdHtmlReader {
	// attributes
	String propUrl;

	// constructor
	public void HtmlReader(String propUrl) {
		this.propUrl = propUrl;
	}

	public InputStream readWebsite() throws ClientProtocolException, IOException {
		
			// contact http-client
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(propUrl);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity(); // fill http-Object (status, parameters, content)
			InputStream httpcontent = entity.getContent(); // ask for content
			return httpcontent;
	}


}
