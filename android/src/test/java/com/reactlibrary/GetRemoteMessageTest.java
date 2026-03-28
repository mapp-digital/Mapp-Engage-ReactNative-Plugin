package com.reactlibrary;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for the getRemoteMessage(String) JSON parsing logic.
 *
 * getRemoteMessage() is a package-private static method on RNMappPluginModule,
 * callable here without instantiating the module.
 *
 * RISK NOTE: Tests that exercise RemoteMessage.Builder.build() depend on whether
 * RemoteMessage (which extends AbstractSafeParcelable) requires Android native
 * Parcel code at runtime. If any test annotated with the risk comment below fails
 * with UnsatisfiedLinkError or ExceptionInInitializerError, annotate it:
 *   @org.junit.Ignore("RemoteMessage.Builder.build() requires Android runtime")
 * The two null/malformed tests are zero-risk and must always pass.
 */
public class GetRemoteMessageTest {

    // -------------------------------------------------------------------------
    // Zero-risk: null guard and JSON parse failure — Builder never reached
    // -------------------------------------------------------------------------

    @Test
    public void getRemoteMessage_nullInput_returnsNull() {
        // The explicit null guard returns before constructing any object
        assertNull(RNMappPluginModule.getRemoteMessage(null));
    }

    @Test
    public void getRemoteMessage_malformedJson_returnsNull() {
        // JSONException is caught and null is returned — Builder is never reached
        assertNull(RNMappPluginModule.getRemoteMessage("{not valid json{{"));
    }

    // -------------------------------------------------------------------------
    // Medium-risk: Builder is constructed — may need Android runtime
    // -------------------------------------------------------------------------

    @Test
    public void getRemoteMessage_emptyJsonObject_returnsNonNull() {
        RemoteMessage result = RNMappPluginModule.getRemoteMessage("{}");
        assertNotNull(result);
    }

    @Test
    public void getRemoteMessage_validJsonWithDataField_returnsNonNull() {
        String json = "{\"messageId\":\"msg-001\",\"ttl\":60," +
                "\"data\":{\"p\":\"1\",\"campaign\":\"abc\"}}";
        RemoteMessage result = RNMappPluginModule.getRemoteMessage(json);
        assertNotNull(result);
        assertNotNull(result.getData());
    }

    @Test
    public void getRemoteMessage_validJsonWithoutDataField_returnsNonNull() {
        String json = "{\"messageId\":\"msg-002\",\"collapseKey\":\"ck\",\"ttl\":30}";
        RemoteMessage result = RNMappPluginModule.getRemoteMessage(json);
        assertNotNull(result);
    }

    @Test
    public void getRemoteMessage_jsonWithOnlyUnknownFields_returnsNonNull() {
        // optString(key, "") defaults prevent crash for unknown keys
        String json = "{\"unknownField\":\"value\"}";
        RemoteMessage result = RNMappPluginModule.getRemoteMessage(json);
        assertNotNull(result);
    }
}
