/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.factory.GeoTools;
import org.joda.time.DateTime;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;

/**
 * This class is responsible for requesting DWD FeatureServices for stationary
 * weather data.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdWfsRequestor {

	/**
	 * Performs a query with the given parameters
	 *
	 * @param url    DWD CDC FeatureService URL
	 * @param params Paramaters for the FeatureService URL
	 * @return metadata for the found stationary weather data
	 * @throws IOException
	 */
	public static DwdProductsMetadata request(String url, DwdWfsRequestParams params) throws IOException {

		// Connect to WFS
		String getCapabilities = url + "?REQUEST=GetCapabilities";
		Map connectionParameters = new HashMap();
		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);
		connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 5000000);

		// schema
		DataStore data = DataStoreFinder.getDataStore(connectionParameters);
		SimpleFeatureType schema = data.getSchema(params.getTypeName());
		String geomName = schema.getGeometryDescriptor().getLocalName();

		// Filter
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
		BBOX bbox = ff.bbox(ff.property(geomName), params.getBbox());
		PropertyIsBetween timeFilter = ff.between(
				ff.property("CDC:ZEITSTEMPEL"), ff.literal(params.getStartDate()), ff.literal(params.getEndDate()));
		And conjunction = ff.and(timeFilter, bbox);

		// Query
		Query query = new Query();
		query.setTypeName(params.getTypeName());
		query.setFilter(conjunction);

		// Request
		FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource(params.getTypeName());
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);

		// create DwdProductsMetaData
		DwdProductsMetadata metadata = new DwdProductsMetadata();
		//ReferencedEnvelope extent = source.getBounds(query);
		//ReferencedEnvelope extent = source.getFeatures(query).getBounds();
		
		// set parameters
		// bbox
		
		ArrayList<Float> extent = generateBBox(features);
		metadata.setExtent(extent.get(0), extent.get(1),  extent.get(2),
				extent.get(3));
				
		// name
		metadata.setLayername(source.getInfo().getName());
		// clearname
		metadata.setParameter(source.getInfo().getTitle());
		// timeframe
		ArrayList<DateTime> timeFrame = generateTimeFrame(features);
		metadata.setStartDate(timeFrame.get(0));
		metadata.setEndDate(timeFrame.get(1));
		// serviceurl
		metadata.setServiceUrl(url);
		return metadata;
	}

	private static ArrayList<DateTime> generateTimeFrame(FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
		FeatureIterator<SimpleFeature> iterator = features.features();
		DateTime startDate = new DateTime();
		DateTime endDate= new DateTime();
		ArrayList<DateTime> timeFrame = new ArrayList<DateTime>(2);
		try {
			for(int i=1;iterator.hasNext(); i++){
				SimpleFeature feature = (SimpleFeature) iterator.next();
				DateTime temp = (DateTime) feature.getAttribute("ZEITSTEMPEL");
				// Set start Values
				if(i==1) {
					startDate = temp;
					endDate = temp;
					timeFrame.add(startDate);
					timeFrame.add(endDate);
				}
				
				// Set StartDate or EndDate
				if(temp.isBefore(startDate)) {
					startDate = temp;
					timeFrame.add(startDate);
				}	
				
				if(temp.isAfter(endDate)) {
					endDate = temp;
					timeFrame.add(endDate);
				}
				
			}
		} finally {
			iterator.close();
		}
		return timeFrame;
	}
	
	private static ArrayList<Float> generateBBox(FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
		FeatureIterator<SimpleFeature> iterator = features.features();
		float xMin = Float.NaN;
		float yMin = Float.NaN;
		float xMax = Float.NaN;
		float yMax = Float.NaN;
		ArrayList<Float> extent = new ArrayList<Float>(4);
		try {
			for(int i=1;iterator.hasNext(); i++){
				SimpleFeature feature = (SimpleFeature) iterator.next();
				//float temp = feature.getAttribute("GEOM");
				BoundingBox bBox = feature.getBounds();
				// Set start Values
				if(i==1) {
					xMin = (float) bBox.getMinX();
					yMin = (float) bBox.getMinY();
					xMax = (float) bBox.getMaxX();
					yMax = (float) bBox.getMaxY();
					
				}
				
				// Set actual Boundingbox
				if(xMin < bBox.getMinX()) {
					xMin = (float) bBox.getMinX();
				}	
				
				if(yMin < bBox.getMinY()) {
					yMin = (float) bBox.getMinY();
			
				}
				if(xMax > bBox.getMaxX()) {
					xMax = (float) bBox.getMaxX();
				}	
				
				if(yMax < bBox.getMaxY()) {
					yMax = (float) bBox.getMaxY();
			
				}
				
				extent.add(xMin);
				extent.add(yMin);
				extent.add(xMax);
				extent.add(yMax);
				
			}
		} finally {
			iterator.close();
		}
		return extent;
	}

}
