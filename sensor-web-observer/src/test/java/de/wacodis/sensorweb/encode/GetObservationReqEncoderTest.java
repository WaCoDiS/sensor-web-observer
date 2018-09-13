package de.wacodis.sensorweb.encode;

import static org.junit.Assert.*;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.n52.svalbard.encode.exception.EncodingException;

import com.google.common.collect.Lists;

import de.wacodis.sensorweb.encode.GetObservationReqEncoder;
import de.wacodis.sensorweb.util.SimpleFileReader;

public class GetObservationReqEncoderTest {

	@Test
	public void encodeShouldBuildValidGetObservationRequest() {
		// parameters to identify data
		List<String> procedures = Lists.newArrayList("Einzelwert");
		List<String> observedProperties = Lists.newArrayList("Wassertemperatur");
		List<String> offerings = Lists.newArrayList("Zeitreihen_Einzelwert");
		List<String> featureIdentifiers = Lists.newArrayList("Laaken");
		
		DateTime start = new DateTime(2018, 3, 3, 0, 0, 0);
		DateTime end = new DateTime(2018, 3, 3, 12, 0, 0);
		
		
		GetObservationReqEncoder encoder = new GetObservationReqEncoder();
		String encoded = "";
		String assertion = "";
		try {
			assertion = SimpleFileReader.readFile("src/test/resources/GetObservationRequest.txt");
			encoded = encoder.encode(procedures, observedProperties, offerings, featureIdentifiers, start.minusSeconds(1), end.plusSeconds(1));
			
		} catch (EncodingException e) {
			e.printStackTrace();
		}
		assertion = assertion.replaceAll("id=\"tp_[0-9a-f]*\"", "id=\"tp_101010");
		encoded = encoded.replaceAll("id=\"tp_[0-9a-f]*\"", "id=\"tp_101010");
		assertEquals(assertion, encoded);
	}
}

