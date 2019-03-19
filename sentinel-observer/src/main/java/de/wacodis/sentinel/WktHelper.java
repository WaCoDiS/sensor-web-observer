/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.sentinel;

/**
 *
 * @author matthes
 */
public class WktHelper {

    /**
     * generate a WKT Polygon from bounding box coordinates
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
