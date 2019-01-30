package org.motechproject.nms.testing.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.motechproject.nms.testing.it.api.*;
import org.motechproject.nms.testing.it.flw.FrontLineWorkerServiceBundleIT;
import org.motechproject.nms.testing.it.flw.ServiceUsageCapServiceBundleIT;
import org.motechproject.nms.testing.it.flw.ServiceUsageServiceBundleIT;
import org.motechproject.nms.testing.it.flw.WhiteListServiceBundleIT;
import org.motechproject.nms.testing.it.flwUpdate.FrontLineWorkerImportServiceBundleIT;
import org.motechproject.nms.testing.it.flwUpdate.FrontLineWorkerUpdateImportServiceBundleIT;
import org.motechproject.nms.testing.it.imi.CdrFileServiceBundleIT;
import org.motechproject.nms.testing.it.imi.ImiControllerCdrBundleIT;
import org.motechproject.nms.testing.it.imi.ImiControllerObdBundleIT;
import org.motechproject.nms.testing.it.imi.TargetFileServiceBundleIT;
import org.motechproject.nms.testing.it.kilkari.*;
import org.motechproject.nms.testing.it.ma.MobileAcademyServiceBundleIT;
import org.motechproject.nms.testing.it.mcts.MctsImportBundleIT;
import org.motechproject.nms.testing.it.mcts.MctsWebServiceFacadeBundleIT;
import org.motechproject.nms.testing.it.props.PropertyServiceBundleIT;
import org.motechproject.nms.testing.it.rch.RchWebServiceFacadeBundleIT;
import org.motechproject.nms.testing.it.region.*;
import org.motechproject.nms.testing.it.testing.BundleIT;
import org.motechproject.nms.testing.it.tracking.TrackChangesBundleIT;
import org.motechproject.nms.testing.it.tracking.TrackManyToManyChangesBundleIT;
import org.motechproject.nms.testing.it.tracking.TrackOneToManyChangesBundleIT;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    /**
     * API
     */
    LanguageControllerBundleIT.class,
    UserControllerBundleIT.class,
    CallDetailsControllerBundleIT.class,
    KilkariControllerBundleIT.class,
    MobileAcademyControllerBundleIT.class,
    OpsControllerBundleIT.class,

    /**
     * FLW
     */
    ServiceUsageServiceBundleIT.class,
    ServiceUsageCapServiceBundleIT.class,
    WhiteListServiceBundleIT.class,
    FrontLineWorkerServiceBundleIT.class,

    /**
     * FLW UPDATE
     */
    FrontLineWorkerImportServiceBundleIT.class,
    FrontLineWorkerUpdateImportServiceBundleIT.class,

    /**
     * IMI
     * https://github.com/motech-implementations/mim/issues/381 (Re-enable)
     */
    ImiControllerCdrBundleIT.class,
    ImiControllerObdBundleIT.class,
    TargetFileServiceBundleIT.class,
    CdrFileServiceBundleIT.class,

    /**
     * Kilkari
     */
    RchBeneficiaryImportServiceBundleIT.class,
    MctsBeneficiaryImportServiceBundleIT.class,
    SubscriptionServiceBundleIT.class,
    SubscriberServiceBundleIT.class,
    CsrServiceBundleIT.class,

    /**
     * Mobile Academy
     */
    MobileAcademyServiceBundleIT.class,

    /**
     * Props
     */
    PropertyServiceBundleIT.class,

    /**
     * Region
     */
    LocationServiceBundleIT.class,
    CircleServiceBundleIT.class,
    NationalDefaultLanguageLocationBundleIT.class,
    LocationDataImportServiceBundleIT.class,
    LanguageLocationCodesImportServiceBundleIT.class,
    LanguageServiceBundleIT.class,
        Xml_LocationDataImportServiceBundleIT.class,

    /**
     * MCTS
     */
    MctsWebServiceFacadeBundleIT.class,
    MctsImportBundleIT.class,

    /**
     * RCH
     */
    RchWebServiceFacadeBundleIT.class,

    /**
     * Testing
     */
    BundleIT.class,

    /**
     * Tracking
     */
    TrackChangesBundleIT.class,
    TrackOneToManyChangesBundleIT.class,
    TrackManyToManyChangesBundleIT.class,
})
public class IntegrationTests {
}