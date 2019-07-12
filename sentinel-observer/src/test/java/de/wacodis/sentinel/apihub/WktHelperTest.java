package de.wacodis.sentinel.apihub;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author matthes rieke
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
