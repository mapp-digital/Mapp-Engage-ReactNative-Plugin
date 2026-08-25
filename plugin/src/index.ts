import { ConfigPlugin, createRunOncePlugin } from '@expo/config-plugins';
import { withMappEngageAndroid } from './android';
import { withMappEngageIos } from './ios';
import type { MappExpoPluginProps } from './types';
import { validateAndNormalizeProps } from './validation';

const pkg = require('../../package.json') as { version: string };

const plugin: ConfigPlugin<MappExpoPluginProps> = (config, props) => {
  const normalized = validateAndNormalizeProps(config, props);
  config = withMappEngageAndroid(config, normalized.android);
  config = withMappEngageIos(config, normalized.ios);
  return config;
};

export const withMappEngage = createRunOncePlugin(
  plugin,
  'react-native-mapp-plugin',
  pkg.version
);

export type { MappExpoPluginProps } from './types';
export { withMappEngageAndroid } from './android';
export { withMappEngageIos, buildAppoxeeConfig, writeAppoxeeConfig } from './ios';
export { validateAndNormalizeProps } from './validation';

export default withMappEngage;
