/* (c) 2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.catalog;

import java.io.IOException;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogException;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.event.CatalogAddEvent;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.catalog.event.CatalogModifyEvent;
import org.geoserver.catalog.event.CatalogPostModifyEvent;
import org.geoserver.catalog.event.CatalogRemoveEvent;
import org.geoserver.platform.resource.Files;

import com.boundlessgeo.geoserver.api.controllers.ImportController;

/**
 * Listens for StoreInfo removals and deletes store files that were uploaded using the 
 * Composer Import API or the GeoServer REST Upload API
 */
public class UploadDeleteCatalogListener  implements CatalogListener {
    Catalog catalog;
    
    public UploadDeleteCatalogListener(Catalog catalog) {
        this.catalog = catalog;
        catalog.addListener(this);
    }
    @Override
    public void handleAddEvent(CatalogAddEvent event) throws CatalogException { }

    @Override
    public void handleRemoveEvent(CatalogRemoveEvent event)
            throws CatalogException {
        CatalogInfo source = event.getSource();
        
        if (source instanceof StoreInfo) {
            StoreInfo store = (StoreInfo)source;
            try {
                // If this store is file based, and was uploaded using the Composer API or the 
                // REST upload API, delete the uploaded file.
                java.io.File uploadDir = ImportController.uploadDir(catalog, store.getWorkspace(), store);
                if (uploadDir.exists()) {
                    Files.delete(uploadDir);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not delete store files for "+store.getName(), e);
            }
        }
    }

    @Override
    public void handleModifyEvent(CatalogModifyEvent event)
            throws CatalogException { }

    @Override
    public void handlePostModifyEvent(CatalogPostModifyEvent event)
            throws CatalogException { }

    @Override
    public void reloaded() { }

}
