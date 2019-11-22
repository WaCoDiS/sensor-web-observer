/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc.model;

import de.wacodis.dwd.cdc.model.DwdProductsMetadata;
import org.joda.time.DateTime;

import de.wacodis.observer.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.observer.model.AbstractDataEnvelopeTimeFrame;
import de.wacodis.observer.model.DwdDataEnvelope;

import java.util.Arrays;

/**
 * Decoder for DWD product metadata
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdProductsMetadataDecoder {

    /**
     * Decodes {@link DwdProductsMetadata} to {@link DwdDataEnvelope}
     *
     * @param metadata the {@link DwdProductsMetadata} to be decoded
     * @return {@link DwdDataEnvelope}
     */
    public DwdDataEnvelope decode(DwdProductsMetadata metadata) {

        // create objects
        DwdDataEnvelope dwdDE = new DwdDataEnvelope();
        AbstractDataEnvelopeAreaOfInterest extent = new AbstractDataEnvelopeAreaOfInterest();

        // serviceUrl
        dwdDE.setServiceUrl(metadata.getServiceUrl());
        // layername and clear text
        dwdDE.setLayerName(metadata.getLayerName());
        dwdDE.setParameter(metadata.getParameter());
        // bbox
        extent.extent(Arrays.asList(
                metadata.getEnvelope().getMinLon(), metadata.getEnvelope().getMinLat(),
                metadata.getEnvelope().getMaxLon(), metadata.getEnvelope().getMaxLat()));
        dwdDE.setAreaOfInterest(extent);
        // timeframe
        DateTime startDate = metadata.getStartDate();
        DateTime endDate = metadata.getEndDate();
        AbstractDataEnvelopeTimeFrame timeframe = new AbstractDataEnvelopeTimeFrame();
        timeframe.setStartTime(startDate);
        timeframe.setEndTime(endDate);
        dwdDE.setTimeFrame(timeframe);

        return dwdDE;
    }

}

