/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

/**
 * This class is responsible for requesting DWD FeatureServices for
 * stationary weather data.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdWfsRequestor {

    /**
     * Performs a query with the given parameters
     *
     * @param url DWD CDC FeatureService URL
     * @param params Paramaters for the FeatureService URL
     * @return metadata for the found stationary weather data
     */
    public DwdProductsMetadata request(String url, DwdWfsRequestParams params) {
        //TODO: implement FeatureService requests
        return null;
    }

}
