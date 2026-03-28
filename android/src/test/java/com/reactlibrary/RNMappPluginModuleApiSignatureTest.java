package com.reactlibrary;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * API contract test: verifies that every @ReactMethod on RNMappPluginModule
 * exists with exactly the expected parameter types.
 *
 * This test must pass both BEFORE and AFTER the SDK v6 → v7 migration.
 * Any accidental rename or signature change will be caught here.
 */
public class RNMappPluginModuleApiSignatureTest {

    /**
     * Asserts that the class declares a method with the given name and
     * parameter types (in order). Fails with a descriptive message if absent.
     */
    private void assertMethod(String name, Class<?>... params) {
        String paramDesc = Arrays.stream(params)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        try {
            RNMappPluginModule.class.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            fail("Missing @ReactMethod: " + name + "(" + paramDesc + ")");
        }
    }

    /**
     * Asserts that the class declares a non-public helper method with the given
     * name and parameter types. Fails if absent or if accidentally made public.
     */
    private void assertHelperMethod(String name, Class<?>... params) {
        String paramDesc = Arrays.stream(params)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        try {
            Method m = RNMappPluginModule.class.getDeclaredMethod(name, params);
            assertFalse(
                    "Helper method " + name + "(" + paramDesc + ") should not be public",
                    java.lang.reflect.Modifier.isPublic(m.getModifiers())
            );
        } catch (NoSuchMethodException e) {
            fail("Missing helper method: " + name + "(" + paramDesc + ")");
        }
    }

    @Test
    public void testAllReactMethodSignatures() {
        // --- Permissions ---
        assertMethod("requestGeofenceLocationPermission", Promise.class);
        assertMethod("requestPostNotificationPermission", Promise.class);

        // --- Remote message / push detection ---
        assertMethod("setRemoteMessage", String.class, Promise.class);
        assertMethod("isPushFromMapp", String.class, Promise.class);

        // --- Token ---
        assertMethod("setToken", String.class, Promise.class);
        assertMethod("getToken", Promise.class);

        // --- Alias ---
        assertMethod("setAlias", String.class, Promise.class);
        assertMethod("setAliasWithResend", String.class, boolean.class, Promise.class);
        assertMethod("getAlias", Promise.class);

        // --- Engage / init ---
        assertMethod("engage2");
        assertMethod("engage",
                String.class, String.class, String.class, String.class, String.class);
        assertMethod("engageTestServer",
                String.class, String.class, String.class, String.class, String.class, String.class);
        assertMethod("onInitCompletedListener", Promise.class);
        assertMethod("isReady", Promise.class);

        // --- Push opt-in/out ---
        assertMethod("setPushEnabled", boolean.class);
        assertMethod("isPushEnabled", Promise.class);

        // --- Custom attributes (bulk) ---
        assertMethod("setAttributes", ReadableMap.class, Promise.class);
        assertMethod("getAttributes", ReadableArray.class, Promise.class);

        // --- Custom attributes (single) ---
        assertMethod("setAttribute", String.class, String.class);
        assertMethod("setAttributeBoolean", String.class, Boolean.class);
        assertMethod("setAttributeInt", String.class, Integer.class);
        assertMethod("removeAttribute", String.class);
        assertMethod("getAttributeStringValue", String.class, Promise.class);

        // --- Tags ---
        assertMethod("addTag", String.class);
        assertMethod("removeTag", String.class);
        assertMethod("getTags", Promise.class);

        // --- Device info ---
        assertMethod("getDeviceInfo", Promise.class);
        assertMethod("getDeviceDmcInfo", Promise.class);
        assertMethod("isDeviceRegistered", Promise.class);

        // --- Screen / badge ---
        assertMethod("lockScreenOrientation", Integer.class);
        assertMethod("removeBadgeNumber");

        // --- Geofencing ---
        assertMethod("startGeofencing", Promise.class);
        assertMethod("stopGeofencing", Promise.class);
        assertMethod("startGeoFencing");
        assertMethod("stopGeoFencing");

        // --- Inbox / InApp ---
        assertMethod("fetchLatestInboxMessage", Promise.class);
        assertMethod("fetchInboxMessage", Promise.class);
        assertMethod("triggerInApp", String.class);
        assertMethod("inAppMarkAsRead", Integer.class, String.class);
        assertMethod("inAppMarkAsUnRead", Integer.class, String.class);
        assertMethod("inAppMarkAsDeleted", Integer.class, String.class);
        assertMethod("triggerStatistic",
                Integer.class, String.class, String.class, int.class, String.class, String.class);

        // --- Event listeners ---
        assertMethod("addAndroidListener", String.class);
        assertMethod("removeAndroidListeners", int.class);
        assertMethod("addListener", String.class);
        assertMethod("removeListeners", Integer.class);

        // --- Notifications ---
        assertMethod("clearNotifications");
        assertMethod("clearNotification", int.class);

        // --- Session ---
        assertMethod("logOut", boolean.class);
    }

    @Test
    public void testPrivateHelperSignatures() {
        // resolveServer(String) — core v7 server-name fix
        assertHelperMethod("resolveServer", String.class);

        // createOptions(String, String, String, String) — delegates to resolveServer
        assertHelperMethod("createOptions",
                String.class, String.class, String.class, String.class);

        // getRemoteMessage(String) — JSON → RemoteMessage parsing
        assertHelperMethod("getRemoteMessage", String.class);

        // messageToJson(InboxMessage) — inbox message serialization
        try {
            Class<?> inboxMessageClass = Class.forName(
                    "com.appoxee.internal.model.response.inbox.InboxMessage");
            assertHelperMethod("messageToJson", inboxMessageClass);
        } catch (ClassNotFoundException e) {
            fail("InboxMessage class not on test classpath: " + e.getMessage());
        }
    }
}
