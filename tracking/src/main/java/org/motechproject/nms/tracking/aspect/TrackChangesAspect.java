package org.motechproject.nms.tracking.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareMixin;
import org.motechproject.mds.util.MemberUtil;
import org.motechproject.mds.util.PropertyUtil;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.exception.TrackChangesException;
import org.motechproject.nms.tracking.service.TrackChangesService;
import org.motechproject.nms.tracking.utils.Change;
import org.motechproject.nms.tracking.utils.TrackChanges;
import org.motechproject.nms.tracking.utils.TrackChangesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

@Aspect
public class TrackChangesAspect {

    public static final Logger LOGGER = LoggerFactory.getLogger(TrackChangesAspect.class);

    private TrackChangesService trackChangesService;

    @DeclareMixin("(@org.motechproject.nms.tracking.annotation.TrackClass *)")
    public static TrackChanges implementTrackChanges() {
        return new TrackChangesImpl();
    }

    @After("staticinitialization(@org.motechproject.nms.tracking.annotation.TrackClass *)")
    public void registerJdoLifecycleListeners(JoinPoint.StaticPart staticPart) {
        trackChangesService.registerLifecycleListeners(staticPart.getSignature().getDeclaringType());
    }

    @Before("execution(* set*(*)) && @annotation(org.motechproject.nms.tracking.annotation.TrackField)")
    public void before(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        if (target instanceof TrackChanges) {
            try {
                trackChange(joinPoint, target);
            } catch (TrackChangesException e) {
                LOGGER.error("Unable to track field changes", e);
            }
        } else {
            LOGGER.warn("Tracked field should be in a class annotated with {}", TrackClass.class.getName());
        }
    }

    private void trackChange(JoinPoint joinPoint, Object target) throws TrackChangesException {
        String propertyName = getPropertyName(joinPoint);
        Map<String, Change> changes = ((TrackChanges) target).changes();
        Change change = changes.get(propertyName);
        if (change == null) {
            trackNewChange(joinPoint, propertyName, changes);
        } else {
            trackExistingChange(joinPoint, propertyName, changes, change);
        }
    }

    private void trackNewChange(JoinPoint joinPoint, String propertyName, Map<String, Change> changes)
            throws TrackChangesException {
        Object oldValue = getOldValue(joinPoint.getTarget(), propertyName);
        Object newValue = getNewValue(joinPoint);
        if (!Objects.equals(oldValue, newValue)) {
            Change change = new Change(oldValue, newValue);
            changes.put(propertyName, change);
        }
    }

    private void trackExistingChange(JoinPoint joinPoint, String propertyName, Map<String, Change> changes, Change change)
            throws TrackChangesException {
        Object oldValue = change.getOldValue();
        Object newValue = getNewValue(joinPoint);
        if (!Objects.equals(oldValue, newValue)) {
            change.setNewValue(newValue);
        } else {
            changes.remove(propertyName);
        }
    }

    private Object getOldValue(Object target, String propertyName) throws TrackChangesException {
        try {
            return PropertyUtil.getProperty(target, propertyName);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new TrackChangesException("Unable to retrieve old value", e);
        }
    }

    private String getPropertyName(JoinPoint joinPoint) {
        String setterName = joinPoint.getSignature().getName();
        return MemberUtil.getFieldNameFromGetterSetterName(setterName);
    }

    private Object getNewValue(JoinPoint joinPoint) throws TrackChangesException {
        if (joinPoint.getArgs().length == 1) {
            return joinPoint.getArgs()[0];
        } else {
            throw new TrackChangesException("Unable to retrieve new value");
        }
    }

    @Autowired
    public void setTrackChangesService(TrackChangesService trackChangesService) {
        this.trackChangesService = trackChangesService;
    }

}
