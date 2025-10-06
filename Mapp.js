// @flow
"use strict";

import { NativeModules, Platform } from "react-native";

import CustomEvent from "./CustomEvent.js";

import MappEventEmitter from "./MappEventEmitter.js";

const { RNMappPluginModule } = NativeModules;
const EventEmitter = new MappEventEmitter();

const IOS_INIT = "com.mapp.init";
const IOS_INBOX_MESSAGE = "com.mapp.inbox_message_received";
const IOS_INBOX_MESSAGES = "com.mapp.inbox_messages_received";
const PUSH_RECEIVED_EVENT = "com.mapp.rich_message_received";
const MappIntentEvent = "com.mapp.deep_link_received";
const IOS_RICH_MESSAGE = "com.mapp.rich_message";

/**
 * @private
 */
function convertEventEnum(type: EventName): ?string {
  if (type === "notificationResponse") {
    return PUSH_RECEIVED_EVENT;
  } else if (type === "deepLink") {
    return MappIntentEvent;
  } else if (type === "iosSDKInit") {
    return IOS_INIT;
  } else if (type === "iosInboxMessages") {
    return IOS_INBOX_MESSAGES;
  } else if (type === "iosInboxMessage") {
    return IOS_INBOX_MESSAGE;
  } else if (type === "iosRichMessage") {
    return IOS_RICH_MESSAGE;
  }
  throw new Error("Invalid event name: " + type);
}

export type EventName = $Enum<{
  notificationResponse: string,
  deepLink: string,
  iosSDKInit: string,
  iosInboxMessages: string,
  iosInboxMessage: string,
  iosRichMessage: string,
}>;

export class Mapp {
  /**
   * Sets firebase token
   *
   * @param token
   */
  static setToken(token: string): Promise<Boolean> {
    return RNMappPluginModule.setToken(token);
  }

  /**
   * Get device's firebase token
   * @returns token
   */
  static getToken(): Promise<string> {
    return RNMappPluginModule.getToken();
  }

  /**
   * Show notification
   *
   * @param {remoteMessage} push message received
   */
  static setRemoteMessage(remoteMessage: Any) {
    return RNMappPluginModule.setRemoteMessage(JSON.stringify(remoteMessage));
  }

  /**
   * Check if firebase push message is from MAPP or not
   * @param {*} remoteMessage
   * @returns Promise with true if push message is from MAPP, otherwise false
   */
  static isPushFromMapp(remoteMessage: RemoteMessage): Promise<Boolean> {
    return RNMappPluginModule.isPushFromMapp(JSON.stringify(remoteMessage));
  }

  /**
   * Sets user alias
   *
   * @param alias
   */
  static setAlias(alias: string): Promise<Boolean> {
    return RNMappPluginModule.setAlias(alias);
  }

    /**
   * Sets user alias
   *
   * @param alias
   * @param resendAttributes
   */
  static setAlias(alias: string, resendAttributes: bool): Promise<Boolean> {
    return RNMappPluginModule.setAlias(alias, resendAttributes);
  }

  /**
   * getAlias
   *
   */

  static getAlias(): Promise<String> {
    return RNMappPluginModule.getAlias();
  }

  /**
   * Engage
   *
   * @return {Promise.<string>} A promise with the result.
   */

  static engage2() {
    if (Platform.OS == "android") {
      return RNMappPluginModule.engage2();
    }
  }

  /**
   * Engage
   *
   */

  static engage(
    sdkKey: string,
    googleProjectId: string,
    server: string,
    appID: string,
    tenantID: string
  ) {
    if (Platform.OS == "ios") {
      RNMappPluginModule.autoengage(server);
      return RNMappPluginModule.engageInapp(server);
    }
    return RNMappPluginModule.engage(
      sdkKey,
      googleProjectId,
      server,
      appID,
      tenantID
    );
  }

  /**
   * On Init Completed Listener
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static onInitCompletedListener(): Promise<boolean> {
    if (Platform.OS == "android") {
      return RNMappPluginModule.onInitCompletedListener();
    }
    return null;
  }

  /**
   *
   *  Checks is sdk ready.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isReady(): Promise<boolean> {
    return RNMappPluginModule.isReady();
  }

  /**
   *
   * Checks if user notifications are enabled or not.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isPushEnabled(): Promise<boolean> {
    return RNMappPluginModule.isPushEnabled();
  }

  /**
   * Enables user notifications.
   */
  static setPushEnabled(optIn: boolean) {
    return RNMappPluginModule.setPushEnabled(optIn);
  }

  /**
   * Set to true to postpone a request for notifications.
   * Setting the property should be performed prior to engaging the SDK.
   * iOS only
   */
  static setPostponeNotificationRequest(postpone: boolean) {
    return RNMappPluginModule.setPostponeNotificationRequest(postpone);
  }

  /**
   * Display allow notification prompt.
   * iOS only
   */
  static showNotificationAlertView() {
    if (Platform.OS === "ios") {
      return RNMappPluginModule.showNotificationAlertView();
    }
  }

  /**
   * Display notifications even app is in foreground .
   * iOS only
   */
  static setShowNotificationsAtForeground(value: boolean) {
    if (Platform.OS === "ios") {
      return RNMappPluginModule.setShowNotificationsAtForeground(value);
    }
  }

   /**
   * Set Custom Attributes
   *
   */
  static setAttributes(attributes: object) {
    return RNMappPluginModule.setAttributes(attributes);
  }

   /**
   * Get Custom Attributes
   *
   */
  static getAttributes(attributes: array): Promise<Object>  {
    return RNMappPluginModule.getAttributes(attributes);
  }

  /**
   * Set Custom Attribute
   *
   */
  static setAttributeString(key: string, value: string) {
    return RNMappPluginModule.setAttribute(key, value);
  }

  /**
   * Set Custom Attribute
   *
   */
  static setAttributeInt(key: string, value: number) {
    return RNMappPluginModule.setAttributeInt(key, value);
  }

  /**
   * Remove Custom Attribute
   * TODO: it is andoid only function
   */
  static removeAttribute(key: string) {
    if (Platform.OS == "android") {
      return RNMappPluginModule.removeAttribute(key);
    }
  }

  /**
   * Removes a  tag.
   *
   * @param {string} tag A channel tag.
   */
  static removeTag(tag: string) {
    RNMappPluginModule.removeTag(tag);
  }

  /**
   * Adds a  tag.
   *
   * @param {string} tag A channel tag.
   */
  static addTag(tag: string) {
    RNMappPluginModule.addTag(tag);
  }

  // static removeTags(tag: string) {
  //     RNMappPluginModule.removeTag(tag);
  // }

  /**
   * Gets the channel tags.
   *
   * @return {Promise.<Array>} A promise with the result.
   */
  static getTags(): Promise<Array<string>> {
    return RNMappPluginModule.getTags();
  }

  static getDeviceInfo(): Promise<Object> {
    return RNMappPluginModule.getDeviceInfo();
  }

  static getAttributeStringValue(value: string): Promise<string> {
    return RNMappPluginModule.getAttributeStringValue(value);
  }

  static lockScreenOrientation(value: boolean) {
    if (Platform.OS == "android") {
      return RNMappPluginModule.lockScreenOrientation(value);
    }
  }

  static removeBadgeNumber() {
    return RNMappPluginModule.removeBadgeNumber();
  }

  static requestGeofenceLocationPermission(): Promise<boolean> {
    return RNMappPluginModule.requestGeofenceLocationPermission();
  }

  static startGeofencing(): Promise<string> {
    return RNMappPluginModule.startGeofencing();
  }

  static stopGeofencing(): Promise<string> {
    return RNMappPluginModule.stopGeofencing();
  }

  static startGeoFencing() {
    return RNMappPluginModule.startGeoFencing();
  }

  static stopGeoFencing() {
    return RNMappPluginModule.stopGeoFencing();
  }

  static fetchLatestInboxMessage(): Promise<any>{
    if (Platform.OS == "ios") {
      print("fatch latest message for iOS part")
      RNMappPluginModule.fetchLatestInboxMessage();
      this.addInboxMessagesListener(messages => {
        print("message arrived ", messages, " length", messages[messages.length - 1])
        messages.sort((message1, message2) => message1["template_id"] > message2["template_id"]);
        alert(JSON.stringify(messages[0]));
        return JSON.stringify(messages[0]);
      });
    } else {
      return RNMappPluginModule.fetchLatestInboxMessage();
    }
  }

  static fetchInboxMessage(): Promise<any> {
    return RNMappPluginModule.fetchInboxMessage();
  }

  static triggerInApp(value: string) {
    return RNMappPluginModule.triggerInApp(value);
  }

  static inAppMarkAsRead(templateId: number, eventId: string) {
    return RNMappPluginModule.inAppMarkAsRead(templateId, eventId);
  }

  static inAppMarkAsUnRead(templateId: number, eventId: string) {
    return RNMappPluginModule.inAppMarkAsUnRead(templateId, eventId);
  }

  static inAppMarkAsDeleted(templateId: number, eventId: string) {
    return RNMappPluginModule.inAppMarkAsDeleted(templateId, eventId);
  }

  static triggerStatistic(
    templateId: number,
    originalEventId: string,
    trackingKey: string,
    displayMillis: number,
    reason: string,
    link
  ): string {
    if (Platform.OS == "android") {
      return RNMappPluginModule.triggerStatistic(
        templateId,
        originalEventId,
        trackingKey,
        displayMillis,
        reason,
        link
      );
    }
    return null;
  }

  static isDeviceRegistered(): Promise<boolean> {
    return RNMappPluginModule.isDeviceRegistered();
  }

  static incrementNumericKey(key: String, value: number) {
    if (Platform.OS == "ios") {
      return RNMappPluginModule.incrementNumericKey(key, value);
    }
    return null;
  }

  static logOut(pushEnabled: Boolean) {
    return RNMappPluginModule.logOut(pushEnabled);
  }

  static getDeviceDmcInfo(): Promise<any> {
    if (Platform.OS == "android") {
      return RNMappPluginModule.getDeviceDmcInfo();
    } else {
      return Promise.resolve(null);
    }
  }

  /**
   * Check if permission for posting notifications on Android 13 and up is granted or not.
   * If permission is not granted, then system dialog will be invoked and shown.
   * @returns true if permission is granted, false if it is not
   */
  static requestPostNotificationPermission(): Promise<any> {
    if (Platform.OS == "android") {
      return RNMappPluginModule.requestPostNotificationPermission();
    } else {
      return Promise.resolve(true);
    }
  }

  /**
   * Adds a custom event.
   *
   * @param {CustomEvent} event The custom event.
   * @return {Promise.<null, Error>}  A promise that returns null if resolved, or an Error if the
   * custom event is rejected.
   */
  static addCustomEvent(event: CustomEvent): Promise {
    var actionArg = {
      event_name: event._name,
      event_value: event._value,
      transaction_id: event._transactionId,
      properties: event._properties,
    };

    return new Promise((resolve, reject) => {
      RNMappPluginModule.runAction("add_custom_event_action", actionArg).then(
        () => {
          resolve();
        },
        (error) => {
          reject(error);
        }
      );
    });
  }

  static runAction(name: string, value: ?any): Promise<any> {
    if (Platform.OS == "android") {
      return RNMappPluginModule.runAction(name, value);
    }
  }

  static addPushListener(listener: Function): EmitterSubscription {
    return this.addListener("notificationResponse", listener);
  }

  static addDeepLinkingListener(listener: Function): EmitterSubscription {
    return this.addListener("deepLink", listener);
  }

  static addInitListener(listener: Function): EmitterSubscription {
    if (Platform.OS == "ios") {
      return this.addListener("iosSDKInit", listener);
    }
    return null;
  }

  static addInboxMessagesListener(listener: Function): EmitterSubscription {
    if (Platform.OS == "ios") {
      return this.addListener("iosInboxMessages", listener);
    }
    return null;
  }

  static addInboxMessageListener(listener: Function): EmitterSubscription {
    if (Platform.OS == "ios") {
      return this.addListener("iosInboxMessage", listener);
    }
    return null;
  }

  static addRichMessagesListener(listener: Function): EmitterSubscription {
    if (Platform.OS == "ios") {
      return this.addListener("iosRichMessage", listener);
    }
    return null;
  }

  static removePushListener(listener: Function): EmitterSubscription {
    return this.removeListener("notificationResponse", listener);
  }

  static removeDeepLinkingListener(listener: Function): EmitterSubscription {
    return this.removeListener("deepLink", listener);
  }

  static addListener(
    eventName: EventName,
    listener: Function
  ): EmitterSubscription {
    let name = convertEventEnum(eventName);
    return EventEmitter.addListener(name, listener);
  }

  static removeListener(eventName: EventName, listener: Function) {
    let name = convertEventEnum(eventName);
    EventEmitter.removeListener(name, listener);
  }

  /**
   * Clears all notifications for the application.
   * Supported on Android and iOS 10+. For older iOS devices, you can set
   * the badge number to 0 to clear notifications.
   */
  static clearNotifications() {
    RNMappPluginModule.clearNotifications();
  }

  static clearNotification(identifier: string) {
    RNMappPluginModule.clearNotification(identifier);
  }
}

module.exports = Mapp;
