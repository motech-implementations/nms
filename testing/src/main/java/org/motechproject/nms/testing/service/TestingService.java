package org.motechproject.nms.testing.service;

import java.io.IOException;

/**
 * Service interface for handling the Testing inbox.
 */
public interface TestingService {

    void clearDatabase();

    void createSubscriptionPacks();

    String createMctsMoms(int count, boolean staticLMP) throws IOException;

    String createMctsKids(int count, boolean staticDOB) throws IOException;
}
