
package de.wacodis.sentinel.apihub;

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author matthes rieke
 */
public class ApiHubClient {

    /**
     * perform an API query with the given parameters
     * 
     * @param lastLatestDate the latest timestamp of an already retrieved product. can be null
     * @param maxCloudPercentage the maximum cloud coverage (considered if > 0.0)
     * @param platformName the platform name (e.g. Sentinel-2)
     * @param areaOfInterest the are of interest
     * @return the list of matching products
     */
    public List<ProductMetadata> requestProducts(DateTime lastLatestDate, Double maxCloudPercentage,
            String platformName, AbstractDataEnvelopeAreaOfInterest areaOfInterest) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
