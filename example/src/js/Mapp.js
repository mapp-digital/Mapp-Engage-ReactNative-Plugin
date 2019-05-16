// @flow
'use strict';

import {
  NativeModules,
  Platform
} from 'react-native';

import CustomEvent from './CustomEvent.js'

import MappEventEmitter from './MappEventEmitter.js'

const RNMappPluginModule = NativeModules.RNMappPluginModule;
const EventEmitter = new MappEventEmitter();


const PUSH_RECEIVED_EVENT = "PUSH_RECEIVED_EVENT";


/**
 * @private
 */
function convertEventEnum(type: UAEventName): ?string {
  if (type === 'notificationResponse') {
    return NOTIFICATION_RESPONSE_EVENT;
  
  }
  throw new Error("Invalid event name: " + type);
}

export type UAEventName = $Enum<{
  notificationResponse: string,
  pushReceived: string,
  register: string,
  deepLink: string,
  notificationOptInStatus: string,
  inboxUpdated: string,
  showInbox: string
}>;

class Mapp {

  /**
   * Sets user notifications enabled. The first time user notifications are enabled
   * on iOS, it will prompt the user for notification permissions.
   *
   * @param {boolean} enabled true to enable notifications, false to disable.
   */
  static setUserNotificationsEnabled(enabled: boolean) {
    RNMappPluginModule.setUserNotificationsEnabled(enabled);
  }

  /**
   * Checks if user notifications are enabled or not.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isUserNotificationsEnabled(): Promise<boolean> {
    return RNMappPluginModule.isUserNotificationsEnabled();
  }

  /**
   * Enables user notifications.
   *
   * @return {Promise.<boolean>} A promise that returns true if enablement was authorized
   * or false if enablement was rejected
   */
  static enableUserPushNotifications(): Promise<boolean> {
    return RNMappPluginModule.enableUserPushNotifications();
  }

  /**
   * Checks if app notifications are enabled or not. Its possible to have `userNotificationsEnabled`
   * but app notifications being disabled if the user opted out of notifications.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isUserNotificationsOptedIn(): Promise<boolean> {
    return RNMappPluginModule.isUserNotificationsOptedIn();
  }

  /**
   * Sets the named user.
   *
   * @param {?string} namedUser The named user string or null to clear the named user.
   */
  static setNamedUser(namedUser: ?string) {
    RNMappPluginModule.setNamedUser(namedUser);
  }

  /**
   * Gets the named user.
   *
   * @return {Promise.<string>} A promise with the result.
   */
  static getNamedUser(): Promise<?string> {
    return RNMappPluginModule.getNamedUser();
  }

  /**
   * Adds a channel tag.
   *
   * @param {string} tag A channel tag.
   */
  static addTag(tag: string) {
    RNMappPluginModule.addTag(tag);
  }

  /**
   * Removes a channel tag.
   *
   * @param {string} tag A channel tag.
   */
  static removeTag(tag: string) {
    RNMappPluginModule.removeTag(tag);
  }

  /**
   * Gets the channel tags.
   *
   * @return {Promise.<Array>} A promise with the result.
   */
  static getTags(): Promise<Array<string>> {
    return RNMappPluginModule.getTags();
  }



  /**
   * Enables or disables analytics.
   *
   * Disabling analytics will delete any locally stored events
   * and prevent any events from uploading. Features that depend on analytics being
   * enabled may not work properly if it's disabled (reports, region triggers,
   * location segmentation, push to local time).
   *
   * @param {boolean} enabled true to enable notifications, false to disable.
   */
  static setAnalyticsEnabled(enabled: boolean) {
    RNMappPluginModule.setAnalyticsEnabled(enabled);
  }

  /**
   * Checks if analytics is enabled or not.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isAnalyticsEnabled(): Promise<boolean> {
    return RNMappPluginModule.isAnalyticsEnabled();
  }

  /**
   * Gets the channel ID.
   *
   * @return {Promise.<string>} A promise with the result.
   */
  static getChannelId(): Promise<?string> {
    return RNMappPluginModule.getChannelId();
  }

  /**
   * Gets the registration token.
   *
   * @return {Promise.<string>} A promise with the result. The registration token
   * might be undefined if registration is currently in progress, if the app is not setup properly
   * for remote notifications, if running on an iOS simulator, or if running on an Android
   * device that has an outdated or missing version of Google Play Services.
   */
  static getRegistrationToken(): Promise<?string> {
    return RNMappPluginModule.getRegistrationToken();
  }

  /**
   * Associates an identifier for the Connect data stream.
   *
   * @param {string} key The identifier's key.
   * @param {string} value The identifier's value.
   */
  static associateIdentifier(key: string, id: ?string) {
    RNMappPluginModule.associateIdentifier(key, id);
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
      properties: event._properties
    }

    return new Promise((resolve, reject) => {
            RNMappPluginModule.runAction("add_custom_event_action", actionArg)
                              .then(() => {
                                  resolve();
                               }, (error) => {
                                 reject(error);
                               });
        });
  }

  /**
   * Enables or disables Urban Airship location services.
   *
   * @param {boolean} enabled true to enable location, false to disable.
   */
  static setLocationEnabled(enabled: boolean) {
    RNMappPluginModule.setLocationEnabled(enabled);
  }

  /**
   * Allows or disallows location services to continue in the background.
   *
   * @param {boolean} allowed true to allow background location, false to disallow.
   */
  static setBackgroundLocationAllowed(allowed: boolean) {
    RNMappPluginModule.setBackgroundLocationAllowed(allowed);
  }

  /**
   * Checks if location is enabled or not.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isLocationEnabled(): Promise<boolean> {
    return RNMappPluginModule.isLocationEnabled();
  }

  /**
   * Checks if background location is allowed or not.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isBackgroundLocationAllowed(): Promise<boolean> {
    return RNMappPluginModule.isBackgroundLocationAllowed();
  }

  /**
   * Runs an Urban Airship action.
   *
   * @param {string} name The name of the action.
   * @param {*} value The action's value.
   * @return {Promise.<*, Error>}  A promise that returns the action result if the action
   * successfully runs, or the Error if the action was unable to be run.
   */
  static runAction(name: string, value: ?any) : Promise<any> {
    return RNMappPluginModule.runAction(name, value);
  }

  /**
   * Sets the foregorund presentation options for iOS.
   *
   * This method is only supported on iOS >= 10. Android and older iOS devices
   * will no-op.
   *
   * @param {Object} options The a map of options.
   * @param {boolean} [options.alert=false] True to display an alert when a notification is received in the foreground, otherwise false.
   * @param {boolean} [options.sound=false] True to play a sound when a notification is received in the foreground, otherwise false.
   * @param {boolean} [options.badge=false] True to update the badge when a notification is received in the foreground, otherwise false.
   */
  static setForegroundPresentationOptions(options: { alert?: boolean, badge?: boolean, sound?: boolean}) {
    if (Platform.OS == 'ios') {
      return RNMappPluginModule.setForegroundPresentationOptions(options);
    }
  }

  /**
   * Adds a listener for an Urban Airship event.
   *
   * @param {string} eventName The event name. Either notificationResponse, pushReceived,
   * register, deepLink, or notificationOptInStatus.
   * @param {Function} listener The event listner.
   * @return {EmitterSubscription} An emitter subscription.
   */
  static addListener(eventName: UAEventName, listener: Function): EmitterSubscription {
    var name = convertEventEnum(eventName);
    return EventEmitter.addListener(name, listener);
  }

  /**
   * Removes a listener for an Urban Airship event.
   *
   * @param {string} eventName The event name. Either notificationResponse, pushReceived,
   * register, deepLink, or notificationOptInStatus.
   * @param {Function} listener The event listner.
   */
  static removeListener(eventName: AirshipEventName, listener: Function) {
    var name = convertEventEnum(eventName);
    EventEmitter.removeListener(name, listener);
  }

  /**
     * Sets the quiet time.
     *
     * @param {Object} quiteTime The quiet time object.
     * @param {number} quiteTime.startHour Start hour.
     * @param {number} quiteTime.startMinute Start minute.
     * @param {number} quiteTime.endHour End hour.
     * @param {number} quiteTime.endMinute End minute.
     */
  static setQuietTime(quietTime: {startHour?: number, startMinute?: number, endHour?: number, endMinute?: number }) {
    return RNMappPluginModule.setQuietTime(quietTime);
  }

  /**
   * Returns the quiet time as an object with the following:
   * "startHour": Number,
   * "startMinute": Number,
   * "endHour": Number,
   * "endMinute": Number
   *
   * @return {Promise.Object} A promise with the result.
   */
  static getQuietTime(): Promise<Object> {
    return RNMappPluginModule.getQuietTime();
  }

  /**
   * Enables or disables quiet time.
   *
   * @param {boolean} enabled true to enable quiet time, false to disable.
   */
  static setQuietTimeEnabled(enabled: boolean) {
    RNMappPluginModule.setQuietTimeEnabled(enabled);
  }

  /**
   * Checks if quietTime is enabled or not.
   *
   * @return {Promise.<boolean>} A promise with the result.
   */
  static isQuietTimeEnabled(): Promise<boolean> {
    return RNMappPluginModule.isQuietTimeEnabled();
  }

  /**
   * Sets the badge number for iOS. Badging is not supported for Android.
   *
   * @param {number} badgeNumber specified badge to set.
   */
  static setBadgeNumber(badgeNumber: number) {
    if (Platform.OS == 'ios') {
      RNMappPluginModule.setBadgeNumber(badgeNumber);
    } else {
      console.log("This feature is not supported on this platform.")
    }
  }

  /**
   * Gets the current badge number for iOS. Badging is not supported for Android
   * and this method will always return 0.
   *
   * @return {Promise.<number>} A promise with the result.
   */
  static getBadgeNumber(): Promise<number> {
    if (Platform.OS != 'ios') {
      console.log("This feature is not supported on this platform.")
    }
    return RNMappPluginModule.getBadgeNumber();
  }

  /**
   * Displays the default message center.
   */
  static displayMessageCenter() {
    RNMappPluginModule.displayMessageCenter();
  }

  /**
   * Dismisses the default message center.
   */
  static dismissMessageCenter() {
    RNMappPluginModule.dismissMessageCenter();
  }

  /**
   * Displays an inbox message.
   *
   * @param {string} messageId The id of the message to be displayed.
   * @param {boolean} [overlay=false] Display the message in an overlay.
   * @return {Promise.<boolean>} A promise with the result.
   */
  static displayMessage(messageId: string, overlay: ?boolean): Promise<boolean> {
    return RNMappPluginModule.displayMessage(messageId, overlay);
  }

  /**
   * Dismisses the currently displayed inbox message.
   *
   * @param {boolean} [overlay=false] Dismisses the message in an overlay.
   */
  static dismissMessage(overlay: ?boolean) {
    RNMappPluginModule.dismissMessage(overlay);
  }

  /**
   * Retrieves the current inbox messages. Each message will have the following properties:
   * "id": string - The messages ID. Needed to display, mark as read, or delete the message.
   * "title": string - The message title.
   * "sentDate": number - The message sent date in milliseconds.
   * "listIconUrl": string, optional - The icon url for the message.
   * "isRead": boolean - The unread/read status of the message.
   * "isDeleted": boolean - The deleted status of the message.
   * "extras": object - String to String map of any message extras.
   *
   * @return {Promise.<Array>} A promise with the result.
   */
  static getInboxMessages(): Promise<Array> {
    return RNMappPluginModule.getInboxMessages();
  }

  /**
   * Deletes an inbox message.
   *
   * @param {string} messageId The id of the message to be deleted.
   * @return {Promise.<boolean>} A promise with the result.
   */
  static deleteInboxMessage(messageId: string): Promise<boolean> {
    return RNMappPluginModule.deleteInboxMessage(messageId);
  }

  /**
   * Marks an inbox message as read.
   *
   * @param {string} messageId The id of the message to be marked as read.
   * @return {Promise.<boolean>} A promise with the result.
   */
  static markInboxMessageRead(messageId: string): Promise<boolean> {
    return RNMappPluginModule.markInboxMessageRead(messageId);
  }

  /**
   * Forces the inbox to refresh. This is normally not needed as the inbox will
   * automatically refresh on foreground or when a push arrives that's associated
   * with a message.
   *
   * @return{Promise.<boolean>} A promise with the result.
   */
  static refreshInbox(): Promise<boolean> {
    return RNMappPluginModule.refreshInbox();
  }

  /**
   * Sets the default behavior when the message center is launched from a push
   * notification. If set to false the message center must be manually launched.
   *
   * @param {boolean} [enabled=true] true to automatically launch the default message center, false to disable.
   */
  static setAutoLaunchDefaultMessageCenter(enabled: boolean) {
    RNMappPluginModule.setAutoLaunchDefaultMessageCenter(enabled);
  }

  /**
   * Gets all the active notifications for the application.
   * Supported on Android Marshmallow (23)+ and iOS 10+.
   *
   * @return {Promise.<Array>} A promise with the result.
   */
  static getActiveNotifications(): Promise<Array> {
    return RNMappPluginModule.getActiveNotifications();
  }

  /**
   * Clears all notifications for the application.
   * Supported on Android and iOS 10+. For older iOS devices, you can set
   * the badge number to 0 to clear notifications.
   */
  static clearNotifications() {
    RNMappPluginModule.clearNotifications();
  }

  /**
   * Clears a specific notification.
   * Supported on Android and iOS 10+.
   *
   * @param {string} identifier The notification identifier. The identifier will
   * available in the pushReceived event and in the active notification response
   * under the "notificationId" field.
   */
  static clearNotification(identifier: string) {
    RNMappPluginModule.clearNotification(identifier)
  }
}

module.exports = Mapp;
