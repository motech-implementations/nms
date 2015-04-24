package org.motechproject.nms.location.service.impl;

import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.location.domain.State;
import org.motechproject.nms.location.service.LocationService;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link LocationService} interface.
 */
@Service("locationService")
public class LocationServiceImpl implements LocationService {

    public static final String HACKY_LANGUAGE_CODE = "34";
    public static final long HACKY_WHITELISTED_STATE_CODE = -1L;
    public static final long HACKY_NON_WHITELISTED_STATE_CODE = -10L;

    @Override
    public State getStateForLanguage(Language language) {
        // TODO: #71 For a given languageLocationCode find the districts it maps to and find the state for
        //       those districts (I assume we'll just take the first district)
        if (language == null) {
            return null;
        } else if (language.getCode() == HACKY_LANGUAGE_CODE) {
            return new State("Whitelist", HACKY_WHITELISTED_STATE_CODE);
        } else {
            return new State("Curacao", HACKY_NON_WHITELISTED_STATE_CODE);
        }
    }
}
