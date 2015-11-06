/* (c) 2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.api.controllers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerGroupHelper;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.wms.WebMapService;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.boundlessgeo.geoserver.AppConfiguration;
import com.boundlessgeo.geoserver.catalog.ThumbnailInvalidatingCatalogListener;
import com.google.common.io.Closeables;
import com.google.common.util.concurrent.Striped;

@Controller
@RequestMapping("/api/thumbnails")
public class ThumbnailController extends ApiController {
    
    private static Logger LOG = Logging.getLogger(ThumbnailController.class);
    static final String TYPE = "png";
    static final String MIME_TYPE = "image/png;+mode=8bit";
    public static final String EXTENSION = ".png";
    public static final String EXTENSION_HR = "@2x.png";
    public static final int THUMBNAIL_SIZE = 75;
    
    @Autowired
    @Qualifier("wmsServiceTarget")
    WebMapService wms;
    
    @Autowired
    AppConfiguration config;
    
    Striped<Semaphore> semaphores = Striped.semaphore(4, 1);
    
    @Autowired
    public ThumbnailController(GeoServer geoServer) {
        super(geoServer);
    }
    
    /**
     * Endpoint to retrieve a cached thumbnail for a map, generating it if it does not exist.
     * If the request parameter "hiRes=true" is provided, returns a high-resolution (x2) thumbnail.
     * @param wsName Workspace name
     * @param name Map name
     * @param hiRes Whether or not to return a high-res thumbnail. Optional, defaults to false
     * @param request
     * @return HttpEntity containing the thumbnail image as a byte array
     * @throws Exception
     */
    @RequestMapping(value = "/maps/{wsName:.+}/{name:.+}", method = RequestMethod.GET)
    public HttpEntity<byte[]> getMap(@PathVariable String wsName, 
            @PathVariable String name, 
            @RequestParam(value="hiRes", required=false, defaultValue="false") Boolean hiRes,
            HttpServletRequest request) throws Exception {
        
        Catalog catalog = geoServer.getCatalog();
        WorkspaceInfo ws = findWorkspace(wsName, catalog);
        LayerGroupInfo map = findMap(wsName, name, catalog);
        
        return get(ws, map, hiRes, request);
    }
    
    /**
     * Endpoint to retrieve a cached thumbnail for a layer, generating it if it does not exist.
     * If the request parameter "hiRes=true" is provided, returns a high-resolution (x2) thumbnail.
     * @param wsName Workspace name
     * @param name Layer name
     * @param hiRes Whether or not to return a high-res thumbnail. Optional, defaults to false
     * @param request
     * @return HttpEntity containing the thumbnail image as a byte array
     * @throws Exception
     */
    @RequestMapping(value = "/layers/{wsName:.+}/{name:.+}", method = RequestMethod.GET)
    public HttpEntity<byte[]> getLayer(@PathVariable String wsName, 
            @PathVariable String name, 
            @RequestParam(value="hiRes", required=false, defaultValue="false") Boolean hiRes,
            HttpServletRequest request) throws Exception {
        
        Catalog catalog = geoServer.getCatalog();
        WorkspaceInfo ws = findWorkspace(wsName, catalog);
        LayerInfo layer = findLayer(wsName, name, catalog);
        
        return get(ws, layer, hiRes, request);
    }
    
    /**
     * Retrieve or create the thumbnail for a PublishedInfo
     * @param ws Workspace for the layer
     * @param layer LayerInfo or LayerGroupInfo to get the thumbnail of
     * @param hiRes Flag to return hi-res (x2) thumbnail
     * @return HttpEntity containing the thumbnail image as a byte array
     * @throws Exception
     */
    public HttpEntity<byte[]> get(WorkspaceInfo ws, PublishedInfo layer, boolean hiRes, HttpServletRequest request) throws Exception {
        String path = thumbnailFilename(layer, hiRes);
        FileInputStream in = null;
        
        File thumbnailFile;
        //If the file has been deleted, recreate it
        if (!config.cacheFile(path).exists()) {
            createThumbnail(ws, layer, request);
        }
        try {
            thumbnailFile = config.cacheFile(path);
            in = new FileInputStream(thumbnailFile);
            byte[] bytes = IOUtils.toByteArray(in);
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(MIME_TYPE));
            headers.setLastModified(thumbnailFile.lastModified());
            return new HttpEntity<byte[]>(bytes, headers);
        } finally {
            if (in != null) { in.close(); }
        }
    }
    
    /**
     * Creates a thumbnail for the layer as a Resource, and updates the layer with the new thumbnail
     * @param ws The workspace of the layer
     * @param layer The layer or layerGroup to get the thumbnail for
     * @return The thumbnail image as a Resource
     * @throws Exception
     */
    protected void createThumbnail(WorkspaceInfo ws, PublishedInfo layer, HttpServletRequest baseRequest) throws Exception {
        //Sync against this map/layer
        Semaphore s = semaphores.get(layer);
        s.acquire();
        try {
            //(SUITE-1072) Initialize the thumbnail to a blank image in case the WMS request crashes geoserver
            BufferedImage blankImage = new BufferedImage(THUMBNAIL_SIZE*2, THUMBNAIL_SIZE*2, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = blankImage.createGraphics();
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, blankImage.getWidth(), blankImage.getHeight());
            writeThumbnail(layer, blankImage);
            
            //Set up getMap request
            String url = baseRequest.getScheme()+"://localhost:"+baseRequest.getLocalPort()
            + baseRequest.getContextPath()+"/"+ws.getName()+"/wms/reflect";
            
            url += "?FORMAT="+MIME_TYPE;
            
            ReferencedEnvelope bbox = null;
            if (layer instanceof LayerInfo) {
                url += "&LAYERS="+layer.getName();
                url += "&STYLES="+((LayerInfo)layer).getDefaultStyle().getName();
                bbox = ((LayerInfo)layer).getResource().boundingBox();
            } else if (layer instanceof LayerGroupInfo) {
                
                LayerGroupHelper helper = new LayerGroupHelper((LayerGroupInfo)layer);
                bbox = ((LayerGroupInfo)layer).getBounds();
                url+="&CRS="+CRS.toSRS(bbox.getCoordinateReferenceSystem());
                
                List<LayerInfo> layerList = helper.allLayersForRendering();
                if (layerList.size() > 0) {
                    url += "&LAYERS=";
                    for (int i = 0; i  < layerList.size(); i++) {
                        if (i > 0) {
                            url+=",";
                        }
                        url+=layerList.get(i) == null ? "" : layerList.get(i).getName();
                    }
                }
                List<StyleInfo> styleList = helper.allStylesForRendering();
                if (styleList.size() > 0) {
                    url += "&STYLES=";
                    for (int i = 0; i  < styleList.size(); i++) {
                        if (i > 0) {
                            url+=",";
                        }
                        if (styleList.get(i) == null) {
                            url += layerList.get(i).getDefaultStyle() == null ? "" : layerList.get(i).getDefaultStyle().getName();
                        } else {
                            url += styleList.get(i) == null ? "" : styleList.get(i).getName();
                        }
                    }
                }
            } else {
                throw new RuntimeException("layer must be one of LayerInfo or LayerGroupInfo");
            }
            //Set the size of the HR thumbnail
            //Take the smallest bbox dimension as the min dimension. We can then crop the other 
            //dimension to give a square thumbnail
            url+="&BBOX="+((float)bbox.getMinX())+","+((float)bbox.getMinY())+","
                         +((float)bbox.getMaxX())+","+((float)bbox.getMaxY());
            if (bbox.getWidth() < bbox.getHeight()) {
                url+="&WIDTH="+(2*THUMBNAIL_SIZE);
                url+="&HEIGHT="+(2*THUMBNAIL_SIZE*Math.round(bbox.getHeight()/bbox.getWidth()));
            } else {
                url+="&HEIGHT="+(2*THUMBNAIL_SIZE);
                url+="&WIDTH="+(2*THUMBNAIL_SIZE*Math.round(bbox.getWidth()/bbox.getHeight()));
            }
            
            //Run the getMap request through the WMS Reflector
            //WebMap response = wms.reflect(request);            
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedImage image = ImageIO.read(con.getInputStream());
            if (image == null) {
                throw new RuntimeException("Failed to encode thumbnail for "+ws.getName()+":"+layer.getName());
            }
            writeThumbnail(layer, image);
        } finally {
            s.release();
        }
    }
    
    /**
     * Clears any cached thumbnail information (called by {@link ThumbnailInvalidatingCatalogListener} when a layer is removed).
     * 
     * @param layer
     * @throws InterruptedException 
     */
    public void clearThumbnail(PublishedInfo layer) {
        Semaphore s = semaphores.get(layer);
        try {
            s.acquire();
        } catch (InterruptedException e) {
            LOG.finer("Unable to clear thumbnail for "+layer.prefixedName()+":"+e.getMessage());
            return;
        }
        try {
            File loRes = config.cacheFile(thumbnailFilename(layer));
            if( loRes.exists() ){
                boolean removed = loRes.delete();
                if (!removed) {
                    loRes.deleteOnExit();
                }
            }
            File hiRes = config.cacheFile(thumbnailFilename(layer,true));
            if( hiRes.exists() ){
                boolean removed = hiRes.delete();
                if (!removed) {
                    hiRes.deleteOnExit();
                }
            }
        } finally {
            s.release();
        }
    }
    
    protected void writeThumbnail(PublishedInfo layer, BufferedImage image) throws FileNotFoundException, IOException, InterruptedException {
      //Write the thumbnail files
        File loResFile = null;
        File hiResFile = null;
        FileOutputStream loRes = null;
        FileOutputStream hiRes = null;
        
        try {
            loResFile = config.createCacheFile(thumbnailFilename(layer));
            hiResFile = config.createCacheFile(thumbnailFilename(layer, true));
            loRes = new FileOutputStream(loResFile);
            hiRes = new FileOutputStream(hiResFile);
            
            ImageIO.write(scaleImage(image, 0.5, true), TYPE, loRes);
            //Don't scale, but crop to square
            ImageIO.write(scaleImage(image, 1.0, true), TYPE, hiRes);
        } catch (Exception e) {
            Closeables.close(loRes, true);
            Closeables.close(hiRes, true);
            
            //If there was an error, remove the files we created, as they will be invalid
            if (loResFile != null && loResFile.exists()) {
                loResFile.delete();
            }
            if (hiResFile != null && hiResFile.exists()) {
                hiResFile.delete();
            }
            //propagate the exception
            throw e;
        } finally {
            Closeables.close(loRes, true);
            Closeables.close(hiRes, true);
        }
    }
    /**
     * Utility method to generate a consistent thumbnail filename
     * @param layer to create the filename for
     * @return relative filename
     */
    public static final String thumbnailFilename(PublishedInfo layer) {
        return thumbnailFilename(layer, false);
    }
    
    /**
     * Utility method to generate a consistent thumbnail filename
     * @param layer to create the filename for
     * @param hiRes is this the name of a hi-res thumbnail file?
     * @return relative filename
     */
    public static final String thumbnailFilename(PublishedInfo layer, boolean hiRes) {
        if (hiRes) {
            return AppConfiguration.sanitizeFilename(layer.getId())+EXTENSION_HR;
        }
        return AppConfiguration.sanitizeFilename(layer.getId())+EXTENSION;
    }
    
    public static RenderedImage scaleImage(BufferedImage image, double scale) throws IOException {
        return scaleImage(image, scale, false);
    }
    /**
     * Utility method for scaling thumbnails. Scales byte[] image by a scale factor.
     * Optionally crops images to square.
     * @param src RenderedImage containing the input image
     * @param scale Scale amount
     * @param square Boolean flag to crop to a square image
     * @return RenderedImage containing the transformed image
     * @throws IOException
     */
    public static BufferedImage scaleImage(BufferedImage image, double scale, boolean square) throws IOException {
        int sx = 0, sy = 0;
        int swidth = image.getWidth();
        int sheight = image.getHeight();
        
        if (square) {
            if (image.getHeight() > image.getWidth()) {
                sy = (int) ((image.getHeight() - image.getWidth())/2.0);
                sheight = swidth;
            } else if (image.getHeight() < image.getWidth()) {
                sx = (int) ((image.getWidth() - image.getHeight())/2.0);
                swidth = sheight;
            }
        }
        int width = (int) (swidth*scale);
        int height = (int) (sheight*scale);
        
        BufferedImage scaled = new BufferedImage(image.getColorModel(), 
                image.getRaster().createCompatibleWritableRaster(width, height), 
                image.isAlphaPremultiplied(), null);
        
        Graphics g = scaled.getGraphics();
        g.drawImage(image, 0, 0, width, height, sx, sy, sx+swidth, sy+sheight, null);
        g.dispose();
        
        return scaled;
    }
}
