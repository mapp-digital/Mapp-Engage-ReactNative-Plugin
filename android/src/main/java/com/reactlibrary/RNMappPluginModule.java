
package com.reactlibrary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

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
import com.appoxee.push.PushData;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by Aleksandar Marinkovic on 2019-05-15.
 * Copyright (c) 2019 MAPP.
 */
public class RNMappPluginModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private Map<Callback, String> mFeedSubscriberMap = new ConcurrentHashMap<>();
    private Map<Callback, Boolean> mCallbackWasCalledMap = new ConcurrentHashMap<>();

    public RNMappPluginModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNMappPlugin";
    }

    @Override
    public void initialize() {
        super.initialize();


        getReactApplicationContext().addLifecycleEventListener(new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                Appoxee.engage(Objects.requireNonNull(reactContext.getCurrentActivity()).getApplication());
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
    public void setAlias(String alias) {
        Appoxee.instance().setAlias(alias);

    }

    @ReactMethod
    public void engage() {
        Appoxee.engage(Objects.requireNonNull(reactContext.getCurrentActivity()).getApplication());

    }

    @ReactMethod
    public void engage(String sdkKey, String googleProjectId, String cepURL, String appID, String tenantID) {

        AppoxeeOptions opt = new AppoxeeOptions();
        opt.appID = appID;
        opt.sdkKey = sdkKey;
        opt.googleProjectId = googleProjectId;
        opt.cepURL = cepURL;
        opt.tenantID = tenantID;
        Appoxee.engage(Objects.requireNonNull(reactContext.getCurrentActivity()).getApplication(), opt);


    }

    @ReactMethod
    public void setPushEnabled(boolean optIn) {
        Appoxee.instance().setPushEnabled(optIn);

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
    public void setAttribute(String key, boolean value) {
        Appoxee.instance().setAttribute(key, value);

    }

    @ReactMethod
    public void setAttribute(String key, int value) {
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
    public void getAttributeStringValue(Promise promise, String value) {
        promise.resolve(Appoxee.instance().getAttributeStringValue(value));
    }


    @ReactMethod
    public void lockScreenOrientation(int orientation) {
        Appoxee.setOrientation(Objects.requireNonNull(reactContext.getCurrentActivity()).getApplication(), orientation);
    }

    @ReactMethod
    public void removeBadgeNumber() {
        Appoxee.removeBadgeNumber(Objects.requireNonNull(reactContext.getCurrentActivity()).getApplication());
    }

    @ReactMethod
    public void startGeoFencing() {
        if (shouldRequestLocationPermissions()) {
            ReqiestPermissionsTask.RequestPermissionsTask task = new ReqiestPermissionsTask.RequestPermissionsTask(getReactApplicationContext(), new ReqiestPermissionsTask.RequestPermissionsTask.Callback() {
                @Override
                public void onResult(boolean enabled) {
                    if (enabled) {
                        Appoxee.instance().startGeoFencing();
                    }
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            Appoxee.instance().startGeoFencing();
        }


    }
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
        Appoxee.instance().triggerDMCCallInApp((reactContext.getCurrentActivity()), key);
    }

    @ReactMethod
    public void inAppMarkAsRead(int templateId, String eventId) {
        Appoxee.instance().triggerStatistcs((reactContext.getCurrentActivity()), getInAppStatisticsRequestObject(templateId,
                eventId,
                InAppStatistics.INBOX_INBOX_MESSAGE_READ_KEY, null, null, null));
    }

    @ReactMethod
    public void inAppMarkAsUnRead(int templateId, String eventId) {
        Appoxee.instance().triggerStatistcs((reactContext.getCurrentActivity()), getInAppStatisticsRequestObject(templateId,
                eventId,
                InAppStatistics.INBOX_INBOX_MESSAGE_UNREAD_KEY, null, null, null));
    }

    @ReactMethod
    public void inAppMarkAsDeleted(int templateId, String eventId) {
        Appoxee.instance().triggerStatistcs((reactContext.getCurrentActivity()), getInAppStatisticsRequestObject(templateId,
                eventId,
                InAppStatistics.INBOX_INBOX_MESSAGE_DELETED_KEY, null, null, null));
    }

    @ReactMethod
    public void triggerStatistic(int templateId, String originalEventId,
                                 String trackingKey, Long displayMillis,
                                 String reason, String link) {
        Appoxee.instance()
                .triggerStatistcs((reactContext.getCurrentActivity())
                        , getInAppStatisticsRequestObject(templateId, originalEventId, trackingKey, displayMillis, reason, link));
    }

    @ReactMethod
    public void isDeviceRegistered(Promise promise) {
        promise.resolve(Appoxee.instance().isDeviceRegistered());

    }

    @ReactMethod
    public void isPushEnabled(Promise promise) {
        promise.resolve(Appoxee.instance().isPushEnabled());
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
            deviceInfo.putInt("density", deviceInfoList.density);
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


    private boolean shouldRequestLocationPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        }

        return ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED &&
                ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED;
    }



}