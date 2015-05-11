package org.motechproject.nms.flw.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.domain.WhitelistEntry;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.service.WhitelistService;
import org.motechproject.nms.region.location.domain.State;
import org.motechproject.nms.region.location.repository.StateDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class WhiteListServiceBundleIT extends BasePaxIT {
    public static final Long WHITELIST_CONTACT_NUMBER = 1111111111l;
    public static final Long NOT_WHITELIST_CONTACT_NUMBER = 9000000000l;

    private State whitelist;
    private State noWhitelist;

    @Inject
    private ServiceUsageCapDataService serviceUsageCapDataService;

    @Inject
    private WhitelistEntryDataService whitelistEntryDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private WhitelistService whitelistService;

    private void setupData() {
        whitelistEntryDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        stateDataService.deleteAll();

        whitelist = new State("Whitelist", 1l);
        stateDataService.create(whitelist);

        noWhitelist = new State("No Whitelist", 2l);
        stateDataService.create(noWhitelist);

        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER, whitelist);
        whitelistEntryDataService.create(entry);
    }

    // Test with null state
    @Test
    public void testNullState() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(null, WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test with null contactNumber
    @Test
    public void testNullContactNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(whitelist, null);
        assertTrue(result);
    }

    // Test both state and contactNumber null
    @Test
    public void testNullStateAndContactNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(null, null);
        assertTrue(result);
    }

    // Test for state without whitelist enabled
    @Test
    public void testStateWithoutWhitelist() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(noWhitelist, WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test state with whitelist, number in list
    @Test
    public void testStateWithWhitelistValidNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(whitelist, WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test state with whitelist, number not in list
    @Test
    public void testStateWithWhitelistInvalidNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(whitelist, NOT_WHITELIST_CONTACT_NUMBER);
        assertFalse(result);
    }
}
