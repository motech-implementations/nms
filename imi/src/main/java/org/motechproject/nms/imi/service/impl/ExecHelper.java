package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.exception.ExecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecHelper.class);

    private String error;

    public String getError() {
        return error;
    }


    /**
     * Inspired by http://stackoverflow.com/questions/808276/how-to-add-a-timeout-value-when-using-javas-runtime-exec
     */

    public void exec(String command, long timeout) throws ExecException {
        error = null;

        LOGGER.debug(command);

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(command);
        } catch (IOException e) {
            throw new ExecException(String.format("Error running '%s': %s", command, e.getMessage()), e);
        }

        Worker worker = new Worker(process);
        worker.start();
        try {
            worker.join(timeout);
            if (worker.exit != null) {
                if (worker.exit != 0) {
                    throw new ExecException(String.format("Error %d running '%s': %s", worker.exit, command,
                            error));
                }
            } else {
                throw new ExecException(String.format("Timeout error running '%s'", command));
            }
        } catch (InterruptedException e) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new ExecException(String.format("Error running '%s': %s", command, e.getMessage()), e);
        } finally {
            process.destroy();
        }
    }


    private static String stderr(Process p) {
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
            LOGGER.error("Error trying to retrieve scp error message: {}", e.getMessage(), e);
        }
        return sb.toString();
    }


    private final class Worker extends Thread {
        private final Process process;
        private Integer exit;
        private Worker(Process process) {
            this.process = process;
        }
        public void run() {
            try {
                exit = process.waitFor();
                if (exit != 0) {
                    error = stderr(process);
                }
            } catch (InterruptedException ignore) { }
        }
    }
}
