package com.boundlessgeo.geoserver.api.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.geoserver.config.GeoServer;
import org.geoserver.ows.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.boundlessgeo.geoserver.util.RecentObjectCache;

/**
 * Summarizes information about the available API endpoints.
 */
@Controller
@RequestMapping("/api")
public class IndexController extends ApiController {
    
    private final RequestMappingHandlerMapping handlerMapping;
    
    @Autowired
    public IndexController(GeoServer geoServer, RecentObjectCache recent, RequestMappingHandlerMapping handlerMapping) {
        super(geoServer, recent);
        this.handlerMapping = handlerMapping;
    }
    
    @RequestMapping(method= RequestMethod.GET)
    public String get(HttpServletRequest req, @ModelAttribute("model") ModelMap model) {
        List<Map<String, Object>> requests = new ArrayList<Map<String, Object>>();
        for (RequestMappingInfo mapping : this.handlerMapping.getHandlerMethods().keySet()) {
            Map<String, Object> request = new HashMap<String, Object>();
            if (mapping.getPatternsCondition().getPatterns().size() <= 0) {
                request.put("url", "/api");
                request.put("pattern", "/api");
            } else {
                request.put("url", mapping.getPatternsCondition().getPatterns().iterator().next().replaceAll(":.*\\+", ""));
                
                if (mapping.getPatternsCondition().getPatterns().size() == 1) {
                    request.put("pattern", mapping.getPatternsCondition().getPatterns().iterator().next().replaceAll(":.*\\+", ""));
                } else {
                    request.put("pattern", mapping.getPatternsCondition().getPatterns().toString().replaceAll(":.*\\+", ""));
                }
            }
            if (mapping.getMethodsCondition().getMethods().size() <= 0) {
                request.put("method", "GET");
            } else if (mapping.getMethodsCondition().getMethods().size() == 1) {
                request.put("method", mapping.getMethodsCondition().getMethods().iterator().next());
            } else {
                request.put("method", mapping.getMethodsCondition().getMethods().toString());
            }
            requests.add(request);
        }
        model.addAttribute("requests", requests);
        String baseUrl = ResponseUtils.baseURL(req).replaceAll("/$", "");
        model.addAttribute("baseUrl", baseUrl);
        
        return "api";
    }
}
