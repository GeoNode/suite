package com.boundlessgeo.geoserver;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class AppConfigurationTest {
    @Test
    public void testSanitizeFilename() {
        assertEquals("test_________.txt", 
                     AppConfiguration.sanitizeFilename("test/?<>\\:*|\".txt"));
    }
}
