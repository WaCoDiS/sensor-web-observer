/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.util.factory.GeoTools;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsBetween;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;

import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for requesting DWD FeatureServices for stationary
 * weather data.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdWfsRequestor {
	
	final static Logger LOG = LoggerFactory.getLogger(DwdWfsRequestor.class);

	/**
	 * Performs a query with the given parameters
	 *
	 * @param url    DWD CDC FeatureService URL
	 * @param params Paramaters for the FeatureService URL
	 * @return metadata for the found stationary weather data
	 * @throws IOException
	 */
	public static DwdProductsMetadata request(String url, DwdWfsRequestParams params) throws IOException {
		LOG.info("Start Buildung Connection Parameters for WFS Service");
		// Connect to WFS
		String getCapabilities = url + "?REQUEST=GetCapabilities";
		Map connectionParameters = new HashMap();
		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);
		connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 5000000);

		// schema
		DataStore data = DataStoreFinder.getDataStore(connectionParameters);
		SimpleFeatureType schema = data.getSchema("CDC:VGSL_"+params.getTypeName());
		String geomName = schema.getGeometryDescriptor().getLocalName();
		
		// Filter
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
		BBOX bbox = ff.bbox(ff.property(geomName), params.getBbox());
				
		PropertyIsBetween timeFilter = ff.between(
				ff.property("CDC:ZEITSTEMPEL"), ff.literal(params.getStartDate().toDate()), ff.literal(params.getEndDate().toDate()));
		
		And conjunction = ff.and(timeFilter, bbox);

		// Query
		Query query = new Query();
		query.setTypeName("CDC:VGSL_"+params.getTypeName());
		query.setFilter(conjunction);

		LOG.info("Requesting WFS Service?");
		// Request
		FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource("CDC:VGSL_"+params.getTypeName());

		// create DwdProductsMetaData
		DwdProductsMetadata metadata = new DwdProductsMetadata();

		LOG.info("Calculating the actual timeFrame and BoundingBox");
		// set parameters		
		SpatioTemporalExtent timeAndBbox = generateSpatioTemporalExtent(source,query);
		
		
		LOG.info("Building DwdProductsMetaData Object");
		// bbox		
		ArrayList<Float> extent = timeAndBbox.getbBox();
		metadata.setExtent(extent.get(0), extent.get(1),  extent.get(2), extent.get(3));
		
		// timeframe
		ArrayList<DateTime> timeFrame = timeAndBbox.getTimeFrame();
		metadata.setStartDate(timeFrame.get(0));
		metadata.setEndDate(timeFrame.get(1));
				
		// name
		metadata.setLayername(source.getInfo().getName());
		// clearname
		metadata.setParameter(source.getInfo().getTitle());
		
		// serviceurl
		metadata.setServiceUrl(url);
		LOG.info("End of request()-Method - Return DwdProductsMetaData Object");
		return metadata;
	}

	private static SpatioTemporalExtent generateSpatioTemporalExtent(FeatureSource<SimpleFeatureType, SimpleFeature> source, Query query ) throws IOException {
		
		// 
		SpatioTemporalExtent timeAndBbox = new SpatioTemporalExtent();
		LOG.info("Connecting WFS Service");
		// Build Iterator
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);
		FeatureIterator<SimpleFeature> iterator = features.features();

		//TimeFrame Parameter
		DateTime startDate = new DateTime();
		DateTime endDate= new DateTime();
		ArrayList<DateTime> timeFrame = new ArrayList<DateTime>();
		
		//BBOX Parameter
		float xMin = Float.NaN;
		float yMin = Float.NaN;
		float xMax = Float.NaN;
		float yMax = Float.NaN;
		ArrayList<Float> extent = new ArrayList<Float>();
		
		
		try {
			for(int i=1;hasNextNew(iterator); i++){
				SimpleFeature feature = (SimpleFeature) iterator.next();
				
				// Request time attribute
				DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd' 'HH:mm:ss.S");
				//DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
				//String wert = feature.getAttribute("ZEITSTEMPEL").toString();
				DateTime temp = DateTime.parse(feature.getAttribute("ZEITSTEMPEL").toString(), df);
				
				// Request BBOX
				BoundingBox bBox = feature.getBounds();
				
				// Set start Values
				if(i==1) {
					
					// Time Frame - First values
					startDate = temp;
					endDate = temp;
					timeFrame.add(0, startDate);
					timeFrame.add(1, endDate);
					
					// BBOX - First values
					xMin = (float) bBox.getMinX();
					yMin = (float) bBox.getMinY();
					xMax = (float) bBox.getMaxX();
					yMax = (float) bBox.getMaxY();
					
					extent.add(0, xMin);
					extent.add(1, yMin);
					extent.add(2, xMax);
					extent.add(3, yMax);
					
				}
				
				// Set StartDate or EndDate
				if(temp.isBefore(startDate)) {
					startDate = temp;
					timeFrame.remove(0);
					timeFrame.add(0, startDate);
				}	
				
				if(temp.isAfter(endDate)) {
					endDate = temp;
					timeFrame.remove(1);
					timeFrame.add(1, endDate);
				}
				
				
				// BBOX - Determine BBox values
				if(xMin > bBox.getMinX()) {
					xMin = (float) bBox.getMinX();
					extent.remove(0);
					extent.add(0, xMin);
				}	
				
				if(yMin > bBox.getMinY()) {
					yMin = (float) bBox.getMinY();
					extent.remove(1);
					extent.add(1, yMin);
			
				}
				if(xMax < bBox.getMaxX()) {
					xMax = (float) bBox.getMaxX();
					extent.remove(2);
					extent.add(2, xMax);
				}	
				
				if(yMax < bBox.getMaxY()) {
					yMax = (float) bBox.getMaxY();
					extent.remove(3);
					extent.add(3, yMax);
			
				}
				
			}
		} finally {
			iterator.close();
		}
		
		timeAndBbox.setbBox(extent);
		timeAndBbox.setTimeFrame(timeFrame);
		
		
		return timeAndBbox;
	}
	
	
	private static boolean hasNextNew(FeatureIterator<SimpleFeature> iterator) {
		boolean hasNext = false;
		try {
			hasNext = iterator.hasNext();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug("error while deserializing features", e);
		}
		
		return hasNext;
		
	}
	
	private static ArrayList<Float> generateBBox(FeatureSource<SimpleFeatureType, SimpleFeature> source, Query query) throws IOException {
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);	
		FeatureIterator<SimpleFeature> iterator = features.features();
		float xMin = Float.NaN;
		float yMin = Float.NaN;
		float xMax = Float.NaN;
		float yMax = Float.NaN;
		ArrayList<Float> extent = new ArrayList<Float>();
		try {
			for(int i=1;hasNextNew(iterator); i++){
				SimpleFeature feature = (SimpleFeature) iterator.next();
				BoundingBox bBox = feature.getBounds();
				// Set start Values
				if(i==1) {
					xMin = (float) bBox.getMinX();
					yMin = (float) bBox.getMinY();
					xMax = (float) bBox.getMaxX();
					yMax = (float) bBox.getMaxY();
					
					extent.add(0, xMin);
					extent.add(1, yMin);
					extent.add(2, xMax);
					extent.add(3, yMax);
				}
				
				// Set actual Boundingbox
				if(xMin > bBox.getMinX()) {
					xMin = (float) bBox.getMinX();
					extent.remove(0);
					extent.add(0, xMin);
				}	
				
				if(yMin > bBox.getMinY()) {
					yMin = (float) bBox.getMinY();
					extent.remove(1);
					extent.add(1, yMin);
			
				}
				if(xMax < bBox.getMaxX()) {
					xMax = (float) bBox.getMaxX();
					extent.remove(2);
					extent.add(2, xMax);
				}	
				
				if(yMax < bBox.getMaxY()) {
					yMax = (float) bBox.getMaxY();
					extent.remove(3);
					extent.add(3, yMax);
				}
				
			}
		} finally {
			iterator.close();
		}
		return extent;
	}

}
