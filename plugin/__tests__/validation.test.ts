import type { ExpoConfig } from '@expo/config-types';
import { validateAndNormalizeProps } from '../src/validation';
import type { MappExpoPluginProps } from '../src/types';

const config = {
  name: 'test',
  slug: 'test',
  android: { package: 'com.example.test', googleServicesFile: './google-services.json' },
  ios: { bundleIdentifier: 'com.example.test' },
} as ExpoConfig;

const props: MappExpoPluginProps = {
  ios: {
    appId: 'app-id',
    dmcSystemId: 123,
    sdkKey: 'private-looking-value',
    isEu: true,
    inAppServerUrl: 'server-url',
  },
};

describe('Mapp Expo config validation', () => {
  it('normalizes optional values and defaults Android push ownership to Mapp', () => {
    expect(validateAndNormalizeProps(config, props)).toMatchObject({
      android: { enableGeofencing: false, pushHandling: 'mapp' },
      ios: { customFields: [], mediaTimeout: 5, openLandingPageInsideApp: false },
    });
  });

  it.each([
    [{ ...config, android: { package: 'com.example.test' } }, props, 'expo.android.googleServicesFile'],
    [{ ...config, android: undefined }, props, 'expo.android.package'],
    [{ ...config, ios: undefined }, props, 'expo.ios.bundleIdentifier'],
    [config, { ...props, android: { pushHandling: 'invalid' } }, 'android.pushHandling'],
    [config, { ...props, ios: { ...props.ios, dmcSystemId: 1.5 } }, 'ios.dmcSystemId'],
    [config, { ...props, ios: { ...props.ios, mediaTimeout: 0 } }, 'ios.mediaTimeout'],
    [config, { ...props, ios: { ...props.ios, enableGeofencing: true } }, 'ios.locationWhenInUsePermission'],
  ] as const)('rejects invalid configuration', (appConfig, options, property) => {
    expect(() => validateAndNormalizeProps(appConfig as ExpoConfig, options as MappExpoPluginProps))
      .toThrow(property);
  });

  it('accepts custom push ownership without a Google services file', () => {
    const customConfig = { ...config, android: { package: 'com.example.test' } } as ExpoConfig;
    expect(validateAndNormalizeProps(customConfig, { ...props, android: { pushHandling: 'custom' } }))
      .toMatchObject({ android: { pushHandling: 'custom' } });
  });

  it('does not reveal supplied configuration values in errors', () => {
    try {
      validateAndNormalizeProps(config, { ...props, ios: { ...props.ios, mediaTimeout: -1 } });
    } catch (error) {
      expect(String(error)).not.toContain(props.ios.sdkKey);
      expect(String(error)).not.toContain(props.ios.appId);
    }
  });
});

