package de.wacodis.dwd.cdc;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdWfsRequestorIT {

	private static DwdWfsRequestParams params;
	private static String serviceurl;

	@BeforeAll
	static void setup() throws ParseException {
		serviceurl = "https://cdc.dwd.de:443/geoserver/CDC/wfs?";
		params = new DwdWfsRequestParams();
		params.setVersion("2.0.0");
		params.setTypeName("CDC:VGSL_FX_MN003");


		DirectPosition2D linksUnten = new DirectPosition2D(51.402, 6.966);
		DirectPosition2D rechtsOben = new DirectPosition2D(51.405, 6.969);
		Envelope2D bounds = new Envelope2D(linksUnten, rechtsOben);
		params.setBbox(bounds);


		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss:SS'Z'");
		DateTime startDate = DateTime.parse("2019-04-24T01:00:00:00Z", df);
		DateTime endDate = DateTime.parse("2019-04-25T10:00:00:00Z", df);
		params.setStartDate(startDate);
		params.setEndDate(endDate);

	}

	@DisplayName("Test request Method")
	@Test
	void testRequest() {
		
		try {
		DwdProductsMetadata result = DwdWfsRequestor.request(serviceurl, params);

		// object of comparison
		DwdProductsMetadata metadata = new DwdProductsMetadata();

		metadata.setServiceUrl(serviceurl);
		metadata.setLayername("CDC:VGSL_FX_MN003");
		metadata.setParameter("Tägliche Stationsmessungen der maximalen Windspitze in ca. 10 m Höhe in m/s");

		List<Float> extent = new ArrayList<Float>();

		Float test = 51.402f;
		extent.add(test);
		extent.add(6.966f);
		extent.add(51.405f);
		extent.add(6.969f);
		metadata.setExtent(extent);

		
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss:SS'Z'");
		DateTime startDate = DateTime.parse("2019-04-24T01:00:00:00Z", df);
		DateTime endDate = DateTime.parse("2019-04-25T10:00:00:00Z", df);
		metadata.setStartDate(startDate);
		metadata.setEndDate(endDate);

		
		Assertions.assertTrue(false);
		// Comparison
		// Assertions.assertEquals(metadata.getExtent(), result.getExtent());
		//Assertions.assertTrue(51.402f, result.getExtent().get(0));
		// Assertions.assertTrue(result.getStartDate().equals(metadata.getStartDate()));
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}