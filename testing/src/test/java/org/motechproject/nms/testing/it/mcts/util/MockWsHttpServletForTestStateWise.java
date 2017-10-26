package org.motechproject.nms.testing.it.mcts.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
    test servlet for data with updated dates in one of the duplicate records
     */
public class MockWsHttpServletForTestStateWise extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String response;
        response = MctsImportTestHelper.getResponseForStateWiseTest();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentLength(response.length());

        IOUtils.write(response, resp.getOutputStream());
    }
}
