package de.wacodis.dataaccess.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.joda.time.DateTime;

/** AbstractDataEnvelope */
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2018-09-07T14:58:59.085+02:00[Europe/Berlin]")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "sourceType",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SensorWebDataEnvelope.class, name = "SensorWebDataEnvelope"),
    @JsonSubTypes.Type(value = CopernicusDataEnvelope.class, name = "CopernicusDataEnvelope"),
    @JsonSubTypes.Type(value = GdiDeDataEnvelope.class, name = "GdiDeDataEnvelope"),
})
public class AbstractDataEnvelope {
    /** shall be used to determine the responsible data backend */
    public enum SourceTypeEnum {
        SENSORWEBDATAENVELOPE("SensorWebDataEnvelope"),

        COPERNICUSDATAENVELOPE("CopernicusDataEnvelope"),

        GDIDEDATAENVELOPE("GdiDeDataEnvelope");

        private String value;

        SourceTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static SourceTypeEnum fromValue(String text) {
            for (SourceTypeEnum b : SourceTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + text + "'");
        }
    }

    @JsonProperty("sourceType")
    private SourceTypeEnum sourceType = null;

    @JsonProperty("areaOfInterest")
    private AbstractDataEnvelopeAreaOfInterest areaOfInterest = null;

    @JsonProperty("timeFrame")
    private AbstractDataEnvelopeTimeFrame timeFrame = null;

    @JsonProperty("created")
    private DateTime created = null;

    @JsonProperty("modified")
    private DateTime modified = null;

    public AbstractDataEnvelope sourceType(SourceTypeEnum sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    /**
     * shall be used to determine the responsible data backend
     *
     * @return sourceType
     */
    @ApiModelProperty(
            required = true,
            value = "shall be used to determine the responsible data backend ")
    @NotNull
    public SourceTypeEnum getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceTypeEnum sourceType) {
        this.sourceType = sourceType;
    }

    public AbstractDataEnvelope areaOfInterest(AbstractDataEnvelopeAreaOfInterest areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
        return this;
    }

    /**
     * Get areaOfInterest
     *
     * @return areaOfInterest
     */
    @ApiModelProperty(value = "")
    @Valid
    public AbstractDataEnvelopeAreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

    public void setAreaOfInterest(AbstractDataEnvelopeAreaOfInterest areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    public AbstractDataEnvelope timeFrame(AbstractDataEnvelopeTimeFrame timeFrame) {
        this.timeFrame = timeFrame;
        return this;
    }

    /**
     * Get timeFrame
     *
     * @return timeFrame
     */
    @ApiModelProperty(value = "")
    @Valid
    public AbstractDataEnvelopeTimeFrame getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(AbstractDataEnvelopeTimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    public AbstractDataEnvelope created(DateTime created) {
        this.created = created;
        return this;
    }

    /**
     * time on which the dataset was created or became available
     *
     * @return created
     */
    @ApiModelProperty(value = "time on which the dataset was created or became available ")
    @Valid
    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public AbstractDataEnvelope modified(DateTime modified) {
        this.modified = modified;
        return this;
    }

    /**
     * time on which the dataset was modified last
     *
     * @return modified
     */
    @ApiModelProperty(value = "time on which the dataset was modified last ")
    @Valid
    public DateTime getModified() {
        return modified;
    }

    public void setModified(DateTime modified) {
        this.modified = modified;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractDataEnvelope abstractDataEnvelope = (AbstractDataEnvelope) o;
        return Objects.equals(this.sourceType, abstractDataEnvelope.sourceType)
                && Objects.equals(this.areaOfInterest, abstractDataEnvelope.areaOfInterest)
                && Objects.equals(this.timeFrame, abstractDataEnvelope.timeFrame)
                && Objects.equals(this.created, abstractDataEnvelope.created)
                && Objects.equals(this.modified, abstractDataEnvelope.modified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, areaOfInterest, timeFrame, created, modified);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AbstractDataEnvelope {\n");

        sb.append("    sourceType: ").append(toIndentedString(sourceType)).append("\n");
        sb.append("    areaOfInterest: ").append(toIndentedString(areaOfInterest)).append("\n");
        sb.append("    timeFrame: ").append(toIndentedString(timeFrame)).append("\n");
        sb.append("    created: ").append(toIndentedString(created)).append("\n");
        sb.append("    modified: ").append(toIndentedString(modified)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first
     * line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}