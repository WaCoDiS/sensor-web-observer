package de.wacodis.sensorweb.decode;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse.DataAvailability;
import org.n52.svalbard.decode.exception.DecodingException;

import de.wacodis.sensorweb.decode.GetDataAvailabilityResDecoder;
import de.wacodis.sensorweb.util.SimpleFileReader;

public class GetDataAvailabilityResDecoderTest {

	@Test
	public void decodeShouldGiveValidDateOfLastObservation() {

		String response = SimpleFileReader.readFile("src/test/resources/GetDataAvailabilityResponse.txt");
		String assertion = SimpleFileReader.readFile("src/test/resources/GetDataAvailability.txt");
		
		GetDataAvailabilityResDecoder decoder = new GetDataAvailabilityResDecoder();
		List<DataAvailability> result = null;
		try {
			result = decoder.decode(response);
		} catch (DecodingException | XmlException e) {
			e.printStackTrace();
		}
		assertEquals(assertion, result.get(0).getPhenomenonTime().getEnd().toString());
	}
}
