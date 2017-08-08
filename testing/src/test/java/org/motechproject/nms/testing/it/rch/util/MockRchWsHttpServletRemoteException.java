package org.motechproject.nms.testing.it.rch.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;

public class MockRchWsHttpServletRemoteException extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestBody = IOUtils.toString(request.getInputStream());

        if (requestBody.contains(RchTestConstants.MOTHER_TYPE_OF_USER)) {
            throw new RemoteException();
        } else if (requestBody.contains(RchTestConstants.CHILD_TYPE_OF_USER)) {
            throw new RemoteException();
        } else {
            throw new RemoteException();
        }
    }
}
