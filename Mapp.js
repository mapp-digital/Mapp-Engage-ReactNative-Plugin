// @flow
'use strict';

import {
    NativeModules,
    Platform,
} from 'react-native';

import CustomEvent from './CustomEvent.js'

import MappEventEmitter from './MappEventEmitter.js'

const {RNMappPluginModule} = NativeModules;
const EventEmitter = new MappEventEmitter();

const IOS_INIT = "com.mapp.init";
const IOS_INBOX_MESSAGE = "com.mapp.inbox_message_received";
const IOS_INBOX_MESSAGES = "com.mapp.inbox_messages_received";
const PUSH_RECEIVED_EVENT = "con.mapp.rich_message_received";
const MappIntentEvent = "com.mapp.deep_link_received";
const IOS_RICH_MESSAGE = "com.mapp.rich_message";

/**
 * @private
 */
function convertEventEnum(type: EventName): ?string {
    if (type === 'notificationResponse') {
        return PUSH_RECEIVED_EVENT;
    }
    else if (type === 'deepLink') {
        return MappIntentEvent;
    }
    else if (type === 'iosSDKInit') {
        return IOS_INIT;
    }
    else if (type === 'iosInboxMessages') {
        return IOS_INBOX_MESSAGES;
    }
    else if (type === 'iosInboxMessage') {
        return IOS_INBOX_MESSAGE;
    }
    else if (type === 'iosRichMessage') {
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
    iosRichMessage: string
}>;

export class Mapp {

    /**
     * Sets user alias
     *
     * @param alias
     */
    static setAlias(alias: string) {
        RNMappPluginModule.setAlias(alias);
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

    static engage(sdkKey: string, googleProjectId: string, server: string, appID: string, tenantID: string) {
        if (Platform.OS == 'ios') {
            RNMappPluginModule.engageInapp(server);
            return RNMappPluginModule.autoengage(server);
        }
        return RNMappPluginModule.engage(sdkKey, googleProjectId, server, appID, tenantID);
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


    static startGeoFencing() {
        return RNMappPluginModule.startGeoFencing();
    }

    static stopGeoFencing() {
        return RNMappPluginModule.stopGeoFencing();
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

    static triggerStatistic(templateId: number, originalEventId: string, trackingKey: string, displayMillis: number, reason: string, link): string {
        if (Platform.OS == "android") {
            return RNMappPluginModule.triggerStatistic(templateId, originalEventId, trackingKey, displayMillis, reason, link);
        }
        return null;
    }

    static isDeviceRegistered(): Promise<boolean> {
        return RNMappPluginModule.isDeviceRegistered(value);
    }

    static incrementNumericKey(key:String, value:number) {
        if (Platform.OS == "ios") {
            return RNMappPluginModule.incrementNumericKey(key,value);
        }
        return null;
    }

    static logOut(pushEnabled:Boolean) {
        return RNMappPluginModule.logOut(pushEnabled);
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


    static addListener(eventName: EventName, listener: Function): EmitterSubscription {
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
        RNMappPluginModule.clearNotification(identifier)
    }
}

module.exports = Mapp;
