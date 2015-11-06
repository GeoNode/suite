package com.boundlessgeo.geoserver.api.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import com.boundlessgeo.geoserver.Proj;
import com.boundlessgeo.geoserver.json.JSONObj;
import com.vividsolutions.jts.geom.Point;

public class IOTest {

    @Test
    public void TestProj() {
        
        //Test regurlar CRS
        String srs = "EPSG:4326";
        JSONObj obj = new JSONObj();
        IO.proj(obj, Proj.get().crs(srs), srs);
        assertEquals(srs, obj.str("srs"));
        assertNotNull(obj.get("wkt"));
        //Test complex CRS (Polar)
        srs = "EPSG:3031";
        obj = new JSONObj();
        IO.proj(obj, Proj.get().crs(srs), srs);
        assertEquals(srs, obj.str("srs"));
        assertNotNull(obj.get("wkt"));
    }
    
    @Test
    public void TestSchema() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("test_schema");
        builder.setCRS(DefaultGeographicCRS.WGS84);
        
        builder.add("the_geom", Point.class);
        builder.add("other_geom", Point.class);
        builder.setDefaultGeometry("other_geom");
        builder.length(15).add("name", String.class);
        
        final SimpleFeatureType type = builder.buildFeatureType();
        
        JSONObj schema = IO.schema(new JSONObj(), type, false);
        assertEquals("test_schema", schema.get("name"));
        assertEquals("other_geom", schema.get("defaultGeometry"));
    }
}
