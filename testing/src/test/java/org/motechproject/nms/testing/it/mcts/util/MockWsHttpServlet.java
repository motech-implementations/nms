package org.motechproject.nms.testing.it.mcts.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
    test servlet for data with updated dates in all the 4 records in xml
     */
public class MockWsHttpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestBody = IOUtils.toString(req.getInputStream());

        String response;
        if (requestBody.contains("GetMother")) {
            response = MctsImportTestHelper.getMotherResponseData();
        } else if (requestBody.contains("GetChild")) {
            response = MctsImportTestHelper.getChildrenResponseData();
        } else {
            response = MctsImportTestHelper.getAnmAshaResponseData();
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentLength(response.length());

        IOUtils.write(response, resp.getOutputStream());
    }
}
