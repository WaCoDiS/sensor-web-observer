/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import org.joda.time.DateTime;

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.api.model.DwdDataEnvelope;

/**
 * Decodes DWD products metadata into a DataEnvelope
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdProductsMetadataDecoder {
    
    public DwdDataEnvelope decode(DwdProductsMetadata metadata){
    	
    	DwdDataEnvelope dwdDE = new DwdDataEnvelope();
    	AbstractDataEnvelopeAreaOfInterest extent = new AbstractDataEnvelopeAreaOfInterest();
    	extent.extent(metadata.getExtent());
    	dwdDE.setAreaOfInterest(extent);
    	dwdDE.setLayerName(metadata.getLayername());
    	dwdDE.setParameter(metadata.getParameter());
    	DateTime startDate = new DateTime(metadata.getStartDate());
    	DateTime endDate = new DateTime(metadata.getEndDate());
    	//dwdDE.setTimeFrame();
        return dwdDE;
    }

}

