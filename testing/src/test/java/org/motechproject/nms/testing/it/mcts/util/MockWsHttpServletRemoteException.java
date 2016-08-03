package org.motechproject.nms.testing.it.mcts.util;


import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;

/*
    test servlet for remote server exception throw
     */
public class MockWsHttpServletRemoteException extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestBody = IOUtils.toString(req.getInputStream());
        if (requestBody.contains("GetMother")) {
            throw new RemoteException();
        } else if (requestBody.contains("GetChild")) {
            throw new RemoteException();
        } else {
            throw new RemoteException();
        }
    }
}
