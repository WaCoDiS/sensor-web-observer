package de.wacodis.codeDe.sentinel;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Encodes a SubsetDefinition into request paramateres for a Code De Opensearch request
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeRequestParamsEncoder {

    /**
     * puts all necessary attributes of a subsetdefinition into a requestParams object
     * @param parentIdentifier    id of the requested satallitesensor
     * @param startDate  start date of the request timeframe
     * @param endDate end date of the request timeframe
     * @param bbox       bbox (minLon, minLat, maxLon, maxLat)
     * @param cloudCover requested cloud cover
     * @return object containing all parameters
     */
    public CodeDeRequestParams encode(String parentIdentifier, DateTime startDate, DateTime endDate,
                                      List<Float> bbox, List<Float>cloudCover) {

        CodeDeRequestParams params = new CodeDeRequestParams();

        params.setParentIdentifier(parentIdentifier);
        params.setStartDate(startDate);
        params.setEndDate(endDate);
        params.setBbox(bbox); // Temporal Coverage?
        params.setCloudCover(cloudCover);

        return params;
    }
}
