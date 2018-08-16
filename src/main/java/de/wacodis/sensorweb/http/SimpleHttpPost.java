package de.wacodis.sensorweb.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpPost {
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpPost.class.getName());
	
	public String doPost(String url, String payload) {
		try {
			URL urlObj = new URL(url);
			HttpURLConnection con;
			con = (HttpURLConnection) urlObj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/soap+xml");
			
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(payload);
			wr.flush();
			wr.close();
			
			int responseCode = con.getResponseCode();
			
			LOG.info("Sending 'POST' request to URL: " + url);
			LOG.info("Response Code: " + responseCode + " " + con.getResponseMessage());
			
			String response = readResponse(con);
			
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Error occured!";
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
