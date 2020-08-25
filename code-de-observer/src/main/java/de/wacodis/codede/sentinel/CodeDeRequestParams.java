package de.wacodis.codede.sentinel;

import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Encapuslates parameters for requesting CODE-DE OpenSearch services.
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeRequestParams {

    public String satellite;
    public String instrument;
    public String productType;
    public String processingLevel;
    public String sensorMode;
    public DateTime startDate;
    public DateTime endDate;
    public Float[] bbox;
    public Float[] cloudCover;

    public CodeDeRequestParams(@NotNull String satellite,  @NotNull String instrument, @NotNull DateTime startDate, @NotNull DateTime endDate,
                               Float[] cloudCover) {
        this.satellite = satellite;
        this.instrument = instrument;
        this.startDate = startDate;
        this.endDate = endDate;
        this.cloudCover = cloudCover;
    }


    public CodeDeRequestParams(@NotNull String satellite, @NotNull String instrument, @NotNull String productType,
                               @NotNull String processingLevel, @NotNull DateTime startDate, @NotNull DateTime endDate,
                               @NotNull Float[] bbox, String sensorMode, Float[] cloudCover) {
        this.satellite = satellite;
        this.instrument = instrument;
        this.productType = productType;
        this.processingLevel = processingLevel;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bbox = bbox;
        this.sensorMode = sensorMode;
        this.cloudCover = cloudCover;
    }

    public String getParentIdentifier() {
        return satellite;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public Float[] getBbox() {
        return bbox;
    }

    public Float[] getCloudCover() {
        return cloudCover;
    }

    public String getSatellite() {
        return satellite;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getProductType() {
        return productType;
    }

    public String getProcessingLevel() {
        return processingLevel;
    }

    public String getSensorMode() {
        return sensorMode;
    }
}
