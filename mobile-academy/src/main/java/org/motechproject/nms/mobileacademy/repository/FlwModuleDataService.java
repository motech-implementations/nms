package org.motechproject.nms.mobileacademy.repository;

import com.fasterxml.jackson.databind.Module;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.nms.mobileacademy.domain.FlwModule;

import java.util.List;

/**
 * data service to perform Create and Read operations on FlwModule
 */

public interface FlwModuleDataService {


    @Lookup
    List<Module> getModulesByexternalId(Long externalId);

    @Lookup
    Void createModule(FlwModule module);
}
