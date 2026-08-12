"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.withMappEngageIos = void 0;
exports.buildAppoxeeConfig = buildAppoxeeConfig;
exports.writeAppoxeeConfig = writeAppoxeeConfig;
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
const config_plugins_1 = require("@expo/config-plugins");
const plist_1 = __importDefault(require("@expo/plist"));
const plistName = 'AppoxeeConfig.plist';
function buildAppoxeeConfig(props) {
    return {
        inapp: {
            custom_fields: props.customFields,
            media_timeout: props.mediaTimeout,
        },
        sdk: {
            app_id: props.appId,
            dmc_system_id: props.dmcSystemId,
            sdk_key: props.sdkKey,
            is_eu: props.isEu,
            jamie_url: props.inAppServerUrl,
            open_landing_page_inside_app: props.openLandingPageInsideApp,
            apx_open_url_internal: 'YES',
        },
    };
}
async function writeAppoxeeConfig(platformProjectRoot, props) {
    const outputPath = path_1.default.join(platformProjectRoot, plistName);
    await fs_1.default.promises.writeFile(outputPath, plist_1.default.build(buildAppoxeeConfig(props)));
    return outputPath;
}
function appendUnique(values, value) {
    const result = Array.isArray(values) ? values.filter(item => typeof item === 'string') : [];
    return result.includes(value) ? result : [...result, value];
}
const withMappEngageIos = (config, props) => {
    config = (0, config_plugins_1.withInfoPlist)(config, configWithPlist => {
        configWithPlist.modResults.UIBackgroundModes = appendUnique(configWithPlist.modResults.UIBackgroundModes, 'remote-notification');
        if (props.enableGeofencing) {
            configWithPlist.modResults.UIBackgroundModes = appendUnique(configWithPlist.modResults.UIBackgroundModes, 'location');
            configWithPlist.modResults.NSLocationWhenInUseUsageDescription = props.locationWhenInUsePermission;
            configWithPlist.modResults.NSLocationAlwaysAndWhenInUseUsageDescription = props.locationAlwaysPermission;
        }
        return configWithPlist;
    });
    config = (0, config_plugins_1.withEntitlementsPlist)(config, configWithEntitlements => {
        configWithEntitlements.modResults['aps-environment'] =
            configWithEntitlements.modResults['aps-environment'] ?? 'development';
        return configWithEntitlements;
    });
    config = (0, config_plugins_1.withDangerousMod)(config, ['ios', async (configWithFiles) => {
            await writeAppoxeeConfig(configWithFiles.modRequest.platformProjectRoot, props);
            return configWithFiles;
        }]);
    return (0, config_plugins_1.withXcodeProject)(config, configWithProject => {
        const project = configWithProject.modResults;
        if (!project.hasFile(plistName)) {
            const target = config_plugins_1.IOSConfig.XcodeUtils.getApplicationNativeTarget({
                project,
                projectName: configWithProject.modRequest.projectName,
            });
            config_plugins_1.IOSConfig.XcodeUtils.addResourceFileToGroup({
                filepath: plistName,
                groupName: configWithProject.modRequest.projectName,
                isBuildFile: true,
                project,
                targetUuid: target.uuid,
            });
        }
        return configWithProject;
    });
};
exports.withMappEngageIos = withMappEngageIos;
