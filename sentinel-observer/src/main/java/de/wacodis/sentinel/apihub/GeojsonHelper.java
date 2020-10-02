package de.wacodis.sentinel.apihub;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryEditor;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.io.gml2.GMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Helper class for dealing with GeoJSON
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class GeojsonHelper implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(GeojsonHelper.class);

    private GeometryFactory factory;
    private GMLReader gmlReader;
    private GeoJsonWriter geojsonWriter;

    @Override
    public void afterPropertiesSet() throws Exception {
        factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
        gmlReader = new GMLReader();
        geojsonWriter = new GeoJsonWriter();
        geojsonWriter.setEncodeCRS(false);
    }

    /**
     * Decodes a GML geometry string to GeoJSON
     *
     * @param gmlGeom GML geometry string
     * @return GeoJSON string
     */
    public String decodeGml(String gmlGeom) {
        String geojsonFootprint = null;
        try {
            Geometry geom = gmlReader.read(gmlGeom, factory);
            // As JTS does not know anything about CRS
            // the coordinate order has to be changed
            // to be compliant with GeoJSON
            GeometryEditor editor = new GeometryEditor();
            Geometry editedGeom = editor.edit(geom, new GeometryEditor.CoordinateOperation() {
                @Override
                public Coordinate[] edit(Coordinate[] coordinates, Geometry geometry) {
                    Coordinate[] switchedXY = new Coordinate[coordinates.length];
                    return Arrays.stream(coordinates).map(c -> new Coordinate(c.getY(), c.getX())).toArray(Coordinate[]::new);
                }
            });
            geojsonFootprint = geojsonWriter.write(editedGeom);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            LOG.warn("Could not decode GML footprint. Cause: {}", ex.getMessage());
            LOG.debug("Could not decode GML footprint.", ex);
        }
        return geojsonFootprint;
    }


}
