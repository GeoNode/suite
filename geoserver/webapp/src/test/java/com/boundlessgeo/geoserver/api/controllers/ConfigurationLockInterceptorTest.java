package com.boundlessgeo.geoserver.api.controllers;

import javax.servlet.http.HttpServletRequest;
import org.geoserver.GeoServerConfigurationLock;
import org.geoserver.GeoServerConfigurationLock.LockType;
import org.geoserver.platform.GeoServerExtensions;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.WebApplicationContext;

public class ConfigurationLockInterceptorTest {

    ConfigurationLockInterceptor interceptor;

    @Before
    public void setup() {
        GeoServerConfigurationLock lock = new GeoServerConfigurationLock();
        WebApplicationContext appContext = mock(WebApplicationContext.class);
        when(appContext.getBeanNamesForType(GeoServerConfigurationLock.class)).thenReturn(new String[]{"lock"});
        when(appContext.getBean("lock")).thenReturn(lock);
        new GeoServerExtensions().setApplicationContext(appContext);
        this.interceptor = new ConfigurationLockInterceptor();
    }

    void assertLock(LockType expected, String method) throws Exception {
        HttpServletRequest req = new MockHttpServletRequest(method, "");
        interceptor.preHandle(req, null, null);
        assertEquals(expected, interceptor.THREAD_LOCK.get());
        interceptor.afterCompletion(req, null, null, null);
        assertEquals(null, interceptor.THREAD_LOCK.get());
    }

    @Test
    public void testReadLock() throws Exception {
        assertLock(LockType.READ, "get");
    }

    @Test
    public void testWriteLock() throws Exception {
        assertLock(LockType.WRITE, "post");
        assertLock(LockType.WRITE, "patch");
        assertLock(LockType.WRITE, "put");
        assertLock(LockType.WRITE, "delete");
    }

}
