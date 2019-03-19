/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.sentinel;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author matthes
 */
public class WktHelperTest {
    
    @Test
    public void testFromBoundingBox() {
        WktHelper helper = new WktHelper();
        
        String wktString = helper.fromBoundingBox(6.9315, 50.9854, 7.6071, 51.3190);
        
        String expected = "POLYGON((7.6071 50.9854,6.9315 50.9854,6.9315 51.3190,7.6071 51.3190,7.6071 50.9854))";
        Assert.assertThat(wktString, CoreMatchers.equalTo(expected));
    }
    
}
