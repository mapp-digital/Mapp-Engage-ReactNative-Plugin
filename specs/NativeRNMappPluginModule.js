// @flow

import type {TurboModule} from 'react-native';
import {TurboModuleRegistry} from 'react-native';
import type {Int32} from 'react-native/Libraries/Types/CodegenTypes';

export interface Spec extends TurboModule {
  requestGeofenceLocationPermission(): Promise<boolean>;
  requestPostNotificationPermission(): Promise<boolean>;
  setRemoteMessage(msgJson: string): Promise<boolean>;
  isPushFromMapp(msgJson: string): Promise<boolean>;
  setToken(token: string): Promise<boolean>;
  getToken(): Promise<string>;
  setAlias(alias: string): Promise<boolean>;
  setAliasWithResend(alias: string, resendCustomAttributes: boolean): Promise<boolean>;
  getAlias(): Promise<string>;
  engage2(): void;
  engage(sdkKey: string, googleProjectId: string, server: string, appID: string, tenantID: string): Promise<boolean>;
  engageTestServer(cepURl: string, sdkKey: string, googleProjectId: string, server: string, appID: string, tenantID: string): void;
  onInitCompletedListener(): Promise<boolean>;
  isReady(): Promise<boolean>;
  setPushEnabled(optIn: boolean): void;
  isPushEnabled(): Promise<boolean>;
  setAttributes(attributes: Object): Promise<boolean>;
  getAttributes(keys: Array<string>): Promise<Object>;
  setAttribute(key: string, value: string): void;
  setAttributeBoolean(key: string, value: boolean): void;
  setAttributeInt(key: string, value: Int32): void;
  removeAttribute(attribute: string): void;
  getAttributeStringValue(key: string): Promise<?string>;
  addTag(tag: string): void;
  removeTag(tag: string): void;
  getTags(): Promise<Array<string>>;
  getDeviceInfo(): Promise<Object>;
  getDeviceDmcInfo(): Promise<Object>;
  isDeviceRegistered(): Promise<boolean>;
  lockScreenOrientation(orientation: Int32): void;
  removeBadgeNumber(): void;
  startGeofencing(): Promise<string>;
  stopGeofencing(): Promise<string>;
  startGeoFencing(): void;
  stopGeoFencing(): void;
  fetchLatestInboxMessage(): Promise<Object>;
  fetchInboxMessage(): Promise<Array<Object>>;
  triggerInApp(key: string): void;
  inAppMarkAsRead(templateId: Int32, eventId: string): void;
  inAppMarkAsUnRead(templateId: Int32, eventId: string): void;
  inAppMarkAsDeleted(templateId: Int32, eventId: string): void;
  triggerStatistic(templateId: Int32, originalEventId: string, trackingKey: string, displayMillis: Int32, reason: string, link: string): void;
  addAndroidListener(eventName: string): void;
  removeAndroidListeners(count: Int32): void;
  addListener(eventName: string): void;
  removeListeners(count: Int32): void;
  clearNotifications(): void;
  clearNotification(id: Int32): void;
  logOut(pushEnabled: boolean): void;
}

export default TurboModuleRegistry.get<Spec>('RNMappPluginModule');
