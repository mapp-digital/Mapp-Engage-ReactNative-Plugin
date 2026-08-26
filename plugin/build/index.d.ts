import { ConfigPlugin } from '@expo/config-plugins';
import type { MappExpoPluginProps } from './types';
export declare const withMappEngage: ConfigPlugin<MappExpoPluginProps>;
export type { MappExpoPluginProps } from './types';
export { withMappEngageAndroid } from './android';
export { withMappEngageIos, buildAppoxeeConfig, writeAppoxeeConfig, buildNotificationServiceInfoPlist, writeNotificationServiceFiles, } from './ios';
export { validateAndNormalizeProps } from './validation';
export default withMappEngage;
