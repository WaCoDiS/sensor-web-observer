package de.wacodis.sentinel.apihub;

import org.joda.time.DateTime;

/**
 *
 * @author matthes rieke
 */
public class ProductMetadata {
    
    private String title;
    private String id;
    private DateTime ingestionDate;
    private DateTime beginPosition;
    private DateTime endPosition;
    private double cloudCoverPercentage;
    private String instrumentShortName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DateTime getIngestionDate() {
        return ingestionDate;
    }

    public void setIngestionDate(DateTime ingestionDate) {
        this.ingestionDate = ingestionDate;
    }

    public DateTime getBeginPosition() {
        return beginPosition;
    }

    public void setBeginPosition(DateTime beginPosition) {
        this.beginPosition = beginPosition;
    }

    public DateTime getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(DateTime endPosition) {
        this.endPosition = endPosition;
    }

    public double getCloudCoverPercentage() {
        return cloudCoverPercentage;
    }

    public void setCloudCoverPercentage(double cloudCoverPercentage) {
        this.cloudCoverPercentage = cloudCoverPercentage;
    }

    public String getInstrumentShortName() {
        return instrumentShortName;
    }

    public void setInstrumentShortName(String instrumentShortName) {
        this.instrumentShortName = instrumentShortName;
    }
    
}
