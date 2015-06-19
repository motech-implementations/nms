package org.motechproject.nms.testing.it.utils;


import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class ChecksumHelper {

    private ChecksumHelper() { }

    public static String checksum(File file) {
        try {
            return DigestUtils.sha1Hex(new FileInputStream(file));
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to generate checksum: %s", e.getMessage()), e);
        }
    }
}
