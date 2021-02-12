// @flow
'use strict';

import {
    NativeModules,
    Platform,
} from 'react-native';

import CustomEvent from './CustomEvent.js'

//import MappEventEmitter from './MappEventEmitter.js'

const {RNMappPluginModule} = NativeModules;
//const EventEmitter = new MappEventEmitter();

const PUSH_RECEIVED_EVENT = "PushNotificationEvent";
const MappIntentEvent = "MappIntentEve";

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
    throw new Error("Invalid event name: " + type);
}

export type EventName = $Enum<{
    notificationResponse: string,
    deepLink: string
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
        return RNMappPluginModule.engage2();
    }


    /**
     * Engage
     *
     */

    static engage(sdkKey: string, googleProjectId: string, cepURL: string, appID: string, tenantID: string) {
        return RNMappPluginModule.engage(sdkKey, googleProjectId, cepURL, appID, tenantID);
    }

    /**
     * On Init Completed Listener
     *
     * @return {Promise.<boolean>} A promise with the result.
     */
    static onInitCompletedListener(): Promise<boolean> {
        return RNMappPluginModule.onInitCompletedListener();
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
     * Set Custom Attribute
     *
     */
    static setAttributeBoolean(key: string, value: boolean) {
        return RNMappPluginModule.setAttributeBoolean(key, value);
    }

    /**
     * Remove Custom Attribute
     * TODO: it is andoid only function
     */
    static removeAttribute(key: string) {
        return RNMappPluginModule.removeAttribute(key);
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

    /* TODO: This methosd is only available for Android */
    static lockScreenOrientation(value: boolean) {
        return RNMappPluginModule.lockScreenOrientation(value);
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
        return RNMappPluginModule.triggerStatistic(templateId, originalEventId, trackingKey, displayMillis, reason, link);
    }
    // TODO: Android only
    static isDeviceRegistered(): Promise<boolean> {
        return RNMappPluginModule.isDeviceRegistered(value);
    }
    // TODO: iOS only
    static removeDeviceAlias() {
        return RNMappPluginModule.removeDeviceAlias();
    }
    // TODO: iOS only
    static incrementNumericKey(key:String, value:number) {
        return RNMappPluginModule.incrementNumericKey(key,value);
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
        return RNMappPluginModule.runAction(name, value);
    }


    static addListener(eventName: EventName, listener: Function): EmitterSubscription {
        var name = convertEventEnum(eventName);
        return EventEmitter.addListener(name, listener);
    }


    static removeListener(eventName: EventName, listener: Function) {
        var name = convertEventEnum(eventName);
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
