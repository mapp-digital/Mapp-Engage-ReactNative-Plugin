package com.reactlibrary;

import com.appoxee.shared.AppoxeeOptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/**
 * Pure-logic tests for resolveServer() and createOptions().
 *
 * These tests require NO mocking. They depend only on the Mapp Engage SDK jar
 * (engage-android:7.0.1) which is already on the test classpath via the
 * module's implementation dependencies.
 *
 * resolveServer() and createOptions() are package-private static methods on
 * RNMappPluginModule so they are directly callable here without instantiating
 * the module (which would require a ReactApplicationContext).
 */
public class ResolveServerTest {

    // -------------------------------------------------------------------------
    // resolveServer — null / empty / whitespace guards
    // -------------------------------------------------------------------------

    @Test
    public void resolveServer_nullInput_returnsNull() {
        assertNull(RNMappPluginModule.resolveServer(null));
    }

    @Test
    public void resolveServer_emptyString_returnsNull() {
        assertNull(RNMappPluginModule.resolveServer(""));
    }

    @Test
    public void resolveServer_whitespaceOnly_returnsNull() {
        assertNull(RNMappPluginModule.resolveServer("   "));
    }

    // -------------------------------------------------------------------------
    // resolveServer — case-insensitivity
    // -------------------------------------------------------------------------

    @Test
    public void resolveServer_lowercaseL3_returnsL3() {
        assertEquals(AppoxeeOptions.Server.L3, RNMappPluginModule.resolveServer("l3"));
    }

    @Test
    public void resolveServer_mixedCaseEmc_returnsEMC() {
        assertEquals(AppoxeeOptions.Server.EMC, RNMappPluginModule.resolveServer("eMc"));
    }

    @Test
    public void resolveServer_mixedCaseTest55_returnsTest55() {
        assertEquals(AppoxeeOptions.Server.TEST_55, RNMappPluginModule.resolveServer("test55"));
    }

    // -------------------------------------------------------------------------
    // resolveServer — trimming
    // -------------------------------------------------------------------------

    @Test
    public void resolveServer_paddedL3_returnsL3() {
        assertEquals(AppoxeeOptions.Server.L3, RNMappPluginModule.resolveServer("  L3  "));
    }

    @Test
    public void resolveServer_paddedEmcUs_returnsEmcUs() {
        assertEquals(AppoxeeOptions.Server.EMC_US, RNMappPluginModule.resolveServer("  EMC_US  "));
    }

    // -------------------------------------------------------------------------
    // resolveServer — all canonical switch cases
    // -------------------------------------------------------------------------

    @Test
    public void resolveServer_L3_canonical() {
        assertEquals(AppoxeeOptions.Server.L3, RNMappPluginModule.resolveServer("L3"));
    }

    @Test
    public void resolveServer_L3_US_canonical() {
        assertEquals(AppoxeeOptions.Server.L3_US, RNMappPluginModule.resolveServer("L3_US"));
    }

    @Test
    public void resolveServer_L3US_alias() {
        // v7 legacy fix: "L3US" (no underscore) must map to L3_US
        assertEquals(AppoxeeOptions.Server.L3_US, RNMappPluginModule.resolveServer("L3US"));
    }

    @Test
    public void resolveServer_EMC_canonical() {
        assertEquals(AppoxeeOptions.Server.EMC, RNMappPluginModule.resolveServer("EMC"));
    }

    @Test
    public void resolveServer_EMC_US_canonical() {
        assertEquals(AppoxeeOptions.Server.EMC_US, RNMappPluginModule.resolveServer("EMC_US"));
    }

    @Test
    public void resolveServer_EMCUS_alias() {
        // v7 legacy fix: "EMCUS" (no underscore) must map to EMC_US
        assertEquals(AppoxeeOptions.Server.EMC_US, RNMappPluginModule.resolveServer("EMCUS"));
    }

    @Test
    public void resolveServer_CROC_canonical() {
        assertEquals(AppoxeeOptions.Server.CROC, RNMappPluginModule.resolveServer("CROC"));
    }

    @Test
    public void resolveServer_TEST_canonical() {
        assertEquals(AppoxeeOptions.Server.TEST, RNMappPluginModule.resolveServer("TEST"));
    }

    @Test
    public void resolveServer_TEST55_alias() {
        // v7 legacy fix: "TEST55" (no underscore) must map to TEST_55
        assertEquals(AppoxeeOptions.Server.TEST_55, RNMappPluginModule.resolveServer("TEST55"));
    }

    @Test
    public void resolveServer_TEST_55_canonical() {
        assertEquals(AppoxeeOptions.Server.TEST_55, RNMappPluginModule.resolveServer("TEST_55"));
    }

    @Test
    public void resolveServer_TEST61_alias() {
        // v7 legacy fix: "TEST61" (no underscore) must map to TEST_61
        assertEquals(AppoxeeOptions.Server.TEST_61, RNMappPluginModule.resolveServer("TEST61"));
    }

    @Test
    public void resolveServer_TEST_61_canonical() {
        assertEquals(AppoxeeOptions.Server.TEST_61, RNMappPluginModule.resolveServer("TEST_61"));
    }

    // -------------------------------------------------------------------------
    // resolveServer — default branch (unknown string delegated to Server.get())
    // -------------------------------------------------------------------------

    @Test
    public void resolveServer_unknownString_doesNotThrow() {
        // resolveServer itself must not throw for unknown strings — it delegates to
        // Server.get() which returns null for unknown inputs. The throw only happens
        // in createOptions() when the result is null.
        AppoxeeOptions.Server result = RNMappPluginModule.resolveServer("BOGUS");
        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // createOptions — invalid server throws IllegalArgumentException
    // -------------------------------------------------------------------------

    @Test
    public void createOptions_nullServer_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                RNMappPluginModule.createOptions(null, "sdkKey", "appId", "tenantId"));
    }

    @Test
    public void createOptions_emptyServer_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                RNMappPluginModule.createOptions("", "sdkKey", "appId", "tenantId"));
    }

    @Test
    public void createOptions_whitespaceServer_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                RNMappPluginModule.createOptions("   ", "sdkKey", "appId", "tenantId"));
    }

    @Test
    public void createOptions_unknownServer_throwsIllegalArgument() {
        // "BOGUS" → resolveServer returns null → IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                RNMappPluginModule.createOptions("BOGUS", "sdkKey", "appId", "tenantId"));
    }

    // -------------------------------------------------------------------------
    // createOptions — valid server returns AppoxeeOptions
    //
    // RISK: AppoxeeOptions constructor may call into Android/Kotlin runtime.
    // If these two tests fail with ExceptionInInitializerError or similar,
    // annotate them @org.junit.Ignore("AppoxeeOptions ctor requires Android runtime")
    // All tests above this point are zero-risk and must always pass.
    // -------------------------------------------------------------------------

    @Test
    public void createOptions_validServer_returnsOptions() {
        AppoxeeOptions result = RNMappPluginModule.createOptions("L3", "myKey", "myApp", "myTenant");
        assertNotNull(result);
    }

    @Test
    public void createOptions_validServerAlias_returnsOptions() {
        // "L3US" is the v7 legacy alias — verifies the whole fix end-to-end
        AppoxeeOptions result = RNMappPluginModule.createOptions("L3US", "myKey", "myApp", "myTenant");
        assertNotNull(result);
    }
}
