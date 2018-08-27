package de.wacodis.sensorweb.decode;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.xmlbeans.XmlException;

import org.junit.Test;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.svalbard.decode.exception.DecodingException;

import de.wacodis.sensorweb.decode.GetObservationResDecoder;
import de.wacodis.sensorweb.util.SimpleFileReader;

public class GetObservationResDecoderTest {

	@Test
	public void decodeShouldGiveValidDataOfLastObservation() {

		String response = SimpleFileReader.readFile("src/test/resources/GetObservationResponse.txt");
		String assertion = SimpleFileReader.readFile("src/test/resources/GetObservation.txt");
		
		String[] obs = assertion.split(",");		
		
		GetObservationResDecoder decoder = new GetObservationResDecoder();
		List<OmObservation> result = null;
		
		try {
			result = decoder.decode(response);
			
			TimeInstant tFirst = (TimeInstant) result.get(0).getPhenomenonTime();
			TimeInstant tLast = (TimeInstant) result.get(result.size()-1).getPhenomenonTime();
			
			assertEquals(obs[0], tFirst.getValue().toString());
			assertEquals(obs[1], result.get(0).getValue().getValue().getValue().toString());
			assertEquals(obs[2], result.get(0).getValue().getValue().getUnit());
			assertEquals(obs[3], tLast.getValue().toString());
			assertEquals(obs[4], result.get(result.size()-1).getValue().getValue().getValue().toString());
			assertEquals(obs[5], result.get(result.size()-1).getValue().getValue().getUnit());
			
			
		} catch (DecodingException | XmlException | OwsExceptionReport e) {
			e.printStackTrace();
		}
		
	
	}
}
