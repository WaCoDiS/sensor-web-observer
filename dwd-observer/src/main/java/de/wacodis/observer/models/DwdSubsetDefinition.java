package de.wacodis.observer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.*;

/** DwdSubsetDefinition */
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2019-05-13T08:43:27.051+02:00[Europe/Berlin]")
public class DwdSubsetDefinition extends AbstractSubsetDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("serviceUrl")
    private String serviceUrl;

    @JsonProperty("layerName")
    private String layerName;

    public DwdSubsetDefinition serviceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        return this;
    }

    /**
     * the base URL of the service
     *
     * @return serviceUrl
     */
    @ApiModelProperty(required = true, value = "the base URL of the service ")
    @NotNull
    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public DwdSubsetDefinition layerName(String layerName) {
        this.layerName = layerName;
        return this;
    }

    /**
     * the name of the queried layer
     *
     * @return layerName
     */
    @ApiModelProperty(required = true, value = "the name of the queried layer")
    @NotNull
    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DwdSubsetDefinition dwdSubsetDefinition = (DwdSubsetDefinition) o;
        return Objects.equals(this.serviceUrl, dwdSubsetDefinition.serviceUrl)
                && Objects.equals(this.layerName, dwdSubsetDefinition.layerName)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceUrl, layerName, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DwdSubsetDefinition {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    serviceUrl: ").append(toIndentedString(serviceUrl)).append("\n");
        sb.append("    layerName: ").append(toIndentedString(layerName)).append("\n");
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
