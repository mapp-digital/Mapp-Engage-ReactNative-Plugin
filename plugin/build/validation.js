"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.validateAndNormalizeProps = validateAndNormalizeProps;
const prefix = '[react-native-mapp-plugin]';
function fail(property, expectation) {
    throw new Error(`${prefix} ${property} ${expectation}. Configure it in the Mapp plugin options.`);
}
function nonEmptyString(value, property) {
    if (typeof value !== 'string' || value.trim().length === 0) {
        fail(property, 'must be a non-empty string');
    }
}
function optionalBoolean(value, property) {
    if (value !== undefined && typeof value !== 'boolean') {
        fail(property, 'must be a boolean');
    }
}
function validateAndNormalizeProps(config, props) {
    if (!props || typeof props !== 'object') {
        fail('plugin options', 'must be an object');
    }
    if (!config.android?.package) {
        fail('expo.android.package', 'must be set to the Firebase Android application package name');
    }
    if (!config.ios?.bundleIdentifier) {
        fail('expo.ios.bundleIdentifier', 'must be set to the APNs-enabled application bundle identifier');
    }
    const android = props.android ?? {};
    const pushHandling = android.pushHandling ?? 'mapp';
    if (pushHandling !== 'mapp' && pushHandling !== 'custom') {
        fail('android.pushHandling', 'must be either "mapp" or "custom"');
    }
    optionalBoolean(android.enableGeofencing, 'android.enableGeofencing');
    if (pushHandling === 'mapp' && !config.android.googleServicesFile) {
        fail('expo.android.googleServicesFile', 'must point to the Firebase Android application google-services.json when android.pushHandling is "mapp"');
    }
    const ios = props.ios;
    if (!ios || typeof ios !== 'object') {
        fail('ios', 'must be an object');
    }
    nonEmptyString(ios.appId, 'ios.appId');
    nonEmptyString(ios.sdkKey, 'ios.sdkKey');
    nonEmptyString(ios.inAppServerUrl, 'ios.inAppServerUrl');
    if (!Number.isInteger(ios.dmcSystemId)) {
        fail('ios.dmcSystemId', 'must be an integer');
    }
    if (typeof ios.isEu !== 'boolean') {
        fail('ios.isEu', 'must be a boolean');
    }
    optionalBoolean(ios.openLandingPageInsideApp, 'ios.openLandingPageInsideApp');
    optionalBoolean(ios.enableGeofencing, 'ios.enableGeofencing');
    if (ios.customFields !== undefined && (!Array.isArray(ios.customFields) ||
        ios.customFields.some(field => typeof field !== 'string' || field.trim().length === 0))) {
        fail('ios.customFields', 'must be an array of non-empty strings');
    }
    if (ios.mediaTimeout !== undefined && (typeof ios.mediaTimeout !== 'number' || !Number.isFinite(ios.mediaTimeout) || ios.mediaTimeout <= 0)) {
        fail('ios.mediaTimeout', 'must be a positive number');
    }
    if (ios.enableGeofencing) {
        nonEmptyString(ios.locationWhenInUsePermission, 'ios.locationWhenInUsePermission');
        nonEmptyString(ios.locationAlwaysPermission, 'ios.locationAlwaysPermission');
    }
    return {
        android: {
            enableGeofencing: android.enableGeofencing ?? false,
            pushHandling,
        },
        ios: {
            ...ios,
            openLandingPageInsideApp: ios.openLandingPageInsideApp ?? false,
            customFields: ios.customFields ?? [],
            mediaTimeout: ios.mediaTimeout ?? 5,
            enableGeofencing: ios.enableGeofencing ?? false,
        },
    };
}
