package org.motechproject.nms.testing.it.rch.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MockRchWsHttpServletForFail extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestBody = IOUtils.toString(request.getInputStream());

        String resp;

        if (requestBody.contains(RchTestConstants.MOTHER_TYPE_OF_USER)) {
            resp = RchImportTestHelper.getRchMothersResponseDataFail();
        } else if (requestBody.contains(RchTestConstants.CHILD_TYPE_OF_USER)) {
            resp = RchImportTestHelper.getRchChildrenResponseDataFail();
        } else {
            resp = RchImportTestHelper.getAnmAshaResponseDataFail();
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLength(resp.length());

        IOUtils.write(resp, response.getOutputStream());
    }
}
