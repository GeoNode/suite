/* (c) 2014 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProjTest {

    Proj proj;

    @Before
    public void setup() {
        Proj.INSTANCE = null;
        proj = Proj.get();
    }
    
    @After
    public void tearDown() {
        Proj.INSTANCE = null;
    }

    @Test
    public void testCRS() throws Exception {
        CoordinateReferenceSystem crs = proj.crs("EPSG:3005");
        assertNotNull(crs);
        assertTrue(crs instanceof ProjectedCRS);
    }

    @Test
    public void testRecent() throws Exception {
        
        Map<String,CoordinateReferenceSystem> recent = proj.recent();
        proj.crs("EPSG:3005");
        
        assertEquals(3, recent.size());

        assertNotNull(recent.get("EPSG:4326"));
        assertNotNull(recent.get("EPSG:3857"));
        assertNotNull(recent.get("EPSG:3005"));
    }
}
