package de.wacodis.codede.sentinel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata for the CODE-DE products.
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeProductsMetadata {

    // required
    private String datasetId;
    private float cloudCover;
    private String downloadLink;

    // optional
    private String productIdentifier;
    private DateTime startDate;
    private DateTime endDate;
    private List<Float> areaOfInterest = new ArrayList<Float>();


    public String getProductIdentifier() {
        return productIdentifier;
    }

    public void setProductIdentifier(String productIdentifier) {
        this.productIdentifier = productIdentifier;
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

    public void setAreaOfInterest(List<Float> areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    public float getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(float cloudCover) {
        this.cloudCover = cloudCover;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }
}
