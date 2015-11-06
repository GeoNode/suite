/* (c) 2014-2015 Boundless, http://boundlessgeo.com
 * This code is licensed under the GPL 2.0 license.
 */
package com.boundlessgeo.geoserver.api.controllers;

import com.boundlessgeo.geoserver.api.exceptions.BadRequestException;
import com.boundlessgeo.geoserver.api.exceptions.NotFoundException;
import com.boundlessgeo.geoserver.json.JSONArr;
import com.boundlessgeo.geoserver.json.JSONObj;
import com.boundlessgeo.geoserver.util.Hasher;
import com.boundlessgeo.geoserver.util.NameUtil;
import com.google.common.collect.Maps;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geoserver.catalog.CascadeDeleteVisitor;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.ResourcePool;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.importer.ASpatialFile;
import org.geoserver.importer.Database;
import org.geoserver.importer.Directory;
import org.geoserver.importer.FileData;
import org.geoserver.importer.ImportContext;
import org.geoserver.importer.ImportData;
import org.geoserver.importer.ImportFilter;
import org.geoserver.importer.ImportTask;
import org.geoserver.importer.ImportTask.State;
import org.geoserver.importer.Importer;
import org.geoserver.importer.SpatialFile;
import org.geoserver.importer.Table;
import org.geoserver.importer.job.Task;
import org.geoserver.platform.resource.Paths;
import org.geoserver.ows.util.ResponseUtils;
import org.geoserver.platform.resource.Resource;
import org.geoserver.rest.util.RESTUploadExternalPathMapper;
import org.geoserver.rest.util.RESTUploadPathMapper;
import org.geoserver.rest.util.RESTUtils;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.vfny.geoserver.util.DataStoreUtils;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/api/imports")
public class ImportController extends ApiController {

    Importer importer;
    Hasher hasher;
    
    private Long id = 0L;
    private Map<Long, ImportHelper> imports = new HashMap<Long, ImportHelper>();
    
    private static Logger LOG = Logging.getLogger(ImportController.class);

    @Autowired
    public ImportController(GeoServer geoServer, Importer importer) {
        super(geoServer);
        this.importer = importer;
        this.hasher = new Hasher(7);
    }
    
    /**
     * API endpoint to get space available info when uploading files
     * 
     * TODO: If a dedicated file/resource API controller gets created, migrate this method to there
     * TODO: If/when workspace-specific upload limitations are implemented, update this to be 
     *       workspace-specific
     * @return A JSON object containing information about the space available in the workspace
     * @throws IOException 
     */
    @RequestMapping(value = "/{wsName:.+}", method = RequestMethod.GET)
    public @ResponseBody JSONObj info(@PathVariable String wsName) throws IOException {
        JSONObj obj = new JSONObj();
        Catalog catalog = geoServer.getCatalog();
        WorkspaceInfo ws = findWorkspace(wsName, catalog);
        
        if (ws != null) {
            obj.put("workspace", ws.getName());
        }
        //Temp dir
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        
        //Global REST upload dir
        String externalRoot = null;
        if (externalRoot == null) {
            externalRoot = RESTUtils.extractMapItem(RESTUtils.loadMapfromWorkSpace(
                    ws == null ? null : ws.getName(), catalog), RESTUtils.ROOT_KEY);
        }
        if (externalRoot == null) {
            externalRoot = RESTUtils.extractMapItem(RESTUtils.loadMapFromGlobal(), RESTUtils.ROOT_KEY);
        }
        if (externalRoot == null) {
            externalRoot = Paths.toFile(catalog.getResourceLoader().getBaseDirectory(), 
                    Paths.path("data", ws == null ? null : ws.getName())).getAbsolutePath();
        }
        
        File destDir = new File(externalRoot);
        
        long freeSpace = 0;
        
        if (tmpDir.exists()) {
            freeSpace = tmpDir.getUsableSpace();
            obj.put("tmpDir", tmpDir.getPath());
            obj.put("tmpSpace", freeSpace);
        }
        if (destDir.exists()) {
            freeSpace = destDir.getUsableSpace();
            obj.put("uploadDir", destDir.getPath());
            obj.put("uploadSpace", freeSpace);
            
            obj.put("spaceUsed", FileUtils.sizeOfDirectory(destDir));
        }
        //In case the destDir is on a different filesystem than the tmpDir, take the min of both
        if (tmpDir.exists() && destDir.exists()) {
            freeSpace = Math.min(tmpDir.getUsableSpace(), destDir.getUsableSpace());
        }
        obj.put("spaceAvailable", freeSpace);
        
        return obj;
    }

    /**
     * API endpoint to import a file or list of files as a new layer or layers into GeoServer. 
     * Files are provided as MediaType.MULTIPART_FORM_DATA_VALUE in the request
     * @param wsName The workspace to import the files into
     * @param request The HTTP request
     * @return a JSON object describing the result of the import. See {@link #get(String, Long) get}.
     * @throws Exception if the request is invalid, or the file upload fails.
     */
    @RequestMapping(value = "/{wsName:.+}", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    JSONObj importFile(@PathVariable String wsName, HttpServletRequest request) throws Exception {
        return importFile(wsName, null, request);
    }
    
    /**
     * API endpoint to import a file or list of files as a new layer or layers into into an existing
     * store inGeoServer. 
     * Files are provided as MediaType.MULTIPART_FORM_DATA_VALUE in the request
     * @param wsName The workspace to import the files into
     * @param storeName The store to import the layers into. If null, tries to import into a new 
     * store.
     * @param request The HTTP request
     * @return a JSON object describing the result of the import. See {@link #get(String, Long) get}.
     * @throws Exception if the request is invalid, or the file upload fails.
     */
    @RequestMapping(value = "/{wsName:.+}/{storeName:.+}", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public @ResponseBody
    JSONObj importFile(@PathVariable String wsName, @PathVariable String storeName, HttpServletRequest request)
        throws Exception {

        // grab the workspace
        Catalog catalog = geoServer.getCatalog();
        WorkspaceInfo ws = findWorkspace(wsName, catalog);

        // get the uploaded files
        FileItemIterator files = doFileUpload(request);
        if (!files.hasNext()) {
            throw new BadRequestException("Request must contain one or more files");
        }
        
        // create a new temp directory for the uploaded file
        File uploadDir = Files.createTempDirectory("importFile").toFile();
        if (!uploadDir.exists()) {
            throw new RuntimeException("Unable to create directory for file upload");
        }
        uploadDir.deleteOnExit();
        
        // pass off the uploaded file(s) to the importer
        Directory dir = new Directory(uploadDir);
        while(files.hasNext()) {
            FileItemStream item = files.next();
            try (InputStream stream = item.openStream()) {
                String name = item.getName();
                dir.accept(name, stream);
            }
        }
        
        Long id;
        if (storeName == null) {
            id = importer.createContextAsync(dir, ws, null);
        } else {
            StoreInfo store = findStore(wsName, storeName, geoServer.getCatalog());
            id = importer.createContextAsync(dir, ws, store);
        }
        return get(wsName, createImport(importer.getTask(id)), request);
    }
    
    public static File uploadDir(Catalog catalog, WorkspaceInfo ws, StoreInfo store) throws IOException {
        RESTUploadPathMapper destDirMapper = new RESTUploadExternalPathMapper(catalog);
        
        //Match org.catalog.rest.StoreFileResource default base dir
        StringBuilder destDirBuilder = new StringBuilder(
                Paths.toFile(catalog.getResourceLoader().getBaseDirectory(), 
                        Paths.path("data", ws.getName(),store.getName())).getAbsolutePath());
        
        //See if we can get the root directory from the Global REST root path
        destDirMapper.mapStorePath(destDirBuilder, ws.getName(), store.getName(), new HashMap<String, String>());
        
        //Move main file
        return new File(destDirBuilder.toString());
    }
    
    /**
     * Moves uploaded files from the temp upload directory to the appropriate store folder
     * Updates the ImportContext tasks and stores with the new location.
     * Updates the catalog store with the new location.
     * 
     * @param t ImportTask containing data about a single import
     */
    void moveFile(ImportTask t) {
        Catalog catalog = geoServer.getCatalog();
        StoreInfo store = catalog.getStore(t.getStore().getId(), StoreInfo.class);
        
        if (store == null) {
            LOG.warning("Trying to move files to a non-existant store");
            return;
        }
        if (! (t.getData() instanceof FileData)) {
            LOG.warning("Trying to move non-file data");
            return;
        }
        if (!t.isDirect()) {
            LOG.warning("Trying to move files from an indirect import");
            return;
        }
        
        WorkspaceInfo ws = store.getWorkspace();
        
        //Special behavior for SpatialFile - linked files
        FileData srcData = (FileData)t.getData();
        File srcFile = srcData.getFile();
        File storeFile;
        
        File destDir;
        FileData destData;
        File destFile;
        
        try {
            destDir = uploadDir(catalog, ws, store);
            destFile = new File(destDir, srcData.getFile().getName());
            if (srcFile.getAbsoluteFile().equals(destFile.getAbsoluteFile())) {
                LOG.warning("Trying to move file to itself");
                return;
            }
            destDir.mkdirs();
            
            //Update Store
            File baseDirectory = catalog.getResourceLoader().getBaseDirectory();
            
            if (store instanceof CoverageStoreInfo) {
                storeFile = catalog.getResourceLoader().url(((CoverageStoreInfo)store).getURL());
                //A CoverageStore needs a single file
                String url = "file:"+Paths.convert(baseDirectory, destFile);
                if (!(srcData.getFile().getAbsolutePath().equals(storeFile.getAbsolutePath())) ) {
                    throw new RuntimeException("CoverageStore file not the same as imported file");
                }
                ((CoverageStoreInfo)store).setURL(url);
            } else if (store instanceof DataStoreInfo){
                storeFile = catalog.getResourceLoader().url(store.getConnectionParameters().get("url").toString());
                /* A DataStore may contain multiple files as separate "tables".
                 * Therefore, we use the store dir for the URL, and ensure the file location is 
                 * somewhere in this directory.
                 * * If the store file is the same as the destination directory, then we may be in 
                 *   progress moving files.
                 * * If the store file is a prefix of the source data file, then all is well
                 * * Otherwise, we have a problem and should abort.
                 */
                String url = "file:"+Paths.convert(baseDirectory, destDir);
                if (!(storeFile.equals(destDir.getAbsoluteFile()) 
                        || srcData.getFile().getAbsolutePath().startsWith(storeFile.getAbsolutePath())) ) {
                    throw new RuntimeException("DataStore file not the same as imported file");
                }
                store.getConnectionParameters().put("url", url);
            } else {
                throw new RuntimeException("Invalid store type: "+store.getClass());
            }
            //Test if we can actually move the file; otherwise copy the file.
            boolean move = srcFile.renameTo(srcFile);
            
            if (move) {
                Files.move(srcFile.toPath(), destFile.toPath());
            } else {
                Files.copy(srcFile.toPath(), destFile.toPath());
            }
            //move any supplementary files, update ImportData
            if (srcData instanceof SpatialFile) {
                destData = new SpatialFile(destFile);
                if (((SpatialFile)srcData).getPrjFile() != null) {
                    File prjFile = new File(destDir, ((SpatialFile)srcData).getPrjFile().getName());
                    if (move) {
                        Files.move(((SpatialFile)srcData).getPrjFile().toPath(), prjFile.toPath());
                    } else{
                        Files.copy(((SpatialFile)srcData).getPrjFile().toPath(), prjFile.toPath());
                    }
                    ((SpatialFile)destData).setPrjFile(prjFile);
                }
                for (File f : ((SpatialFile)srcData).getSuppFiles()) {
                    File suppFile = new File(destDir, f.getName());
                    if (move) {
                        Files.move(f.toPath(), suppFile.toPath());
                    } else {
                        Files.copy(f.toPath(), suppFile.toPath());
                    }
                    ((SpatialFile)destData).getSuppFiles().add(suppFile);
                }
            } else if (srcData instanceof ASpatialFile) {
                destData = new ASpatialFile(destFile);
            } else {
                destData = new FileData(destFile);
            }
        } catch (Exception e) {
            //If this occurs, the store files will be in a temporary folder, so we should abort the import
            t.setError(e);
            t.setState(State.ERROR);
            store.accept(new CascadeDeleteVisitor(catalog));
            throw new RuntimeException("Failed to move imported files to uploads directory", e);
        }
        //Copy over attributes from srcData
        destData.setFormat(srcData.getFormat());
        destData.setMessage(srcData.getMessage());
        destData.setCharsetEncoding(srcData.getCharsetEncoding());
        
        //Update data
        t.setData(destData);
        t.setStore(store);
        
        //Save the updated store to the catalog
        catalog.save(store);
    }

    /**
     * API endpoint to import a database as a new layer or layers into GeoServer. 
     * Connection details are provided as a JSONObject, formatted according to the 
     * {@see com.boundlessgeo.geoserver.api.controllers.FormatController} API.
     * @param wsName The workspace to import the database into
     * @param request The HTTP request
     * @return a JSON object describing the result of the import. See {@link #get(String, Long) get}.
     * @throws Exception if the request is invalid.
     */
    @RequestMapping(value = "/{wsName:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody JSONObj importDb(@PathVariable String wsName, @RequestBody JSONObj obj, 
            HttpServletRequest request) throws Exception {
        return importDb(wsName, null, obj, request);
    }
    
    /**
     * API endpoint to import the contents of a database as a new layer or layers into an existing 
     * data store in GeoServer. 
     * Connection details are provided as a JSONObject, formatted according to the 
     * {@see com.boundlessgeo.geoserver.api.controllers.FormatController} API.
     * @param wsName The workspace to import the database into
     * @param storeName The store to import the layers into. If null, tries to import into a new 
     * store.
     * @param request The HTTP request
     * @return a JSON object describing the result of the import. See {@link #get(String, Long) get}.
     * @throws Exception if the request is invalid.
     */
    @RequestMapping(value = "/{wsName:.+}/{storeName:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody JSONObj importDb(@PathVariable String wsName, @PathVariable String storeName, @RequestBody JSONObj obj, 
            HttpServletRequest request) throws Exception {
        
        // grab the workspace
        Catalog catalog = geoServer.getCatalog();
        WorkspaceInfo ws = findWorkspace(wsName, catalog);
        
        // create the import data
        Database db = new Database(hack(obj));
        
        Long id; 
        if (storeName == null) {
            id = importer.createContextAsync(db, ws, null);
        
            //Check if this store already exists in the catalog
            StoreInfo store = findStore(hack(obj), ws);
            if (store != null) {
                return (new JSONObj()).put("store", IO.store(new JSONObj(), store, request, geoServer));
            }
            
        } else {
            StoreInfo store = findStore(wsName, storeName, geoServer.getCatalog());
            id = importer.createContextAsync(db, ws, store);
        }
        
        //Return to requester to allow selection of tables.
        //Complete the import using update()
        return get(ws.getName(), createImport(importer.getTask(id)), request);
    }

    Map<String, Serializable> hack(JSONObj obj) {
        Map<String,Serializable> map = Maps.newLinkedHashMap();
        for (Object e : obj.raw().entrySet()) {
            Map.Entry<String,Serializable> entry = (Map.Entry) e;
            Serializable value = entry.getValue();
            if (value instanceof Long) {
                value = ((Long)value).intValue();
            }
            map.put(entry.getKey(), value);
        }
        return map;
    }
    
    /*
     * Extracts details on a task filter from the JSONObj and creates a ImportFilter object based on these 
     * details. Valid filters are:
     * * "filter":"ALL" - All tasks
     * * "tasks":[<list of tasks>] where each task object must contain "task":<task id> - Only 
     *   select the tasks identified in the list
     */
    ImportFilter filter(JSONObj obj, ImportContext imp) {
        Object filter = obj.get("filter");
        JSONArr arr = obj.array("tasks");
        
        if (filter == null && arr == null) {
            throw new BadRequestException("Request must contain a filter or a list of tasks");
        }
        if (arr == null) {
            if ("ALL".equals(filter)) {
                return ImportFilter.ALL;
            }
            throw new BadRequestException("Invalid filter: "+filter);
        } else {
            final List<Long> tasks = new ArrayList<Long>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                JSONObj taskObj = arr.object(i);
                
                Long taskId = Long.parseLong(taskObj.str("task"));
                if (taskId == null) {
                    throw new BadRequestException("Request must contain task identifier");
                }
                ImportTask task = imp.task(taskId);
                
                if (task == null) {
                    throw new NotFoundException("No such task: " + taskId + " for import: " + imp.getId());
                }
                tasks.add(taskId);
            }
            return new TaskIdFilter(tasks);
        }
    }
    
    /**
     * Runs an import using the GeoServer importer
     * @param data - The data to import
     * @param ws - The workspace to import into
     * @return JSON representation of the import
     */
    JSONObj doImport(ImportHelper helper, WorkspaceInfo ws, HttpServletRequest request) throws Exception {
        return doImport(helper, ws, ImportFilter.ALL, request);
    }
    
    /**
     * Runs an import using the GeoServer importer
     * @param helper - The ImportWrapper containing the context to run the import from
     * @param ws - The workspace to import into
     * @param f - Filter to select import tasks
     * @return JSON representation of the import
     */
    JSONObj doImport(ImportHelper helper, WorkspaceInfo ws, ImportFilter f, HttpServletRequest request) throws Exception {
        helper.setTask(null);
        ImportContext imp = helper.getContext();
        
        // run the import
        imp.setState(ImportContext.State.RUNNING);
        GeoServerDataDirectory dataDir = dataDir();
        for (ImportTask t : imp.getTasks()) {
            if (f.include(t)) {
                prepTask(t, ws, dataDir);
            }
        }
        helper.setTask(importer.getTask(importer.runAsync(imp, f)));
        return get(ws.getName(), helper.id, request);
    }
    
    
    /*
     * 1. hack the context object to ensure that all styles are workspace local
     * 2. mark layers as "imported" so we can safely delete styles later
     */
    void prepTask(ImportTask t, WorkspaceInfo ws, GeoServerDataDirectory dataDir) {
        LayerInfo l = t.getLayer();
        
        //If the task has no associated layer, it is probably a junk file that won't get imported
        if (l == null) {
            return;
        }
        
        l.getMetadata().put(Metadata.IMPORTED, new Date());
        l.getResource().setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);

        //If the style exists, and we haven't already moved it into the workspace, move it to ws.
        if (l != null && l.getDefaultStyle() != null && !ws.equals(l.getDefaultStyle().getWorkspace())) {
            StyleInfo s = l.getDefaultStyle();

            // JD: have to regenerate the unique name here, the importer already does this but because we are
            // putting it into the workspace we have to redo it, this should really be part of the importer
            // with an option to create styles in the workspace
            s.setName(findUniqueStyleName(l.getResource(), ws, catalog()));

            Resource from = dataDir.style(s);

            s.setWorkspace(ws);
            Resource to = dataDir.style(s);

            try {
                try (
                    InputStream in = from.in();
                    OutputStream out = to.out();
                ) {
                    IOUtils.copy(in, out);
                    from.delete();
                }
            }
            catch(IOException e){
                throw new RuntimeException("Error copying style to workspace", e);
            }
        }
    }

    String findUniqueStyleName(ResourceInfo resource, WorkspaceInfo workspace, Catalog catalog) {
        String styleName = resource.getName();
        StyleInfo style = catalog.getStyleByName(workspace, styleName);
        int i = 1;
        while(style != null) {
            styleName = resource.getName() + i;
            style = catalog.getStyleByName(workspace, styleName);
            i++;
        }
        return styleName;
    }

    /**
     * Get details on an existing import.
     * @param wsName The workspace of the import
     * @param id The id of the import
     * @return A JSONObj containing the import details:
     * {
     *    "id": 0,
     *    "task": "Processing data usa-merc.tif"
     *    "importerEndpoint":"http:\/\/localhost:8000\/geoserver\/geoserver\/rest\/imports\/1",
     *    "preimport": [],
     *    "running": [],
     *    "imported": [
     *      {
     *        "task": 0, 
     *        "name": "stipple.shp",
     *        "type": "file"
     *        "layer": {
     *          "name": "stipple", 
     *          "workspace": "opengeo",
     *          ... rest of layer definition
     *        }
     *      }
     *    ], 
     *    "pending": [
     *      {
     *        "task": 1, 
     *        "name": "usa-merc.tif",
     *        "type": "file"
     *        "problem": "NO_CRS"
     *      }
     *    ],
     *    "ignored": [], 
     *    "failed": []
     *    "status":"complete"
     *    "tasksComplete":2
     *    "tasksTotal":2
     *  }
     * 
     * The response is an object with 7 properties. If this is a new import, and the context has
     * not yet been created, only id and task will be included.
     * * id - The identifier of this import job, needed to make modifications to it
     * * task - A textual representation of the current task. Not included if no task is currently running.
     * * importerEndpoint - REST endpoint to manage this import using the GeoServer Importer REST API
     * * status - The current status of the import. May be "pending", "running", or "complete"
     * * tasksComplete - The number of tasks that have been processed and have either been 
     *   successfully imported or given an error.
     * * tasksTotal - The total number of tasks associated with this import
     * * tasks - An array of tasks. See {@see #task(ImportTask)}.
     *
     * @throws Exception if the request is invalid, or another error occurs.
     */
    @RequestMapping(value = "/{wsName}/{id:\\d+}", method = RequestMethod.GET)
    public @ResponseBody JSONObj get(@PathVariable String wsName, @PathVariable Long id, HttpServletRequest request) throws Exception {
        ImportHelper helper = imports.get(id);
        if (helper == null) {
            throw new NotFoundException("Import with id "+id+" does not exist");
        }
        JSONObj result = new JSONObj().put("id",id);
        
        Task<ImportContext> t = helper.getTask();
        ImportContext imp = helper.getContext();
        
        if (t != null) {
            result.put("task", t.toString());
        }
        
        if (imp == null) {
            if (t == null) {
                throw new RuntimeException("Invalid import");
            }
            return result;
        }
        result.put("importerEndpoint", ResponseUtils.baseURL(request)+"geoserver/rest/imports/"+imp.getId());
        
        JSONArr tasks = result.putArray("tasks");

        int completed = 0;
        int pending = 0;
        for (ImportTask task : imp.getTasks()) {
            tasks.add(task(task));
            switch(task.getState()) {
                case READY:
                case PENDING:
                case RUNNING:
                    pending++;
                    break;
                default:
                    completed++;
                    break;
              
            }
        }
        
        result.put("tasksCompleted", completed);
        result.put("tasksTotal", imp.getTasks().size());
        
        //If there are no more tasks to run, consider the import complete
        if (pending == 0) {
            imp.setState(ImportContext.State.COMPLETE);
        }
        switch(imp.getState()) {
            case COMPLETE:
                result.put("state", "complete");
                break;
            case PENDING:
                result.put("state", "pending");
                break;
            case RUNNING:
                result.put("state", "running");
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Modify an existing import. This can involve updating projections, or importing previously 
     * excluded tasks.
     * 
     * @param wsName
     * @param id
     * @param obj The update request: 
     * The request payload can be a list of task objects with any additional data that needs to be 
     * specified by the user. This example (re)imports of task 0, and sets the projection and 
     * re-imports task 1:
     * { 
     *     "tasks":[
     *         {
     *              "task": 0
     *         },
     *         {
     *              "task": 1,
     *              "proj": "epsg:3857"
     *         }]
     * }
     * Alternatively, the request can specify a filter to select tasks to import. 
     * Currently "ALL" is the only supported filter. This would import all tasks:
     * { "filter":"ALL" }
     * 
     * If the import is currently running, you cannot update it, as this may interfere with running
     * tasks and cause an inconsistent state. To cancel a running import, provide the value
     * "cancelled":true in the request object.
     * 
     * @return a JSON object describing the result of the import. See {@link #get(String, Long) get}.
     * @throws Exception if the request is invalid.
     */
    @RequestMapping(value = "/{wsName}/{id:\\d+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody JSONObj update(@PathVariable String wsName, @PathVariable Long id, @RequestBody JSONObj obj, HttpServletRequest request)
        throws Exception {

        Catalog catalog = geoServer.getCatalog();
        WorkspaceInfo ws = findWorkspace(wsName, catalog);
        ImportHelper helper = imports.get(id);
        if (helper == null) {
            throw new NotFoundException("Import with id "+id+" does not exist");
        }
        JSONObj result = new JSONObj().put("id",id);
        
        Task<ImportContext> t = helper.getTask();
        ImportContext imp = helper.getContext();
        
        //If this import is currently running, we should not start another job
        if (t != null) {
            if (obj.bool("cancelled")) {
                t.cancel(true);
                return get(wsName, id, request);
            } else {
                throw new RuntimeException("This import is currently running.");
            }
        }
        
        if (imp == null) {
            throw new RuntimeException("Invalid import");
        }
        ImportFilter f = filter(obj, imp);
        JSONArr arr = obj.array("tasks");
        
        //Filter only: run on all tasks that match the filter
        if (arr == null) {
            return doImport(helper, ws, f, request);
        }
        
        //Task List: Update CRS tasks, run all tasks that match filter or list.
        for (int i = 0; i < arr.size(); i++) {
            
            JSONObj taskObj = arr.object(i);
            
            Long taskId = Long.parseLong(taskObj.str("task"));
            if (taskId == null) {
                throw new BadRequestException("Request must contain task identifier");
            }
            ImportTask task = imp.task(taskId);
            if (task == null) {
                throw new NotFoundException("No such task: " + taskId + " for import: " + id);
            }
            
            ResourceInfo resource = task.getLayer().getResource();
            
            JSONObj proj = taskObj.object("proj");
            
            if (task.getState() == ImportTask.State.NO_CRS && proj == null) {
                throw new BadRequestException("Task "+taskId+" requires a 'proj' property");
            }
            if (proj != null) {
                try {
                    resource.setSRS(IO.srs(proj));
                    resource.setNativeCRS(IO.crs(proj));
                    importer.changed(task);
                } catch(Exception e) {
                    throw new BadRequestException("Unable to parse proj: " + proj.toString());
                }
            }
        }
        return doImport(helper, ws, f, request);
    }

    ImportContext findImport(Long id) {
        ImportContext imp = importer.getContext(id);
        if (imp == null) {
            throw new NotFoundException("No such import: " + id);
        }
        return imp;
    }
    
    /*
     * Searches the catalog for existing stores that have the same connection parameters 
     * as those described by the DataFormat of this ImportContext, in order to test if the store 
     * that would be created by this import already exists.
     * 
     * For better matching, only compares the required parameters of the store.
     */
    StoreInfo findStore(Map<String, Serializable> params, WorkspaceInfo ws) throws Exception {
        Catalog catalog = geoServer.getCatalog();
        
        Map<String, Serializable> requiredParams = new HashMap<String, Serializable>();
        DataStoreFactorySpi factory = (DataStoreFactorySpi) DataStoreUtils.aquireFactory(params);
        
        if (factory == null) {
            return null;
        }
        for (Param p : factory.getParametersInfo()) {
            if (p.isRequired() && params.get(p.getName()) != null) {
                requiredParams.put(p.getName(), params.get(p.getName()));
            }
            
        }
        //Special case to support connections to different databases on the same database server
        if (params.get("database") != null) {
            requiredParams.put("database", params.get("database"));
        }
        
        List<? extends StoreInfo> stores = catalog.getStoresByWorkspace(ws, StoreInfo.class);
        for (StoreInfo s : stores) {
            boolean matches = true;
            Map<String, Serializable> p = ResourcePool.getParams(s.getConnectionParameters(), catalog.getResourceLoader() );
            for (String key : requiredParams.keySet()) {
                //On-disk params read as strings, so compare as strings
                if (!(requiredParams.get(key).toString()).equals(
                        (p.get(key) == null ? "" : p.get(key).toString()))) {
                    matches = false;
                }
            }
            if (matches) {
                return s;
            }
        }
        return null;
    }
    
    void touch(ImportTask task) {
        LayerInfo l = task.getLayer();
        l = catalog().getLayer(l.getId());
        if (l != null) {
            Date now = new Date();
            //If we have already touched this task, don't update
            if (Metadata.created(l) == null) {
                Metadata.created(l, now);
            }
            Metadata.modified(l, now);
            geoServer.getCatalog().save(l);
        }
    }

    /**
     * A json representation of an importer task
     * 
     * The following attributes are included:
     *   "task": task id
     *   "name": task name
     *   "status': task status. One of READY, PENDING, RUNNING, NO_BOUNDS, NO_CRS, COMPLETE, ERROR, or IGNORED
     *   "layer": details of the imported layer. Only applicable for "status":COMPLETE
     *   "error": details of the error. Only applicable for "status":ERROR
     *   "type": data type (file | database | table) of the task. Not applicable for "status":IGNORED
     *   "geometry": geometry type ({@see com.boundlessgeo.geoserver.api.controllers.IO#geometry(LayerInfo) }) of the task.  Not applicable for "status":IGNORED
     * @param task
     * @return
     */
    JSONObj task(ImportTask task) {
        JSONObj obj = new JSONObj();
        
        obj.put("task", task.getId())
           .put("name", name(task));
        
        switch(task.getState()) {
            case READY:
            case PENDING:
            case RUNNING:
            case NO_BOUNDS:
            case NO_CRS:
                obj.put("status", task.getState().toString());
                break;
            case COMPLETE:
                obj.put("status", task.getState().toString());
                touch(task);
                LayerInfo layer = task.getLayer();
                IO.layerDetails(obj.putObject("layer"), layer, null);
                
                break;
            case ERROR:
                // error, dump out some details
                obj.put("status", task.getState().toString());
                IO.error(obj.putObject("error"), task.getError() );
                break;
            default:
                obj.put("status", "IGNORED");
                //no type or geometry
                return obj;
        }
        
        obj.put("type", type(task))
           .put("geometry", IO.geometry(task.getLayer()));
        return obj;
    }

    JSONObj complete(ImportTask task) {
        touch(task);

        LayerInfo layer = task.getLayer();
        JSONObj obj = task(task);
        IO.layerDetails(obj.putObject("layer"), layer, null);
        return obj;
    }

    JSONObj pending(ImportTask task) {
        return task(task).put("problem", task.getState().toString());
    }

    JSONObj failed(ImportTask task) {
        JSONObj err = task(task);
        IO.error(err, task.getError() );
        return err;
    }

    JSONObj ignored(ImportTask task) {
        return new JSONObj().put("task", task.getId()).put("name", name(task));
    }
    
    String name(ImportTask task) {
        ImportData data = task.getData();
        if (data instanceof FileData) {
            return FilenameUtils.getName(data.toString());
        }
        return data.getName();
    }
    
    String type(ImportTask task) {
        ImportData data = task.getData();
        if (data instanceof Database) {
            return "database";
        }
        if (data instanceof FileData) {
            return "file";
        }
        if (data instanceof Table) {
            return "table";
        }
        return "null";
    }
    
    static class TaskIdFilter implements ImportFilter {
        List<Long> tasks;
        
        public TaskIdFilter(List<Long> tasks) {
            this.tasks = tasks;
        }
        @Override
        public boolean include(ImportTask task) {
            return tasks.contains(task.getId());
        }
    
    }
    
    /**
     * Create a new ImportHelper and add it to {@code imports}, tracking the index.
     * @param t
     * @return
     */
    private Long createImport(Task<ImportContext> t) {
        ImportHelper i = new ImportHelper(t);
        //Make sure we get the right index
        synchronized (id) {
            imports.put(id, i);
            i.id = id;
            id++;
        }
        return i.id;
    }
    
    /**
     * Utility class to track both the current Task, and the ImportContext with a consistent id.
     * The intended use pattern is to use {@link #createImport} to create the wrapper with the Task
     * object created by importer.createContextAsync. When this task has finished execution, 
     * this class is updated with the newly created context.
     * Subsequent tasks can be added with the setTask method.
     *
     */
    private class ImportHelper {
        private Task<ImportContext> currentTask = null;
        private ImportContext context = null;
        Long id = -1L;
        
        protected ImportHelper(Task<ImportContext> initTask) {
            if (initTask == null) {
                throw new NullPointerException("Initial task cannot be null");
            }
            currentTask = initTask;
            new Thread(new TaskListener(initTask)).start();
        }
        
        public ImportContext getContext() {
            return context;
        }
        
        /**
         * Returns the currently running task. If the current task has completed, updates the 
         * ImportContext and sets the current task to null.
         * @return The currently running task, or null if no task is running
         * @throws InterruptedException
         * @throws ExecutionException
         */
        public Task<ImportContext> getTask() throws InterruptedException, ExecutionException {
            if (currentTask != null && currentTask.isDone())  {
                context = currentTask.get();
                currentTask = null;
            }
            return currentTask;
        }
        
        /**
         * Sets the currentTask, as long as no task is currently running. 
         * Applies a TaskListener to the new task, used to update file locations when an import completes.
         * @param t
         * @throws RuntimeException If a currentTask is already running
         * @throws InterruptedException
         * @throws ExecutionException
         */
        public void setTask(Task<ImportContext> t) throws RuntimeException, InterruptedException, ExecutionException {
            if (t != null) {
                new Thread(new TaskListener(t)).start();
            }
            if (getTask() == null) {
                currentTask = t;
            } else {
                throw new RuntimeException("Cannot set task: another task is still running");
            }
        }
    }
    
    /**
     * Utility class which blocks on a running Task, and updates the location of any Imported files 
     * when that task completes.
     */
    private class TaskListener implements Runnable {
        Task<ImportContext> task;
        public TaskListener(Task<ImportContext> task) {
            this.task = task;
        }
        
        public void run() {
            try {
                ImportContext context = task.get();
                for (ImportTask t : context.getTasks()) {
                    if (t.getState() == State.COMPLETE) {
                        //Clean up names
                        Catalog catalog = ImportController.this.geoServer.getCatalog();
                        
                        StoreInfo store = catalog.getStore(t.getStore().getId(), StoreInfo.class);
                        store.setName(NameUtil.sanitizeEnsureUnique(store.getName(), StoreInfo.class, catalog));
                        LayerInfo layer = catalog.getLayer(t.getLayer().getId());
                        layer.setName(NameUtil.sanitizeEnsureUnique(layer.getName(), LayerInfo.class, catalog));
                        //ResourceInfo resource = catalog.getResource(layer.getResource().getId(), layer.getResource().getClass());
                        //resource.setName(NameUtil.sanitizeEnsureUnique(resource.getName(), ResourceInfo.class, catalog));
                        StyleInfo style = catalog.getStyle(layer.getDefaultStyle().getId());
                        style.setName(NameUtil.sanitizeEnsureUnique(style.getName(), StyleInfo.class, catalog));
                        
                        catalog.save(store);
                        catalog.save(layer);
                        catalog.save(style);
                        
                        t.setStore(store);
                        t.setLayer(layer);
                        
                        moveFile(t);
                        //Set created date
                        store = t.getStore();
                        if (store != null && Metadata.created(store) == null) {
                            Metadata.created(store, new Date());
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.log(Level.WARNING, "Failed to move imported files", e);
            }
        }
    }
}
