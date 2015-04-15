package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.Subscriber;

/**
 *
 */
public interface KilkariService {

    Subscriber getSubscriber(String callingNumber);

}
