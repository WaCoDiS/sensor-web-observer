package de.wacodis.sensorweb.encode;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.n52.svalbard.encode.exception.EncodingException;

import com.google.common.collect.Lists;

import de.wacodis.sensorweb.encode.GetDataAvailabilityReqEncoder;
import de.wacodis.sensorweb.util.SimpleFileReader;

public class GetDataAvailabilityReqEncoderTest {

	@Test
	public void encodeShouldBuildValidGetDataAvailabilityRequest() {
		// parameters to identify data
		List<String> procedures = Lists.newArrayList("Einzelwert");
		List<String> observedProperties = Lists.newArrayList("Wassertemperatur");
		List<String> offerings = Lists.newArrayList("Zeitreihen_Einzelwert");
		List<String> featureIdentifiers = Lists.newArrayList("Laaken");
	
		GetDataAvailabilityReqEncoder encoder = new GetDataAvailabilityReqEncoder();
		String encoded = "";
		String assertion = "";
		try {
			assertion = SimpleFileReader.readFile("src/test/resources/GetDataAvailabilityRequest.txt")/*.replaceAll("  ", "")*/;
			encoded = encoder.encode(procedures, observedProperties, offerings, featureIdentifiers)/*.replaceAll("\n", "")*/;
		} catch (EncodingException e) {
			e.printStackTrace();
		}
		assertEquals(assertion, encoded);		
	}
}
