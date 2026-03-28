"use strict";

/**
 * Bridge tests: verify that Mapp.js calls the correct native methods
 * with the correct parameters for every public static method.
 *
 * Uses a mocked react-native module so no native runtime is needed.
 * Platform.OS is set per describe block to test platform branches.
 */

const rn = require("react-native");
const native = rn._mockNativeModule;
const platform = rn._mockPlatform;

// Mapp.js uses `module.exports = Mapp` (CommonJS) with Flow types stripped by Babel
const Mapp = require("../Mapp");

beforeEach(() => {
  // Reset all mock call history and restore Android as default platform
  Object.values(native).forEach((fn) => {
    if (typeof fn === "function" && fn.mockClear) fn.mockClear();
  });
  platform.OS = "android";
});

// ---------------------------------------------------------------------------
// Token
// ---------------------------------------------------------------------------

describe("setToken", () => {
  test("passes token to native", () => {
    Mapp.setToken("my-firebase-token");
    expect(native.setToken).toHaveBeenCalledWith("my-firebase-token");
    expect(native.setToken).toHaveBeenCalledTimes(1);
  });
});

describe("getToken", () => {
  test("calls native getToken with no args", () => {
    Mapp.getToken();
    expect(native.getToken).toHaveBeenCalledWith();
    expect(native.getToken).toHaveBeenCalledTimes(1);
  });
});

// ---------------------------------------------------------------------------
// Push detection
// ---------------------------------------------------------------------------

describe("setRemoteMessage", () => {
  test("serialises message to JSON before passing to native", () => {
    const msg = { data: { p: "1", campaign: "test" } };
    Mapp.setRemoteMessage(msg);
    expect(native.setRemoteMessage).toHaveBeenCalledWith(JSON.stringify(msg));
  });
});

describe("isPushFromMapp", () => {
  test("serialises message to JSON before passing to native", () => {
    const msg = { data: { p: "1" } };
    Mapp.isPushFromMapp(msg);
    expect(native.isPushFromMapp).toHaveBeenCalledWith(JSON.stringify(msg));
  });
});

// ---------------------------------------------------------------------------
// Alias
// ---------------------------------------------------------------------------

describe("setAlias", () => {
  test("calls setAliasWithResend when resendAttributes is true", () => {
    Mapp.setAlias("user123", true);
    expect(native.setAliasWithResend).toHaveBeenCalledWith("user123", true);
    expect(native.setAlias).not.toHaveBeenCalled();
  });

  test("calls setAliasWithResend when resendAttributes is false", () => {
    Mapp.setAlias("user123", false);
    expect(native.setAliasWithResend).toHaveBeenCalledWith("user123", false);
    expect(native.setAlias).not.toHaveBeenCalled();
  });

  test("calls plain setAlias when resendAttributes is undefined", () => {
    Mapp.setAlias("user123");
    expect(native.setAlias).toHaveBeenCalledWith("user123");
    expect(native.setAliasWithResend).not.toHaveBeenCalled();
  });

  test("calls plain setAlias when resendAttributes is null", () => {
    Mapp.setAlias("user123", null);
    expect(native.setAlias).toHaveBeenCalledWith("user123");
    expect(native.setAliasWithResend).not.toHaveBeenCalled();
  });
});

describe("getAlias", () => {
  test("calls native getAlias", () => {
    Mapp.getAlias();
    expect(native.getAlias).toHaveBeenCalledWith();
  });
});

// ---------------------------------------------------------------------------
// Engage / init — Android
// ---------------------------------------------------------------------------

describe("engage (Android)", () => {
  test("passes all 5 params to native engage", () => {
    Mapp.engage("sdkKey", "projectId", "L3", "appId", "tenantId");
    expect(native.engage).toHaveBeenCalledWith(
      "sdkKey", "projectId", "L3", "appId", "tenantId"
    );
    expect(native.autoengage).not.toHaveBeenCalled();
    expect(native.engageInapp).not.toHaveBeenCalled();
  });
});

describe("engage (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("calls autoengage and engageInapp with server, not the 5-param engage", () => {
    Mapp.engage("sdkKey", "projectId", "L3", "appId", "tenantId");
    expect(native.autoengage).toHaveBeenCalledWith("L3");
    expect(native.engageInapp).toHaveBeenCalledWith("L3");
    expect(native.engage).not.toHaveBeenCalled();
  });
});

describe("engageTestServer (Android)", () => {
  test("passes all 6 params to native", () => {
    Mapp.engageTestServer("cepUrl", "sdkKey", "projectId", "TEST55", "appId", "tenantId");
    expect(native.engageTestServer).toHaveBeenCalledWith(
      "cepUrl", "sdkKey", "projectId", "TEST55", "appId", "tenantId"
    );
  });
});

describe("engageTestServer (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native on iOS", () => {
    Mapp.engageTestServer("cepUrl", "sdkKey", "projectId", "TEST55", "appId", "tenantId");
    expect(native.engageTestServer).not.toHaveBeenCalled();
  });
});

describe("engage2 (Android)", () => {
  test("calls native engage2", () => {
    Mapp.engage2();
    expect(native.engage2).toHaveBeenCalledWith();
  });
});

describe("engage2 (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native on iOS", () => {
    Mapp.engage2();
    expect(native.engage2).not.toHaveBeenCalled();
  });
});

describe("onInitCompletedListener (Android)", () => {
  test("calls native onInitCompletedListener", () => {
    Mapp.onInitCompletedListener();
    expect(native.onInitCompletedListener).toHaveBeenCalledWith();
  });
});

describe("onInitCompletedListener (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("returns null without calling native", () => {
    const result = Mapp.onInitCompletedListener();
    expect(result).toBeNull();
    expect(native.onInitCompletedListener).not.toHaveBeenCalled();
  });
});

describe("isReady", () => {
  test("calls native isReady", () => {
    Mapp.isReady();
    expect(native.isReady).toHaveBeenCalledWith();
  });
});

// ---------------------------------------------------------------------------
// Push opt-in
// ---------------------------------------------------------------------------

describe("setPushEnabled", () => {
  test("passes true to native", () => {
    Mapp.setPushEnabled(true);
    expect(native.setPushEnabled).toHaveBeenCalledWith(true);
  });

  test("passes false to native", () => {
    Mapp.setPushEnabled(false);
    expect(native.setPushEnabled).toHaveBeenCalledWith(false);
  });
});

describe("isPushEnabled", () => {
  test("calls native isPushEnabled", () => {
    Mapp.isPushEnabled();
    expect(native.isPushEnabled).toHaveBeenCalledWith();
  });
});

// ---------------------------------------------------------------------------
// Custom attributes
// ---------------------------------------------------------------------------

describe("setAttributes", () => {
  test("passes attributes object to native", () => {
    const attrs = { name: "Alice", age: 30 };
    Mapp.setAttributes(attrs);
    expect(native.setAttributes).toHaveBeenCalledWith(attrs);
  });
});

describe("getAttributes", () => {
  test("passes keys array to native", () => {
    const keys = ["name", "age"];
    Mapp.getAttributes(keys);
    expect(native.getAttributes).toHaveBeenCalledWith(keys);
  });
});

describe("setAttributeString", () => {
  test("calls native setAttribute with key and value", () => {
    Mapp.setAttributeString("city", "Berlin");
    expect(native.setAttribute).toHaveBeenCalledWith("city", "Berlin");
  });
});

describe("setAttributeInt", () => {
  test("calls native setAttributeInt with key and value", () => {
    Mapp.setAttributeInt("score", 42);
    expect(native.setAttributeInt).toHaveBeenCalledWith("score", 42);
  });
});

describe("removeAttribute (Android)", () => {
  test("calls native removeAttribute", () => {
    Mapp.removeAttribute("city");
    expect(native.removeAttribute).toHaveBeenCalledWith("city");
  });
});

describe("removeAttribute (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native on iOS", () => {
    Mapp.removeAttribute("city");
    expect(native.removeAttribute).not.toHaveBeenCalled();
  });
});

describe("getAttributeStringValue", () => {
  test("passes key to native", () => {
    Mapp.getAttributeStringValue("city");
    expect(native.getAttributeStringValue).toHaveBeenCalledWith("city");
  });
});

// ---------------------------------------------------------------------------
// Tags
// ---------------------------------------------------------------------------

describe("addTag", () => {
  test("passes tag to native", () => {
    Mapp.addTag("vip");
    expect(native.addTag).toHaveBeenCalledWith("vip");
  });
});

describe("removeTag", () => {
  test("passes tag to native", () => {
    Mapp.removeTag("vip");
    expect(native.removeTag).toHaveBeenCalledWith("vip");
  });
});

describe("getTags", () => {
  test("calls native getTags", () => {
    Mapp.getTags();
    expect(native.getTags).toHaveBeenCalledWith();
  });
});

// ---------------------------------------------------------------------------
// Device info
// ---------------------------------------------------------------------------

describe("getDeviceInfo", () => {
  test("calls native getDeviceInfo", () => {
    Mapp.getDeviceInfo();
    expect(native.getDeviceInfo).toHaveBeenCalledWith();
  });
});

describe("getDeviceDmcInfo (Android)", () => {
  test("calls native getDeviceDmcInfo", () => {
    Mapp.getDeviceDmcInfo();
    expect(native.getDeviceDmcInfo).toHaveBeenCalledWith();
  });
});

describe("getDeviceDmcInfo (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native, returns resolved promise", async () => {
    const result = await Mapp.getDeviceDmcInfo();
    expect(result).toBeNull();
    expect(native.getDeviceDmcInfo).not.toHaveBeenCalled();
  });
});

describe("isDeviceRegistered", () => {
  test("calls native isDeviceRegistered", () => {
    Mapp.isDeviceRegistered();
    expect(native.isDeviceRegistered).toHaveBeenCalledWith();
  });
});

// ---------------------------------------------------------------------------
// Geofencing
// ---------------------------------------------------------------------------

describe("requestGeofenceLocationPermission", () => {
  test("calls native", () => {
    Mapp.requestGeofenceLocationPermission();
    expect(native.requestGeofenceLocationPermission).toHaveBeenCalledWith();
  });
});

describe("startGeofencing", () => {
  test("calls native", () => {
    Mapp.startGeofencing();
    expect(native.startGeofencing).toHaveBeenCalledWith();
  });
});

describe("stopGeofencing", () => {
  test("calls native", () => {
    Mapp.stopGeofencing();
    expect(native.stopGeofencing).toHaveBeenCalledWith();
  });
});

// ---------------------------------------------------------------------------
// Screen / badge
// ---------------------------------------------------------------------------

describe("lockScreenOrientation (Android)", () => {
  test("passes value to native", () => {
    Mapp.lockScreenOrientation(1);
    expect(native.lockScreenOrientation).toHaveBeenCalledWith(1);
  });
});

describe("lockScreenOrientation (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native on iOS", () => {
    Mapp.lockScreenOrientation(1);
    expect(native.lockScreenOrientation).not.toHaveBeenCalled();
  });
});

describe("removeBadgeNumber", () => {
  test("calls native", () => {
    Mapp.removeBadgeNumber();
    expect(native.removeBadgeNumber).toHaveBeenCalledWith();
  });
});

// ---------------------------------------------------------------------------
// Inbox / InApp
// ---------------------------------------------------------------------------

describe("fetchLatestInboxMessage (Android)", () => {
  test("calls native fetchLatestInboxMessage", () => {
    Mapp.fetchLatestInboxMessage();
    expect(native.fetchLatestInboxMessage).toHaveBeenCalledWith();
  });
});

describe("fetchInboxMessage", () => {
  test("calls native fetchInboxMessage", () => {
    Mapp.fetchInboxMessage();
    expect(native.fetchInboxMessage).toHaveBeenCalledWith();
  });
});

describe("triggerInApp", () => {
  test("passes key to native", () => {
    Mapp.triggerInApp("welcome_screen");
    expect(native.triggerInApp).toHaveBeenCalledWith("welcome_screen");
  });
});

describe("inAppMarkAsRead", () => {
  test("passes templateId and eventId to native", () => {
    Mapp.inAppMarkAsRead(42, "evt-001");
    expect(native.inAppMarkAsRead).toHaveBeenCalledWith(42, "evt-001");
  });
});

describe("inAppMarkAsUnRead", () => {
  test("passes templateId and eventId to native", () => {
    Mapp.inAppMarkAsUnRead(42, "evt-001");
    expect(native.inAppMarkAsUnRead).toHaveBeenCalledWith(42, "evt-001");
  });
});

describe("inAppMarkAsDeleted", () => {
  test("passes templateId and eventId to native", () => {
    Mapp.inAppMarkAsDeleted(42, "evt-001");
    expect(native.inAppMarkAsDeleted).toHaveBeenCalledWith(42, "evt-001");
  });
});

describe("triggerStatistic (Android)", () => {
  test("passes all 6 params to native", () => {
    Mapp.triggerStatistic(1, "origEvt", "trackKey", 5000, "reason", "http://link");
    expect(native.triggerStatistic).toHaveBeenCalledWith(
      1, "origEvt", "trackKey", 5000, "reason", "http://link"
    );
  });
});

describe("triggerStatistic (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native on iOS, returns null", () => {
    const result = Mapp.triggerStatistic(1, "origEvt", "trackKey", 5000, "reason", "http://link");
    expect(result).toBeNull();
    expect(native.triggerStatistic).not.toHaveBeenCalled();
  });
});

// ---------------------------------------------------------------------------
// Notifications
// ---------------------------------------------------------------------------

describe("clearNotifications", () => {
  test("calls native clearNotifications", () => {
    Mapp.clearNotifications();
    expect(native.clearNotifications).toHaveBeenCalledWith();
  });
});

describe("clearNotification", () => {
  test("passes identifier to native", () => {
    Mapp.clearNotification("notif-123");
    expect(native.clearNotification).toHaveBeenCalledWith("notif-123");
  });
});

// ---------------------------------------------------------------------------
// Permissions
// ---------------------------------------------------------------------------

describe("requestPostNotificationPermission (Android)", () => {
  test("calls native", () => {
    Mapp.requestPostNotificationPermission();
    expect(native.requestPostNotificationPermission).toHaveBeenCalledWith();
  });
});

describe("requestPostNotificationPermission (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native, resolves to true", async () => {
    const result = await Mapp.requestPostNotificationPermission();
    expect(result).toBe(true);
    expect(native.requestPostNotificationPermission).not.toHaveBeenCalled();
  });
});

// ---------------------------------------------------------------------------
// iOS-only methods
// ---------------------------------------------------------------------------

describe("showNotificationAlertView (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("calls native on iOS", () => {
    Mapp.showNotificationAlertView();
    expect(native.showNotificationAlertView).toHaveBeenCalledWith();
  });
});

describe("showNotificationAlertView (Android)", () => {
  test("does not call native on Android", () => {
    Mapp.showNotificationAlertView();
    expect(native.showNotificationAlertView).not.toHaveBeenCalled();
  });
});

describe("setShowNotificationsAtForeground (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("passes value to native", () => {
    Mapp.setShowNotificationsAtForeground(true);
    expect(native.setShowNotificationsAtForeground).toHaveBeenCalledWith(true);
  });
});

describe("setShowNotificationsAtForeground (Android)", () => {
  test("does not call native on Android", () => {
    Mapp.setShowNotificationsAtForeground(true);
    expect(native.setShowNotificationsAtForeground).not.toHaveBeenCalled();
  });
});

describe("incrementNumericKey (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("passes key and value to native", () => {
    Mapp.incrementNumericKey("score", 5);
    expect(native.incrementNumericKey).toHaveBeenCalledWith("score", 5);
  });
});

describe("incrementNumericKey (Android)", () => {
  test("does not call native on Android, returns null", () => {
    const result = Mapp.incrementNumericKey("score", 5);
    expect(result).toBeNull();
    expect(native.incrementNumericKey).not.toHaveBeenCalled();
  });
});

// ---------------------------------------------------------------------------
// Session
// ---------------------------------------------------------------------------

describe("logOut", () => {
  test("passes pushEnabled to native", () => {
    Mapp.logOut(true);
    expect(native.logOut).toHaveBeenCalledWith(true);
  });

  test("passes false to native", () => {
    Mapp.logOut(false);
    expect(native.logOut).toHaveBeenCalledWith(false);
  });
});

// ---------------------------------------------------------------------------
// addCustomEvent — JS transform before native call
// ---------------------------------------------------------------------------

describe("addCustomEvent", () => {
  test("transforms CustomEvent fields into action arg and calls runAction", () => {
    native.runAction.mockReturnValue(Promise.resolve());

    const CustomEvent = require("../CustomEvent");
    const event = new CustomEvent("purchase", 9.99);
    event.transactionId = "txn-001";
    event.addProperty("item", "book");

    Mapp.addCustomEvent(event);

    expect(native.runAction).toHaveBeenCalledWith("add_custom_event_action", {
      event_name: "purchase",
      event_value: 9.99,
      transaction_id: "txn-001",
      properties: { item: "book" },
    });
  });
});

// ---------------------------------------------------------------------------
// runAction (Android)
// ---------------------------------------------------------------------------

describe("runAction (Android)", () => {
  test("passes name and value to native", () => {
    Mapp.runAction("my_action", { key: "val" });
    expect(native.runAction).toHaveBeenCalledWith("my_action", { key: "val" });
  });
});

describe("runAction (iOS)", () => {
  beforeEach(() => { platform.OS = "ios"; });

  test("does not call native on iOS", () => {
    Mapp.runAction("my_action", { key: "val" });
    expect(native.runAction).not.toHaveBeenCalled();
  });
});
