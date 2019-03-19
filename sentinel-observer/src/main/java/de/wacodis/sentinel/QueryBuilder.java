/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.sentinel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.StringUtils;

/**
 *
 * @author matthes
 */
public class QueryBuilder {
    
    public enum FootprintOperator {
        Intersects
    }
    
    public enum ProductType {
        SLC,
        GRD
    }
    
    public enum PlatformName {
        Sentinel1 {
            @Override
            public String toString() {
                return "Sentinel-1";
            }
            
        },
        Sentinel2 {
            @Override
            public String toString() {
                return "Sentinel-2";
            }
            
        },
        Sentinel3 {
            @Override
            public String toString() {
                return "Sentinel-3";
            }
            
        },
        Sentinel5 {
            @Override
            public String toString() {
                return "Sentinel-5";
            }
            
        },
    }
    
    private final DateTimeFormatter format = ISODateTimeFormat.dateTimeNoMillis();
    
    private String ingestionDate;
    private String productType;
    private String footprint;
    private String platformName;
    private String filename;
    private String cloudCoverPercentage;
    private String beginPosition;
    
    public QueryBuilder withBeginPosition(String bp) {
        this.beginPosition = bp;
        return this;
    }

    public QueryBuilder withBeginPosition(DateTime periodStart, DateTime periodEnd) {
        // convert to UTC
        DateTime startUtc = new LocalDateTime(periodStart.getMillis()).toDateTime(DateTimeZone.UTC);
        DateTime endUtc = new LocalDateTime(periodEnd.getMillis()).toDateTime(DateTimeZone.UTC);
        return this.withBeginPosition(String.format("[%s TO %s]", startUtc.toString(format), endUtc.toString(format)));
    }
    
    public QueryBuilder withFilename(String fn) {
        this.filename = fn;
        return this;
    }
    
    public QueryBuilder withCloudcoverPercentage(String c) {
        this.cloudCoverPercentage = c;
        return this;
    }
    
    public QueryBuilder withMaximumCloudcoverPercentage(int i) {
        return this.withCloudcoverPercentage(String.format("[0 TO %d]", i));
    }
    
    public QueryBuilder withIngestionDate(String ingestiondate) {
        this.ingestionDate = ingestiondate;
        return this;
    }
    
    public QueryBuilder withIngestionDateByPreviousDays(int days) {
        return this.withIngestionDate(String.format("[NOW-%dDAYS TO NOW]", days));
    }
    
    public QueryBuilder withPlatformName(String platformName) {
        this.platformName = platformName;
        return this;
    }
    
    public QueryBuilder withPlatformName(PlatformName platformName) {
        return this.withPlatformName(platformName.toString());
    }
    
    public QueryBuilder withProductType(String productType) {
        this.productType = productType;
        return this;
    }
    
    public QueryBuilder withProductType(ProductType productType) {
        return this.withProductType(productType.name());
    }
    
    public QueryBuilder withFootprint(String footprint) {
        this.footprint = footprint;
        return this;
    }
    
    public QueryBuilder withFootprint(FootprintOperator op, double minX, double minY, double maxX, double maxY) {
        return this.withFootprint(String.format("%s(%s)", op.name(), new WktHelper().fromBoundingBox(minX, minY, maxX, maxY)));
    }
    
    
    public String build() {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(ingestionDate)) {
            sb.append("ingestiondate:");
            sb.append(ingestionDate);
            sb.append(" AND ");
        }
        
        if (!StringUtils.isEmpty(productType)) {
            sb.append("producttype:");
            sb.append(productType);
            sb.append(" AND ");
        }
        
        if (!StringUtils.isEmpty(filename)) {
            sb.append("filename:");
            sb.append(filename);
            sb.append(" AND ");
        }
        
        if (!StringUtils.isEmpty(cloudCoverPercentage)) {
            sb.append("cloudcoverpercentage:");
            sb.append(cloudCoverPercentage);
            sb.append(" AND ");
        }
        
        if (!StringUtils.isEmpty(platformName)) {
            sb.append("platformname:");
            sb.append(platformName);
            sb.append(" AND ");
        }
        
        if (!StringUtils.isEmpty(beginPosition)) {
            sb.append("beginposition:");
            sb.append(beginPosition);
            sb.append(" AND ");
        }
        
        if (!StringUtils.isEmpty(footprint)) {
            sb.append("footprint:\"");
            sb.append(footprint);
            sb.append("\" AND ");
        }
        
        if (sb.length() > 0 ) {
            // remove the last AND
            sb.delete(sb.length() - 4, sb.length());
        }
        
        return sb.toString().trim();
    }
}
