/* (c) 2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NameUtilTest {

    @Test
    public void testSanitize() {
        assertEquals("us_states", NameUtil.sanitize("us states"));
        assertEquals("us_states", NameUtil.sanitize("us%20states"));
        assertEquals("us_states", NameUtil.sanitize("us  states"));
        assertEquals("us_states", NameUtil.sanitize("us %20\\/#?states"));
    }
}
