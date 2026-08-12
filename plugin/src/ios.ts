import fs from 'fs';
import path from 'path';
import {
  ConfigPlugin,
  IOSConfig,
  withDangerousMod,
  withEntitlementsPlist,
  withInfoPlist,
  withXcodeProject,
} from '@expo/config-plugins';
import plist from '@expo/plist';
import type { NormalizedMappExpoPluginProps } from './types';

const plistName = 'AppoxeeConfig.plist';

export function buildAppoxeeConfig(props: NormalizedMappExpoPluginProps['ios']): Record<string, unknown> {
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

export async function writeAppoxeeConfig(
  platformProjectRoot: string,
  props: NormalizedMappExpoPluginProps['ios']
): Promise<string> {
  const outputPath = path.join(platformProjectRoot, plistName);
  await fs.promises.writeFile(outputPath, plist.build(buildAppoxeeConfig(props)));
  return outputPath;
}

function appendUnique(values: unknown, value: string): string[] {
  const result = Array.isArray(values) ? values.filter(item => typeof item === 'string') : [];
  return result.includes(value) ? result : [...result, value];
}

export const withMappEngageIos: ConfigPlugin<NormalizedMappExpoPluginProps['ios']> = (
  config,
  props
) => {
  config = withInfoPlist(config, configWithPlist => {
    configWithPlist.modResults.UIBackgroundModes = appendUnique(
      configWithPlist.modResults.UIBackgroundModes,
      'remote-notification'
    );
    if (props.enableGeofencing) {
      configWithPlist.modResults.UIBackgroundModes = appendUnique(
        configWithPlist.modResults.UIBackgroundModes,
        'location'
      );
      configWithPlist.modResults.NSLocationWhenInUseUsageDescription = props.locationWhenInUsePermission;
      configWithPlist.modResults.NSLocationAlwaysAndWhenInUseUsageDescription = props.locationAlwaysPermission;
    }
    return configWithPlist;
  });

  config = withEntitlementsPlist(config, configWithEntitlements => {
    configWithEntitlements.modResults['aps-environment'] =
      configWithEntitlements.modResults['aps-environment'] ?? 'development';
    return configWithEntitlements;
  });

  config = withDangerousMod(config, ['ios', async configWithFiles => {
    await writeAppoxeeConfig(configWithFiles.modRequest.platformProjectRoot, props);
    return configWithFiles;
  }]);

  return withXcodeProject(config, configWithProject => {
    const project = configWithProject.modResults;
    if (!project.hasFile(plistName)) {
      const target = IOSConfig.XcodeUtils.getApplicationNativeTarget({
        project,
        projectName: configWithProject.modRequest.projectName!,
      });
      IOSConfig.XcodeUtils.addResourceFileToGroup({
        filepath: plistName,
        groupName: configWithProject.modRequest.projectName!,
        isBuildFile: true,
        project,
        targetUuid: target.uuid,
      });
    }
    return configWithProject;
  });
};
