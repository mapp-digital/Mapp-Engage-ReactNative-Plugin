import fs from 'fs';
import os from 'os';
import path from 'path';
import plist from '@expo/plist';
import { ConfigPlugin, ExportedConfig, IOSConfig } from '@expo/config-plugins';
import {
  buildAppoxeeConfig,
  buildNotificationServiceInfoPlist,
  writeAppoxeeConfig,
  writeNotificationServiceFiles,
  withMappEngageIos,
} from '../src/ios';

const options = {
  appId: 'app',
  dmcSystemId: 123,
  sdkKey: 'key',
  isEu: true,
  inAppServerUrl: 'server',
  openLandingPageInsideApp: false,
  customFields: ['customString'],
  mediaTimeout: 5,
  enableGeofencing: false,
};

describe('iOS Appoxee configuration', () => {
  it('generates correctly typed plist values', () => {
    expect(buildAppoxeeConfig(options)).toEqual({
      inapp: { custom_fields: ['customString'], media_timeout: 5 },
      sdk: {
        app_id: 'app',
        dmc_system_id: 123,
        sdk_key: 'key',
        is_eu: true,
        jamie_url: 'server',
        open_landing_page_inside_app: false,
        apx_open_url_internal: 'YES',
      },
    });
  });

  it('writes a valid plist and replaces it idempotently', async () => {
    const root = await fs.promises.mkdtemp(path.join(os.tmpdir(), 'mapp-plugin-'));
    const output = await writeAppoxeeConfig(root, options);
    await writeAppoxeeConfig(root, { ...options, mediaTimeout: 7 });
    const parsed = plist.parse(await fs.promises.readFile(output, 'utf8')) as any;
    expect(parsed.inapp.media_timeout).toBe(7);
    expect((await fs.promises.readdir(root))).toEqual(['AppoxeeConfig.plist']);
  });

  it('generates the notification service extension plist', () => {
    expect(buildNotificationServiceInfoPlist()).toEqual({
      NSExtension: {
        NSExtensionPointIdentifier: 'com.apple.usernotifications.service',
        NSExtensionPrincipalClass: '$(PRODUCT_MODULE_NAME).NotificationService',
      },
    });
  });

  it('writes idempotent rich-push extension files that consume ios_apx_media', async () => {
    const root = await fs.promises.mkdtemp(path.join(os.tmpdir(), 'mapp-plugin-nse-'));
    const bundleIdentifier = 'com.example.mappapp';
    const output = await writeNotificationServiceFiles(root, bundleIdentifier);
    await writeNotificationServiceFiles(root, bundleIdentifier);

    expect(output.map(file => path.relative(root, file))).toEqual([
      'MappNotificationService/Info.plist',
      'MappNotificationService/MappNotificationService.entitlements',
      'MappNotificationService/NotificationService.swift',
    ]);
    const source = await fs.promises.readFile(output[2], 'utf8');
    expect(source).toContain('userInfo["ios_apx_media"]');
    expect(source).toContain('UNNotificationAttachment');
    expect(source).toContain('UNUserNotificationCenter.current().getNotificationCategories');
    expect(source).toContain('categoryIdentifier');
    expect(source).toContain('serviceExtensionTimeWillExpire');
    const infoPlist = plist.parse(await fs.promises.readFile(output[0], 'utf8')) as any;
    expect(infoPlist.NSExtension.NSExtensionPointIdentifier)
      .toBe('com.apple.usernotifications.service');
    const entitlements = plist.parse(await fs.promises.readFile(output[1], 'utf8')) as any;
    expect(entitlements['com.apple.security.application-groups'])
      .toEqual([`group.${bundleIdentifier}`]);
  });
});

describe('iOS notification service target metadata', () => {
  const appConfig = {
    name: 'TestApp',
    slug: 'test-app',
    version: '3.2.1',
    ios: { bundleIdentifier: 'com.example.app', buildNumber: '42' },
  };

  function createProject(): any {
    const project = require('xcode').project('TestApp.xcodeproj/project.pbxproj');
    project.hash = {
      project: {
        rootObject: 'PROJECT',
        objects: {
          PBXProject: { PROJECT: { isa: 'PBXProject', mainGroup: 'ROOT', targets: [] } },
          PBXGroup: { ROOT: { isa: 'PBXGroup', children: [], sourceTree: '"<group>"' } },
          PBXNativeTarget: {},
          PBXBuildFile: {},
          PBXFileReference: {},
          XCBuildConfiguration: {},
          XCConfigurationList: {},
          PBXContainerItemProxy: {},
          PBXTargetDependency: {},
        },
      },
    };
    project.addPbxGroup([], 'Products');
    const app = project.addTarget('TestApp', 'application', 'TestApp', appConfig.ios.bundleIdentifier);
    app.pbxNativeTarget.name = 'TestApp';
    app.pbxNativeTarget.dependencies = [];
    project.addBuildPhase([], 'PBXResourcesBuildPhase', 'Resources', app.uuid);
    const group = project.addPbxGroup([], 'TestApp', 'TestApp');
    project.getPBXGroupByKey('ROOT').children.push({ value: group.uuid, comment: 'TestApp' });
    return project;
  }

  async function applyPlugin(project: any, input: Parameters<ConfigPlugin>[0] = appConfig) {
    const config: ExportedConfig = withMappEngageIos(structuredClone(input), options);
    await config.mods!.ios!.xcodeproj!({
      ...config,
      modResults: project,
      modRawConfig: input,
      modRequest: {
        projectName: 'TestApp',
        projectRoot: os.tmpdir(),
        platformProjectRoot: path.join(os.tmpdir(), 'ios'),
        platform: 'ios',
        modName: 'xcodeproj',
        introspect: false,
      },
    });
  }

  function extensionConfigurations(project: any) {
    const [, target] = IOSConfig.Target.findNativeTargetByName(project, 'MappNotificationService');
    return IOSConfig.XcodeUtils.getBuildConfigurationsForListId(project, target.buildConfigurationList);
  }

  it('generates complete bundle metadata with matching app versions in Debug and Release', async () => {
    const project = createProject();
    await applyPlugin(project);
    const configurations = extensionConfigurations(project);
    expect(configurations.map(([, configuration]) => configuration.name)).toEqual(['Debug', 'Release']);
    for (const [, { buildSettings }] of configurations) {
      expect(buildSettings).toMatchObject({
        GENERATE_INFOPLIST_FILE: 'YES',
        PRODUCT_BUNDLE_IDENTIFIER: '"com.example.app.mappnotificationservice"',
        MARKETING_VERSION: '3.2.1',
        CURRENT_PROJECT_VERSION: '42',
      });
    }
  });

  it('repairs existing targets and updates versions without duplicating targets or changing signing', async () => {
    const project = createProject();
    await applyPlugin(project);
    for (const [, { buildSettings }] of extensionConfigurations(project)) {
      buildSettings.GENERATE_INFOPLIST_FILE = 'NO';
      delete buildSettings.MARKETING_VERSION;
      delete buildSettings.CURRENT_PROJECT_VERSION;
      buildSettings.CODE_SIGN_STYLE = 'Manual';
    }
    const updatedConfig = {
      ...appConfig,
      version: '4.0.0',
      ios: { bundleIdentifier: 'com.example.updated', buildNumber: '43' },
    };
    const appTarget = IOSConfig.XcodeUtils.getApplicationNativeTarget({
      project,
      projectName: 'TestApp',
    });
    for (const [, configuration] of IOSConfig.XcodeUtils.getBuildConfigurationsForListId(
      project,
      appTarget.target.buildConfigurationList
    )) {
      configuration.buildSettings.PRODUCT_BUNDLE_IDENTIFIER = '"com.example.updated"';
    }
    await applyPlugin(project, updatedConfig);
    const firstResult = project.writeSync();
    await applyPlugin(project, updatedConfig);
    expect(project.writeSync()).toBe(firstResult);
    expect(IOSConfig.Target.getNativeTargets(project)).toHaveLength(2);
    for (const [, { buildSettings }] of extensionConfigurations(project)) {
      expect(buildSettings).toMatchObject({
        GENERATE_INFOPLIST_FILE: 'YES',
        PRODUCT_BUNDLE_IDENTIFIER: '"com.example.updated.mappnotificationservice"',
        MARKETING_VERSION: '4.0.0',
        CURRENT_PROJECT_VERSION: '43',
        CODE_SIGN_STYLE: 'Manual',
      });
    }
  });

  it('uses the matching app configuration bundle identifier for each extension configuration', async () => {
    const project = createProject();
    const appTarget = IOSConfig.XcodeUtils.getApplicationNativeTarget({
      project,
      projectName: 'TestApp',
    });
    const appConfigurations = IOSConfig.XcodeUtils.getBuildConfigurationsForListId(
      project,
      appTarget.target.buildConfigurationList
    );
    for (const [, configuration] of appConfigurations) {
      configuration.buildSettings.PRODUCT_BUNDLE_IDENTIFIER =
        configuration.name === 'Debug' ? '"com.example.app.debug"' : '"com.example.app"';
    }

    await applyPlugin(project);

    const extensionConfigurationsByName = new Map(
      extensionConfigurations(project).map(([, configuration]) => [
        configuration.name,
        configuration.buildSettings.PRODUCT_BUNDLE_IDENTIFIER,
      ])
    );
    expect(extensionConfigurationsByName).toEqual(new Map([
      ['Debug', '"com.example.app.debug.mappnotificationservice"'],
      ['Release', '"com.example.app.mappnotificationservice"'],
    ]));
  });
});
