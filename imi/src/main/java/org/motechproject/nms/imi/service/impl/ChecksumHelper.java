package org.motechproject.nms.imi.service.impl;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public final class ChecksumHelper {

    private ChecksumHelper() { }

    public static String checksum(File file) throws IOException {
            return DigestUtils.md5Hex(new FileInputStream(file));
    }
}
