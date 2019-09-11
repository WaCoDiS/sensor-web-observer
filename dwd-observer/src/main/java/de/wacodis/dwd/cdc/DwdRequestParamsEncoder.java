/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.util.Date;
import java.util.List;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.joda.time.DateTime;

import de.wacodis.observer.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.observer.model.WacodisJobDefinition;

/**
 * Encodes a SubsetDefinition into request paramateres for a DWD WFS request
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdRequestParamsEncoder {

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
