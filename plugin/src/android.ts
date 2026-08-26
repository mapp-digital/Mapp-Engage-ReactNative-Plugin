import {
  AndroidConfig,
  ConfigPlugin,
  withAndroidManifest,
} from '@expo/config-plugins';
import type { NormalizedMappExpoPluginProps } from './types';

const messageService = 'com.reactlibrary.MessageService';
const pushBroadcastReceiver = 'com.reactlibrary.MyPushBroadcastReceiver';
const sdkMessagingServices = [
  'com.appoxee.shared.MappMessagingService',
  'com.appoxee.push.fcm.MappMessagingService',
] as const;
const managedMessagingServices = [messageService, ...sdkMessagingServices];
const receiveBootCompleted = 'android.permission.RECEIVE_BOOT_COMPLETED';
const internet = 'android.permission.INTERNET';
const postNotifications = 'android.permission.POST_NOTIFICATIONS';
const fineLocation = 'android.permission.ACCESS_FINE_LOCATION';
const backgroundLocation = 'android.permission.ACCESS_BACKGROUND_LOCATION';
const firebaseMessagingEvent = 'com.google.firebase.MESSAGING_EVENT';

function removeAll<T>(items: T[] | undefined, predicate: (item: T) => boolean): T[] {
  return (items ?? []).filter(item => !predicate(item));
}

function addPermission(androidManifest: any, permission: string): void {
  const permissions = androidManifest.manifest['uses-permission'] ?? [];
  if (!permissions.some((item: any) => item.$?.['android:name'] === permission)) {
    permissions.push({ $: { 'android:name': permission } });
  }
  androidManifest.manifest['uses-permission'] = permissions;
}

export function updateAndroidManifest(
  androidManifest: any,
  props: NormalizedMappExpoPluginProps['android']
): any {
  const manifest = androidManifest.manifest;
  const application = AndroidConfig.Manifest.getMainApplicationOrThrow(androidManifest);
  application.service = removeAll(application.service, service => {
    return managedMessagingServices.includes(service.$?.['android:name']);
  });
  application.receiver = removeAll(application.receiver, receiver => (
    receiver.$?.['android:name'] === pushBroadcastReceiver
  ));

  manifest.$ = manifest.$ ?? {};
  manifest.$['xmlns:tools'] = 'http://schemas.android.com/tools';
  for (const permission of [receiveBootCompleted, internet, postNotifications]) {
    addPermission(androidManifest, permission);
  }
  if (props.enableGeofencing) {
    addPermission(androidManifest, fineLocation);
    addPermission(androidManifest, backgroundLocation);
  }

  application.receiver.push({
    $: {
      'android:name': pushBroadcastReceiver,
      'android:enabled': 'true',
      'android:exported': 'false',
    },
    'intent-filter': [{
      action: [
        { $: { 'android:name': 'com.appoxee.PUSH_OPENED' } },
        { $: { 'android:name': 'com.appoxee.PUSH_RECEIVED' } },
        { $: { 'android:name': 'com.appoxee.PUSH_DISMISSED' } },
        { $: { 'android:name': 'com.appoxee.BUTTON_CLICKED' } },
        { $: { 'android:name': 'android.intent.action.VIEW' } },
      ],
      category: [
        { $: { 'android:name': '${applicationId}' } },
        { $: { 'android:name': 'android.intent.category.DEFAULT' } },
        { $: { 'android:name': 'android.intent.category.BROWSABLE' } },
      ],
    }],
  });

  for (const serviceName of sdkMessagingServices) {
    application.service.push({
      $: {
        'android:name': serviceName,
        'tools:node': 'remove',
      },
    });
  }
  if (props.pushHandling === 'mapp') {
    application.service.push({
      $: {
        'android:name': messageService,
        'android:exported': 'false',
      },
      'intent-filter': [{
        action: [{ $: { 'android:name': firebaseMessagingEvent } }],
      }],
    });
  } else {
    application.service.push({
      $: {
        'android:name': messageService,
        'tools:node': 'remove',
      },
    });
  }
  return androidManifest;
}

export const withMappEngageAndroid: ConfigPlugin<NormalizedMappExpoPluginProps['android']> = (
  config,
  props
) => {
  return withAndroidManifest(config, configWithManifest => {
    configWithManifest.modResults = updateAndroidManifest(
      configWithManifest.modResults,
      props
    );
    return configWithManifest;
  });
};
