
package com.reactlibrary;

import android.Manifest;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.appoxee.Appoxee;
import com.appoxee.AppoxeeOptions;
import com.appoxee.DeviceInfo;
import com.appoxee.internal.inapp.model.APXInboxMessage;
import com.appoxee.internal.inapp.model.ApxInAppExtras;
import com.appoxee.internal.inapp.model.InAppInboxCallback;
import com.appoxee.internal.inapp.model.InAppStatistics;
import com.appoxee.internal.inapp.model.MessageContext;
import com.appoxee.internal.inapp.model.Tracking;
import com.appoxee.internal.inapp.model.TrackingAttributes;
import com.appoxee.internal.service.AppoxeeServiceAdapter;
import com.appoxee.internal.util.ResultCallback;
import com.appoxee.push.NotificationMode;
import com.appoxee.push.PushData;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aleksandar Marinkovic on 2019-05-15.
 * Copyright (c) 2019 MAPP.
 */
@SuppressWarnings("ALL")
public class RNMappPluginModule extends ReactContextBaseJavaModule {

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
        return "RNMappPluginModule";
    }

    @Override
    public void initialize() {
        super.initialize();
        // application is initialized in constructor
/*        if (getCurrentActivity() != null)
            application = (Application) getCurrentActivity().getApplication();*/
        getReactApplicationContext().addLifecycleEventListener(new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                Appoxee.engage(application);
                Appoxee.setOrientation(application, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    private void reportResultWithCallback(Callback callback, String error, Object result) {
        if (callback != null) {
            if (error != null) {
                callback.invoke(error);
            } else {
                callback.invoke(null, result);
            }
        }
    }

    @ReactMethod
      public void setRemoteMessage(String msgJson) {
          RemoteMessage remoteMessage = getRemoteMessage(msgJson);
          if (remoteMessage != null && AppoxeeServiceAdapter.getInstance() != null) {
              Appoxee.instance().setRemoteMessage(remoteMessage);
          }
      }

    @ReactMethod
    public void isPushFromMapp(String msgJson, Promise promise) {
        try {
            JSONObject json = new JSONObject(msgJson);
            boolean mappPush = json.has("data") && json.getJSONObject("data").has("p");
            promise.resolve(mappPush);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    @ReactMethod
    public void setToken(String token) {
    if (AppoxeeServiceAdapter.getInstance() != null)
        Appoxee.instance().setToken(token);
    }

    @ReactMethod
    public void setAlias(String alias) {
        Appoxee.instance().setAlias(alias);
    }

    @ReactMethod
    public void engage2() {
        Appoxee.engage(Objects.requireNonNull(application));

    }

    @ReactMethod
    public void engage(String sdkKey, String googleProjectId, String server, String appID, String tenantID) {
        AppoxeeOptions opt = new AppoxeeOptions();
        opt.appID = appID;
        opt.sdkKey = sdkKey;
        opt.googleProjectId = googleProjectId;
        opt.server = AppoxeeOptions.Server.valueOf(server);
        if (server.equals("TEST") || server.equals("TEST55") || server.equals("TEST_55")) {
            opt.cepURL = "https://jamie-test.shortest-route.com";
        }
        opt.notificationMode = NotificationMode.BACKGROUND_AND_FOREGROUND;
        opt.tenantID = tenantID;
        Appoxee.engage(Objects.requireNonNull(application), opt);
        Appoxee.instance().addInitListener(new Appoxee.OnInitCompletedListener() {
            @Override
            public void onInitCompleted(boolean successful, Exception failReason) {
                /**
                 * OnInitCompleteListener must be attached;
                 * Internally {@link AppoxeeServiceAdapter#getDeviceInfoDMC()} is called and "user_id" created.
                 * If "user_id" is null InApp messages are not working.
                 */
            }
        });
        Appoxee.setOrientation(application, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @ReactMethod
    public void engageTestServer(String cepURl, String sdkKey, String googleProjectId, String server, String appID, String tenantID) {
        AppoxeeOptions opt = new AppoxeeOptions();
        opt.appID = appID;
        opt.sdkKey = sdkKey;
        opt.googleProjectId = googleProjectId;
        opt.server = AppoxeeOptions.Server.valueOf(server);
        opt.cepURL = cepURl;
        opt.tenantID = tenantID;
        Appoxee.setOrientation(application, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Appoxee.engage(Objects.requireNonNull(application), opt);
    }

    @ReactMethod
    public void setPushEnabled(boolean optIn) {
        Appoxee.instance().setPushEnabled(optIn);

    }

    @ReactMethod
    public void isPushEnabled(Promise promise) {
        promise.resolve(Appoxee.instance().isPushEnabled());
    }


    @ReactMethod
    public void isReady(Promise promise) {
        promise.resolve(Appoxee.instance().isReady());

    }

    @ReactMethod
    public void onInitCompletedListener(final Promise promise) {
        Appoxee.instance().addInitListener(new Appoxee.OnInitCompletedListener() {
            @Override
            public void onInitCompleted(boolean b, Exception e) {
                promise.resolve(b);
            }
        });
    }


    @ReactMethod
    public void setAttribute(String key, String value) {
        Appoxee.instance().setAttribute(key, value);

    }

    @ReactMethod
    public void setAttributeBoolean(String key, Boolean value) {
        Appoxee.instance().setAttribute(key, value);

    }

    @ReactMethod
    public void setAttributeInt(String key, Integer value) {
        Appoxee.instance().setAttribute(key, value);

    }

    @ReactMethod
    public void addTag(String tag) {
        Appoxee.instance().addTag(tag);

    }

    @ReactMethod
    public void removeTag(String tag) {
        Appoxee.instance().removeTag(tag);

    }

    @ReactMethod
    public void getTags(Promise promise) {

        WritableArray array = Arguments.createArray();
        for (String tag : Appoxee.instance().getTags()) {
            array.pushString(tag);
        }
        promise.resolve(array);
    }

    @ReactMethod
    public void removeAttribute(String attribute) {
        Appoxee.instance().removeAttribute(attribute);
    }


    @ReactMethod
    public void getAlias(Promise promise) {
        promise.resolve(Appoxee.instance().getAlias());
    }

    @ReactMethod
    public void getDeviceInfo(Promise promise) {
        promise.resolve(getDeviceInfoJson(Appoxee.instance().getDeviceInfo()));
    }

    @ReactMethod
    public void getAttributeStringValue(String value, Promise promise) {
        promise.resolve(Appoxee.instance().getAttributeStringValue(value));
    }


    @ReactMethod
    public void lockScreenOrientation(Integer orientation) {
        Appoxee.setOrientation(Objects.requireNonNull((Application) reactContext.getApplicationContext()), orientation);
    }

    @ReactMethod
    public void removeBadgeNumber() {
        Appoxee.removeBadgeNumber(Objects.requireNonNull((Application) reactContext.getApplicationContext()));
    }

    @ReactMethod
    public void startGeofencing(final Promise promise) {
        Appoxee.instance().startGeoFencing(new ResultCallback<String>() {
            @Override
            public void onResult(@Nullable String result) {
                promise.resolve(result);
            }
        });
    }

    @ReactMethod
    public void stopGeofencing(final Promise promise) {
        Appoxee.instance().stopGeoFencing(new ResultCallback<String>() {
            @Override
            public void onResult(@Nullable String result) {
                promise.resolve(result);
            }
        });
    }

    /**
     * This method is deprecated in Java. Use method {@link #startGeofencing(ResultCallback)}}
     */
    @ReactMethod
    @Deprecated()
    public void startGeoFencing() {
        Appoxee.instance().startGeoFencing();
    }

    /**
     * Deprecated in Java. Use method {@link #stopGeofencing(ResultCallback)}
     */
    @Deprecated
    @ReactMethod
    public void stopGeoFencing() {
        Appoxee.instance().stopGeoFencing();
    }

    @ReactMethod
    public void fetchInboxMessage(final Promise promise) {

        Appoxee.instance().fetchInboxMessages(reactContext.getApplicationContext());

        InAppInboxCallback inAppInboxCallback = new InAppInboxCallback();
        inAppInboxCallback.addInAppInboxMessagesReceivedCallback(new InAppInboxCallback.onInAppInboxMessagesReceived() {
            @Override
            public void onInAppInboxMessages(List<APXInboxMessage> richMessages) {
                WritableArray messagesArray = Arguments.createArray();
                if (richMessages != null)
                    for (APXInboxMessage message : richMessages) {
                        messagesArray.pushMap(messageToJson(message));
                    }
                promise.resolve(messagesArray);
            }

            @Override
            public void onInAppInboxMessage(final APXInboxMessage message) {
                promise.resolve(messageToJson(message));
            }
        });
    }

    @ReactMethod
    public void triggerInApp(String key) {
        Appoxee.instance().triggerInApp((getCurrentActivity()), key);
    }

    @ReactMethod
    public void inAppMarkAsRead(Integer templateId, String eventId) {
        Appoxee.instance().triggerStatistcs((getCurrentActivity()), getInAppStatisticsRequestObject(templateId,
                eventId,
                InAppStatistics.INBOX_INBOX_MESSAGE_READ_KEY, null, null, null));
    }

    @ReactMethod
    public void inAppMarkAsUnRead(Integer templateId, String eventId) {
        Appoxee.instance().triggerStatistcs((reactContext.getApplicationContext()), getInAppStatisticsRequestObject(templateId,
                eventId,
                InAppStatistics.INBOX_INBOX_MESSAGE_UNREAD_KEY, null, null, null));
    }

    @ReactMethod
    public void inAppMarkAsDeleted(Integer templateId, String eventId) {
        Appoxee.instance().triggerStatistcs((reactContext.getApplicationContext()), getInAppStatisticsRequestObject(templateId,
                eventId,
                InAppStatistics.INBOX_INBOX_MESSAGE_DELETED_KEY, null, null, null));
    }

    @ReactMethod
    public void triggerStatistic(Integer templateId, String originalEventId,
                                 String trackingKey, Long displayMillis,
                                 String reason, String link) {
        Appoxee.instance()
                .triggerStatistcs((reactContext.getApplicationContext())
                        , getInAppStatisticsRequestObject(templateId, originalEventId, trackingKey, displayMillis, reason, link));
    }

    @ReactMethod
    public void isDeviceRegistered(Promise promise) {
        promise.resolve(Appoxee.instance().isDeviceRegistered());

    }


    @ReactMethod
    public void addAndroidListener(String eventName) {
        EventEmitter.shared().addAndroidListener(eventName);
    }

    @ReactMethod
    public void removeAndroidListeners(int count) {
        EventEmitter.shared().removeAndroidListeners(count);
    }


    private static InAppStatistics getInAppStatisticsRequestObject(int templateId, String originalEventId,
                                                                   String trackingKey, Long displayMillis,
                                                                   String reason, String link) {

        InAppStatistics inAppStatistics = new InAppStatistics();
        //This will be received from the respective Screens.
        MessageContext mc = new MessageContext();
        mc.setTemplateId(templateId);
        mc.setOriginialEventid(originalEventId);
        inAppStatistics.setMessageContext(mc);
        Tracking tk = new Tracking();
        tk.setTrackingKey(trackingKey);
        TrackingAttributes ta = new TrackingAttributes();
        ta.setTimeSinceDisplayMillis(displayMillis);
        ta.setReason(reason);
        ta.setLink(link);
        tk.setTrackingAttributes(ta);
        inAppStatistics.setTracking(tk);

        return inAppStatistics;

    }


    private WritableMap messageToJson(APXInboxMessage msg) {
        WritableMap msgJson = new WritableNativeMap();

        try {
            msgJson.putInt("templateId", msg.getTemplateId());
            msgJson.putString("title", msg.getContent());
            msgJson.putString("eventId", msg.getEventId());
            if (msg.getExpirationDate() != null)
                msgJson.putString("expirationDate", msg.getExpirationDate().toString());
            if (msg.getIconUrl() != null)
                msgJson.putString("iconURl", msg.getIconUrl());
            if (msg.getStatus() != null)
                msgJson.putString("status", msg.getStatus());
            if (msg.getSubject() != null)
                msgJson.putString("subject", msg.getSubject());
            if (msg.getSummary() != null)
                msgJson.putString("summary", msg.getSummary());
            if (msg.getExtras() != null)
                for (ApxInAppExtras apxInAppExtras : msg.getExtras())
                    msgJson.putString(apxInAppExtras.getName(), apxInAppExtras.getValue());


        } catch (Exception e) {
            e.printStackTrace();
        }

        return msgJson;
    }


    private WritableMap getDeviceInfoJson(DeviceInfo deviceInfoList) {
        WritableMap deviceInfo = new WritableNativeMap();
        try {
            deviceInfo.putString("id", deviceInfoList.id);
            deviceInfo.putString("appVersion", deviceInfoList.appVersion);
            deviceInfo.putString("sdkVersion", deviceInfoList.sdkVersion);
            deviceInfo.putString("locale", deviceInfoList.locale);
            deviceInfo.putString("timezone", deviceInfoList.timezone);
            deviceInfo.putString("deviceModel", deviceInfoList.deviceModel);
            deviceInfo.putString("manufacturer", deviceInfoList.manufacturer);
            deviceInfo.putString("osVersion", deviceInfoList.osVersion);
            deviceInfo.putString("resolution", deviceInfoList.resolution);
            deviceInfo.putString("density", String.valueOf(deviceInfoList.density));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deviceInfo;
    }


    public static WritableMap getPushMessageToJSon(PushData pushData) {
        WritableMap deviceInfo = new WritableNativeMap();
        try {
            deviceInfo.putString("id", String.valueOf(pushData.id));
            deviceInfo.putString("title", pushData.title);
            deviceInfo.putString("bigText", pushData.bigText);
            deviceInfo.putString("sound", pushData.sound);
            if (pushData.actionUri != null)
                deviceInfo.putString("actionUri", pushData.actionUri.toString());
            deviceInfo.putString("collapseKey", pushData.collapseKey);
            deviceInfo.putInt("badgeNumber", pushData.badgeNumber);
            deviceInfo.putString("silentType", pushData.silentType);
            deviceInfo.putString("silentData", pushData.silentData);
            deviceInfo.putString("category", pushData.category);
            if (pushData.extraFields != null)
                for (Map.Entry<String, String> entry : pushData.extraFields.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    deviceInfo.putString(key, value);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deviceInfo;
    }

private RemoteMessage getRemoteMessage(String jsonMsg) {
        if (jsonMsg == null)
            return null;

        final String KEY_TOKEN = "token";
        final String KEY_COLLAPSE_KEY = "collapseKey";
        final String KEY_DATA = "data";
        final String KEY_FROM = "from";
        final String KEY_MESSAGE_ID = "messageId";
        final String KEY_MESSAGE_TYPE = "messageType";
        final String KEY_SENT_TIME = "sentTime";
        final String KEY_ERROR = "error";
        final String KEY_TO = "to";
        final String KEY_TTL = "ttl";

        JSONObject json = null;
        try {
            json = new JSONObject(jsonMsg);

            String collapseKey = json.has(KEY_COLLAPSE_KEY) ? json.getString(KEY_COLLAPSE_KEY) : "";
            String messageId = json.has(KEY_MESSAGE_ID) ? json.getString(KEY_MESSAGE_ID) : "";
            String messageType = json.has(KEY_MESSAGE_TYPE) ? json.getString(KEY_MESSAGE_TYPE) : "";
            int ttl = json.has(KEY_TTL) ? json.getInt(KEY_TTL) : 0;
            JSONObject data = json.has(KEY_DATA) ? json.getJSONObject(KEY_DATA) : null;

            RemoteMessage.Builder builder = new RemoteMessage.Builder("appoxee@gcm.googleapis.com")
                    .setMessageType(messageType)
                    .setMessageId(messageId)
                    .setTtl(ttl)
                    .setCollapseKey(collapseKey);

            if (data != null) {
                for (Iterator<String> it = data.keys(); it.hasNext();) {
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

    private boolean shouldRequestLocationPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        }

        return ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED &&
                ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED;
    }

    @ReactMethod
    public void clearNotifications() {
        NotificationManagerCompat.from(reactContext.getApplicationContext()).cancelAll();
    }


    @ReactMethod
    public void clearNotification(int id) {
        NotificationManagerCompat.from(reactContext.getApplicationContext()).cancel(id);
    }

    @ReactMethod
    public void logOut(boolean pushEnabled) {
        Appoxee.instance().logOut(application, pushEnabled);
    }


}
