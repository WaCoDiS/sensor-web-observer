package de.wacodis.codeDe.sentinel;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Encapuslates parameters for requesting CODE-DE OpenSearch services.
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeRequestParams {

    public String parentIdentifier;
    public DateTime startDate;
    public DateTime endDate;
    public List<Float> bbox;
    public List<Float> cloudCover;

    public CodeDeRequestParams(String parentIdentifier, DateTime startDate, DateTime endDate, List<Float> bbox, List<Float> cloudCover) {
        this.parentIdentifier = parentIdentifier;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bbox = bbox;
        this.cloudCover = cloudCover;
    }

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

    public void setBbox(List<Float> bbox) {
        this.bbox = bbox;
    }

    public List<Float> getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(List<Float> cloudCover) {
        this.cloudCover = cloudCover;
    }
}
