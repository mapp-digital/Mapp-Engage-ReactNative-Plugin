import { ConfigPlugin } from '@expo/config-plugins';
import type { NormalizedMappExpoPluginProps } from './types';
export declare function updateAndroidManifest(androidManifest: any, props: NormalizedMappExpoPluginProps['android']): any;
export declare const withMappEngageAndroid: ConfigPlugin<NormalizedMappExpoPluginProps['android']>;
