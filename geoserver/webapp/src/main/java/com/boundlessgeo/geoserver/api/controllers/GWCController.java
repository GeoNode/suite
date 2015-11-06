package com.boundlessgeo.geoserver.api.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.geoserver.config.GeoServer;
import org.geoserver.gwc.GWC;
import org.geowebcache.grid.GridSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boundlessgeo.geoserver.json.JSONArr;
import com.boundlessgeo.geoserver.json.JSONObj;

@Controller
@RequestMapping("/api/gwc")
public class GWCController extends ApiController {
    
    final GWC mediator = GWC.get();
    
    @Autowired
    public GWCController(GeoServer geoServer) {
        super(geoServer);
    }
    
    /**
     * List the GWC gridsets available to geoserver
     * 
     * @param wsName
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/gridsets", method = RequestMethod.GET)
    public @ResponseBody JSONArr list(HttpServletRequest request) throws IOException {
        List<GridSet> gridsets = mediator.getGridSetBroker().getGridSets();
        
        JSONArr arr = new JSONArr();
        for (GridSet gridset: gridsets) {
            arr.add(gridset(new JSONObj(), gridset));
        }
        return arr;
    }
    
    /**
     * Encode a GWC gridset as a JSON object
     * @param obj
     * @param gridset
     * @return encoded gridset
     */
    JSONObj gridset(JSONObj obj, GridSet gridset) {
        obj.put("name", gridset.getName());
        obj.put("proj", gridset.getSrs().toString());
        obj.put("levels", gridset.getNumLevels());
        
        return obj;
    }



}
