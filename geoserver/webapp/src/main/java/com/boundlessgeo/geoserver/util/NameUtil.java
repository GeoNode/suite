/* (c) 2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.util;

import java.util.regex.Pattern;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.Predicates;
import org.geoserver.catalog.util.CloseableIterator;

public class NameUtil {
    
    static final String PATTERN = "(?:%[0-9A-Fa-f][0-9A-Fa-f]|[ :\\\\/#?])+";
    
    /**
     * Test whether or not a string will behave well as a name in geoserver
     * @param name the string to test
     * @return true if the name is does not contain invalid characters, false otherwise.
     */
    public static boolean isValid(String name) {
        return !Pattern.matches(".*"+PATTERN+".*", name);
    }
    
    /**
     * Sanitize a string to behave well as a name in geoserver, replacing invalid characters with "_"
     * @param name the string to sanitize
     * @return the sanitized name
     */
    public static String sanitize(String name) {
        return sanitize(name, null);
    }
    /**
     * Sanitize a string to behave well as a name in geoserver
     * @param name the string to sanitize
     * @param replacement the string used to replace invalid characters. "_" is used if null
     * @return the sanitized name
     */
    public static String sanitize(String name, String replacement) {
        if (replacement == null) {
            replacement = "_";
        }
        if (!isValid(replacement))  {
            throw new IllegalArgumentException("\""+replacement+"\" is not a valid replacement string");
        }
        
        return name.replaceAll(PATTERN, replacement);
    }
    
    public static String sanitizeEnsureUnique(String name, Class<? extends CatalogInfo> clazz, Catalog catalog) {
        return sanitizeEnsureUnique(name, clazz, catalog, null);
    }
    
    /**
     * Sanitize a name, and ensure the changed name is unique in the catalog
     * @param name the string to sanitize
     * @param clazz the class of the CatalogInfo object the name belongs to
     * @param catalog the catalog
     * @param replacement the string used to replace invalid characters. "_" is used if null
     * @return
     */
    public static String sanitizeEnsureUnique(String name, Class<? extends CatalogInfo> clazz, Catalog catalog, String replacement) {
        if (isValid(name)) {
            return name;
        }
        //Only make unique if the name is changed, to avoid colliding with itself
        return unique(sanitize(name, replacement), clazz, catalog);
    }
    
    /**
     * Constructs a unique name for a new catalog object of a given class
     * Searches the catalog for an object with the given name. If found, appends a digit to the name
     * and tries again. Otherwise, returns the name.
     * @param name
     * @param clazz
     * @param catalog
     * @return The unique name
     */
    public static String unique(String name, Class<? extends CatalogInfo> clazz, Catalog catalog) {
        //TODO: put an upper limit on how many times to try
        //TODO: add workspace support
        //Using catalog.list in case of multiple matching objects in different workspaces
       CloseableIterator iterator = catalog.list(clazz, Predicates.equal("name", name));
        if (iterator.hasNext()) {
            int i = 0;
            name += i;
            iterator.close();
            iterator = catalog.list(clazz, Predicates.equal("name", name));
            while (iterator.hasNext()) {
                name = name.replaceAll(i + "$", String.valueOf(i+1));
                iterator.close();
                iterator = catalog.list(clazz, Predicates.equal("name", name));
                i++;
            }
        }
        return name;
    }

}
