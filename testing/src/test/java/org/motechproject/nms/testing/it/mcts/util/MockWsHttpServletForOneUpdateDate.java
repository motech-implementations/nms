package org.motechproject.nms.testing.it.mcts.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by motech on 29/7/16.
 */
public class MockWsHttpServletForOneUpdateDate extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestBody = IOUtils.toString(req.getInputStream());

        String response;
        if (requestBody.contains("GetMother")) {
            response = MctsImportTestHelper.getMotherResponseDataForOneUpdateDate();
        } else if (requestBody.contains("GetChild")) {
            response = MctsImportTestHelper.getChildrenResponseDataForOneUpdateDate();
        } else {
            response = MctsImportTestHelper.getAnmAshaResponseDataForOneUpdateDate();
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentLength(response.length());

        IOUtils.write(response, resp.getOutputStream());
    }
}
