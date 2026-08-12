import { ConfigPlugin } from '@expo/config-plugins';
import type { NormalizedMappExpoPluginProps } from './types';
export declare function updatePushHandling(androidManifest: any, pushHandling: NormalizedMappExpoPluginProps['android']['pushHandling']): any;
export declare const withMappEngageAndroid: ConfigPlugin<NormalizedMappExpoPluginProps['android']>;
