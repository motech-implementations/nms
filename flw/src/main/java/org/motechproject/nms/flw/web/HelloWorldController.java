package org.motechproject.nms.flw.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for HelloWorld message and bundle status.
 */
@Controller
public class HelloWorldController {

    private static final String OK = "OK";

    @RequestMapping("/web-api/status")
    @ResponseBody
    public String status() {
        return OK;
    }
}
