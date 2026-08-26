import { ConfigPlugin } from '@expo/config-plugins';
import type { NormalizedMappExpoPluginProps } from './types';
export declare function buildAppoxeeConfig(props: NormalizedMappExpoPluginProps['ios']): Record<string, unknown>;
export declare function writeAppoxeeConfig(platformProjectRoot: string, props: NormalizedMappExpoPluginProps['ios']): Promise<string>;
export declare function buildNotificationServiceInfoPlist(): Record<string, unknown>;
export declare function writeNotificationServiceFiles(platformProjectRoot: string): Promise<string[]>;
export declare const withMappEngageIos: ConfigPlugin<NormalizedMappExpoPluginProps['ios']>;
