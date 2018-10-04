package de.wacodis.dataaccess.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.constraints.*;

/** GdiDeDataEnvelope */
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2018-09-07T14:58:59.085+02:00[Europe/Berlin]")
public class GdiDeDataEnvelope extends AbstractDataEnvelope {
    @JsonProperty("catalougeUrl")
    private String catalougeUrl = null;

    @JsonProperty("recordRefId")
    private String recordRefId = null;

    public GdiDeDataEnvelope catalougeUrl(String catalougeUrl) {
        this.catalougeUrl = catalougeUrl;
        return this;
    }

    /**
     * URL of the GDI-DE catalogue
     *
     * @return catalougeUrl
     */
    @ApiModelProperty(value = "URL of the GDI-DE catalogue ")
    public String getCatalougeUrl() {
        return catalougeUrl;
    }

    public void setCatalougeUrl(String catalougeUrl) {
        this.catalougeUrl = catalougeUrl;
    }

    public GdiDeDataEnvelope recordRefId(String recordRefId) {
        this.recordRefId = recordRefId;
        return this;
    }

    /**
     * the id of the dataset within the GDI-DE catalogue
     *
     * @return recordRefId
     */
    @ApiModelProperty(required = true, value = "the id of the dataset within the GDI-DE catalogue ")
    @NotNull
    public String getRecordRefId() {
        return recordRefId;
    }

    public void setRecordRefId(String recordRefId) {
        this.recordRefId = recordRefId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GdiDeDataEnvelope gdiDeDataEnvelope = (GdiDeDataEnvelope) o;
        return Objects.equals(this.catalougeUrl, gdiDeDataEnvelope.catalougeUrl)
                && Objects.equals(this.recordRefId, gdiDeDataEnvelope.recordRefId)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalougeUrl, recordRefId, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GdiDeDataEnvelope {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    catalougeUrl: ").append(toIndentedString(catalougeUrl)).append("\n");
        sb.append("    recordRefId: ").append(toIndentedString(recordRefId)).append("\n");
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