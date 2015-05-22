package org.motechproject.nms.scp.service.impl;

import org.motechproject.nms.scp.service.exception.SecureCopyException;
import org.motechproject.nms.scp.service.exception.SortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Service("secureCopyService")
public final class SecureCopyServiceImpl {

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


    /**
     * Sorts the source file into the destination file using the first 51 characters of the file as the sort key.
     * The first 51 characters correspond to the requestId field in a CDR file.
     */
    void sort(String source, String destination) throws SortException {
        Runtime r = Runtime.getRuntime();
        try {
            String command = String.format("sort -o %s -k=1,52 %s", destination, source);
            LOGGER.debug(command);
            Process p = r.exec(command);
            int retVal = p.waitFor();
            if (retVal != 0) {
                throw new SortException(String.format("Error sorting %s: sort returned %d - %s", source, retVal,
                        getStderr(p)));
            }
        } catch (IOException | InterruptedException e) {
            throw new SortException(String.format("Error sorting %s: %s", source, e.getMessage()), e);
        }

    }
}
