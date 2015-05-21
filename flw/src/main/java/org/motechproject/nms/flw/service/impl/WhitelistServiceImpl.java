package org.motechproject.nms.flw.service.impl;

import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.WhitelistService;
import org.motechproject.nms.region.domain.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;

@Service("whitelistService")
public class WhitelistServiceImpl implements WhitelistService {
    private WhitelistStateDataService whitelistStateDataService;
    private WhitelistEntryDataService whitelistEntryDataService;

    @Autowired
    public WhitelistServiceImpl(WhitelistEntryDataService whitelistEntryDataService,
                                WhitelistStateDataService whitelistStateDataService) {
        this.whitelistEntryDataService = whitelistEntryDataService;
        this.whitelistStateDataService = whitelistStateDataService;
    }

    private boolean whitelistEnabledForState(final State state) {
        // Find a state cap by providing a state
        QueryExecution<Long> stateQueryExecution = new QueryExecution<Long>() {
            @Override
            public Long execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("state == flw_state");
                query.declareParameters("org.motechproject.nms.region.domain.State flw_state");
                query.setResult("count(state)");
                query.setUnique(true);

                return (Long) query.execute(state);
            }
        };

        Long isWhitelisted = whitelistStateDataService.executeQuery(stateQueryExecution);

        if (isWhitelisted != null && isWhitelisted > 0) {
            return true;
        }

        return false;
    }

    /**
     * Checks if a number is whitelisted for a particular state.
     * The api will verify if whitelisting is turned on for a state and if it is check if the number is
     * in the whitelist.  The api will consider a number as being whitelisted if any of the following are true
     *   1) whitelisting is not turned on for a state
     *   2) whitelisting is on and the number is in the list
     *   3) provided state is null
     *   4) provided contactNumber is null
     *
     * @param state The state's whitelist to check
     * @param contactNumber The number to look for on the whitelist
     * @return true if the number is whitelisted false if the number is not allowed
     */
    @Override
    public boolean numberWhitelistedForState(final State state, final Long contactNumber) {

        if (state == null || contactNumber == null) {
            // By default I allow calls through if needed fields are missing
            return true;
        }

        if (!whitelistEnabledForState(state)) {
            // If whitelisting is not enabled for a state then all calls are allowed through
            return true;
        }

        QueryExecution<Long> stateQueryExecution = new QueryExecution<Long>() {
            @Override
            public Long execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("state == flw_state && contactNumber == flw_number");
                query.declareParameters("org.motechproject.nms.region.domain.State flw_state, Long flw_number");
                query.setResult("count(contactNumber)");
                query.setUnique(true);

                return (Long) query.execute(state, contactNumber);
            }
        };

        Long isWhitelisted = whitelistEntryDataService.executeQuery(stateQueryExecution);

        return isWhitelisted > 0;

    }
}
