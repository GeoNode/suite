/* (c) 2014-2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver;

import com.boundlessgeo.geoserver.api.controllers.IO;
import com.boundlessgeo.geoserver.api.controllers.IconController;
import com.boundlessgeo.geoserver.api.controllers.ImportController;
import com.boundlessgeo.geoserver.api.controllers.StoreController;
import com.boundlessgeo.geoserver.api.controllers.WorkspaceController;
import com.boundlessgeo.geoserver.json.JSONArr;
import com.boundlessgeo.geoserver.json.JSONObj;
import com.boundlessgeo.geoserver.util.NameUtil;
import com.boundlessgeo.geoserver.util.RecentObjectCache;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourcePool;
import org.geoserver.catalog.SLDHandler;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.SettingsInfo;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.importer.Importer;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geoserver.rest.util.RESTUtils;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.geoserver.ysld.YsldHandler;
import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.geotools.util.NullProgressListener;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

public class AppIntegrationTest extends GeoServerSystemTestSupport {

    @Override
    protected void setUpTestData(SystemTestData testData) throws Exception {
        super.setUpTestData(testData);
        testData.setUpWcs10RasterLayers();
    }

    @Before
    public void removeMaps() {
        removeLayerGroup("sf", "map1");
        removeLayerGroup("sf", "map2");
        
        removeLayerGroup("cgf", "map1");
        removeLayerGroup("cgf", "map2");
    }
    
    @Before
    public void removeLayers() {
        removeLayer("gs", "foo");
        removeLayer("sf", "foo");
        removeLayer("cdf", "foo");
        
        removeLayer("gs", "point");
        removeLayer("gs", "point space");
        removeLayer("gs", "point_space");
        
        removeLayer("cgf", "renamedLayer");
        removeLayer("cgf", "Points-map");
        removeLayer("cgf", "Lines-map");
    }
    
    @Before
    public void removeFiles() {
        removeStore("gs","point");
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point/point.shp").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point/point.prj").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point/point.shx").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point/point.dbf").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point/point.shp").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point/point.prj").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point/point.shx").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point/point.dbf").delete();
        
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point_space/point space.shp").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point_space/point space.prj").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point_space/point space.shx").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "data/gs/point_space/point space.dbf").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point_space/point space.shp").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point_space/point space.prj").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point_space/point space.shx").delete();
        new File(getCatalog().getResourceLoader().getBaseDirectory(), "uploads/gs/point_space/point space.dbf").delete();
    }

    @Test
    public void testPageLayers() throws Exception {
        JSONObject obj = (JSONObject) getAsJSON(("/app/api/layers/sf"));
        JSONArray arr = obj.getJSONArray("layers");
        assertEquals(3, arr.size());

        obj = (JSONObject) getAsJSON(("/app/api/layers/sf?page=1&count=1"));
        arr = obj.getJSONArray("layers");
        assertEquals(1, arr.size());
    }

    @Test
    public void testPageMaps() throws Exception {
        Catalog cat = getCatalog();
        CatalogBuilder catBuilder = new CatalogBuilder(cat);

        LayerInfo pgf = cat.getLayerByName("sf:PrimitiveGeoFeature");

        LayerGroupInfo map = cat.getFactory().createLayerGroup();
        map.setWorkspace(cat.getWorkspaceByName("sf"));
        map.setName("map1");
        map.getLayers().add(pgf);
        map.getStyles().add(null);
        catBuilder.calculateLayerGroupBounds(map);
        cat.add(map);

        map = cat.getFactory().createLayerGroup();
        map.setWorkspace(cat.getWorkspaceByName("sf"));
        map.setName("map2");
        map.getLayers().add(pgf);
        map.getStyles().add(null);
        catBuilder.calculateLayerGroupBounds(map);
        cat.add(map);

        JSONObject obj = (JSONObject) getAsJSON(("/app/api/maps/sf"));
        assertEquals(2, obj.getInt("total"));
        assertEquals(0, obj.getInt("page"));
        assertEquals(2, obj.getInt("count"));

        JSONArray arr = obj.getJSONArray("maps");
        assertEquals(2, arr.size());

        obj = (JSONObject) getAsJSON(("/app/api/maps/sf?page=1&count=1"));
        assertEquals(2, obj.getInt("total"));
        assertEquals(1, obj.getInt("page"));
        assertEquals(1, obj.getInt("count"));
        arr = obj.getJSONArray("maps");
        assertEquals(1, arr.size());
    }
    
    @Test
    public void testPageStores() throws Exception {
        JSONObject obj = (JSONObject) getAsJSON(("/app/api/stores/sf"));
        JSONArray arr = obj.getJSONArray("stores");
        assertEquals(1, arr.size());

        obj = (JSONObject) getAsJSON(("/app/api/stores/sf?page=1&count=1"));
        arr = obj.getJSONArray("stores");
        assertEquals(0, arr.size());
    }

    @Test
    public void testListAttributes() throws Exception {
        Catalog cat = getCatalog();
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("get");
        
        StoreController ctrl = new StoreController(getGeoServer());
        JSONObj obj = ctrl.attributes("sf", "sf", "PrimitiveGeoFeature", 10, request);
        JSONArr attributes = obj.object("schema").array("attributes");
        JSONArr values = obj.array("values");
        
        //Page through actual features and ensure values match what is returned
        StoreInfo store = cat.getStoreByName("sf", "sf", StoreInfo.class);
        DataAccess data = ((DataStoreInfo)store).getDataStore(new NullProgressListener());
        FeatureSource source = data.getFeatureSource(new NameImpl("PrimitiveGeoFeature"));
        
        Query query = new Query(Query.ALL);
        query.setMaxFeatures(10);
        FeatureIterator features = source.getFeatures(query).features();
        int featureIndex = 0;
        while (features.hasNext()) {
            Feature feature = features.next();
            JSONArr featureJSON = (JSONArr) values.at(featureIndex);
            Property[] properties = feature.getProperties().toArray(
                    new Property[feature.getProperties().size()]);
            
            for (int i = 0; i < attributes.size(); i++) {
                
                //Verify the schema matches the feature
                JSONObj attribute = attributes.object(i);
                assertEquals(attribute.get("type"), properties[i].getDescriptor().getType().getBinding().getSimpleName());
                assertEquals(attribute.get("name"), properties[i].getDescriptor().getName().getLocalPart());
                
                //Verify the value matches the feature
                assertEquals(featureJSON.str(i), 
                        properties[i].getValue() == null ? null : properties[i].getValue().toString());
            }
            featureIndex++;
        }
    }

    @Test
    public void testImportShapefileAsZip() throws Exception {
        Catalog catalog = getCatalog();
        //Test default root of "data"
        RESTUtils.loadMapFromGlobal().remove("root");
        assertNull(catalog.getLayerByName("gs:point"));

        Importer importer =
            GeoServerExtensions.bean(Importer.class, applicationContext);
        ImportController ctrl = new ImportController(getGeoServer(), importer);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("post");

        createMultiPartFormContent(request, "form-data; name=\"upload\"; filename=\"point.zip\"", "application/zip",
                IOUtils.toByteArray(getClass().getResourceAsStream("point.shp.zip")));

        JSONObj result = ctrl.importFile("gs", request);
        Long id = Long.parseLong(result.str("id"));
        
        //Wait for the import to complete
        result = pollImport(ctrl, "gs", id, "pending", request);
        verifyImporterEndpoint(result.str("importerEndpoint"), request);
        assertNotNull(result);
        result = ctrl.update("gs", id, getUpdateTasks(result), request);
        result = pollImport(ctrl, "gs", id, "complete", request);
        assertNotNull(result);
        
        assertEquals(1, result.array("tasks").size());
        JSONObj obj = result.array("tasks").object(0);

        assertEquals("gs", obj.object("layer").str("workspace"));
        assertEquals("point", obj.object("layer").str("name"));

        LayerInfo l = catalog.getLayerByName("gs:point");
        StoreInfo s = catalog.getStoreByName("gs", "point", StoreInfo.class);
        assertNotNull(l);
        assertNotNull(s);
        //Wait for the taskListener to move the imported file
        Thread.sleep(500);
        assertTrue(new File(catalog.getResourceLoader().getBaseDirectory(), "data/gs/point/point.shp").exists());
        assertTrue(new File(new URL(ResourcePool.getParams(s.getConnectionParameters(), catalog.getResourceLoader()).get("url").toString()).getFile()).exists());
        assertEquals(new File(catalog.getResourceLoader().getBaseDirectory(), "data/gs/point").getAbsoluteFile(),
                new File(new URL(ResourcePool.getParams(s.getConnectionParameters(), catalog.getResourceLoader()).get("url").toString()).getFile()).getAbsoluteFile());

        // ensure style in workspace
        StyleInfo style = l.getDefaultStyle();
        assertNotNull(style.getWorkspace());
        
        //Try to reimport the same store - should succeed
        createMultiPartFormContent(request, "form-data; name=\"upload\"; filename=\"point.zip\"", "application/zip",
                IOUtils.toByteArray(getClass().getResourceAsStream("point.shp.zip")));
        obj = ctrl.importFile("gs", request);
        
        assertNotNull(obj.get("id"));

    }
    
    @Test
    public void testImportShapefiles() throws Exception {
        Catalog catalog = getCatalog();
        //Test REST global root
        GeoServerInfo gsInfo = GeoServerExtensions.bean(GeoServer.class).getGlobal();
        SettingsInfo info = gsInfo.getSettings();
        info.getMetadata().put("root", catalog.getResourceLoader().findOrCreateDirectory("uploads").getAbsolutePath());
        getGeoServer().save(gsInfo);
        assertNull(catalog.getLayerByName("gs:point"));

        Importer importer =
            GeoServerExtensions.bean(Importer.class, applicationContext);
        ImportController ctrl = new ImportController(getGeoServer(), importer);
        StoreController storeCtrl = new StoreController(getGeoServer());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("post");
        
        //Import as separate files
        MimeMultipart body = initMultiPartFormContent(request);

        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point.dbf\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point.dbf")));
        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point.prj\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point.prj")));
        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point.shp\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point.shp")));
        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point.shx\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point.shx")));
        
        createMultiPartFormContent(body, request);


        JSONObj result = ctrl.importFile("gs", request);
        Long id = Long.parseLong(result.str("id"));
        
        //Wait for the import to complete
        result = pollImport(ctrl, "gs", id, "pending", request);
        assertNotNull(result);
        verifyImporterEndpoint(result.str("importerEndpoint"), request);
        result = ctrl.update("gs", id, getUpdateTasks(result), request);
        result = pollImport(ctrl, "gs", id, "complete", request);
        assertNotNull(result);
        
        assertEquals(1, result.array("tasks").size());
        JSONObj obj = result.array("tasks").object(0);

        assertEquals("gs", obj.object("layer").str("workspace"));
        assertEquals("point", obj.object("layer").str("name"));

        LayerInfo l = catalog.getLayerByName("gs:point");
        StoreInfo s = catalog.getStoreByName("gs", "point", StoreInfo.class);
        assertNotNull(l);
        assertNotNull(s);
        //Wait for the taskListener to move the imported file
        Thread.sleep(500);
        assertTrue(new File(catalog.getResourceLoader().getBaseDirectory(), "uploads/gs/point/point.shp").exists());
        assertTrue(new File(new URL(ResourcePool.getParams(s.getConnectionParameters(), catalog.getResourceLoader()).get("url").toString()).getFile()).exists());
        assertEquals(new File(catalog.getResourceLoader().getBaseDirectory(), "uploads/gs/point").getAbsoluteFile(),
                new File(new URL(ResourcePool.getParams(s.getConnectionParameters(), catalog.getResourceLoader()).get("url").toString()).getFile()).getAbsoluteFile());

        // ensure style in workspace
        StyleInfo style = l.getDefaultStyle();
        assertNotNull(style.getWorkspace());
        
        //Delete the store and re-upload:
        MockHttpServletRequest deleteRequest = new MockHttpServletRequest();
        deleteRequest.setContextPath("/geoserver");
        deleteRequest.setRequestURI("/geoserver/hello");
        deleteRequest.setMethod("delete");
        storeCtrl.delete("gs", "point", true, deleteRequest);
        assertFalse(new File(catalog.getResourceLoader().getBaseDirectory(), "uploads/gs/point/point.shp").exists());
        result = ctrl.importFile("gs", request);
        id = Long.parseLong(result.str("id"));
        //Wait for the import to complete
        result = pollImport(ctrl, "gs", id, "pending", request);
        assertNotNull(result);
        result = ctrl.update("gs", id, getUpdateTasks(result), request);
        result = pollImport(ctrl, "gs", id, "complete", request);
        assertNotNull(result);
        assertEquals(1, result.array("tasks").size());
        assertEquals("COMPLETE", result.array("tasks").object(0).get("status"));
    }
    
    @Test
    public void testImportShapefilesWithSpaceInName() throws Exception {
        Catalog catalog = getCatalog();
        //Test REST global root
        GeoServerInfo gsInfo = GeoServerExtensions.bean(GeoServer.class).getGlobal();
        SettingsInfo info = gsInfo.getSettings();
        info.getMetadata().put("root", catalog.getResourceLoader().findOrCreateDirectory("uploads").getAbsolutePath());
        getGeoServer().save(gsInfo);
        assertNull(catalog.getLayerByName("gs:point_space"));

        Importer importer =
            GeoServerExtensions.bean(Importer.class, applicationContext);
        ImportController ctrl = new ImportController(getGeoServer(), importer);
        StoreController storeCtrl = new StoreController(getGeoServer());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("post");
        
        //Import as separate files
        MimeMultipart body = initMultiPartFormContent(request);

        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point space.dbf\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point space.dbf")));
        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point space.prj\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point space.prj")));
        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point space.shp\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point space.shp")));
        appendMultiPartFormContent(body, "form-data; name=\"upload\"; filename=\"point space.shx\"", "application/octet-stream",
                IOUtils.toByteArray(getClass().getResourceAsStream("point space.shx")));
        
        createMultiPartFormContent(body, request);


        JSONObj result = ctrl.importFile("gs", request);
        Long id = Long.parseLong(result.str("id"));
        
        //Wait for the import to complete
        result = pollImport(ctrl, "gs", id, "pending", request);
        assertNotNull(result);
        verifyImporterEndpoint(result.str("importerEndpoint"), request);
        result = ctrl.update("gs", id, getUpdateTasks(result), request);
        result = pollImport(ctrl, "gs", id, "complete", request);
        assertNotNull(result);
        
        LayerInfo l = catalog.getLayerByName("gs:point_20space");
        StoreInfo s = catalog.getStoreByName("gs", "point_space", StoreInfo.class);
        assertNotNull(l);
        assertNotNull(s);
        //Wait for the taskListener to move the imported file
        Thread.sleep(500);
        assertTrue(new File(catalog.getResourceLoader().getBaseDirectory(), "uploads/gs/point_space/point space.shp").exists());
        assertTrue(new File(new URL(ResourcePool.getParams(s.getConnectionParameters(), catalog.getResourceLoader()).get("url").toString()).getFile()).exists());
        assertEquals(new File(catalog.getResourceLoader().getBaseDirectory(), "uploads/gs/point_space").getAbsoluteFile(),
                new File(new URL(ResourcePool.getParams(s.getConnectionParameters(), catalog.getResourceLoader()).get("url").toString()).getFile()).getAbsoluteFile());
        
        StyleInfo style = l.getDefaultStyle();
        assertEquals("point_space", l.getDefaultStyle().getName());
        assertEquals("point_20space", l.getResource().getName());
    }
    
    @Test
    public void testImportDb() throws Exception {
        Catalog catalog = getCatalog();
        assertNull(catalog.getLayerByName("gs:point"));
        
        Importer importer =
            GeoServerExtensions.bean(Importer.class, applicationContext);
        ImportController ctrl = new ImportController(getGeoServer(), importer);
        
        try (H2TestData data = new H2TestData()) {
            
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContextPath("/geoserver");
            request.setRequestURI("/geoserver/hello");
            request.setMethod("post");
            
            JSONObj result = data.createConnectionParameters();
            result = ctrl.importDb("gs", result, request);
            
            Long id = Long.parseLong(result.str("id"));
            result = pollImport(ctrl, "gs", id, "pending", request);
            assertNotNull(result);
            verifyImporterEndpoint(result.str("importerEndpoint"), request);
            assertTrue(result.integer("tasksTotal") > 0);
            List<String> names = Arrays.asList(new String[]{"ft1","ft2","ft3"});
            JSONArr tasks = new JSONArr();
            for (JSONObj o : result.array("tasks").objects()) {
                if (names.contains(o.get("name")) && o.get("status").equals("READY")) {
                    tasks.add(new JSONObj().put("task", o.get("task").toString()));
                    assertEquals("table", o.get("type"));
                }
            }
            assertTrue(tasks.size() > 0);
            JSONObj response = new JSONObj();
            response.put("tasks", tasks);
            
            result = ctrl.update("gs", id, response, request);
            result = pollImport(ctrl, "gs", id, "complete", request);
            assertNotNull(result);
            
            int complete = 0;
            int ready = 0;
            int no_crs = 0;
            int failed = 0;
            tasks = new JSONArr();
            
            for (JSONObj o : result.array("tasks").objects()) {
                if (o.get("status").equals("READY")) {
                    ready++;
                }
                if (o.get("status").equals("NO_CRS")) {
                    no_crs++;
                    String srs = "EPSG:4326";
                    tasks.add(new JSONObj().put("task", o.get("task").toString())
                                           .put("proj", IO.proj(new JSONObj(), CRS.decode(srs), srs)));
                }
                if (o.get("status").equals("COMPLETE")) {
                    complete++;
                }
                if (o.get("status").equals("ERROR") || o.get("status").equals("NO_BOUNDS")) {
                    failed++;
                }
            }
            assertEquals(0, ready);
            assertEquals(1, complete);
            assertEquals(2, no_crs);
            assertEquals(0, failed);
            
            response = new JSONObj();
            response.put("tasks", tasks);
            
            result = ctrl.update("gs", id, response, request);
            //Wait for the import to complete
            result = pollImport(ctrl, "gs", id, "complete", request);
            assertNotNull(result);
            
            for (JSONObj o : result.array("tasks").objects()) {
                assertEquals("COMPLETE", o.get("status"));
            }
            
            //Try to reimport the same store - should fail and return existing store
            result = data.createConnectionParameters();
            result = ctrl.importDb("gs", result, request);
            
            assertNotNull(result.get("store"));
            assertNull(result.get("id"));
        }
    }
    
    @Test
    public void testImportGeoJSONintoDb() throws IOException, Exception {
        Catalog catalog = getCatalog();
        StoreInfo targetStore = catalog.getStoreByName("sf", "sf", StoreInfo.class);
        assertNotNull(targetStore);
        
        Importer importer =
                GeoServerExtensions.bean(Importer.class, applicationContext);
        ImportController ctrl = new ImportController(getGeoServer(), importer);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("post");
        
        createMultiPartFormContent(request, "form-data; name=\"upload\"; filename=\"point.json\"", "application/json",
                IOUtils.toByteArray(getClass().getResourceAsStream("point.json")));
        
        JSONObj result = ctrl.importFile("sf", "sf", request);
        Long id = Long.parseLong(result.str("id"));
        
        //Wait for the import to complete
        result = pollImport(ctrl, "gs", id, "pending", request);
        assertNotNull(result);
        verifyImporterEndpoint(result.str("importerEndpoint"), request);
        result = ctrl.update("gs", id, getUpdateTasks(result), request);
        result = pollImport(ctrl, "gs", id, "complete", request);
        assertNotNull(result);
        
        assertEquals(1, result.array("tasks").size());
        JSONObj obj = result.array("tasks").object(0);
        assertEquals("COMPLETE", obj.get("status"));

        assertEquals("sf", obj.object("layer").str("workspace"));
        assertEquals("point", obj.object("layer").str("name"));

        LayerInfo l = catalog.getLayerByName("sf:point");
        assertNotNull(l);
        FeatureTypeInfo f = (FeatureTypeInfo)l.getResource();
        assertEquals(targetStore, f.getStore());

        // ensure style in workspace
        StyleInfo s = l.getDefaultStyle();
        assertNotNull(s.getWorkspace());
    }
    private JSONObj getUpdateTasks(JSONObj result) {
        JSONArr tasks = new JSONArr();
        for (JSONObj o : result.array("tasks").objects()) {
            if (o.get("status").equals("READY")) {
                tasks.add(new JSONObj().put("task", o.get("task").toString()));
            }
        }
        JSONObj response = new JSONObj();
        return response.put("tasks", tasks);
    }
    
    private void verifyImporterEndpoint(String url, HttpServletRequest request) throws Exception {
        String baseURL = ResponseUtils.baseURL(request)+"geoserver/";
        String relativeURL = url.substring(baseURL.length());
        com.mockrunner.mock.web.MockHttpServletResponse res = getAsServletResponse(relativeURL);
        assertEquals("application/json", res.getContentType());
    }
    private JSONObj pollImport(ImportController ctrl, String ws, Long id, String state, HttpServletRequest request) {
        int attempts = 100;
        int interval = 10;
        
        for (int i = 0; i < attempts; i++) {
            try {
                Thread.sleep(interval);
                JSONObj obj = ctrl.get(ws, id, request);
                if (obj.get("state") != null && obj.get("state").equals(state)) {
                    return obj;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        
        return null;
    }
    @Test
    public void testImportInfo() throws IOException {
        Catalog catalog = getCatalog();
        StoreInfo targetStore = catalog.getStoreByName("sf", "sf", StoreInfo.class);
        assertNotNull(targetStore);
        
        Importer importer =
                GeoServerExtensions.bean(Importer.class, applicationContext);
        ImportController ctrl = new ImportController(getGeoServer(), importer);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("post");
        
        JSONObj obj = ctrl.info("sf");
        
        assertEquals("sf", obj.str("workspace"));
        assertNotNull(obj.get("spaceAvailable"));
        assertEquals(obj.get("spaceAvailable"), obj.get("tmpSpace"));
    }
    
    @Test
    public void testIconsUploadDelete() throws Exception {
        Catalog catalog = getCatalog();
        IconController ctrl = new IconController(getGeoServer());
        
        // test upload
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/api/icons/cite");
        request.setMethod("post");

        createMultiPartFormContent(request, "form-data; name=\"icon\"; filename=\"STYLE.PROPERTIES\"",
            "text/x-java-properties", "square=LINESTRING((0 0,0 1,1 1,1 0,0 0))".getBytes() );

        JSONArr arr = ctrl.create("cite", request);
        assertEquals( 1, arr.size() );
        
        Resource r = catalog.getResourceLoader().get("workspaces/cite/styles/STYLE.PROPERTIES");
        assertEquals("created", Resource.Type.RESOURCE, r.getType() );
        
        // test delete
        MockHttpServletRequestBuilder delete = delete("/api/icons/cite/icon.png");
        ctrl.delete("cite","STYLE.PROPERTIES");
        
        r = catalog.getResourceLoader().get("workspaces/cite/styles/STYLE.PROPERTIES");
        assertEquals("deleted", Resource.Type.UNDEFINED, r.getType() );
        
        //Global style directory
        
        // test upload
        request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/api/icons");
        request.setMethod("post");

        createMultiPartFormContent(request, "form-data; name=\"icon\"; filename=\"STYLE.PROPERTIES\"",
            "text/x-java-properties", "square=LINESTRING((0 0,0 1,1 1,1 0,0 0))".getBytes() );

        arr = ctrl.create(request);
        assertEquals( 1, arr.size() );
        
        r = catalog.getResourceLoader().get("styles/STYLE.PROPERTIES");
        assertEquals("created", Resource.Type.RESOURCE, r.getType() );
        
        // test delete
        delete = delete("/api/icons/icon.png");
        ctrl.delete("STYLE.PROPERTIES");
        
        r = catalog.getResourceLoader().get("styles/STYLE.PROPERTIES");
        assertEquals("deleted", Resource.Type.UNDEFINED, r.getType() );
    }

    @Test
    public void testWorkspaceExport() throws Exception {
        MockHttpServletResponse response = doWorkspaceExport("sf");

        assertEquals("application/zip", response.getContentType());
        assertEquals("attachment; filename=\"sf.zip\"", response.getHeader("Content-Disposition"));

        Path tmp = Files.createTempDirectory(Paths.get("target"), "export");
        org.geoserver.data.util.IOUtils.decompress(
            new ByteArrayInputStream(response.getContentAsByteArray()), tmp.toFile());

        assertTrue(tmp.resolve("bundle.json").toFile().exists());
    }

    @Test
    public void testWorkspaceImport() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        createMultiPartFormContent(request, "form-data; name=\"file\"; filename=\"sf.zip\"", "application/zip",
            doWorkspaceExport("sf").getContentAsByteArray());

        Catalog cat = getCatalog();
        assertNull(cat.getLayerByName("gs:PrimitiveGeoFeature"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        WorkspaceController ctrl = new WorkspaceController(getGeoServer(), new RecentObjectCache());
        ctrl.inport("gs", request, response);

        assertNotNull(cat.getLayerByName("gs:PrimitiveGeoFeature"));
    }

    MockHttpServletResponse doWorkspaceExport(String wsName) throws Exception {
        WorkspaceController ctrl = new WorkspaceController(getGeoServer(), new RecentObjectCache());

        MockHttpServletResponse response = new MockHttpServletResponse();
        ctrl.export(wsName, response);

        return response;
    }

    void createMultiPartFormContent(MockHttpServletRequest request, String contentDisposition, String contentType,
        byte[] content) throws Exception {
        MimeMultipart body = new MimeMultipart();
        request.setContentType(body.getContentType());
        InternetHeaders headers = new InternetHeaders();
        headers.setHeader("Content-Disposition", contentDisposition);
        headers.setHeader("Content-Type", contentType);
        body.addBodyPart(new MimeBodyPart(headers, content ));

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        body.writeTo(bout);
        request.setContent(bout.toByteArray());
    }
    
    void createMultiPartFormContent(MimeMultipart body, MockHttpServletRequest request) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        body.writeTo(bout);
        request.setContent(bout.toByteArray());
    }
    
    MimeMultipart initMultiPartFormContent(MockHttpServletRequest request) throws Exception {
        MimeMultipart body = new MimeMultipart();
        request.setContentType(body.getContentType());
        
        return body;
    }
    
    MimeMultipart appendMultiPartFormContent(MimeMultipart body, String contentDisposition, String contentType,
            byte[] content) throws Exception {
        InternetHeaders headers = new InternetHeaders();
        headers.setHeader("Content-Disposition", contentDisposition);
        headers.setHeader("Content-Type", contentType);
        body.addBodyPart(new MimeBodyPart(headers, content ));

        return body;
    }

    @Test
    public void testCopyMapWithLayers() throws Exception {
        Catalog catalog = getCatalog();
        CatalogBuilder catalogBuilder = new CatalogBuilder(catalog);
        
        LayerInfo points = catalog.getLayerByName("cgf:Points");
        LayerInfo lines = catalog.getLayerByName("cgf:Lines");
        
        LayerGroupInfo map = catalog.getFactory().createLayerGroup();
        map.setWorkspace(catalog.getWorkspaceByName("cgf"));
        map.setName("map1");
        map.getLayers().add(lines);
        map.getLayers().add(points);
        map.getStyles().add(null);
        map.getStyles().add(null);
        catalogBuilder.calculateLayerGroupBounds(map);
        catalog.add(map);
        
        assertNotNull(catalog.getLayerGroupByName("cgf:map1"));
        assertNotNull(catalog.getLayerByName("cgf:Points"));
        assertNotNull(catalog.getLayerByName("cgf:Lines"));
        assertNull(catalog.getLayerGroupByName("cgf:map2"));
        assertNull(catalog.getLayerByName("cgf:renamedLayer"));
        assertNull(catalog.getLayerByName("cgf:Lines-map"));
        

        JSONObj obj = new JSONObj();
        obj.put("name", "map2");
        obj.put("copylayers", true);
        obj.putArray("layers").addObject()
            .put("name", "renamedLayer")
            .putObject("layer")
                .put("name", "Points")
                .put("workspace", "cgf");
        

        com.mockrunner.mock.web.MockHttpServletResponse resp =
            putAsServletResponse("/app/api/maps/cgf/map1/copy", obj.toString(), MediaType.APPLICATION_JSON_VALUE);
        assertEquals(200,resp.getStatusCode());

        assertNotNull(catalog.getLayerGroupByName("cgf:map2"));
        assertNotNull(catalog.getLayerByName("cgf:renamedLayer"));
        assertNotNull(catalog.getLayerByName("cgf:Lines-map"));
    }
    
    @Test
    public void testCreateLayerFromCopy() throws Exception {
        Catalog catalog = getCatalog();
        assertNull(catalog.getLayerByName("sf:foo"));

        JSONObj obj = new JSONObj();
        obj.put("name", "foo");
        obj.putObject("layer")
            .put("name", "PrimitiveGeoFeature")
            .put("workspace", "sf");

        com.mockrunner.mock.web.MockHttpServletResponse resp =
            postAsServletResponse("/app/api/layers/sf", obj.toString(), MediaType.APPLICATION_JSON_VALUE);
        assertEquals(201,resp.getStatusCode());

        assertNotNull(catalog.getLayerByName("sf:foo"));
    }

    @Test
    public void testCreateLayerFromResource() throws Exception {
        Catalog catalog = getCatalog();
        assertNull(catalog.getLayerByName("sf:foo"));

        JSONObj obj = new JSONObj();
        obj.put("name", "foo");
        obj.putObject("resource")
            .put("name", "PrimitiveGeoFeature")
            .put("store", "sf")
            .put("workspace", "sf");

        com.mockrunner.mock.web.MockHttpServletResponse resp =
                postAsServletResponse("/app/api/layers/sf", obj.toString(), MediaType.APPLICATION_JSON_VALUE);
        assertEquals(201, resp.getStatusCode());

        assertNotNull(catalog.getLayerByName("sf:foo"));
    }

    @Test
    public void testCreateLayerFromRasterResource() throws Exception {
        Catalog catalog = getCatalog();
        assertNull(catalog.getLayerByName("cdf:foo"));
        assertNotNull(catalog.getLayerByName("cdf:usa"));

        JSONObj obj = new JSONObj();
        obj.put("name", "foo");
        obj.putObject("resource")
                .put("name", "usa")
                .put("store", "usa")
                .put("workspace", "cdf");

        com.mockrunner.mock.web.MockHttpServletResponse resp =
            postAsServletResponse("/app/api/layers/cdf", obj.toString(), MediaType.APPLICATION_JSON_VALUE);
        assertEquals(201, resp.getStatusCode());

        assertNotNull(catalog.getLayerByName("cdf:foo"));

    }
    
    @Test
    public void testPutStyleExistingSLD() throws Exception {
        Catalog catalog = getCatalog();
        LayerInfo layer = catalog.getLayerByName("sf:PrimitiveGeoFeature");
        assertNotNull(layer.getDefaultStyle());
        assertEquals(SLDHandler.FORMAT, layer.getDefaultStyle().getFormat());
        
        String sldName = layer.getDefaultStyle().getName();
        
        com.mockrunner.mock.web.MockHttpServletResponse resp =
                putAsServletResponse("/app/api/layers/sf/PrimitiveGeoFeature/style", "title: ysld", YsldHandler.MIMETYPE);
        assertEquals(200,resp.getStatusCode());

        layer = catalog.getLayerByName("sf:PrimitiveGeoFeature");
        assertNotNull(layer.getDefaultStyle());
        assertEquals(2, layer.getStyles().size());
        assertEquals(sldName+"_YSLD", layer.getDefaultStyle().getName());
        assertEquals(YsldHandler.FORMAT, layer.getDefaultStyle().getFormat());
    }
    
    /* (SUITE-1072) Full WMS URL is not compatible with GeoServerSystemTestSupport
     * Removing test case until a viable workaround can be found
    @Test
    public void testThumbnail() throws Exception {
        ThumbnailController ctrl = applicationContext.getBean(ThumbnailController.class);
        LayerController layerCtrl = applicationContext.getBean(LayerController.class);
        AppConfiguration config = applicationContext.getBean(AppConfiguration.class);
        
        //Precision on file.lastModified for the current architecture
        int filePrecision = 1000;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            //Still keep a small delay, just in case
            filePrecision = 10;
        }
        
        //Setup map
        Catalog catalog = getCatalog();
        CatalogBuilder catBuilder = new CatalogBuilder(catalog);
        
        LayerInfo layer = catalog.getLayerByName("sf:PrimitiveGeoFeature");
        
        StyleGenerator styleGenerator = new StyleGenerator(catalog);
        StyleInfo style = styleGenerator.createStyle((FeatureTypeInfo)layer.getResource());
        
        catalog.add(style);
        layer.setDefaultStyle(style);
        catalog.save(layer);
        
        LayerGroupInfo map = catalog.getFactory().createLayerGroup();
        map.setWorkspace(catalog.getWorkspaceByName("sf"));
        map.setName("map");
        map.getLayers().add(layer);
        map.getStyles().add(style);
        catBuilder.calculateLayerGroupBounds(map);
        catalog.add(map);
        
        assertNotNull(map);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("get");
        
        //Test initial get
        assertFalse(config.cacheFile(ThumbnailController.thumbnailFilename(layer)).exists());
        assertFalse(config.cacheFile(ThumbnailController.thumbnailFilename(map)).exists());
        HttpEntity<byte[]> response = ctrl.getMap("sf", "map", false, request);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.getBody()));
        
        File imageFile = config.cacheFile(ThumbnailController.thumbnailFilename(map));
        assertTrue(imageFile.exists());
        
        long lastModified = imageFile.lastModified();
        
        //Test cached get
        response = ctrl.getMap("sf", "map", true, request);
        imageFile = config.cacheFile(ThumbnailController.thumbnailFilename(map));
        assertTrue(imageFile.exists());
        assertEquals(lastModified, imageFile.lastModified());
        
        //Test invalidate
        ctrl.clearThumbnail(map);
        assertFalse(config.cacheFile(ThumbnailController.thumbnailFilename(map)).exists());
        //file.lastModified is only accurate to the second
        Thread.sleep(filePrecision);
        
        response = ctrl.getMap("sf", "map", true, request);
        
        imageFile = config.cacheFile(ThumbnailController.thumbnailFilename(map));
        assertTrue(imageFile.exists());
        long lm2 = imageFile.lastModified();
        assertTrue(lastModified < lm2);
        lastModified = imageFile.lastModified();
        
        //Test layer get
        response = ctrl.getLayer("sf", "PrimitiveGeoFeature", true, request);
        BufferedImage image2 = ImageIO.read(new ByteArrayInputStream(response.getBody()));
        
        //Compare high/low res
        assertEquals(image.getWidth()*2, image2.getWidth());
        assertEquals(image.getHeight()*2, image2.getHeight());
        
        
        imageFile = config.cacheFile(ThumbnailController.thumbnailFilename(layer));
        assertTrue(imageFile.exists());
        
        //Test layer invalidating map
        request = new MockHttpServletRequest();
        request.setContextPath("/geoserver");
        request.setRequestURI("/geoserver/hello");
        request.setMethod("put");
        
        layerCtrl.put("sf", "PrimitiveGeoFeature", new JSONObj().put("title", layer.getTitle()), request);
        
        //Update proxy
        assertFalse(config.cacheFile(ThumbnailController.thumbnailFilename(layer)).exists());
        assertFalse(config.cacheFile(ThumbnailController.thumbnailFilename(map)).exists());
    }
    */
    
    @Test
    public void testNameUtil() throws Exception {
        Catalog catalog = getCatalog();
        CatalogBuilder catalogBuilder = new CatalogBuilder(catalog);
        
        LayerInfo points = catalog.getLayerByName("cgf:Points");
        StyleInfo pointStyle = points.getDefaultStyle();
        
        LayerGroupInfo pointMap = catalog.getFactory().createLayerGroup();
        pointMap.setWorkspace(catalog.getWorkspaceByName("cgf"));
        pointMap.setName("Points");
        pointMap.getLayers().add(points);
        pointMap.getStyles().add(null);
        catalogBuilder.calculateLayerGroupBounds(pointMap);
        catalog.add(pointMap);
        
        assertNotNull(catalog.getLayerGroupByName(pointMap.prefixedName()));
        assertNotNull(catalog.getLayerByName(points.prefixedName()));
        assertNotNull(catalog.getStyleByName(pointStyle.prefixedName()));
        
        //Test unique against existing catalog entries
        assertEquals(points.getName()+"0", NameUtil.unique(points.getName(), points.getClass(), catalog));
        assertEquals(pointMap.getName()+"0", NameUtil.unique(pointMap.getName(), pointMap.getClass(), catalog));
        assertEquals(pointStyle.getName()+"0", NameUtil.unique(pointStyle.getName(), pointStyle.getClass(), catalog));
        
        //Test unique with no existing entries
        assertEquals("unique", NameUtil.unique("unique", points.getClass(), catalog));
        assertEquals("unique", NameUtil.unique("unique", pointMap.getClass(), catalog));
        assertEquals("unique", NameUtil.unique("unique", pointStyle.getClass(), catalog));
        
    }
}
