package de.wacodis.codeDe.sentinel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class CodeDeProductsMetadata {

    // required
    public String datasetId;
    public String satellite;
    public float cloudCover;
    public String portal;

    // optional
    public String parentIdentifier;
    public DateTime startDate;
    public DateTime endDate;
    public ArrayList<Float> areaOfInterest = new ArrayList<Float>();


    public String getParentIdentifier() {
        return parentIdentifier;
    }

    public void setParentIdentifier(String parentIdentifier) {
        this.parentIdentifier = parentIdentifier;
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

    public List<Float> getAreaOfInterest() {
        return areaOfInterest;
    }

    public void setAreaOfInterest(ArrayList<Float> areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    public void setBbox(float xMin, float yMin, float xMax, float yMax){
        this.areaOfInterest.add(xMin);
        this.areaOfInterest.add(yMin);
        this.areaOfInterest.add(xMax);
        this.areaOfInterest.add(yMax);
    }

    public float getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(float cloudCover) {
        this.cloudCover = cloudCover;
    }

}
