package org.motechproject.nms.testing.it.mcts.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by vishnu on 7/2/18.
 */
public class MockWsHttpServletForTestChildMotherCast extends HttpServlet{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String response;
        response = MctsImportTestHelper.getResponseForTestChildMotherCast();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentLength(response.length());

        IOUtils.write(response, resp.getOutputStream());
    }
}


