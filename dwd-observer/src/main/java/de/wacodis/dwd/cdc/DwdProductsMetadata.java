/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.util.Date;
import java.util.List;

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;

/**
 * Metadata for DWD station products
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdProductsMetadata {
    //TODO: define required station metadata
	
	String serviceUrl;
	String layername;
	String parameter;
	List<Float> extent;
	Date startDate;
	Date endDate;
	
	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getLayername() {
		return layername;
	}

	public void setLayername(String layername) {
		this.layername = layername;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public List<Float> getExtent() {
		return extent;
	}

	
	public void setExtent(List<Float> extent) {
		this.extent = extent;
	}

	public void setExtent(float xmin, float ymin, float xmax, float ymax) {
		this.extent.add(xmin);
		this.extent.add(ymin);
		this.extent.add(xmax);
		this.extent.add(ymax);
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
/*
	public DwdProductsMetadata(String serviceUrl, String layername, String parameter, List<Float> extent,
			Date startDate, Date endDate) {
		super();
		this.serviceUrl = serviceUrl;
		this.layername = layername;
		this.parameter = parameter;
		this.extent = extent;
		this.startDate = startDate;
		this.endDate = endDate;
	}
*/	
	
	
	
}
