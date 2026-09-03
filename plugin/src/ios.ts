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
const notificationServiceTargetName = 'MappNotificationService';
const notificationServiceInfoPlistName = 'Info.plist';
const notificationServiceEntitlementsName = 'MappNotificationService.entitlements';
const notificationServiceSourceName = 'NotificationService.swift';
const notificationServiceDeploymentTarget = '15.0';

const notificationServiceSource = `import UserNotifications

class NotificationService: UNNotificationServiceExtension {

    var contentHandler: ((UNNotificationContent) -> Void)?

    var bestAttemptContent: UNMutableNotificationContent?

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {

        self.contentHandler = contentHandler

        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)

        print("categori identifier: ", request.content)

        UNUserNotificationCenter.current().getNotificationCategories { (categories) in

            if let categoryIdentifier = self.bestAttemptContent?.categoryIdentifier, let lc = request.content.userInfo["aps"] {

                self.bestAttemptContent?.categoryIdentifier = categoryIdentifier + "_" + ((lc as! NSDictionary)["lc"] as! String)

                let categoryExistArray = categories.filter { (category) -> Bool in
                    category.identifier == self.bestAttemptContent?.categoryIdentifier
                }

                if categoryExistArray.isEmpty {
                    self.bestAttemptContent?.categoryIdentifier = categoryIdentifier + "_en"
                }
            }

            if let urlString = request.content.userInfo["ios_apx_media"], let fileUrl = URL(string: urlString as? String ?? "") {

                URLSession.shared.downloadTask(with: fileUrl ) { (location, response, error) in

                    if let location = location {
                        let tmpDirectory = NSTemporaryDirectory()
                        let tmpFile = "file://".appending(tmpDirectory).appending(fileUrl.lastPathComponent)
                        let tmpUrl = URL(string: tmpFile)!

                        try! FileManager.default.moveItem(at: location, to: tmpUrl)

                        if let attachment = try? UNNotificationAttachment(identifier: "", url: tmpUrl) {
                            self.bestAttemptContent?.attachments = [attachment]
                        }
                    }

                    print("categori identifier: ", self.bestAttemptContent?.categoryIdentifier ?? "no category identifier")

                    self.contentHandler!(self.bestAttemptContent!)

                }.resume()

            } else {
                self.contentHandler!(self.bestAttemptContent!)
            }
        }
    }

    override func serviceExtensionTimeWillExpire() {
        if let contentHandler = contentHandler, let bestAttemptContent = bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }
}
`;

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

export function buildNotificationServiceInfoPlist(): Record<string, unknown> {
  return {
    NSExtension: {
      NSExtensionPointIdentifier: 'com.apple.usernotifications.service',
      NSExtensionPrincipalClass: '$(PRODUCT_MODULE_NAME).NotificationService',
    },
  };
}

async function writeFileIfChanged(filePath: string, contents: string): Promise<void> {
  const current = await fs.promises.readFile(filePath, 'utf8').catch(() => undefined);
  if (current !== contents) {
    await fs.promises.writeFile(filePath, contents);
  }
}

export async function writeNotificationServiceFiles(
  platformProjectRoot: string,
  bundleIdentifier: string
): Promise<string[]> {
  const extensionRoot = path.join(platformProjectRoot, notificationServiceTargetName);
  const infoPlistPath = path.join(extensionRoot, notificationServiceInfoPlistName);
  const entitlementsPath = path.join(extensionRoot, notificationServiceEntitlementsName);
  const sourcePath = path.join(extensionRoot, notificationServiceSourceName);
  await fs.promises.mkdir(extensionRoot, { recursive: true });
  await writeFileIfChanged(infoPlistPath, plist.build(buildNotificationServiceInfoPlist()));
  await writeFileIfChanged(
    entitlementsPath,
    plist.build(buildNotificationServiceEntitlements(bundleIdentifier))
  );
  await writeFileIfChanged(sourcePath, notificationServiceSource);
  return [infoPlistPath, entitlementsPath, sourcePath];
}

function getNotificationServiceBundleIdentifier(config: { ios?: { bundleIdentifier?: string } }): string {
  return `${config.ios!.bundleIdentifier}.mappnotificationservice`;
}

function getAppGroupIdentifier(bundleIdentifier: string): string {
  return `group.${bundleIdentifier}`;
}

function buildNotificationServiceEntitlements(bundleIdentifier: string): Record<string, unknown> {
  return {
    'com.apple.security.application-groups': [getAppGroupIdentifier(bundleIdentifier)],
  };
}

function addNotificationServiceToEasConfig(config: any, bundleIdentifier: string): void {
  config.extra = config.extra ?? {};
  config.extra.eas = config.extra.eas ?? {};
  config.extra.eas.build = config.extra.eas.build ?? {};
  config.extra.eas.build.experimental = config.extra.eas.build.experimental ?? {};
  config.extra.eas.build.experimental.ios = config.extra.eas.build.experimental.ios ?? {};
  const ios = config.extra.eas.build.experimental.ios;
  const appExtensions = Array.isArray(ios.appExtensions) ? ios.appExtensions : [];
  ios.appExtensions = [
    ...appExtensions.filter((extension: any) => extension?.targetName !== notificationServiceTargetName),
    { targetName: notificationServiceTargetName, bundleIdentifier },
  ];
}

function addNotificationServiceTarget(project: any, bundleIdentifier: string): void {
  const existingTarget = IOSConfig.Target.getNativeTargets(project).find(([, target]) => (
    IOSConfig.XcodeUtils.unquote(target.name) === notificationServiceTargetName
  ));
  if (existingTarget) {
    return;
  }

  project.hash.project.objects.PBXContainerItemProxy =
    project.hash.project.objects.PBXContainerItemProxy ?? {};
  project.hash.project.objects.PBXTargetDependency =
    project.hash.project.objects.PBXTargetDependency ?? {};
  const target = project.addTarget(
    notificationServiceTargetName,
    'app_extension',
    notificationServiceTargetName,
    bundleIdentifier
  );
  project.addBuildPhase([], 'PBXSourcesBuildPhase', 'Sources', target.uuid);
  project.addBuildPhase([], 'PBXFrameworksBuildPhase', 'Frameworks', target.uuid);

  const groupKey = project.pbxCreateGroup(
    notificationServiceTargetName,
    notificationServiceTargetName
  );
  const { firstProject } = project.getFirstProject();
  project.getPBXGroupByKey(firstProject.mainGroup).children.push({
    value: groupKey,
    comment: notificationServiceTargetName,
  });
  project.addFile(notificationServiceInfoPlistName, groupKey);
  project.addFile(notificationServiceEntitlementsName, groupKey);
  project.addSourceFile(notificationServiceSourceName, { target: target.uuid }, groupKey);
  project.addFramework('UserNotifications.framework', { target: target.uuid });

  for (const [, buildConfiguration] of IOSConfig.XcodeUtils.getBuildConfigurationsForListId(
    project,
    target.pbxNativeTarget.buildConfigurationList
  )) {
    const settings = buildConfiguration.buildSettings;
    settings.CODE_SIGN_STYLE = 'Automatic';
    settings.CODE_SIGN_ENTITLEMENTS = `"${notificationServiceTargetName}/${notificationServiceEntitlementsName}"`;
    settings.GENERATE_INFOPLIST_FILE = 'NO';
    settings.INFOPLIST_FILE = `"${notificationServiceTargetName}/${notificationServiceInfoPlistName}"`;
    settings.IPHONEOS_DEPLOYMENT_TARGET = notificationServiceDeploymentTarget;
    settings.PRODUCT_BUNDLE_IDENTIFIER = `"${bundleIdentifier}"`;
    settings.SWIFT_VERSION = '5.0';
    settings.TARGETED_DEVICE_FAMILY = '"1,2"';
  }
}

function appendUnique(values: unknown, value: string): string[] {
  const result = Array.isArray(values) ? values.filter(item => typeof item === 'string') : [];
  return result.includes(value) ? result : [...result, value];
}

export const withMappEngageIos: ConfigPlugin<NormalizedMappExpoPluginProps['ios']> = (
  config,
  props
) => {
  const notificationServiceBundleIdentifier = getNotificationServiceBundleIdentifier(config);
  addNotificationServiceToEasConfig(config, notificationServiceBundleIdentifier);

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
    configWithEntitlements.modResults['com.apple.security.application-groups'] = appendUnique(
      configWithEntitlements.modResults['com.apple.security.application-groups'],
      getAppGroupIdentifier(config.ios!.bundleIdentifier!)
    );
    return configWithEntitlements;
  });

  config = withDangerousMod(config, ['ios', async configWithFiles => {
    await writeAppoxeeConfig(configWithFiles.modRequest.platformProjectRoot, props);
    await writeNotificationServiceFiles(
      configWithFiles.modRequest.platformProjectRoot,
      config.ios!.bundleIdentifier!
    );
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
    addNotificationServiceTarget(project, notificationServiceBundleIdentifier);
    return configWithProject;
  });
};
