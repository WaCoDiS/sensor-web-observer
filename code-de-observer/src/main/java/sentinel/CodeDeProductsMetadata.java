package sentinel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class CodeDeProductsMetadata {

    public String parentIdentifier;
    public DateTime startDate;
    public DateTime endDate;
    public ArrayList<Float> bbox = new ArrayList<Float>();
    public ArrayList<Byte> cloudCover = new ArrayList<Byte>();

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

    public List<Float> getBbox() {
        return bbox;
    }

    public void setBbox(ArrayList<Float> bbox) {
        this.bbox = bbox;
    }

    public void setBbox(float xMin, float yMin, float xMax, float yMax){
        this.bbox.add(xMin);
        this.bbox.add(yMin);
        this.bbox.add(xMax);
        this.bbox.add(yMax);
    }

    public List<Byte> getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(ArrayList<Byte> cloudCover) {
        this.cloudCover = cloudCover;
    }

    public void setCloudCover(byte min, byte max){
        this.cloudCover.add(min);
        this.cloudCover.add(max);
    }
}
