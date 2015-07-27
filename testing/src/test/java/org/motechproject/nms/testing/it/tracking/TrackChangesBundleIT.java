package org.motechproject.nms.testing.it.tracking;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.testing.tracking.domain.TrackedSimple;
import org.motechproject.nms.testing.tracking.repository.TrackedSimpleDataService;
import org.motechproject.nms.tracking.domain.ChangeLog;
import org.motechproject.nms.tracking.repository.ChangeLogDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import javax.inject.Inject;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class TrackChangesBundleIT extends BasePaxIT {

    @Inject
    TrackedSimpleDataService trackedSimpleDataService;

    @Inject
    ChangeLogDataService changeLogDataService;

    @Before
    public void setUp() {
        for (TrackedSimple trackedSimple : trackedSimpleDataService.retrieveAll()) {
            trackedSimple.setInstance(null);
            trackedSimpleDataService.update(trackedSimple);
        }
        trackedSimpleDataService.deleteAll();
        changeLogDataService.deleteAll();
    }

    @Test
    public void testChangesTrackedForInstanceCreation() {
        final TrackedSimple relatedInstance = trackedSimpleDataService.create(new TrackedSimple());
        TrackedSimple trackedInstance = createInstanceInTransaction(42, "hello", relatedInstance);

        List<ChangeLog> changes = changeLogDataService.findByEntityNameAndInstanceId(TrackedSimple.class.getName(), trackedInstance.getId());

        assertEquals(1, changes.size());
        String change = changes.get(0).getChange();
        assertFalse(change.contains("integer"));
        assertTrue(change.contains("string(null, hello)"));
        assertTrue(change.contains(String.format("instance(null, %d)", relatedInstance.getId())));
    }

    @Test
    public void testChangesTrackedForInstanceUpdate() {
        final TrackedSimple oldRelatedInstance = trackedSimpleDataService.create(new TrackedSimple());
        final TrackedSimple newRelatedInstance = trackedSimpleDataService.create(new TrackedSimple());
        final TrackedSimple trackedInstance = createInstanceInTransaction(42, "hello", oldRelatedInstance);
        TrackedSimple updatedTrackedInstance = updateInstanceInTransaction(trackedInstance.getId(), 24, "world", newRelatedInstance);

        List<ChangeLog> changes = changeLogDataService.findByEntityNameAndInstanceId(TrackedSimple.class.getName(), updatedTrackedInstance.getId());

        assertEquals(2, changes.size());
        String change = changes.get(0).getTimestamp().isAfter(changes.get(1).getTimestamp()) ?
                changes.get(0).getChange() :
                changes.get(1).getChange();
        assertFalse(change.contains("integer"));
        assertTrue(change.contains("string(hello, world)"));
        assertTrue(change.contains(String.format("instance(%d, %d)", oldRelatedInstance.getId(), newRelatedInstance.getId())));
    }

    @Test
    public void testChangesDeletedWithInstanceDeletion() {
        final TrackedSimple oldRelatedInstance = trackedSimpleDataService.create(new TrackedSimple());
        final TrackedSimple newRelatedInstance = trackedSimpleDataService.create(new TrackedSimple());
        final TrackedSimple trackedInstance = createInstanceInTransaction(42, "hello", oldRelatedInstance);
        TrackedSimple updatedTrackedInstance = updateInstanceInTransaction(trackedInstance.getId(), 24, "world", newRelatedInstance);

        List<ChangeLog> changes;
        changes = changeLogDataService.findByEntityNameAndInstanceId(TrackedSimple.class.getName(), updatedTrackedInstance.getId());
        assertEquals(2, changes.size());

        trackedSimpleDataService.delete(updatedTrackedInstance);

        changes = changeLogDataService.findByEntityNameAndInstanceId(TrackedSimple.class.getName(), updatedTrackedInstance.getId());
        assertTrue(CollectionUtils.isEmpty(changes));
    }

    private TrackedSimple createInstanceInTransaction(final int integer, final String string, final TrackedSimple instance) {
        return trackedSimpleDataService.doInTransaction(new TransactionCallback<TrackedSimple>() {
            @Override
            public TrackedSimple doInTransaction(TransactionStatus transactionStatus) {
                TrackedSimple trackedInstance = new TrackedSimple();
                trackedInstance.setInteger(integer); // not tracked
                trackedInstance.setString(string); // tracked
                trackedInstance.setInstance(instance); // tracked
                return trackedSimpleDataService.create(trackedInstance);
            }
        });
    }

    private TrackedSimple updateInstanceInTransaction(final Long instanceId, final int integer, final String string, final TrackedSimple instance) {
        return trackedSimpleDataService.doInTransaction(new TransactionCallback<TrackedSimple>() {
            @Override
            public TrackedSimple doInTransaction(TransactionStatus transactionStatus) {
                TrackedSimple updatedTrackedInstance = trackedSimpleDataService.findById(instanceId);
                updatedTrackedInstance.setInteger(integer); // not tracked
                updatedTrackedInstance.setString(string); // tracked
                updatedTrackedInstance.setInstance(instance); // tracked
                return updatedTrackedInstance;
            }
        });
    }
}
