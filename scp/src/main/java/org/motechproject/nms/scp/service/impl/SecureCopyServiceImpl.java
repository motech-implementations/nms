package org.motechproject.nms.scp.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.scp.service.exception.SecureCopyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Service("secureCopyService")
public final class SecureCopyServiceImpl {
    private static final String FROM_SUBJECT = "nms.imi.scp.from";
    private static final String TO_SUBJECT = "nms.imi.scp.to";
    private static final String EVENT_PARAM_KEY_HOST = "host";
    private static final String EVENT_PARAM_KEY_USER = "user";
    private static final String EVENT_PARAM_KEY_IDENTITY = "identity";
    private static final String EVENT_PARAM_KEY_LOCAL_SOURCE = "localSource";
    private static final String EVENT_PARAM_KEY_REMOTE_SOURCE = "remoteSource";
    private static final String EVENT_PARAM_KEY_LOCAL_DESTINATION = "localDestination";
    private static final String EVENT_PARAM_KEY_REMOTE_DESTINATION = "remoteDestination";

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureCopyServiceImpl.class);


    private static String getStderr(Process p) {
        StringBuilder sb = new StringBuilder();
        BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;

        try {
            while ((line = b.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

            b.close();
        } catch (IOException e) {
            LOGGER.error("Error trying to retrieve scp error message: {}", e.getMessage());
        }
        return sb.toString();
    }


    public void copyFrom(String user, String host, String identityFile, String remoteSource,
                                String localDestination) throws SecureCopyException {

        Runtime r = Runtime.getRuntime();
        try {
            String identity;
            if (identityFile == null || identityFile.isEmpty()) {
                identity = "";
            } else {
                identity = String.format("-i %s ", identityFile);
            }
            String command = String.format("scp %s%s@%s:%s %s", identity, user, host, remoteSource, localDestination);
            LOGGER.debug(command);
            Process p = r.exec(command);
            int retVal = p.waitFor();
            if (retVal != 0) {
                throw new SecureCopyException(String.format("Error copying from %s: scp returned %d - %s", host, retVal,
                        getStderr(p)));
            }
        } catch (IOException | InterruptedException e) {
            throw new SecureCopyException(String.format("Error copying from %s: %s", host, e.getMessage()), e);
        }
    }


    public static void copyTo(String user, String host, String identityFile, String localSource,
                              String remoteDestination) throws SecureCopyException {

        Runtime r = Runtime.getRuntime();
        try {
            String identity;
            if (identityFile == null || identityFile.isEmpty()) {
                identity = "";
            } else {
                identity = String.format("-i %s ", identityFile);
            }
            String command = String.format("scp %s%s %s@%s:%s", identity, localSource, user, host, remoteDestination);
            LOGGER.debug(command);
            Process p = r.exec(command);
            int retVal = p.waitFor();
            if (retVal != 0) {
                throw new SecureCopyException(String.format("Error copying to %s: scp returned %d - %s", host, retVal,
                        getStderr(p)));
            }
        } catch (IOException | InterruptedException e) {
            throw new SecureCopyException(String.format("Error copying to %s: %s", host, e.getMessage()), e);
        }
    }


    @MotechListener(subjects = { FROM_SUBJECT })
    public void handleCopyFrom(MotechEvent event) throws SecureCopyException {
        String user = (String) event.getParameters().get(EVENT_PARAM_KEY_USER);
        String host = (String) event.getParameters().get(EVENT_PARAM_KEY_USER);
        String identity = (String) event.getParameters().get(EVENT_PARAM_KEY_IDENTITY);
        String remoteSource = (String) event.getParameters().get(EVENT_PARAM_KEY_REMOTE_SOURCE);
        String localDestination = (String) event.getParameters().get(EVENT_PARAM_KEY_LOCAL_DESTINATION);
        copyFrom(user, host, identity, remoteSource, localDestination);
    }


    @MotechListener(subjects = { TO_SUBJECT })
    public void handleCopyTo(MotechEvent event) throws SecureCopyException {
        String user = (String) event.getParameters().get(EVENT_PARAM_KEY_USER);
        String host = (String) event.getParameters().get(EVENT_PARAM_KEY_USER);
        String identity = (String) event.getParameters().get(EVENT_PARAM_KEY_IDENTITY);
        String localSource = (String) event.getParameters().get(EVENT_PARAM_KEY_LOCAL_SOURCE);
        String remoteDestination = (String) event.getParameters().get(EVENT_PARAM_KEY_REMOTE_DESTINATION);
        copyTo(user, host, identity, localSource, remoteDestination);
    }
}
