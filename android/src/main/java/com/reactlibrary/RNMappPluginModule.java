
package com.reactlibrary;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.appoxee.Appoxee;
import com.appoxee.internal.model.response.DevicePayload;
import com.appoxee.internal.model.response.inbox.InboxMessage;
import com.appoxee.shared.AppoxeeObserver;
import com.appoxee.shared.AppoxeeOptions;
import com.appoxee.shared.MappResult;
import com.appoxee.shared.NotificationMode;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.module.annotations.ReactModule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aleksandar Marinkovic on 2019-05-15.
 * Copyright (c) 2019 MAPP.
 * <p>
 * Updated for Engage SDK v7:
 * - All async operations use Call<T>.enqueue(MappCallback) instead of listener interfaces.
 * - Push broadcast base class changed to LocalPushBroadcast via setPushBroadcast().
 * - Ready/init observation changed to subscribe(AppoxeeObserver).
 * - InApp statistics methods stubbed (v6 internal classes removed in v7).
 */
@SuppressWarnings("ALL")
@ReactModule(name = RNMappPluginModule.NAME)
public class RNMappPluginModule extends ReactContextBaseJavaModule {

    public static final String NAME = "RNMappPluginModule";
    private final ReactApplicationContext reactContext;
    private Map<Callback, String> mFeedSubscriberMap = new ConcurrentHashMap<>();
    private Map<Callback, Boolean> mCallbackWasCalledMap = new ConcurrentHashMap<>();
    private Application application = null;

    public RNMappPluginModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        application = (Application) reactContext.getApplicationContext();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize() {
        super.initialize();
        getReactApplicationContext().addLifecycleEventListener(new LifecycleEventListener() {
            @Override
            public void onHostResume() {
            }

            @Override
            public void onHostPause() {
            }

            @Override
            public void onHostDestroy() {
            }
        });

        EventEmitter.shared().attachReactContext(getReactApplicationContext());
    }

    // -------------------------------------------------------------------------
    // Permissions
    // -------------------------------------------------------------------------

    @ReactMethod
    public void requestGeofenceLocationPermission(Promise promise) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            promise.resolve(true);
            return;
        }
        int fineLocation = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_FINE_LOCATION);
        int backgroundLocation = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        promise.resolve(fineLocation == PackageManager.PERMISSION_GRANTED
                && backgroundLocation == PackageManager.PERMISSION_GRANTED);
    }

    @ReactMethod
    public void requestPostNotificationPermission(Promise promise) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            promise.resolve(true);
            return;
        }
        int result = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.POST_NOTIFICATIONS);
        promise.resolve(result == PackageManager.PERMISSION_GRANTED);
    }

    // -------------------------------------------------------------------------
    // Remote message
    // -------------------------------------------------------------------------

    @ReactMethod
    public void setRemoteMessage(String msgJson, Promise promise) {
        RemoteMessage remoteMessage = getRemoteMessage(msgJson);
        if (remoteMessage != null) {
            Appoxee.instance().handlePushMessage(remoteMessage);
            promise.resolve(true);
        } else {
            promise.resolve(false);
        }
    }

    @ReactMethod
    public void isPushFromMapp(String msgJson, Promise promise) {
        promise.resolve(isMappPush(msgJson));
    }

    static boolean isMappPush(@Nullable String msgJson) {
        try {
            JSONObject json = new JSONObject(msgJson);
            return json.has("data") && json.getJSONObject("data").has("p");
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Token
    // -------------------------------------------------------------------------

    @ReactMethod
    public void setToken(String token, Promise promise) {
        Appoxee.instance().updateFirebaseToken(token).enqueue(result -> promise.resolve(true));
    }

    @ReactMethod
    public void getToken(Promise promise) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                promise.resolve(task.getResult());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Alias
    // -------------------------------------------------------------------------

    @ReactMethod
    public void setAlias(String alias, Promise promise) {
        Appoxee.instance().setAlias(alias, false).enqueue(result -> promise.resolve(true));
    }

    @ReactMethod
    public void setAliasWithResend(String alias, boolean resendCustomAttributes, Promise promise) {
        Appoxee.instance().setAlias(alias, resendCustomAttributes).enqueue(result -> promise.resolve(true));
    }

    @ReactMethod
    public void getAlias(Promise promise) {
        Appoxee.instance().getAlias().enqueue(result -> promise.resolve(result.getData()));
    }

    // -------------------------------------------------------------------------
    // Engage / init
    // -------------------------------------------------------------------------

    @ReactMethod
    @Deprecated(forRemoval = true)
    public void engage2() {
        Appoxee.engage(application,null);
    }

    @ReactMethod
    public void engage(String sdkKey, String googleProjectId, String server, String appID, String tenantID) {
        AppoxeeOptions opt = createOptions(server, sdkKey, appID, tenantID);
        opt.setNotificationMode(NotificationMode.BACKGROUND_AND_FOREGROUND);

        new Handler(Looper.getMainLooper()).post(() -> {
            Appoxee.engage(Objects.requireNonNull(application), opt);

            Appoxee.instance().subscribe(new AppoxeeObserver() {
                @Override
                public void onReadyStatusChanged(boolean status, MappResult<DevicePayload> result) {
                }
            });

            Appoxee.instance().setPushBroadcast(MyPushBroadcastReceiver.class);
        });
    }

    @ReactMethod
    public void engageTestServer(String cepURl, String sdkKey, String googleProjectId, String server,
                                 String appID, String tenantID) {
        AppoxeeOptions opt = createOptions(server, sdkKey, appID, tenantID);

        new Handler(Looper.getMainLooper()).post(() -> {
            Appoxee.engage(Objects.requireNonNull(application), opt);

            Appoxee.instance().subscribe(new AppoxeeObserver() {
                @Override
                public void onReadyStatusChanged(boolean status, MappResult<DevicePayload> result) {
                }
            });

            Appoxee.instance().setPushBroadcast(MyPushBroadcastReceiver.class);
        });
    }

    static AppoxeeOptions createOptions(String server, String sdkKey, String appID, String tenantID) {
        AppoxeeOptions.Server resolvedServer = resolveServer(server);
        if (resolvedServer == null) {
            throw new IllegalArgumentException(
                    "Unsupported Mapp server '" + server
                            + "'. Use L3, L3_US, EMC, EMC_US, CROC, TEST, TEST55, TEST61, or a full server URL."
            );
        }

        return new AppoxeeOptions(resolvedServer, sdkKey, appID, tenantID);
    }

    @Nullable
    static AppoxeeOptions.Server resolveServer(@Nullable String server) {
        if (server == null) {
            return null;
        }

        String normalizedServer = server.trim();
        if (normalizedServer.isEmpty()) {
            return null;
        }

        switch (normalizedServer.toUpperCase(Locale.ROOT)) {
            case "L3":
                return AppoxeeOptions.Server.L3;
            case "L3_US":
            case "L3US":
                return AppoxeeOptions.Server.L3_US;
            case "EMC":
                return AppoxeeOptions.Server.EMC;
            case "EMC_US":
            case "EMCUS":
                return AppoxeeOptions.Server.EMC_US;
            case "CROC":
                return AppoxeeOptions.Server.CROC;
            case "TEST":
                return AppoxeeOptions.Server.TEST;
            case "TEST55":
            case "TEST_55":
                return AppoxeeOptions.Server.TEST_55;
            case "TEST61":
            case "TEST_61":
                return AppoxeeOptions.Server.TEST_61;
            default:
                return AppoxeeOptions.Server.get(normalizedServer);
        }
    }

    @ReactMethod
    public void onInitCompletedListener(final Promise promise) {
        Appoxee.instance().subscribe(new AppoxeeObserver() {
            @Override
            public void onReadyStatusChanged(boolean status, MappResult<DevicePayload> result) {
                promise.resolve(status);
            }
        });
    }

    @ReactMethod
    public void isReady(Promise promise) {
        promise.resolve(Appoxee.instance().isReady());
    }

    // -------------------------------------------------------------------------
    // Push opt-in / opt-out
    // -------------------------------------------------------------------------

    @ReactMethod
    public void setPushEnabled(boolean optIn) {
        Appoxee.instance().enablePush(optIn, null).enqueue(result -> {
        });
    }

    @ReactMethod
    public void isPushEnabled(Promise promise) {
        Appoxee.instance().isPushEnabled().enqueue(result -> {
            Boolean enabled = (result != null && result.isSuccess()) ? result.getData() : false;
            promise.resolve(enabled != null ? enabled : false);
        });
    }

    // -------------------------------------------------------------------------
    // Custom attributes (bulk)
    // -------------------------------------------------------------------------

    @ReactMethod
    public void setAttributes(ReadableMap attributes, Promise promise) {
        if (attributes != null) {
            Map<String, Object> internalMap = new HashMap<>();
            attributes.getEntryIterator().forEachRemaining(entry -> {
                Object value = entry.getValue() == null ? "" : entry.getValue();
                String key = entry.getKey();
                if (value instanceof Number) {
                    internalMap.put(key, ((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    internalMap.put(key, (Boolean) value);
                } else if (value instanceof Date) {
                    internalMap.put(key, ((Date) value).getDate());
                } else {
                    internalMap.put(key, value.toString());
                }
            });
            Appoxee.instance().addCustomAttributes(internalMap).enqueue(result -> promise.resolve(true));
        } else {
            promise.resolve(true);
        }
    }

    @ReactMethod
    public void getAttributes(ReadableArray keys, Promise promise) {
        List<String> internalKeys = new ArrayList<>();
        if (keys != null && keys.size() > 0) {
            for (int i = 0; i < keys.size(); i++) {
                internalKeys.add(keys.getString(i));
            }
        }

        if (!internalKeys.isEmpty()) {
            Appoxee.instance().getCustomAttributes(new HashSet<>(internalKeys)).enqueue(result -> {
                if (result != null && result.isSuccess()) {
                    Map<String, Object> customAttributes = (Map<String, Object>) result.getData();
                    WritableMap resultMap = new WritableNativeMap();
                    if (customAttributes != null) {
                        for (Map.Entry<String, Object> entry : customAttributes.entrySet()) {
                            resultMap.putString(entry.getKey(),
                                    entry.getValue() != null ? entry.getValue().toString() : "");
                        }
                    }
                    promise.resolve(resultMap);
                } else {
                    Throwable error = (result != null && result.getError() != null) ? result.getError() : new Throwable("Error getting attributes");
                    promise.reject(error);
                }
            });
        } else {
            promise.resolve(new WritableNativeMap());
        }
    }

    // -------------------------------------------------------------------------
    // Custom attributes (single)
    // -------------------------------------------------------------------------

    @ReactMethod
    public void setAttribute(String key, String value) {
        Map<String, Object> attr = new HashMap<>();
        attr.put(key, value);
        Appoxee.instance().addCustomAttributes(attr).enqueue(result -> {
        });
    }

    @ReactMethod
    public void setAttributeBoolean(String key, Boolean value) {
        Map<String, Object> attr = new HashMap<>();
        attr.put(key, value);
        Appoxee.instance().addCustomAttributes(attr).enqueue(result -> {
        });
    }

    @ReactMethod
    public void setAttributeInt(String key, Integer value) {
        Map<String, Object> attr = new HashMap<>();
        attr.put(key, value);
        Appoxee.instance().addCustomAttributes(attr).enqueue(result -> {
        });
    }

    @ReactMethod
    public void removeAttribute(String attribute) {
        Appoxee.instance().removeCustomAttributes(Collections.singleton(attribute)).enqueue(result -> {
        });
    }

    @ReactMethod
    public void getAttributeStringValue(String key, Promise promise) {
        Appoxee.instance().getCustomAttributes(Collections.singleton(key)).enqueue(result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                Map<String, Object> attrs = (Map<String, Object>) result.getData();
                Object value = attrs.get(key);
                promise.resolve(value != null ? value.toString() : null);
            } else {
                promise.resolve(null);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Tags
    // -------------------------------------------------------------------------

    @ReactMethod
    public void addTag(String tag) {
        Appoxee.instance().addTags(Collections.singleton(tag)).enqueue(result -> {
        });
    }

    @ReactMethod
    public void removeTag(String tag) {
        Appoxee.instance().removeTags(Collections.singleton(tag)).enqueue(result -> {
        });
    }

    @ReactMethod
    public void getTags(Promise promise) {
        Appoxee.instance().getTags().enqueue(result -> {
            WritableArray array = Arguments.createArray();
            if (result != null && result.isSuccess() && result.getData() != null) {
                for (String tag : result.getData()) {
                    array.pushString(tag);
                }
            }
            promise.resolve(array);
        });
    }

    // -------------------------------------------------------------------------
    // Device info
    // -------------------------------------------------------------------------

    @ReactMethod
    public void getDeviceInfo(Promise promise) {
        Appoxee.instance().getDevice().enqueue(result -> {
            WritableMap deviceInfo = new WritableNativeMap();
            try {
                // Use dmcUserId from v7 DevicePayload as the device id
                DevicePayload payload = (result != null && result.isSuccess()) ? result.getData() : null;
                deviceInfo.putString("id", payload != null && payload.getDmcUserId() != null
                        ? payload.getDmcUserId() : "");

                // Reconstruct app/device metadata from Android APIs (removed from v7 DevicePayload)
                try {
                    String appVersion = reactContext.getPackageManager()
                            .getPackageInfo(reactContext.getPackageName(), 0).versionName;
                    deviceInfo.putString("appVersion", appVersion != null ? appVersion : "");
                } catch (PackageManager.NameNotFoundException e) {
                    deviceInfo.putString("appVersion", "");
                }
                deviceInfo.putString("sdkVersion", "7.0.0");
                deviceInfo.putString("locale", Locale.getDefault().toString());
                deviceInfo.putString("timezone", TimeZone.getDefault().getID());
                deviceInfo.putString("deviceModel", Build.MODEL);
                deviceInfo.putString("manufacturer", Build.MANUFACTURER);
                deviceInfo.putString("osVersion", Build.VERSION.RELEASE);

                DisplayMetrics dm = reactContext.getResources().getDisplayMetrics();
                deviceInfo.putString("resolution", dm.widthPixels + "x" + dm.heightPixels);
                deviceInfo.putString("density", String.valueOf(dm.density));
            } catch (Exception e) {
                e.printStackTrace();
            }
            promise.resolve(deviceInfo);
        });
    }

    @ReactMethod
    public void getDeviceDmcInfo(final Promise promise) {
        Appoxee.instance().getDevice().enqueue(result -> {
            try {
                DevicePayload payload = (result != null && result.isSuccess()) ? result.getData() : null;
                WritableMap wm = new WritableNativeMap();
                if (payload != null) {
                    if (payload.getDmcUserId() != null)
                        wm.putString("dmcUserId", payload.getDmcUserId());
                    if (payload.getUdidHashed() != null)
                        wm.putString("udidHashed", payload.getUdidHashed());
                    if (payload.getPushTokenBk() != null)
                        wm.putString("pushTokenBk", payload.getPushTokenBk());
                    if (payload.getPushToken() != null)
                        wm.putString("pushToken", payload.getPushToken());
                    if (payload.getAlias() != null) wm.putString("alias", payload.getAlias());
                }
                promise.resolve(wm);
            } catch (Exception e) {
                promise.reject("Error getting DMC Info", e);
            }
        });
    }

    @ReactMethod
    public void isDeviceRegistered(Promise promise) {
        // isDeviceRegistered() removed in v7; derive from DevicePayload presence
        Appoxee.instance().getDevice().enqueue(result -> {
            boolean registered = result != null && result.isSuccess()
                    && result.getData() != null
                    && result.getData().getDmcUserId() != null;
            promise.resolve(registered);
        });
    }

    // -------------------------------------------------------------------------
    // Screen / badge
    // -------------------------------------------------------------------------

    @ReactMethod
    public void lockScreenOrientation(Integer orientation) {
        // setOrientation() removed in SDK v7 — no-op
    }

    @ReactMethod
    public void removeBadgeNumber() {
        // removeBadgeNumber() removed in SDK v7 — no-op
    }

    // -------------------------------------------------------------------------
    // Geofencing
    // -------------------------------------------------------------------------

    @ReactMethod
    public void startGeofencing(final Promise promise) {
        Appoxee.instance().startGeofencing(0).enqueue(result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                promise.resolve(result.getData().getStatus());
            } else {
                promise.resolve("GEOFENCE_GENERAL_ERROR");
            }
        });
    }

    @ReactMethod
    public void stopGeofencing(final Promise promise) {
        Appoxee.instance().stopGeofencing().enqueue(result -> {
            if (result != null && result.isSuccess() && result.getData() != null) {
                promise.resolve(result.getData().getStatus());
            } else {
                promise.resolve("GEOFENCE_GENERAL_ERROR");
            }
        });
    }

    @Deprecated
    @ReactMethod
    public void startGeoFencing() {
        Appoxee.instance().startGeofencing(0).enqueue(result -> {
        });
    }

    @Deprecated
    @ReactMethod
    public void stopGeoFencing() {
        Appoxee.instance().stopGeofencing().enqueue(result -> {
        });
    }

    // -------------------------------------------------------------------------
    // Inbox / InApp
    // -------------------------------------------------------------------------

    @ReactMethod
    public void fetchLatestInboxMessage(final Promise promise) {
        Appoxee.instance().fetchLatestInboxMessage().enqueue(result -> {
            if (result != null && result.isSuccess()) {
                promise.resolve(messageToJson(result.getData()));
            } else {
                promise.resolve(null);
            }
        });
    }

    @ReactMethod
    public void fetchInboxMessage(final Promise promise) {
        Appoxee.instance().fetchInboxMessages().enqueue(result -> {
            WritableArray messagesArray = Arguments.createArray();
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<InboxMessage> messages = result.getData().getMessages();
                if (messages != null) {
                    for (InboxMessage message : messages) {
                        messagesArray.pushMap(messageToJson(message));
                    }
                }
            }
            promise.resolve(messagesArray);
        });
    }

    @ReactMethod
    public void triggerInApp(String key) {
        Appoxee.instance().triggerInApp(getCurrentActivity(), key).enqueue(result -> {
        });
    }

    /**
     * Stubbed: InApp statistics internal classes were removed in v7.
     * The @ReactMethod signature is preserved to avoid breaking the JS public API.
     */
    @ReactMethod
    public void inAppMarkAsRead(Integer templateId, String eventId) {
        // no-op in v7
    }

    /**
     * @see #inAppMarkAsRead
     */
    @ReactMethod
    public void inAppMarkAsUnRead(Integer templateId, String eventId) {
        // no-op in v7
    }

    /**
     * @see #inAppMarkAsRead
     */
    @ReactMethod
    public void inAppMarkAsDeleted(Integer templateId, String eventId) {
        // no-op in v7
    }

    /**
     * @see #inAppMarkAsRead
     */
    @ReactMethod
    public void triggerStatistic(Integer templateId, String originalEventId,
                                 String trackingKey, int displayMillis,
                                 String reason, String link) {
        // no-op in v7
    }

    // -------------------------------------------------------------------------
    // Event listeners (pass-through to EventEmitter)
    // -------------------------------------------------------------------------

    @ReactMethod
    public void addAndroidListener(String eventName) {
        EventEmitter.shared().addAndroidListener(eventName);
    }

    @ReactMethod
    public void removeAndroidListeners(int count) {
        EventEmitter.shared().removeAndroidListeners(count);
    }

    @ReactMethod
    public void addListener(String eventName) {
        addAndroidListener(eventName);
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        removeAndroidListeners(count);
    }

    // -------------------------------------------------------------------------
    // Notifications
    // -------------------------------------------------------------------------

    @ReactMethod
    public void clearNotifications() {
        NotificationManagerCompat.from(reactContext.getApplicationContext()).cancelAll();
    }

    @ReactMethod
    public void clearNotification(int id) {
        NotificationManagerCompat.from(reactContext.getApplicationContext()).cancel(id);
    }

    // -------------------------------------------------------------------------
    // Session
    // -------------------------------------------------------------------------

    @ReactMethod
    public void logOut(boolean pushEnabled) {
        Appoxee.instance().logout(pushEnabled).enqueue(result -> {
        });
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    static WritableMap messageToJson(@Nullable InboxMessage msg) {
        WritableMap msgJson = new WritableNativeMap();
        if (msg == null) return msgJson;
        try {
            msgJson.putInt("templateId", (int) (long) msg.getTemplateId());
            msgJson.putString("title", msg.getContent() != null ? msg.getContent() : "");
            if (msg.getSubject() != null) msgJson.putString("subject", msg.getSubject());
            if (msg.getSummary() != null) msgJson.putString("summary", msg.getSummary());
            if (msg.getIconUrl() != null) msgJson.putString("iconURl", msg.getIconUrl());
            if (msg.getStatus() != null) msgJson.putString("status", msg.getStatus().name());
            if (msg.getExpireDate() != null)
                msgJson.putString("expirationDate", msg.getExpireDate().toString());
            if (msg.getExtras() != null) {
                for (Map.Entry<String, String> entry : msg.getExtras().entrySet()) {
                    msgJson.putString(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgJson;
    }

    static RemoteMessage getRemoteMessage(String jsonMsg) {
        if (jsonMsg == null) return null;

        final String KEY_COLLAPSE_KEY = "collapseKey";
        final String KEY_DATA = "data";
        final String KEY_MESSAGE_ID = "messageId";
        final String KEY_MESSAGE_TYPE = "messageType";
        final String KEY_TTL = "ttl";

        try {
            JSONObject json = new JSONObject(jsonMsg);
            String collapseKey = json.optString(KEY_COLLAPSE_KEY, "");
            String messageId = json.optString(KEY_MESSAGE_ID, "");
            String messageType = json.optString(KEY_MESSAGE_TYPE, "");
            int ttl = json.optInt(KEY_TTL, 0);
            JSONObject data = json.has(KEY_DATA) ? json.getJSONObject(KEY_DATA) : null;

            RemoteMessage.Builder builder = new RemoteMessage.Builder("appoxee@gcm.googleapis.com")
                    .setMessageType(messageType)
                    .setMessageId(messageId)
                    .setTtl(ttl)
                    .setCollapseKey(collapseKey);

            if (data != null) {
                for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                    String k = it.next();
                    builder.addData(k, data.getString(k));
                }
            }
            return builder.build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
