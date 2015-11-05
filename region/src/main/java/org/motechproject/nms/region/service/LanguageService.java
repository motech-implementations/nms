package org.motechproject.nms.region.service;

import org.motechproject.event.MotechEvent;
import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;

import java.util.Set;

public interface LanguageService {

    Language getForCode(String code);

    Language getForName(String name);

    Set<Language> getAllForCircle(Circle circle);

    Set<Language> getAll();

    Language getNationalDefaultLanguage();

    Set<State> getAllStatesForLanguage(Language language);

    /**
     * Lifecycle listener that broadcasts a cache evict message for the language cache
     *
     * @param language
     */
    @InstanceLifecycleListener({InstanceLifecycleListenerType.POST_CREATE, InstanceLifecycleListenerType.PRE_DELETE,
            InstanceLifecycleListenerType.PRE_STORE} )
    void broadcastCacheEvictMessage(Language language);

    /**
     *
     * Language cache evict
     *
     */
    void cacheEvict(MotechEvent event);
}
