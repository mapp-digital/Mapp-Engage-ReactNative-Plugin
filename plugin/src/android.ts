import {
  AndroidConfig,
  ConfigPlugin,
  withAndroidManifest,
} from '@expo/config-plugins';
import type { NormalizedMappExpoPluginProps } from './types';

const messageService = 'com.reactlibrary.MessageService';
const fineLocation = 'android.permission.ACCESS_FINE_LOCATION';
const backgroundLocation = 'android.permission.ACCESS_BACKGROUND_LOCATION';

function removeAll<T>(items: T[] | undefined, predicate: (item: T) => boolean): T[] {
  return (items ?? []).filter(item => !predicate(item));
}

export function updatePushHandling(
  androidManifest: any,
  pushHandling: NormalizedMappExpoPluginProps['android']['pushHandling']
): any {
  const manifest = androidManifest.manifest;
  const application = AndroidConfig.Manifest.getMainApplicationOrThrow(androidManifest);
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

export const withMappEngageAndroid: ConfigPlugin<NormalizedMappExpoPluginProps['android']> = (
  config,
  props
) => {
  if (props.enableGeofencing) {
    config = AndroidConfig.Permissions.withPermissions(config, [fineLocation, backgroundLocation]);
  }

  return withAndroidManifest(config, configWithManifest => {
    configWithManifest.modResults = updatePushHandling(
      configWithManifest.modResults,
      props.pushHandling
    );
    return configWithManifest;
  });
};
