"use strict";

/**
 * Shared react-native mock for Jest tests.
 *
 * Provides a fully-mocked RNMappPluginModule where every method is a jest.fn().
 * Tests can override Platform.OS per describe/test block via:
 *   mockPlatform.OS = 'ios';
 * and reset it in afterEach.
 */

const mockNativeModule = {
  // Engage / init
  engage: jest.fn(),
  engage2: jest.fn(),
  engageTestServer: jest.fn(),
  autoengage: jest.fn(),
  engageInapp: jest.fn(),
  onInitCompletedListener: jest.fn(),
  isReady: jest.fn(),

  // Token
  setToken: jest.fn(),
  getToken: jest.fn(),

  // Push
  setRemoteMessage: jest.fn(),
  isPushFromMapp: jest.fn(),
  setPushEnabled: jest.fn(),
  isPushEnabled: jest.fn(),
  setPostponeNotificationRequest: jest.fn(),
  showNotificationAlertView: jest.fn(),
  setShowNotificationsAtForeground: jest.fn(),
  requestPostNotificationPermission: jest.fn(),

  // Alias
  setAlias: jest.fn(),
  setAliasWithResend: jest.fn(),
  getAlias: jest.fn(),

  // Attributes
  setAttributes: jest.fn(),
  getAttributes: jest.fn(),
  setAttribute: jest.fn(),
  setAttributeInt: jest.fn(),
  removeAttribute: jest.fn(),
  getAttributeStringValue: jest.fn(),

  // Tags
  addTag: jest.fn(),
  removeTag: jest.fn(),
  getTags: jest.fn(),

  // Device
  getDeviceInfo: jest.fn(),
  getDeviceDmcInfo: jest.fn(),
  isDeviceRegistered: jest.fn(),
  incrementNumericKey: jest.fn(),

  // Geofencing
  requestGeofenceLocationPermission: jest.fn(),
  startGeofencing: jest.fn(),
  stopGeofencing: jest.fn(),
  startGeoFencing: jest.fn(),
  stopGeoFencing: jest.fn(),

  // Screen / badge
  lockScreenOrientation: jest.fn(),
  removeBadgeNumber: jest.fn(),

  // Inbox / InApp
  fetchLatestInboxMessage: jest.fn(),
  fetchInboxMessage: jest.fn(),
  triggerInApp: jest.fn(),
  inAppMarkAsRead: jest.fn(),
  inAppMarkAsUnRead: jest.fn(),
  inAppMarkAsDeleted: jest.fn(),
  triggerStatistic: jest.fn(),

  // Notifications
  clearNotifications: jest.fn(),
  clearNotification: jest.fn(),

  // Session
  logOut: jest.fn(),

  // Actions
  runAction: jest.fn(),

  // Listeners
  addAndroidListener: jest.fn(),
  removeAndroidListeners: jest.fn(),
};

const mockPlatform = { OS: "android" };

const mockEventEmitter = {
  addListener: jest.fn(() => ({ remove: jest.fn() })),
  removeListener: jest.fn(),
  removeAllListeners: jest.fn(),
  listeners: jest.fn(() => []),
};

module.exports = {
  NativeModules: { RNMappPluginModule: mockNativeModule },
  NativeEventEmitter: jest.fn(() => mockEventEmitter),
  Platform: mockPlatform,
  // Export for direct use in tests
  _mockNativeModule: mockNativeModule,
  _mockPlatform: mockPlatform,
};
