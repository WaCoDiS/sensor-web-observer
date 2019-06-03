/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import org.joda.time.DateTime;

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.api.model.AbstractDataEnvelopeTimeFrame;
import de.wacodis.api.model.DwdDataEnvelope;

/**
 * Decodes DWD products metadata into a DataEnvelope
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdProductsMetadataDecoder {
    
    public static DwdDataEnvelope decode(DwdProductsMetadata metadata){
    	
    	// create objects
    	DwdDataEnvelope dwdDE = new DwdDataEnvelope();
    	AbstractDataEnvelopeAreaOfInterest extent = new AbstractDataEnvelopeAreaOfInterest();
    	
    	// serviceUrl
    	dwdDE.setServiceUrl(metadata.getServiceUrl());
    	// layername and clear text
    	dwdDE.setLayerName(metadata.getLayername());
    	dwdDE.setParameter(metadata.getParameter());
    	// bbox
    	extent.extent(metadata.getExtent());
    	dwdDE.setAreaOfInterest(extent);
    	// timeframe
    	DateTime startDate = metadata.getStartDate();
    	DateTime endDate =metadata.getEndDate();
    	AbstractDataEnvelopeTimeFrame timeframe = new AbstractDataEnvelopeTimeFrame();
    	timeframe.setStartTime(startDate);
    	timeframe.setEndTime(endDate);
    	dwdDE.setTimeFrame(timeframe);
    	
        return dwdDE;
    }

}

