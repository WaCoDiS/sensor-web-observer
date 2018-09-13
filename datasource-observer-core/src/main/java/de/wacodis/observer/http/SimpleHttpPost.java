package de.wacodis.observer.http;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SimpleHttpPost implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(SimpleHttpPost.class);

	private String contentType;
	private String accept;
	
	public SimpleHttpPost(String contentType, String accept) {
		this.contentType = contentType;
		this.accept = accept;
	}
	
	public String doPost(String url, String payload) {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", contentType);
		headers.set("Accept", accept);
		HttpEntity<String> requestBody = new HttpEntity<>(payload, headers);
		ResponseEntity<String> responseEntity = rest.postForEntity(url, requestBody, String.class);
		
		log.info("Response from {}:\n{}", url, responseEntity.getBody());
		return responseEntity.getBody();
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}
	
	

}
