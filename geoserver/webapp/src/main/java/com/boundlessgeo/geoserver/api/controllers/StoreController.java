/* (c) 2014-2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.api.controllers;

import static org.geoserver.catalog.Predicates.equal;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.util.file.File;
import org.geoserver.catalog.CascadeDeleteVisitor;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogFactory;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.Predicates;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.ResourcePool;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.util.CloseableIterator;
import org.geoserver.config.GeoServer;
import org.geoserver.platform.resource.Files;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WebMapServer;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.util.NullProgressListener;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.boundlessgeo.geoserver.json.JSONArr;
import com.boundlessgeo.geoserver.json.JSONObj;

/**
 * Used to connect to data storage (file, database, or service).
 * <p>
 * This API is locked down for map composer and is (not intended to be stable between releases).</p>
 * 
 * @see <a href="https://github.com/boundlessgeo/suite/wiki/Stores-API">Store API</a> (Wiki)
 */
 @Controller
 @RequestMapping("/api/stores")
 public class StoreController extends ApiController {
     static Logger LOG = Logging.getLogger(StoreController.class);

    @Autowired
    public StoreController(GeoServer geoServer) {
        super(geoServer);
    }
    
    /**
     * API endpoint to list the stores in the workspace
     * @param wsName The workspace
     * @param page Page of the list
     * @param count Number of items per page
     * @param sort Sort order (asc or desc)
     * @param textFilter Search filter to limit results
     * @param req The HTTP request
     * @return A JSONObj containing the current page, the number of items returned, the total number
     * of stores matching the current filter, and the list of stores for the current page.
     */
    @RequestMapping(value = "/{wsName:.+}", method = RequestMethod.GET)
    public @ResponseBody
    JSONObj list(@PathVariable String wsName, 
            @RequestParam(value="page", required=false) Integer page,
            @RequestParam(value="count", required=false, defaultValue=""+DEFAULT_PAGESIZE) Integer count,
            @RequestParam(value="sort", required=false) String sort,
            @RequestParam(value="filter", required=false) String textFilter, 
            HttpServletRequest req) {
        
        Catalog cat = geoServer.getCatalog();
        
        if ("default".equals(wsName)) {
            WorkspaceInfo def = cat.getDefaultWorkspace();
            if (def != null) {
                wsName = def.getName();
            }
        }
        
        Filter filter = equal("workspace.name", wsName);
        if (textFilter != null) {
            filter = Predicates.and(filter, Predicates.fullTextSearch(textFilter));
        }
        
        SortBy sortBy = parseSort(sort);
        
        Integer total = cat.count(StoreInfo.class, filter);
        
        JSONObj obj = new JSONObj();
        obj.put("total", total);
        obj.put("page", page != null ? page : 0);
        obj.put("count", Math.min(total, count != null ? count : total));
        
        JSONArr arr = obj.putArray("stores");
        try (
            CloseableIterator<StoreInfo> it =
                cat.list(StoreInfo.class, filter, offset(page, count), count, sortBy);
        ) {
            while (it.hasNext()) {
                StoreInfo store = it.next();
                IO.store(arr.addObject(), store, req, geoServer);
            }
        }
        return obj;
    }
    
    /**
     * API endpoint to get details on a specific store
     * @param wsName The workspace name
     * @param name The store name
     * @param req The HTTP request
     * @return The store, encoded as a JSON object
     */
    @RequestMapping(value = "/{wsName}/{name:.+}", method = RequestMethod.GET)
    public @ResponseBody
    JSONObj get(@PathVariable String wsName, @PathVariable String name, HttpServletRequest req) {
        StoreInfo store = findStore(wsName, name, geoServer.getCatalog());
        if (store == null) {
            throw new IllegalArgumentException("Store " + wsName + ":" + name + " not found");
        }
        try {
            return IO.storeDetails(new JSONObj(), store, req, geoServer);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error occured accessing store: %s,%s",wsName, name), e);
        }
    }

    /**
     * API endpoint to get details on a specific resource contained in a store, 
     * such as a database table.
     * @param wsName The workspace name
     * @param stName The store name
     * @param name The resource name
     * @param req The HTTP request
     * @return The store, encoded as a JSON object
     */
    @RequestMapping(value = "/{wsName}/{stName}/{name:.+}", method = RequestMethod.GET)
    public @ResponseBody JSONObj resource(@PathVariable String wsName, @PathVariable String stName, @PathVariable String name, HttpServletRequest req) throws IOException {
        Catalog cat = geoServer.getCatalog();
        StoreInfo store = findStore(wsName, stName, cat );
        JSONObj obj = IO.resource( new JSONObj(), store, name, geoServer);
        obj.putObject("store")
            .put("name", stName )
            .put("url", IO.url(req, "/stores/%s/%s",wsName,stName));
        return obj;
    }
    
    /**
     * API endpoint to get the list of attributes of a specific resource contained in a store, 
     * such as a database table.
     * @param wsName The workspace name
     * @param stName The store name
     * @param name The resource name
     * @param req The HTTP request
     * @return The schema and list of attributes, encoded as a JSON object
     */
    @RequestMapping(value = "/{wsName}/{stName}/{name:.+}/attributes", method = RequestMethod.GET)
    public @ResponseBody JSONObj attributes(
            @PathVariable String wsName, @PathVariable String stName, @PathVariable String name, 
            @RequestParam(value="count", required=false, defaultValue=""+DEFAULT_PAGESIZE) Integer count,
            HttpServletRequest req) throws IOException {
        
        Catalog cat = geoServer.getCatalog();
        StoreInfo store = findStore(wsName, stName, cat );
        
        JSONObj obj = new JSONObj();
        if (store instanceof DataStoreInfo) {
            DataStoreInfo data = (DataStoreInfo) store;
            
            @SuppressWarnings("rawtypes")
            FeatureSource source;
            @SuppressWarnings("rawtypes")
            DataAccess dataStore = data.getDataStore(new NullProgressListener());
            
            if (dataStore instanceof DataStore) {
                source = ((DataStore) dataStore).getFeatureSource(name);
            } else {
                NameImpl qname = new NameImpl(name);
                source = dataStore.getFeatureSource(qname);
            }
            //Limit number of features;
            Query query = new Query(Query.ALL);
            query.setMaxFeatures(count);
            
            FeatureCollection features = source.getFeatures(query);
            obj.put("schema", IO.schema(new JSONObj(), features.getSchema(), false));
            obj.put("values", IO.features(new JSONArr(), features));
        }
        
        return obj;
    }
    
    /**
     * API endpoint to delete a store from the catalog
     * @param wsName The workspace name
     * @param name The store name
     * @param recurse Flag to recursively delete dependent maps and layers
     * @param req The HTTP request
     * @return The name and workspace of the deleted store
     */
    @RequestMapping(value = "/{wsName}/{name:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String wsName,
                   @PathVariable String name,
                   @RequestParam(value="recurse",defaultValue="false") boolean recurse,
                   HttpServletRequest req) {
        StoreInfo store = findStore(wsName, name, geoServer.getCatalog());
        Catalog cat = geoServer.getCatalog();
        
        List<ResourceInfo> layers = cat.getResourcesByStore(store, ResourceInfo.class );
        if( layers.isEmpty() ){
            cat.remove(store);
        } else if (recurse){
            store.accept(new CascadeDeleteVisitor(cat));
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Use recurse=true to remove ").append(name).append(" along with layers:");
            for( ResourceInfo l : layers ){
                message.append(' ').append(l.getName());
            }
            throw new IllegalStateException( message.toString() );
        }
    }
    
    /**
     * API endpoint to create a new store
     * @param wsName The workspace to create the store in
     * @param name The name of the new store
     * @param obj The connection parameters for the store
     * @param req The HTTP request
     * @return The description of the newly created store
     * @throws IOException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/{wsName}", method = RequestMethod.POST)
    public @ResponseBody
    JSONObj create(@PathVariable String wsName, @RequestBody JSONObj obj, HttpServletRequest req) throws IOException {
        Catalog cat = geoServer.getCatalog();
        CatalogFactory factory = cat.getFactory();
        
        WorkspaceInfo workspace = findWorkspace(wsName);
        StoreInfo store = null;
        
        JSONObj params = obj.object("connection");
        if( params == null ){
            throw new IllegalArgumentException("connection parameters required");
        }
        if( params.has("raster")){
            String url = params.str("raster");            
            CoverageStoreInfo info = factory.createCoverageStore();
            
            // connect and defaults
            info.setURL(url);
            try {
                GridCoverageReader reader = info.getGridCoverageReader(null, null);
                Format format = reader.getFormat();
                info.setDescription( format.getDescription() );
                info.setEnabled(true);
            } catch (IOException e) {
                info.setError(e);
                info.setEnabled(false);
            }
            store = info;
        }
        else if ( params.has("url") &&
                params.str("url").toLowerCase().contains("Service=WMS") &&
                params.str("url").startsWith("http")){
            WMSStoreInfo info = factory.createWebMapServer();
            
            // connect and defaults
            info.setCapabilitiesURL(params.str("url"));
            try {
                WebMapServer service = info.getWebMapServer(new NullProgressListener());
                info.setDescription( service.getInfo().getDescription() );
                info.setEnabled(true);
            } catch (Throwable e) {
                info.setError(e);
                info.setEnabled(false);
            }
            store = info;
        }
        else {
            HashMap map = new HashMap(params.raw());
            Map resolved = ResourcePool.getParams(map, cat.getResourceLoader() );
            DataAccess dataStore = DataAccessFinder.getDataStore(resolved);            
            if( dataStore == null ){
                throw new IllegalArgumentException("Connection parameters incomplete (does not match an available data store, coverage store or wms store).");
            }
            DataStoreInfo info = factory.createDataStore();
            
            info.getConnectionParameters().putAll(map);
            try {
                info.setDescription( dataStore.getInfo().getDescription());
                info.setEnabled(true);
            } catch (Throwable e) {
                info.setError(e);
                info.setEnabled(false);
            }
            store = info;
        }        
        boolean refresh = define( store, obj );
        if( refresh ){
            LOG.log( Level.FINE, "Inconsistent: default connection used for store creation required refresh");
        }
        store.setWorkspace(workspace);
        if (obj.get("name") != null) {
            store.setName(obj.str("name"));
        }
        if (obj.get("type") != null) {
            store.setType(obj.str("type"));
        }
        cat.add(store);
        Metadata.created(store, new Date());
        
        return IO.storeDetails(new JSONObj(), store, req, geoServer);
    }
    
    /**
     * API endpoint to update an existing store
     * @param wsName The workspace of the store
     * @param name The name of the store
     * @param obj Partial description of the store, containing all changes
     * @param req HTTP request
     * @return The description of the updated store
     * @throws IOException
     */
    @RequestMapping(value="/{wsName}/{name:.+}", method = RequestMethod.PATCH)
    public @ResponseBody JSONObj patch(@PathVariable String wsName, @PathVariable String name, @RequestBody JSONObj obj, HttpServletRequest req) throws IOException {
        Catalog cat = geoServer.getCatalog();
        StoreInfo store = cat.getStoreByName(wsName, name, StoreInfo.class );
        
        boolean refresh = define(store, obj);
        cat.save(store);
        Metadata.modified(store, new Date());
        if (refresh) {
            resetConnection(store);
        }
        return IO.storeDetails(new JSONObj(), store, req, geoServer);
    }
    
    private void resetConnection(StoreInfo store ){
        Catalog cat = geoServer.getCatalog();
        if (store instanceof CoverageStoreInfo) {
            cat.getResourcePool().clear((CoverageStoreInfo) store);
        } else if (store instanceof DataStoreInfo) {
            cat.getResourcePool().clear((DataStoreInfo) store);
        } else if (store instanceof WMSStoreInfo) {
            cat.getResourcePool().clear((WMSStoreInfo) store);
        }
    }
    
    /**
     * API endpoint to update an existing store
     * @param wsName The workspace of the store
     * @param name The name of the store
     * @param obj Partial description of the store, containing all changes
     * @param req HTTP request
     * @return The description of the updated store
     * @throws IOException
     */
    @RequestMapping(value="/{wsName}/{name:.+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody JSONObj put(@PathVariable String wsName, @PathVariable String name, @RequestBody JSONObj obj, HttpServletRequest req) throws IOException {
        Catalog cat = geoServer.getCatalog();
        StoreInfo store = cat.getStoreByName(wsName, name, StoreInfo.class );
        
        // pending: clear store to defaults
        boolean refresh = define( store, obj );
        cat.save( store );
        Metadata.modified(store, new Date());
        if (refresh) {
            resetConnection(store);
        }
        return IO.storeDetails(new JSONObj(), store, req, geoServer);
    }
    
    @SuppressWarnings("unchecked")
    private boolean define( StoreInfo store, JSONObj obj ){
        boolean reconnect = false;
        for( String prop : obj.keys()){
            if("description".equals(prop)){
                store.setDescription(obj.str(prop));
            }
            else if("enabled".equals(prop)){
                store.setEnabled(obj.bool(prop));
                reconnect = true;
            }
            else if("name".equals(prop)){
                store.setName(obj.str(prop));
            }
            else if("workspace".equals(prop)){
                WorkspaceInfo newWorkspace = findWorkspace(obj.str(prop));
                store.setWorkspace( newWorkspace );
            }
            else if( store instanceof CoverageStoreInfo){
                CoverageStoreInfo info = (CoverageStoreInfo) store;
                if("connection".equals(prop)){
                    JSONObj connection = obj.object(prop);
                    if(!connection.has("raster") && connection.str("raster") != null){
                        throw new IllegalArgumentException("Property connection.raster required for coverage store");
                    }
                    for( String param : connection.keys()){
                        if("raster".equals(param)){
                            String url = connection.str(param);
                            reconnect = reconnect || url == null || !url.equals(info.getURL());
                            info.setURL(url);
                        }
                    }
                }
            }
            else if( store instanceof WMSStoreInfo){
                WMSStoreInfo info = (WMSStoreInfo) store;
                if("connection".equals(prop)){
                    JSONObj connection = obj.object(prop);
                    if(!connection.has("url") && connection.str("url") != null){
                        throw new IllegalArgumentException("Property connection.url required for wms store");
                    }
                    for( String param : connection.keys()){
                        if("url".equals(param)){
                            String url = connection.str(param);
                            reconnect = reconnect || url == null || !url.equals(info.getCapabilitiesURL()); 
                            info.setCapabilitiesURL(url);
                        }
                    }
                }
            }
            if( store instanceof DataStoreInfo){
                DataStoreInfo info = (DataStoreInfo) store;
                if("connection".equals(prop)){
                    JSONObj connection = obj.object(prop);
                    info.getConnectionParameters().clear();
                    info.getConnectionParameters().putAll( connection.raw() );
                    reconnect = true;
                }
            }
        }
        
        return reconnect;
    }
    
    private WorkspaceInfo findWorkspace(String wsName) {
        Catalog cat = geoServer.getCatalog();
        WorkspaceInfo ws;
        if ("default".equals(wsName)) {
            ws = cat.getDefaultWorkspace();
        } else {
            ws = cat.getWorkspaceByName(wsName);
        }
        if (ws == null) {
            throw new RuntimeException("Unable to find workspace " + wsName);
        }
        return ws;
    }
 }
