"use strict";

/**
 * API surface contract test for Mapp.js
 *
 * Inspects Mapp.js source to verify every expected public static method is
 * declared. Uses source-text assertions so no Babel/transpilation is needed.
 *
 * This test must pass both BEFORE and AFTER the Android SDK v6 → v7 migration.
 * Any accidental removal of a public method will be caught here.
 */

const fs = require("fs");
const path = require("path");

const mappSource = fs.readFileSync(path.join(__dirname, "..", "Mapp.js"), "utf8");

/**
 * Assert that `static <name>(` appears in Mapp.js.
 * Handles both compact (`static foo(`) and spaced (`static  foo (`) forms.
 */
function assertStaticMethod(name) {
  const pattern = new RegExp(`static\\s+${name}\\s*\\(`);
  expect(mappSource).toMatch(pattern);
}

describe("Mapp.js public static API surface", () => {
  test("token methods", () => {
    assertStaticMethod("setToken");
    assertStaticMethod("getToken");
  });

  test("remote message methods", () => {
    assertStaticMethod("setRemoteMessage");
    assertStaticMethod("isPushFromMapp");
  });

  test("alias methods", () => {
    assertStaticMethod("setAlias");
    assertStaticMethod("getAlias");
  });

  test("engage methods", () => {
    assertStaticMethod("engage");
    assertStaticMethod("engage2");
    assertStaticMethod("onInitCompletedListener");
    assertStaticMethod("isReady");
  });

  test("push opt-in methods", () => {
    assertStaticMethod("setPushEnabled");
    assertStaticMethod("isPushEnabled");
  });

  test("custom attribute methods", () => {
    assertStaticMethod("setAttributes");
    assertStaticMethod("getAttributes");
    assertStaticMethod("setAttributeString");
    assertStaticMethod("setAttributeInt");
    assertStaticMethod("removeAttribute");
    assertStaticMethod("getAttributeStringValue");
  });

  test("tag methods", () => {
    assertStaticMethod("addTag");
    assertStaticMethod("removeTag");
    assertStaticMethod("getTags");
  });

  test("device info methods", () => {
    assertStaticMethod("getDeviceInfo");
    assertStaticMethod("getDeviceDmcInfo");
    assertStaticMethod("isDeviceRegistered");
  });

  test("screen / badge methods", () => {
    assertStaticMethod("lockScreenOrientation");
    assertStaticMethod("removeBadgeNumber");
  });

  test("geofencing methods", () => {
    assertStaticMethod("requestGeofenceLocationPermission");
    assertStaticMethod("startGeofencing");
    assertStaticMethod("stopGeofencing");
    assertStaticMethod("startGeoFencing");
    assertStaticMethod("stopGeoFencing");
  });

  test("inbox / inapp methods", () => {
    assertStaticMethod("fetchLatestInboxMessage");
    assertStaticMethod("fetchInboxMessage");
    assertStaticMethod("triggerInApp");
    assertStaticMethod("inAppMarkAsRead");
    assertStaticMethod("inAppMarkAsUnRead");
    assertStaticMethod("inAppMarkAsDeleted");
    assertStaticMethod("triggerStatistic");
  });

  test("session methods", () => {
    assertStaticMethod("logOut");
  });

  test("notification methods", () => {
    assertStaticMethod("clearNotifications");
    assertStaticMethod("clearNotification");
    assertStaticMethod("requestPostNotificationPermission");
  });

  test("event listener methods", () => {
    assertStaticMethod("addPushListener");
    assertStaticMethod("addDeepLinkingListener");
    assertStaticMethod("addListener");
    assertStaticMethod("removeListener");
  });
});
