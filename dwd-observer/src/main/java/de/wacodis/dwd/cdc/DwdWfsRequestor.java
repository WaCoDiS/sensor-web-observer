/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.util.HashMap;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.spatial.BBOX;

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
	public DwdProductsMetadata request(String url, DwdWfsRequestParams params) throws IOException {

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
		PropertyIsBetween timeFilter = ff.between(ff.property("CDC:ZEITSTEMPEL"), ff.literal(params.getStartDate()),
				ff.literal(params.getEndDate()));
		And conjunction = ff.and(timeFilter, bbox);

		// Query
		Query query = new Query();
		query.setTypeName(params.getTypeName());
		query.setFilter(conjunction);

		// Request
		FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource(params.getTypeName());
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);
		FeatureIterator<SimpleFeature> iterator = features.features();

		// DwdProductsMetaData anlegen
		DwdProductsMetadata metadata = new DwdProductsMetadata();
		ReferencedEnvelope extent = features.getBounds();

		metadata.setExtent((float) extent.getMinX(), (float) extent.getMinY(), (float) extent.getMaxX(),
				(float) extent.getMaxY());
		
		metadata.setLayername(source.getInfo().getName());
		metadata.setParameter(source.getInfo().getTitle());
		
		metadata.setEndDate(params.getEndDate());
		metadata.setStartDate(params.getStartDate());
		/*
		try {
			int i = 1;
			while (iterator.hasNext()) {
				SimpleFeature feature = (SimpleFeature) iterator.next();
				System.out.print(i + ". ");
				for (int k = 0; k < feature.getAttributeCount(); k++)
					System.out.print(feature.getAttribute(k) + " ");

				System.out.println();

				i = i + 1;
			}
		} finally {
			iterator.close();
		}
	*/
		return metadata;
	}

}
