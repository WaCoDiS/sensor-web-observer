package de.wacodis.sentinel.apihub;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.io.gml2.GMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
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
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
        gmlReader = new GMLReader();
        geojsonWriter = new GeoJsonWriter();
    }

    public String decodeGml(String gmlGeom) {
        String geojsonFootprint = null;
        try {
            Geometry geom = gmlReader.read(gmlGeom, factory);
            geojsonFootprint = geojsonWriter.write(geom);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            LOG.warn("Could not decode GML footprint. Cause: {}", ex.getMessage());
            LOG.debug("Could not decode GML footprint.", ex);
        }
        return geojsonFootprint;
    }


}
