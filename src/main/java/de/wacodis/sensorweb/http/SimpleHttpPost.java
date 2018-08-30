package de.wacodis.sensorweb.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SimpleHttpPost implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpPost.class.getName());
	
	public String doPost(String url, String payload) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/soap+xml");
		headers.set("Accept", "application/soap+xml");
		HttpEntity<String> requestBody = new HttpEntity<>(payload, headers);
		ResponseEntity<String> responseEntity = rest.postForEntity(url, requestBody, String.class);
		                        
		return responseEntity.getBody();
	}

	private String readResponse(HttpURLConnection con) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		LOG.info(response.toString());
		return response.toString();
	}

}
