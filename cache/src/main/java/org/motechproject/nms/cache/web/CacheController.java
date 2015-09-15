package org.motechproject.nms.cache.web;

import org.motechproject.nms.cache.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class CacheController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private CacheService cacheService;


    @RequestMapping(value = "/clear")
    @ResponseStatus(HttpStatus.OK)
    public void clear() {
        LOGGER.debug("/clear");
        cacheService.clear();
    }
}
