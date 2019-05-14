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

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.api.model.WacodisJobDefinition;

/**
 * Encodes a SubsetDefinition into request paramateres for a DWD WFS request
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdRequestParamsEncoder {

	public DwdWfsRequestParams encode(String version, String typeName, WacodisJobDefinition jobDefinition,
			Date startDate, Date endDate) {

		DwdWfsRequestParams params = new DwdWfsRequestParams();

		// Bounding Box
		AbstractDataEnvelopeAreaOfInterest areaOfInterest = jobDefinition.getAreaOfInterest();
		List<Float> coordinates = areaOfInterest.getExtent();
		DirectPosition2D bottomLeft = new DirectPosition2D(coordinates.get(1), coordinates.get(0));
		DirectPosition2D upperRight = new DirectPosition2D(coordinates.get(3), coordinates.get(2));
		Envelope2D bounds = new Envelope2D(bottomLeft, upperRight);

		params.setVersion(version);
		params.setTypeName(typeName);
		params.setBbox(bounds);
		params.setStartDate(startDate);
		params.setEndDate(endDate);

		return params;
	}

}
