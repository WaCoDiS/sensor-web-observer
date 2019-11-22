/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.util.List;

import de.wacodis.observer.model.DwdSubsetDefinition;
import org.joda.time.DateTime;

/**
 * Encoder for certain parameters from a {@link DwdSubsetDefinition}
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdRequestParamsEncoder {

    /**
     * Encodes certain parameters from {@link DwdSubsetDefinition} into { link DwdWfsRequestParams} that can be used
     * to perform for a DWD WFS request
     *
     * @param version     version number of WFS - usually 2.0.0
     * @param typeName    short designation of layer
     * @param coordinates bbox (minLon, minLat, maxLon, maxLat)
     * @param startDate   start date of the request timeframe
     * @param endDate     end date of the request timeframe
     * @return
     */
    public static DwdWfsRequestParams encode(String version, String typeName, List<Float> coordinates,
                                             DateTime startDate, DateTime endDate) {

        DwdWfsRequestParams params = new DwdWfsRequestParams();

        params.setVersion(version);
        params.setTypeName(typeName);
        params.setBbox(coordinates);
        params.setStartDate(startDate); // Temporal Coverage?
        params.setEndDate(endDate);

        return params;
    }

}
