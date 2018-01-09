package org.motechproject.nms.testing.it.rch.util;

import org.apache.commons.io.IOUtils;
import org.motechproject.nms.testing.it.mcts.util.MctsImportTestHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by beehyvsc on 1/11/17.
 */
public class MockRchWsHttpcheckServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String response;
        response = RchImportTestHelper.getRchMothersResponseData();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentLength(response.length());

        IOUtils.write(response, resp.getOutputStream());
    }
}
