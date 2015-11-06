/* (c) 2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.catalog;

import javax.annotation.PostConstruct;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogException;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.event.CatalogAddEvent;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.catalog.event.CatalogModifyEvent;
import org.geoserver.catalog.event.CatalogPostModifyEvent;
import org.geoserver.catalog.event.CatalogRemoveEvent;
import org.geoserver.platform.GeoServerExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boundlessgeo.geoserver.api.controllers.Metadata;
import com.boundlessgeo.geoserver.api.controllers.ThumbnailController;

/**
 * Removes thumbnail metadata from LayerInfo and LayerGroupInfo objects when those objects or any
 * StyleInfo objects they depend upon are changed. This ensures the thumbnail listed in the metadata
 * is always up to date.
 */
@Component
public class ThumbnailInvalidatingCatalogListener implements CatalogListener {
    @Autowired
    Catalog catalog;
    @Autowired
    ThumbnailController controller;
    
    /** Flag to prevent recursion during layer, layergroup events */
    private boolean modifying = false;
    
    public ThumbnailInvalidatingCatalogListener() { }
    
    @PostConstruct
    private void addListener() {
        catalog.addListener(this);
    }
    
    @Override
    public void handleAddEvent(CatalogAddEvent event) throws CatalogException {
        // No change on add, as thumbnails will not have been generated yet
    }

    @Override
    public void handleRemoveEvent(CatalogRemoveEvent event)
            throws CatalogException {
        CatalogInfo source = event.getSource();
        
        //If we delete a style or layer used by a layer or map, invalidate upstream thumbnails
        if (!modifying) {
            try {
                modifying = true;
                if (source instanceof StyleInfo) {
                    // Consider cache directory based on naming convention to prevent full catalog scan                    
                    StyleInfo style = (StyleInfo) source;

                    // Invalidate any maps or layers using this style
                    for (LayerInfo layer : catalog.getLayers(style)) {
                        
                        if (source.equals(layer.getDefaultStyle())) {
                            controller.clearThumbnail(layer);
                        }
                    }
                    for (LayerGroupInfo layerGroup : catalog.getLayerGroups()) {
                        if (layerGroup.getStyles().contains(source)) {
                            controller.clearThumbnail(layerGroup);
                        }
                    }
                } else if(source instanceof LayerInfo) {
                    LayerInfo layer = (LayerInfo) source;
                    controller.clearThumbnail(layer);
                } else if (source instanceof LayerGroupInfo) {
                    LayerGroupInfo layerGroup = (LayerGroupInfo) source;
                    controller.clearThumbnail(layerGroup);
                }
            } finally {
                modifying = false;
            }
        }
    }
    
    /**
     * Invalidates all thumbnails that depend on the modified resources, by removing the thumbnail 
     * entry from the associated metadata map. Only affects LayerInfo and LayerGroupInfo objects, 
     * and only runs for StyleInfo, LayerInfo, or LayerGroupInfo objects.
     * If the event updates the thumbnail entry, this new value is preserved for the source object, 
     * but the thumbnails for all dependent objects are still invalidated.
     */
    @Override
    public void handleModifyEvent(CatalogModifyEvent event)
            throws CatalogException {
        //Handle all modifications postModify, so metadata is updated correctly
        
        CatalogInfo source = event.getSource();
        
        if (!modifying) {
            try {
                modifying = true;
                if (source instanceof StyleInfo) {
                    //Invalidate any maps or layers using this style
                    for (LayerInfo layer : catalog.getLayers()) {
                        if (source.equals(layer.getDefaultStyle())) {
                            controller.clearThumbnail(layer);
                        }
                    }
                    for (LayerGroupInfo layerGroup : catalog.getLayerGroups()) {
                        if (layerGroup.getStyles().contains(source)) {
                            controller.clearThumbnail(layerGroup);
                        }
                    }
                } else if(source instanceof LayerInfo) {
                    //Invalidate layer, plus any maps using this layer
                    LayerInfo layer = catalog.getLayer(source.getId());
                    controller.clearThumbnail(layer);
                    for (LayerGroupInfo layerGroup : catalog.getLayerGroups()) {
                        if (layerGroup.getLayers().contains(source)) {
                            controller.clearThumbnail(layerGroup);
                        }
                    }
                } else if (source instanceof LayerGroupInfo) {
                    //Only invalidate map
                    LayerGroupInfo layerGroup = catalog.getLayerGroup(source.getId());
                    controller.clearThumbnail(layerGroup);
                }
            } finally {
                modifying = false;
            }
        }
        
    }
    
    @Override
    public void handlePostModifyEvent(CatalogPostModifyEvent event)
            throws CatalogException { }

    @Override
    public void reloaded() { }

}
