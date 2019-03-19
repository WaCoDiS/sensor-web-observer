package de.wacodis.sentinel.apihub;

/**
 *
 * @author matthes rieke
 */
public class WktHelper {

    /**
     * generate a WKT Polygon from bounding box coordinates
     * 
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @return 
     */
    public String fromBoundingBox(double minX, double minY, double maxX, double maxY) {
        // "POLYGON((7.6071 50.9854,6.9315 50.9854,6.9315 51.3190,7.6071 51.3190,7.6071 50.9854,7.6071 50.9854))"
        return String.format("POLYGON((%.4f %.4f,%.4f %.4f,%.4f %.4f,%.4f %.4f,%.4f %.4f))",
                maxX, minY,
                minX, minY,
                minX, maxY,
                maxX, maxY,
                maxX, minY);
    }
    
}
