package org.motechproject.nms.cache.ut;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.nms.cache.service.CacheService;
import org.motechproject.nms.cache.service.impl.CacheServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for the Cache Service
 */
public class CacheServiceUnitTest {

    @Mock
    private CacheService cacheService;

    @Before
    public void setup() {
        initMocks(this);
        cacheService = new CacheServiceImpl();
    }

    @Test
    public void verifyCacheNotInitialized() {
        try {
            cacheService.get("foo", "bar");
        } catch (IllegalStateException e) {
            assertEquals("No cache was registered.", e.getMessage());
        }
    }

    @Test
    public void verifyCacheNotPresent() {
        cacheService.create("foo", new CacheService.Refresher() {
            @Override
            public Object get(Object key) {
                return "xxx";
            }
        });

        try {
            cacheService.get("bar", "baz");
        } catch (IllegalStateException e) {
            assertEquals("No cache found for 'bar'.", e.getMessage());
        }
    }

    @Test
    public void verifyCacheFunctional() {
        cacheService.create("foo", new CacheService.Refresher() {
            @Override
            public Object get(Object key) {
                if ("key".equals(key))
                    return "value";
                return null;
            }
        });

        assertEquals("value", cacheService.get("foo", "key"));
    }
}
