
package de.wacodis.sentinel.apihub;

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author matthes rieke
 */
public class ApiHubClient {

    private final String url;
    private final String password;
    private final String user;

    public ApiHubClient(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

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
        return Collections.emptyList();
    }
    
}
