package de.wacodis.codede.sentinel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.wacodis.codede.sentinel.exception.ParsingException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.decode.json.GeoJSONDecoder;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class CodeDeResponseJsonResolver {

    public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final String FEATURES_PATH = "/features";
    private static final String IDENTIFIER_PATH = "/id";
    private static final String PRODUCT_IDENTIFIER_PATH = "/properties/productIdentifier";
    private static final String DOWNLOAD_LINK_PATH = "/properties/services/download/url";
    private static final String START_DATE_PATH = "/properties/startDate";
    private static final String COMPLETION_DATE_PATH = "/properties/completionDate";
    private static final String CLOUD_COVERAGE_PATH = "/properties/cloudCover";
    private static final String GEOMETRY_PATH = "/geometry";

    private GeoJSONDecoder decoder;

    public CodeDeResponseJsonResolver() {
        decoder = new GeoJSONDecoder();
    }

    public ArrayNode resolveFeatures(JsonNode node) {
        return (ArrayNode) node.at(FEATURES_PATH);
    }

    public String getIdentifier(JsonNode node) throws ParsingException {
        JsonNode idNode = node.at(IDENTIFIER_PATH);
        if (idNode.isMissingNode()) {
            throw new ParsingException(String.format("Missing node: '%s'", IDENTIFIER_PATH));
        }
        return idNode.asText();
    }

    public String getProductIdentifier(JsonNode node) throws ParsingException {
        JsonNode prodIdNode = node.at(PRODUCT_IDENTIFIER_PATH);
        if (prodIdNode.isMissingNode()) {
            throw new ParsingException(String.format("Missing node: '%s'", PRODUCT_IDENTIFIER_PATH));
        }
        return prodIdNode.asText();
    }

    public String getDownloadLink(JsonNode node) throws ParsingException {
        JsonNode downloadNode = node.at(DOWNLOAD_LINK_PATH);
        if (downloadNode.isMissingNode()) {
            throw new ParsingException(String.format("Missing node: '%s'", DOWNLOAD_LINK_PATH));
        }
        return downloadNode.asText();
    }

    public DateTime[] getTimeFrame(JsonNode node) throws ParsingException {
        JsonNode startDateNode = node.at(START_DATE_PATH);
        if (startDateNode.isMissingNode()) {
            throw new ParsingException(String.format("Missing node: '%s'", START_DATE_PATH));
        }
        JsonNode endDateNode = node.at(COMPLETION_DATE_PATH);
        if (endDateNode.isMissingNode()) {
            throw new ParsingException(String.format("Missing node: '%s'", COMPLETION_DATE_PATH));
        }
        return new DateTime[]{
                DateTime.parse(startDateNode.asText(), FORMATTER),
                DateTime.parse(endDateNode.asText(), FORMATTER)
        };
    }

    public Float getCloudCoverage(JsonNode node) {
        JsonNode cloudCoverNode = node.at(CLOUD_COVERAGE_PATH);
        if (cloudCoverNode.isMissingNode() || cloudCoverNode.asText().equals("-1")) {
            return null;
        } else {
            return Float.parseFloat(cloudCoverNode.asText());
        }
    }

    public Coordinate[] getBbox(JsonNode node) throws ParsingException {
        JsonNode geomNode = node.at(GEOMETRY_PATH);
        if (geomNode.isMissingNode()) {
            throw new ParsingException(String.format("Missing node: '%s'", GEOMETRY_PATH));
        }
        Geometry geom;
        try {
            geom = decoder.decode(geomNode);
        } catch (DecodingException ex) {
            throw new ParsingException("Error while decoding geomerty.", ex);
        }
        return geom.getEnvelope().getCoordinates();
    }

}
