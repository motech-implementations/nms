package org.motechproject.nms.rch.service;

import java.io.IOException;
/**
 * NmsWebServices.java
 *
 * created by rakesh(beehyv) on 20/08/2020
 *
 */
public interface NmsWebServices {
       String getLocationApiResponse(String urlString) throws IOException;
}
