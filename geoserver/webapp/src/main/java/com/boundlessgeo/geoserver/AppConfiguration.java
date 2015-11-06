/* (c) 2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.geoserver.catalog.Catalog;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.util.logging.Logging;
import org.springframework.web.context.ServletContextAware;

/**
 * Tracks boundless suite specific configuration data:
 * * Composer cache directory, used for storing cached thumbnails. 
 *   Refer to {@link #lookupCacheDirectory(ServletContext)} for how this location is generated.
 */
public class AppConfiguration implements ServletContextAware {
    Catalog catalog;
    ServletContext servletContext;
    
    //When writing cache files, replace any invalid file chars with the replace char.
    //This regex matches /?<>\:*|"
    public static final String invalidCharRegex = "[/\\?<>\\\\:\\*|\"]";
    public static final String replaceChar = "_";
    
    /** Cache directory location set by configuration on startup */
    private String cacheDir;
    
    private static Logger LOGGER = Logging.getLogger(AppConfiguration.class);
    
    public AppConfiguration(Catalog catalog) {
        this.catalog = catalog;
    }
    
    /**
     * Determines the location of the composer thumbnail cache directory by running a property 
     * lookup on COMPOSER_CACHE_DIR. If this property is not found, uses the default value of 
     * GEOSERVER_DATA_DIR/composer
     * 
     * @param servContext The servlet context.
     * @return String The absolute path to the data directory, or <code>null</code> if it could not
     * be found. 
     */
    private String lookupCacheDirectory(ServletContext servContext) {
        
        //Try property lookup
        String cacheDir = GeoServerExtensions.getProperty("COMPOSER_CACHE_DIR", servContext);
        
        //Use the default of data/composer
        if(cacheDir == null) {
            cacheDir = (GeoServerExtensions.bean(GeoServerDataDirectory.class).root().getPath())
                    +File.separator+"composer";
        }
        try {
            File cacheFile = new File(cacheDir);
            if (!cacheFile.exists()) {
                cacheFile.mkdirs();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not initilize composer cache directory", e);
        }
        return cacheDir;
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        cacheDir = lookupCacheDirectory(servletContext);
    }

    /**
     * Returns the location of the composer cache directory
     * @return Path to the composer cache directory
     */
    public String cacheDir() {
        return cacheDir;
    }
    
    /**
     * File from the cache directory denoted by a relative path.
     * 
     * @return the file (which may or may not exist)
     */
    public File cacheFile(String path) {
        if (path == null) {
            throw new NullPointerException("cache file path required");
        }
        return new File(cacheDir + File.separator + path);
    }
    
    /**
     * Retrieves a file from the cache directory denoted by a relative path. 
     * If the file does not exist, creates a new file.
     * @param path relative path within cache dir
     * @return the file
     * @throws IOException if there is an error creating the file
     */
    public File createCacheFile(String path) throws IOException {
        File file = cacheFile(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
    /**
     * Replace any invalid characters with the replaceChar
     */
    public static String sanitizeFilename(String filename) {
        return filename.replaceAll(invalidCharRegex, replaceChar);
    }
}
