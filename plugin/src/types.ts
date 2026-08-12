export type MappAndroidPushHandling = 'mapp' | 'custom';

export type MappExpoPluginProps = {
  android?: {
    enableGeofencing?: boolean;
    pushHandling?: MappAndroidPushHandling;
  };
  ios: {
    appId: string;
    dmcSystemId: number;
    sdkKey: string;
    isEu: boolean;
    inAppServerUrl: string;
    openLandingPageInsideApp?: boolean;
    customFields?: string[];
    mediaTimeout?: number;
    enableGeofencing?: boolean;
    locationWhenInUsePermission?: string;
    locationAlwaysPermission?: string;
  };
};

export type NormalizedMappExpoPluginProps = {
  android: {
    enableGeofencing: boolean;
    pushHandling: MappAndroidPushHandling;
  };
  ios: MappExpoPluginProps['ios'] & {
    openLandingPageInsideApp: boolean;
    customFields: string[];
    mediaTimeout: number;
    enableGeofencing: boolean;
  };
};

