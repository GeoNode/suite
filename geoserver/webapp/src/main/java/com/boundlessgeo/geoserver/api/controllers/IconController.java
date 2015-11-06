/* (c) 2014 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CatalogVisitorAdapter;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Paths;
import org.geoserver.platform.resource.Resource;
import org.geoserver.platform.resource.Resource.Type;
import org.geoserver.platform.resource.Resources;
import org.geotools.styling.Style;
import org.geotools.util.KVP;
import org.geotools.util.logging.Logging;
import org.opengis.metadata.citation.OnLineResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.boundlessgeo.geoserver.api.exceptions.NotFoundException;
import com.boundlessgeo.geoserver.json.JSONArr;
import com.boundlessgeo.geoserver.json.JSONObj;
import com.boundlessgeo.geoserver.util.StyleAdaptor;

@Controller
@RequestMapping("/api/icons")
public class IconController extends ApiController {

    static final Logger LOG = Logging.getLogger(IconController.class);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static final Map<String,String> ICON_FORMATS = new HashMap<>(
        (Map)new KVP(
            "png","image/png",
            "jpeg","image/jpeg",
            "jpg","image/jpeg",
            "gif","image/gif",
            "svg","image/svg+xml",
            "ttf","application/font-sfnt",
            "properties","text/x-java-properties")
    );
    
    @Autowired
    public IconController(GeoServer geoServer) {
        super(geoServer);
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody JSONArr list(HttpServletRequest request) throws IOException {
        return list(null, request);
    }
    
    @RequestMapping(value = "/list/{wsName:.+}", method = RequestMethod.GET)
    public @ResponseBody JSONArr list(@PathVariable String wsName, HttpServletRequest request) throws IOException {

        JSONArr arr = new JSONArr();
        
        WorkspaceInfo ws;
        Resource styles;
        
        if (wsName == null) {
            ws = null;
            styles = dataDir().getRoot("styles");
        } else {
            ws = findWorkspace(wsName, catalog());
            styles = dataDir().get(ws, "styles");
        }
        if (styles.getType() != Type.UNDEFINED) {
            
            Set<String> usedGraphics = findAllGraphics(ws);
            
            for(Resource r : styles.list()){
                String name = r.name();
                String ext = fileExt(name);

                if(!ICON_FORMATS.containsKey(ext.toLowerCase())){
                    continue;
                }

                JSONObj item = icon(arr.addObject(), ws, r, request);
                item.put("used", usedGraphics.contains(name));
            }
        }

        return arr;
    }
    
    @RequestMapping(value = "", method = RequestMethod.POST,consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseStatus(value=HttpStatus.CREATED)
        @ResponseBody JSONArr create(HttpServletRequest request )
        throws IOException, FileUploadException {
        return create(null, request);
    }
    @RequestMapping(value = "/{wsName:.+}", method = RequestMethod.POST,consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseStatus(value=HttpStatus.CREATED)
        @ResponseBody JSONArr create(@PathVariable String wsName,  HttpServletRequest request )
        throws IOException, FileUploadException {

        WorkspaceInfo ws;
        Resource styles;
        
        if (wsName == null) {
            ws = null;
            styles = dataDir().getRoot("styles");
        } else {
            ws = findWorkspace(wsName, catalog());
            styles = dataDir().get(ws, "styles");
        }
        
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        @SuppressWarnings("unchecked")
        List<FileItem> input = (List<FileItem>) upload.parseRequest(request);

        JSONArr created = new JSONArr();
        for (FileItem file : input) {
            String filename = file.getName();

            // trim filename if required
            if (filename.lastIndexOf('/') != -1) {
                filename = filename.substring(filename.lastIndexOf('/'));
            }
            if (filename.lastIndexOf('\\') != -1) {
                filename = filename.substring(filename.lastIndexOf('\\'));
            }
            String ext = fileExt(filename);
            if( !ICON_FORMATS.containsKey(ext)){
                String msg = "Icon "+filename+" format "+ext+" unsupported - try:"+ICON_FORMATS.keySet();
                LOG.warning(msg);
                throw new FileUploadException(msg);
            }
            try {
                InputStream data = file.getInputStream();                
                Resources.copy(data, styles, filename);

                icon(created.addObject(), ws, styles.get(filename), request);
            } catch (Exception e) {
                throw new FileUploadException("Unable to write "+filename,e);
            }
        }

        return created;
    }
    

    JSONObj icon(JSONObj obj, WorkspaceInfo ws, Resource r, HttpServletRequest req) {
        String filename = r.name();
        String ext = fileExt(filename);
        Object format = ICON_FORMATS.get(ext.toLowerCase());
        
        obj.put("name", filename)
            .put("format",ext)
            .put("mime",format);
        if (ws == null) {
            obj.put("url", IO.url( req,  "/icons/%s", filename ));
        } else {
            obj.put("url", IO.url( req,  "/icons/%s/%s", ws.getName(),filename ));
        }

        IO.date(obj.putObject("modified"), new Date(r.lastmodified()));
        return obj;
    }
    
    @RequestMapping(value = "/{icon:.+}", method = RequestMethod.GET)
    public HttpEntity raw(@PathVariable String icon) throws IOException {
        return raw(null, icon);
    }
    @RequestMapping(value = "/{wsName}/{icon:.+}", method = RequestMethod.GET)
    public HttpEntity raw(@PathVariable String wsName, @PathVariable String icon) throws IOException {
        
        WorkspaceInfo ws;
        Resource resource;
        
        if (wsName == null) {
            ws = null;
            resource = dataDir().getRoot("styles", icon);
        } else {
            ws = findWorkspace(wsName, catalog());
            resource = dataDir().get(ws, "styles", icon);
        }
        
        if( resource.getType() != Type.RESOURCE ){
            throw new NotFoundException("Icon "+icon+" not found");
        }
        String ext = fileExt(icon);
        if( !ICON_FORMATS.containsKey(ext)){
            throw new NotFoundException("Icon "+icon+" format unsupported");
        }
        String mimeType = ICON_FORMATS.get(ext.toLowerCase());

        try (
            InputStream in = resource.in();
        ) {
            byte[] bytes = IOUtils.toByteArray(in);
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setLastModified(resource.lastmodified());
            return new HttpEntity(bytes, headers);
        }
    }
    @RequestMapping(value = "/{icon:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String icon) throws IOException {
        delete(null, icon);
    }

    @RequestMapping(value = "/{wsName}/{icon:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String wsName, @PathVariable String icon) throws IOException {
        
        WorkspaceInfo ws;
        Resource resource;
        
        if (wsName == null) {
            ws = null;
            resource = dataDir().getRoot("styles", icon);
        } else {
            ws = findWorkspace(wsName, catalog());
            resource = dataDir().get(ws, "styles", icon);
        }
        
        if( resource.getType() != Type.RESOURCE ){
            throw new NotFoundException("Icon "+icon+" not found");
        }
        String ext = fileExt(icon);
        if( !ICON_FORMATS.containsKey(ext)){
            throw new NotFoundException("Icon "+icon+" format unsupported");
        }
        if (!resource.delete()) {
            throw new RuntimeException("Failed to delete icon "+icon);
        }
    }
    
    @ExceptionHandler(FileUploadException.class)
    public @ResponseBody JSONObj error(FileUploadException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return IO.error(new JSONObj(), e );
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody JSONObj error(IllegalArgumentException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return IO.error(new JSONObj(), e );
    }
    

    /** 
     * Find which icons/fonts are used by any styles used in the layers and layerGroups of the 
     * given workspace
     * @param ws The workspace to search, or null to search all styles in the catalog
     * @return Set of icons used by all layers in the workspace.
     * @throws IOException Trouble access default style
     */    
    @SuppressWarnings("unchecked")
    private Set<String> findAllGraphics(WorkspaceInfo ws) throws IOException {
        final Catalog cat = geoServer.getCatalog();
        
        final Set<String> externalGraphics = new HashSet<String>();
        CatalogVisitorAdapter styleVisitor = new CatalogVisitorAdapter() {
            @Override
            public void visit(Catalog catalog) {
                for (StyleInfo style: catalog.getStyles()) {
                    style.accept(this);
                }
            }
            @Override
            public void visit(WorkspaceInfo workspace) {
                for( StoreInfo store : cat.getStoresByWorkspace(workspace,StoreInfo.class)){
                    try {
                        store.accept(this);
                    }
                    catch( Throwable t){
                        LOG.log(Level.FINE,String.format("Trouble checking store %s for icon use:%s", store.getName(),t));
                    }
                }
                for( LayerGroupInfo map : cat.getLayerGroupsByWorkspace(workspace)){
                    try {
                        map.accept(this);
                    }
                    catch( Throwable t){
                        LOG.log(Level.FINE,String.format("Trouble checking map %s for icon use:%s", map.getName(),t));
                    }    
                }
                for( StyleInfo style : cat.getStylesByWorkspace(workspace)){
                    try {
                        style.accept(this);
                    }
                    catch( Throwable t){
                        LOG.log(Level.FINE,String.format("Trouble checking style %s for icon use:%s", style.getName(),t));
                    }
                }
            }
            public void visit( DataStoreInfo dataStore ) {
                for (ResourceInfo resource : cat.getResourcesByStore(dataStore, ResourceInfo.class)) {
                    resource.accept(this);
                }
            }
            
            public void visit( CoverageStoreInfo coverageStore ) {
                for (ResourceInfo resource : cat.getResourcesByStore(coverageStore, ResourceInfo.class)) {
                    resource.accept(this);
                }
            }
            
            public void visit( WMSStoreInfo wmsStore ) {
                for (ResourceInfo resource : cat.getResourcesByStore(wmsStore, ResourceInfo.class)) {
                    resource.accept(this);
                }
            }
            
            public void visit (ResourceInfo resource) {
                for (LayerInfo layer : cat.getLayers(resource)) {
                    layer.accept(this);
                }
            }
            @Override
            public void visit(LayerGroupInfo layerGroup) {
                for( PublishedInfo child : layerGroup.getLayers() ){
                    try {
                        child.accept(this);
                    }
                    catch( Throwable t){
                        LOG.log(Level.FINE,String.format("Trouble checking layer %s for icon use:%s", child.getName(),t));
                    }
                }
            }
            @Override
            public void visit(LayerInfo layer) {
                StyleInfo style = layer.getDefaultStyle();
                if( style != null ){
                    try {
                        style.accept(this);
                    }
                    catch( Throwable t ){
                        LOG.log(Level.FINE,String.format("Trouble checking layer %s for icon use:%s", layer.getName(),t));
                    }
                }
                else {
                    LOG.fine(String.format("Layer %s has no default style", layer.getName()));
                }
            }
            @Override
            public void visit(StyleInfo s) {
                Style style = null;
                try {
                    style = s.getStyle();
                } catch (IOException e) {
                    LOG.log(Level.FINE,"Unable to access style "+s.getName()+":"+e,e);
                }        
                if( style != null ){
                    style.accept(new StyleAdaptor() {
                        public Object visit(OnLineResource resource, Object data) {
                            try {
                                URI uri = resource.getLinkage();
                                if (uri != null) {
                                    String path = uri.toString();
                                    if( path != null ){
                                        if( path.lastIndexOf('/') != -1 ){
                                            path = path.substring(path.lastIndexOf('/')+1);
                                        }
                                        ((Set<String>) data).add(path);
                                    }
                                }
                            }
                            catch( Throwable t){
                                LOG.log(Level.FINE,String.format("Trouble OnLineResource %s for icon use:%s", resource,t));
                            }
                            return data;
                        }
                    }, externalGraphics);
                }
            }
        };
        if (ws == null) {
            cat.accept(styleVisitor);
        } else {
            ws.accept(styleVisitor);
        }
        
        return externalGraphics;
    }

    String fileExt(String filename) {
        return FilenameUtils.getExtension(filename.toLowerCase());
    }
}
