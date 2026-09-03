import fs from 'fs';
import os from 'os';
import path from 'path';
import plist from '@expo/plist';
import {
  buildAppoxeeConfig,
  buildNotificationServiceInfoPlist,
  writeAppoxeeConfig,
  writeNotificationServiceFiles,
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
