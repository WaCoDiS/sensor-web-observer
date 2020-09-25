package de.wacodis.sentinel.apihub;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author matthes rieke
 */
public class ProductMetadata {
    
    private static final Logger LOG = LoggerFactory.getLogger(ProductMetadata.class);
    
    private String title;
    private String id;
    private DateTime ingestionDate;
    private DateTime beginPosition;
    private DateTime endPosition;
    private double cloudCoverPercentage;
    private String instrumentShortName;
    private String footprintWkt;
    private String platformName;
    private String sensorMode;
    private String productType;
    private String processingLevel;
    private Envelope bbox;
    private String gmlFootprint;

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

    public String getFootprintWkt() {
        return footprintWkt;
    }

    public void setFootprintWkt(String footprintWkt) {
        this.footprintWkt = footprintWkt;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getSensorMode() {
        return sensorMode;
    }

    public void setSensorMode(String sensorMode) {
        this.sensorMode = sensorMode;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProcessingLevel() {
        return processingLevel;
    }

    public void setProcessingLevel(String processingLevel) {
        this.processingLevel = processingLevel;
    }

    public String getGmlFootprint() {
        return gmlFootprint;
    }

    public void setGmlFootprint(String gmlFootprint) {
        this.gmlFootprint = gmlFootprint;
    }

    public Envelope resolveBbox() {
        if (this.bbox == null && this.footprintWkt != null) {
            WKTReader reader = new WKTReader();
            try {
                Geometry geom = reader.read(this.footprintWkt);
                this.bbox = geom.getEnvelopeInternal();
            } catch (ParseException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
        
        return this.bbox;
    }
    
}
