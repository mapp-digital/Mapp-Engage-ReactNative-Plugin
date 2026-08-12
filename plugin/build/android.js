"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.withMappEngageAndroid = void 0;
exports.updatePushHandling = updatePushHandling;
const config_plugins_1 = require("@expo/config-plugins");
const messageService = 'com.reactlibrary.MessageService';
const fineLocation = 'android.permission.ACCESS_FINE_LOCATION';
const backgroundLocation = 'android.permission.ACCESS_BACKGROUND_LOCATION';
function removeAll(items, predicate) {
    return (items ?? []).filter(item => !predicate(item));
}
function updatePushHandling(androidManifest, pushHandling) {
    const manifest = androidManifest.manifest;
    const application = config_plugins_1.AndroidConfig.Manifest.getMainApplicationOrThrow(androidManifest);
    application.service = removeAll(application.service, service => {
        return service.$?.['android:name'] === messageService && service.$?.['tools:node'] === 'remove';
    });
    if (pushHandling === 'custom') {
        manifest.$ = manifest.$ ?? {};
        manifest.$['xmlns:tools'] = 'http://schemas.android.com/tools';
        application.service.push({
            $: {
                'android:name': messageService,
                'tools:node': 'remove',
            },
        });
    }
    return androidManifest;
}
const withMappEngageAndroid = (config, props) => {
    if (props.enableGeofencing) {
        config = config_plugins_1.AndroidConfig.Permissions.withPermissions(config, [fineLocation, backgroundLocation]);
    }
    return (0, config_plugins_1.withAndroidManifest)(config, configWithManifest => {
        configWithManifest.modResults = updatePushHandling(configWithManifest.modResults, props.pushHandling);
        return configWithManifest;
    });
};
exports.withMappEngageAndroid = withMappEngageAndroid;
