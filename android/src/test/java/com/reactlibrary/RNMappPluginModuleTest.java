package com.reactlibrary;

import android.app.Application;
import android.os.Looper;

import com.appoxee.Appoxee;
import com.appoxee.internal.network.Call;
import com.appoxee.shared.AppoxeeOptions;
import com.appoxee.shared.MappCallback;
import com.appoxee.shared.MappResult;
import com.appoxee.shared.NotificationMode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration-style unit tests for RNMappPluginModule.
 *
 * Uses Robolectric to provide a real Android Application context so the module
 * can be instantiated, and Mockito static mocking to intercept Appoxee.engage()
 * and Appoxee.instance() — verifying that the correct native SDK calls are made
 * with the correct parameters.
 *
 * Pattern for async (enqueue) methods:
 *   stubCall(mockCall, successResult(value)) makes enqueue() invoke the callback
 *   synchronously, so Promise.resolve/reject can be verified inline.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class RNMappPluginModuleTest {

    private RNMappPluginModule module;
    private Appoxee mockAppoxeeInstance;
    private MockedStatic<Appoxee> mockedAppoxee;

    @Before
    public void setUp() {
        // Robolectric provides a real Application — no mocking of context needed
        Application app = RuntimeEnvironment.getApplication();
        ReactApplicationContext reactContext = mock(ReactApplicationContext.class);
        when(reactContext.getApplicationContext()).thenReturn(app);

        module = new RNMappPluginModule(reactContext);

        // Mock the Appoxee interface instance returned by Appoxee.instance()
        mockAppoxeeInstance = mock(Appoxee.class);

        // Open static mock for Appoxee — closed in @After
        mockedAppoxee = mockStatic(Appoxee.class);
        mockedAppoxee.when(Appoxee::instance).thenReturn(mockAppoxeeInstance);
    }

    @After
    public void tearDown() {
        mockedAppoxee.close();
    }

    // =========================================================================
    // engage() — verifies AppoxeeOptions is built correctly and SDK is called
    // =========================================================================

    @Test
    public void engage_callsAppoxeeEngageOnMainThread() {
        module.engage("myKey", "projectId", "L3", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(eq(RuntimeEnvironment.getApplication()), any(AppoxeeOptions.class))
        );
    }

    @Test
    public void engage_passesCorrectServerToSdk() {
        module.engage("myKey", "projectId", "L3", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        opt.getServer() == AppoxeeOptions.Server.L3
                ))
        );
    }

    @Test
    public void engage_passesCorrectSdkKeyToSdk() {
        module.engage("myKey", "projectId", "L3", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        "myKey".equals(opt.getSdkKey())
                ))
        );
    }

    @Test
    public void engage_passesCorrectAppIdAndTenantIdToSdk() {
        module.engage("myKey", "projectId", "L3", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        "appId".equals(opt.getAppId()) && "tenantId".equals(opt.getTenantId())
                ))
        );
    }

    @Test
    public void engage_setsBackgroundAndForegroundNotificationMode() {
        module.engage("myKey", "projectId", "L3", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        opt.getNotificationMode() == NotificationMode.BACKGROUND_AND_FOREGROUND
                ))
        );
    }

    @Test
    public void engage_legacyAlias_L3US_resolvesToL3_US() {
        module.engage("myKey", "projectId", "L3US", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        opt.getServer() == AppoxeeOptions.Server.L3_US
                ))
        );
    }

    @Test
    public void engage_legacyAlias_TEST55_resolvesToTEST_55() {
        module.engage("myKey", "projectId", "TEST55", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        opt.getServer() == AppoxeeOptions.Server.TEST_55
                ))
        );
    }

    @Test
    public void engage_legacyAlias_TEST61_resolvesToTEST_61() {
        module.engage("myKey", "projectId", "TEST61", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        opt.getServer() == AppoxeeOptions.Server.TEST_61
                ))
        );
    }

    @Test
    public void engage_legacyAlias_EMCUS_resolvesToEMC_US() {
        module.engage("myKey", "projectId", "EMCUS", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        opt.getServer() == AppoxeeOptions.Server.EMC_US
                ))
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void engage_invalidServer_throwsBeforeCallingSDK() {
        module.engage("myKey", "projectId", "BOGUS", "appId", "tenantId");
    }

    // =========================================================================
    // engageTestServer()
    // =========================================================================

    @Test
    public void engageTestServer_callsAppoxeeEngageWithCorrectOptions() {
        module.engageTestServer("cepUrl", "myKey", "projectId", "TEST", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(eq(RuntimeEnvironment.getApplication()), argThat(opt ->
                        opt.getServer() == AppoxeeOptions.Server.TEST
                                && "myKey".equals(opt.getSdkKey())
                                && "appId".equals(opt.getAppId())
                                && "tenantId".equals(opt.getTenantId())
                ))
        );
    }

    @Test
    public void engageTestServer_doesNotSetNotificationMode() {
        // engageTestServer must NOT force BACKGROUND_AND_FOREGROUND (unlike engage)
        module.engageTestServer("cepUrl", "myKey", "projectId", "TEST", "appId", "tenantId");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        mockedAppoxee.verify(() ->
                Appoxee.engage(any(), argThat(opt ->
                        opt.getNotificationMode() != NotificationMode.BACKGROUND_AND_FOREGROUND
                ))
        );
    }

    // =========================================================================
    // isReady()
    // =========================================================================

    @Test
    public void isReady_resolvesWithSdkValue_true() {
        when(mockAppoxeeInstance.isReady()).thenReturn(true);
        Promise promise = mock(Promise.class);

        module.isReady(promise);

        verify(promise).resolve(true);
    }

    @Test
    public void isReady_resolvesWithSdkValue_false() {
        when(mockAppoxeeInstance.isReady()).thenReturn(false);
        Promise promise = mock(Promise.class);

        module.isReady(promise);

        verify(promise).resolve(false);
    }

    // =========================================================================
    // setAlias() / setAliasWithResend()
    // =========================================================================

    @Test
    public void setAlias_callsSdkWithResendFalse() {
        Call<String> mockCall = mockCall();
        when(mockAppoxeeInstance.setAlias(anyString(), anyBoolean())).thenReturn(mockCall);

        module.setAlias("myAlias", mock(Promise.class));

        verify(mockAppoxeeInstance).setAlias(eq("myAlias"), eq(false));
    }

    @Test
    public void setAlias_resolvesTrue_onSuccess() {
        Call<String> mockCall = mockCall();
        stubCallEnqueue(mockCall, successResult("ok"));
        when(mockAppoxeeInstance.setAlias(anyString(), anyBoolean())).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.setAlias("myAlias", promise);

        verify(promise).resolve(true);
    }

    @Test
    public void setAliasWithResend_callsSdkWithResendTrue() {
        Call<String> mockCall = mockCall();
        when(mockAppoxeeInstance.setAlias(anyString(), anyBoolean())).thenReturn(mockCall);

        module.setAliasWithResend("myAlias", true, mock(Promise.class));

        verify(mockAppoxeeInstance).setAlias(eq("myAlias"), eq(true));
    }

    @Test
    public void setAliasWithResend_resendFalse_passesFalseToSdk() {
        Call<String> mockCall = mockCall();
        when(mockAppoxeeInstance.setAlias(anyString(), anyBoolean())).thenReturn(mockCall);

        module.setAliasWithResend("myAlias", false, mock(Promise.class));

        verify(mockAppoxeeInstance).setAlias(eq("myAlias"), eq(false));
    }

    @Test
    public void getAlias_resolvesWithSdkValue() {
        Call<String> mockCall = mockCall();
        stubCallEnqueue(mockCall, successResult("theAlias"));
        when(mockAppoxeeInstance.getAlias()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.getAlias(promise);

        verify(promise).resolve("theAlias");
    }

    // =========================================================================
    // setToken()
    // =========================================================================

    @Test
    public void setToken_callsSdkWithToken() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.updateFirebaseToken(anyString())).thenReturn(mockCall);

        module.setToken("myToken", mock(Promise.class));

        verify(mockAppoxeeInstance).updateFirebaseToken("myToken");
    }

    @Test
    public void setToken_resolvesTrue_onSuccess() {
        Call<Boolean> mockCall = mockCall();
        stubCallEnqueue(mockCall, successResult(true));
        when(mockAppoxeeInstance.updateFirebaseToken(anyString())).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.setToken("myToken", promise);

        verify(promise).resolve(true);
    }

    // =========================================================================
    // setPushEnabled() / isPushEnabled()
    // =========================================================================

    @Test
    public void setPushEnabled_true_callsEnablePushWithTrue() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.enablePush(anyBoolean(), any())).thenReturn(mockCall);

        module.setPushEnabled(true);

        verify(mockAppoxeeInstance).enablePush(eq(true), eq(null));
    }

    @Test
    public void setPushEnabled_false_callsEnablePushWithFalse() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.enablePush(anyBoolean(), any())).thenReturn(mockCall);

        module.setPushEnabled(false);

        verify(mockAppoxeeInstance).enablePush(eq(false), eq(null));
    }

    @Test
    public void isPushEnabled_resolvesTrueWhenEnabled() {
        Call<Boolean> mockCall = mockCall();
        stubCallEnqueue(mockCall, successResult(true));
        when(mockAppoxeeInstance.isPushEnabled()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.isPushEnabled(promise);

        verify(promise).resolve(true);
    }

    @Test
    public void isPushEnabled_resolvesFalseWhenDisabled() {
        Call<Boolean> mockCall = mockCall();
        stubCallEnqueue(mockCall, successResult(false));
        when(mockAppoxeeInstance.isPushEnabled()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.isPushEnabled(promise);

        verify(promise).resolve(false);
    }

    @Test
    public void isPushEnabled_resolvesFalseOnNullResult() {
        Call<Boolean> mockCall = mockCall();
        stubCallEnqueue(mockCall, null);
        when(mockAppoxeeInstance.isPushEnabled()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.isPushEnabled(promise);

        verify(promise).resolve(false);
    }

    // =========================================================================
    // isPushFromMapp()
    // =========================================================================

    @Test
    public void isPushFromMapp_resolvesTrueForMappPayload() {
        Promise promise = mock(Promise.class);
        module.isPushFromMapp("{\"data\":{\"p\":\"1\"}}", promise);
        verify(promise).resolve(true);
    }

    @Test
    public void isPushFromMapp_resolvesFalseForNonMappPayload() {
        Promise promise = mock(Promise.class);
        module.isPushFromMapp("{\"data\":{\"other\":\"val\"}}", promise);
        verify(promise).resolve(false);
    }

    @Test
    public void isPushFromMapp_resolvesFalseForNull() {
        Promise promise = mock(Promise.class);
        module.isPushFromMapp(null, promise);
        verify(promise).resolve(false);
    }

    // =========================================================================
    // setRemoteMessage()
    // =========================================================================

    @Test
    public void setRemoteMessage_resolveTrueForValidJson() {
        doNothing().when(mockAppoxeeInstance).handlePushMessage(any());
        Promise promise = mock(Promise.class);

        module.setRemoteMessage("{\"messageId\":\"id1\"}", promise);

        verify(promise).resolve(true);
    }

    @Test
    public void setRemoteMessage_callsHandlePushMessageOnSdk() {
        doNothing().when(mockAppoxeeInstance).handlePushMessage(any());

        module.setRemoteMessage("{\"messageId\":\"id1\"}", mock(Promise.class));

        verify(mockAppoxeeInstance).handlePushMessage(any());
    }

    @Test
    public void setRemoteMessage_resolveFalseForNullInput() {
        Promise promise = mock(Promise.class);

        module.setRemoteMessage(null, promise);

        verify(promise).resolve(false);
        verify(mockAppoxeeInstance, never()).handlePushMessage(any());
    }

    @Test
    public void setRemoteMessage_resolveFalseForMalformedJson() {
        Promise promise = mock(Promise.class);

        module.setRemoteMessage("not-json", promise);

        verify(promise).resolve(false);
        verify(mockAppoxeeInstance, never()).handlePushMessage(any());
    }

    // =========================================================================
    // addTag() / removeTag() / logOut()
    // =========================================================================

    @Test
    public void addTag_callsAddTagsWithSingletonSet() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.addTags(any())).thenReturn(mockCall);

        module.addTag("sports");

        verify(mockAppoxeeInstance).addTags(eq(Collections.singleton("sports")));
    }

    @Test
    public void removeTag_callsRemoveTagsWithSingletonSet() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.removeTags(any())).thenReturn(mockCall);

        module.removeTag("sports");

        verify(mockAppoxeeInstance).removeTags(eq(Collections.singleton("sports")));
    }

    @Test
    public void logOut_callsSdkLogoutWithPushEnabledTrue() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.logout(anyBoolean())).thenReturn(mockCall);

        module.logOut(true);

        verify(mockAppoxeeInstance).logout(true);
    }

    @Test
    public void logOut_callsSdkLogoutWithPushEnabledFalse() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.logout(anyBoolean())).thenReturn(mockCall);

        module.logOut(false);

        verify(mockAppoxeeInstance).logout(false);
    }

    // =========================================================================
    // setAttribute() / setAttributeBoolean() / setAttributeInt() / removeAttribute()
    // =========================================================================

    @Test
    public void setAttribute_callsAddCustomAttributesWithKeyAndValue() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.addCustomAttributes(any())).thenReturn(mockCall);

        module.setAttribute("color", "red");

        ArgumentCaptor<java.util.Map> captor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(mockAppoxeeInstance).addCustomAttributes(captor.capture());
        assertEquals("red", captor.getValue().get("color"));
    }

    @Test
    public void setAttributeBoolean_callsAddCustomAttributesWithBooleanValue() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.addCustomAttributes(any())).thenReturn(mockCall);

        module.setAttributeBoolean("active", true);

        ArgumentCaptor<java.util.Map> captor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(mockAppoxeeInstance).addCustomAttributes(captor.capture());
        assertEquals(true, captor.getValue().get("active"));
    }

    @Test
    public void setAttributeInt_callsAddCustomAttributesWithIntValue() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.addCustomAttributes(any())).thenReturn(mockCall);

        module.setAttributeInt("age", 30);

        ArgumentCaptor<java.util.Map> captor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(mockAppoxeeInstance).addCustomAttributes(captor.capture());
        assertEquals(30, captor.getValue().get("age"));
    }

    @Test
    public void removeAttribute_callsRemoveCustomAttributesWithSingletonSet() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.removeCustomAttributes(any())).thenReturn(mockCall);

        module.removeAttribute("color");

        verify(mockAppoxeeInstance).removeCustomAttributes(eq(Collections.singleton("color")));
    }

    // =========================================================================
    // setAttributes() (bulk)
    // =========================================================================

    @Test
    public void setAttributes_nullInput_resolvesTrueWithoutCallingSDK() {
        Promise promise = mock(Promise.class);

        module.setAttributes(null, promise);

        verify(promise).resolve(true);
        verify(mockAppoxeeInstance, never()).addCustomAttributes(any());
    }

    @Test
    public void setAttributes_validMap_callsAddCustomAttributes() {
        Call<Boolean> mockCall = mockCall();
        stubCallEnqueue(mockCall, successResult(true));
        when(mockAppoxeeInstance.addCustomAttributes(any())).thenReturn(mockCall);

        ReadableMap map = mock(ReadableMap.class);
        java.util.Iterator<java.util.Map.Entry<String, Object>> emptyIter =
                Collections.<java.util.Map.Entry<String, Object>>emptyList().iterator();
        when(map.getEntryIterator()).thenReturn(emptyIter);
        Promise promise = mock(Promise.class);

        module.setAttributes(map, promise);

        verify(mockAppoxeeInstance).addCustomAttributes(any());
        verify(promise).resolve(true);
    }

    // =========================================================================
    // getAttributes() / getAttributeStringValue()
    // =========================================================================

    // getAttributes with empty/null keys calls promise.resolve(new WritableNativeMap())
    // directly — WritableNativeMap requires React Native JNI unavailable in plain JVM tests.
    // Covered indirectly: getAttributes_withKeys verifies the SDK path; the empty-keys
    // branching logic is covered by code inspection (line 384 of RNMappPluginModule).

    @Test
    public void getAttributes_withKeys_callsGetCustomAttributesOnSdk() {
        Call<java.util.Map<String, Object>> mockCall = mockCall();
        when(mockAppoxeeInstance.getCustomAttributes(any())).thenReturn(mockCall);

        ReadableArray keys = mock(ReadableArray.class);
        when(keys.size()).thenReturn(1);
        when(keys.getString(0)).thenReturn("color");
        Promise promise = mock(Promise.class);

        module.getAttributes(keys, promise);

        ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
        verify(mockAppoxeeInstance).getCustomAttributes(captor.capture());
        assertTrue(captor.getValue().contains("color"));
    }

    @Test
    public void getAttributeStringValue_callsGetCustomAttributesForKey() {
        Call<java.util.Map<String, Object>> mockCall = mockCall();
        when(mockAppoxeeInstance.getCustomAttributes(any())).thenReturn(mockCall);

        module.getAttributeStringValue("myKey", mock(Promise.class));

        verify(mockAppoxeeInstance).getCustomAttributes(eq(Collections.singleton("myKey")));
    }

    @Test
    public void getAttributeStringValue_resolvesNullOnFailure() {
        Call<java.util.Map<String, Object>> mockCall = mockCall();
        stubCallEnqueue(mockCall, null);
        when(mockAppoxeeInstance.getCustomAttributes(any())).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.getAttributeStringValue("myKey", promise);

        verify(promise).resolve(null);
    }

    // =========================================================================
    // getTags()
    // =========================================================================

    @Test
    public void getTags_callsGetTagsOnSdk() {
        Call<java.util.List<String>> mockCall = mockCall();
        when(mockAppoxeeInstance.getTags()).thenReturn(mockCall);

        module.getTags(mock(Promise.class));

        verify(mockAppoxeeInstance).getTags();
    }

    @Test
    public void getTags_callsEnqueueOnCall() {
        // Verifies getTags() wires up to the Call returned by the SDK.
        // Promise result is not verified here because Arguments.createArray()
        // requires React Native JNI which is unavailable in plain JVM tests.
        Call<java.util.List<String>> mockCall = mockCall();
        when(mockAppoxeeInstance.getTags()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.getTags(promise);

        verify(mockCall).enqueue(any());
    }

    // =========================================================================
    // startGeofencing() / stopGeofencing()
    // =========================================================================

    @Test
    public void startGeofencing_callsSdkStartGeofencingWithZeroRadius() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.startGeofencing(anyInt())).thenReturn(mockCall);

        module.startGeofencing(mock(Promise.class));

        verify(mockAppoxeeInstance).startGeofencing(0);
    }

    @Test
    public void startGeofencing_resolvesErrorStringOnNullResult() {
        Call mockCall = mockCall();
        stubCallEnqueue(mockCall, null);
        when(mockAppoxeeInstance.startGeofencing(anyInt())).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.startGeofencing(promise);

        verify(promise).resolve("GEOFENCE_GENERAL_ERROR");
    }

    @Test
    public void stopGeofencing_callsSdkStopGeofencing() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.stopGeofencing()).thenReturn(mockCall);

        module.stopGeofencing(mock(Promise.class));

        verify(mockAppoxeeInstance).stopGeofencing();
    }

    @Test
    public void stopGeofencing_resolvesErrorStringOnNullResult() {
        Call mockCall = mockCall();
        stubCallEnqueue(mockCall, null);
        when(mockAppoxeeInstance.stopGeofencing()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.stopGeofencing(promise);

        verify(promise).resolve("GEOFENCE_GENERAL_ERROR");
    }

    // =========================================================================
    // Deprecated geofencing no-ops (startGeoFencing / stopGeoFencing)
    // =========================================================================

    @Test
    public void startGeoFencing_deprecated_callsSdkStartGeofencing() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.startGeofencing(anyInt())).thenReturn(mockCall);

        module.startGeoFencing();

        verify(mockAppoxeeInstance).startGeofencing(0);
    }

    @Test
    public void stopGeoFencing_deprecated_callsSdkStopGeofencing() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.stopGeofencing()).thenReturn(mockCall);

        module.stopGeoFencing();

        verify(mockAppoxeeInstance).stopGeofencing();
    }

    // =========================================================================
    // triggerInApp()
    // =========================================================================

    @Test
    public void triggerInApp_callsSdkTriggerInAppWithKey() {
        Call<Boolean> mockCall = mockCall();
        when(mockAppoxeeInstance.triggerInApp(any(), anyString())).thenReturn(mockCall);

        module.triggerInApp("promo_banner");

        verify(mockAppoxeeInstance).triggerInApp(any(), eq("promo_banner"));
    }

    // =========================================================================
    // InApp no-ops (v7 stubs) — smoke tests: must not throw
    // =========================================================================

    @Test
    public void inAppMarkAsRead_doesNotThrow() {
        module.inAppMarkAsRead(1, "eventId");
        // no-op in v7 — just verifying no exception
    }

    @Test
    public void inAppMarkAsUnRead_doesNotThrow() {
        module.inAppMarkAsUnRead(1, "eventId");
    }

    @Test
    public void inAppMarkAsDeleted_doesNotThrow() {
        module.inAppMarkAsDeleted(1, "eventId");
    }

    @Test
    public void triggerStatistic_doesNotThrow() {
        module.triggerStatistic(1, "origEventId", "trackingKey", 1000, "reason", "http://link");
    }

    // =========================================================================
    // Screen / badge no-ops (v7) — smoke tests
    // =========================================================================

    @Test
    public void lockScreenOrientation_doesNotThrow() {
        module.lockScreenOrientation(1);
    }

    @Test
    public void removeBadgeNumber_doesNotThrow() {
        module.removeBadgeNumber();
    }

    // =========================================================================
    // fetchInboxMessage() / fetchLatestInboxMessage()
    // =========================================================================

    @Test
    public void fetchLatestInboxMessage_callsSdkFetchLatestInboxMessage() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.fetchLatestInboxMessage()).thenReturn(mockCall);

        module.fetchLatestInboxMessage(mock(Promise.class));

        verify(mockAppoxeeInstance).fetchLatestInboxMessage();
    }

    @Test
    public void fetchLatestInboxMessage_resolvesNullOnFailure() {
        Call mockCall = mockCall();
        stubCallEnqueue(mockCall, null);
        when(mockAppoxeeInstance.fetchLatestInboxMessage()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.fetchLatestInboxMessage(promise);

        verify(promise).resolve(null);
    }

    @Test
    public void fetchInboxMessage_callsSdkFetchInboxMessages() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.fetchInboxMessages()).thenReturn(mockCall);

        module.fetchInboxMessage(mock(Promise.class));

        verify(mockAppoxeeInstance).fetchInboxMessages();
    }

    @Test
    public void fetchInboxMessage_callsEnqueueOnCall() {
        // Verifies fetchInboxMessage() wires up to the Call returned by the SDK.
        // Arguments.createArray() requires React Native JNI so Promise result
        // is not verified in plain JVM tests.
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.fetchInboxMessages()).thenReturn(mockCall);

        module.fetchInboxMessage(mock(Promise.class));

        verify(mockCall).enqueue(any());
    }

    // =========================================================================
    // isDeviceRegistered()
    // =========================================================================

    @Test
    public void isDeviceRegistered_callsGetDeviceOnSdk() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.getDevice()).thenReturn(mockCall);

        module.isDeviceRegistered(mock(Promise.class));

        verify(mockAppoxeeInstance).getDevice();
    }

    @Test
    public void isDeviceRegistered_resolvesFalseOnNullResult() {
        Call mockCall = mockCall();
        stubCallEnqueue(mockCall, null);
        when(mockAppoxeeInstance.getDevice()).thenReturn(mockCall);
        Promise promise = mock(Promise.class);

        module.isDeviceRegistered(promise);

        verify(promise).resolve(false);
    }

    // =========================================================================
    // getDeviceInfo() / getDeviceDmcInfo()
    // =========================================================================

    @Test
    public void getDeviceInfo_callsGetDeviceOnSdk() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.getDevice()).thenReturn(mockCall);

        module.getDeviceInfo(mock(Promise.class));

        verify(mockAppoxeeInstance).getDevice();
    }

    @Test
    public void getDeviceInfo_callsEnqueueOnCall() {
        // new WritableNativeMap() requires React Native JNI so Promise result
        // is not verified in plain JVM tests.
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.getDevice()).thenReturn(mockCall);

        module.getDeviceInfo(mock(Promise.class));

        verify(mockCall).enqueue(any());
    }

    @Test
    public void getDeviceDmcInfo_callsGetDeviceOnSdk() {
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.getDevice()).thenReturn(mockCall);

        module.getDeviceDmcInfo(mock(Promise.class));

        verify(mockAppoxeeInstance).getDevice();
    }

    @Test
    public void getDeviceDmcInfo_callsEnqueueOnCall() {
        // new WritableNativeMap() requires React Native JNI so Promise result
        // is not verified in plain JVM tests.
        Call mockCall = mockCall();
        when(mockAppoxeeInstance.getDevice()).thenReturn(mockCall);

        module.getDeviceDmcInfo(mock(Promise.class));

        verify(mockCall).enqueue(any());
    }

    // =========================================================================
    // addAndroidListener / removeAndroidListeners / addListener / removeListeners
    // =========================================================================

    @Test
    public void addAndroidListener_doesNotThrow() {
        // EventEmitter is a singleton — just verify no exception is thrown
        module.addAndroidListener("push_received");
    }

    @Test
    public void removeAndroidListeners_doesNotThrow() {
        module.removeAndroidListeners(1);
    }

    @Test
    public void addListener_delegatesToAddAndroidListener() {
        // addListener() is a thin alias — smoke test
        module.addListener("push_received");
    }

    @Test
    public void removeListeners_delegatesToRemoveAndroidListeners() {
        module.removeListeners(1);
    }

    // =========================================================================
    // onInitCompletedListener()
    // =========================================================================

    @Test
    public void onInitCompletedListener_callsSubscribeOnSdk() {
        module.onInitCompletedListener(mock(Promise.class));

        verify(mockAppoxeeInstance).subscribe(any());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Creates a mock Call<T> whose enqueue() does nothing by default. */
    @SuppressWarnings("unchecked")
    private static <T> Call<T> mockCall() {
        return mock(Call.class);
    }

    /**
     * Stubs call.enqueue() to invoke the MappCallback synchronously with the
     * given result — so Promise.resolve/reject assertions work inline.
     */
    @SuppressWarnings("unchecked")
    private static <T> void stubCallEnqueue(Call<T> call, MappResult<T> result) {
        doAnswer(inv -> {
            MappCallback<T> cb = (MappCallback<T>) inv.getArgument(0);
            cb.onResult(result);
            return null;
        }).when(call).enqueue(any());
    }

    /** Creates a successful MappResult wrapping the given value. */
    @SuppressWarnings("unchecked")
    private static <T> MappResult<T> successResult(T value) {
        MappResult<T> result = mock(MappResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.getData()).thenReturn(value);
        return result;
    }

    /** Mockito ArgumentMatcher via lambda — avoids importing hamcrest. */
    private static <T> T argThat(java.util.function.Predicate<T> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
