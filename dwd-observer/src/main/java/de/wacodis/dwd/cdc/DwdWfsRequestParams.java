/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.util.List;

import org.joda.time.DateTime;

/**
 * Encapuslates parameters for requesting DWD WFS services
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdWfsRequestParams {

	// https://cdc.dwd.de:443/geoserver/CDC/wfs? +

	private String version;
	private String typeName;
	private List<Float> bbox;
	private DateTime startDate;
	private DateTime endDate;

	// Konstruktor
	public DwdWfsRequestParams() {

	}

	public DwdWfsRequestParams(String version, String typeName, List<Float> bbox, DateTime startDate, DateTime endDate) {
		super();
		this.version = version;
		this.typeName = typeName;
		this.bbox = bbox;
		this.startDate = startDate;
		this.endDate = endDate;

	}

	// Getters and Setters

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public List<Float> getBbox() {
		return bbox;
	}

	public void setBbox(List<Float> bounds) {
		this.bbox = bounds;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

}
