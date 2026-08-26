import { updateAndroidManifest } from '../src/android';
import type { NormalizedMappExpoPluginProps } from '../src/types';

const options = (
  pushHandling: 'mapp' | 'custom',
  enableGeofencing = false
): NormalizedMappExpoPluginProps['android'] => ({ pushHandling, enableGeofencing });

const manifest = (): any => ({
  manifest: {
    $: { 'xmlns:android': 'http://schemas.android.com/apk/res/android' },
    application: [{
      $: { 'android:name': '.MainApplication' },
      service: [{ $: { 'android:name': 'com.example.CustomMessagingService' } }],
    }],
  },
});

describe('Android push ownership manifest changes', () => {
  it('adds exact, idempotent SDK and plugin removal markers in custom mode', () => {
    const result = updateAndroidManifest(manifest(), options('custom'));
    updateAndroidManifest(result, options('custom'));
    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.push.fcm.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.reactlibrary.MessageService', 'tools:node': 'remove' } },
    ]);
    expect(result.manifest.application[0].receiver).toHaveLength(1);
    expect(result.manifest.$['xmlns:tools']).toBe('http://schemas.android.com/tools');
  });

  it('removes the stale plugin marker but retains SDK removal when switching to Mapp mode', () => {
    const result = updateAndroidManifest(
      updateAndroidManifest(manifest(), options('custom')),
      options('mapp')
    );
    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.push.fcm.MappMessagingService', 'tools:node': 'remove' } },
      {
        $: {
          'android:name': 'com.reactlibrary.MessageService',
          'android:exported': 'false',
        },
        'intent-filter': [{
          action: [{ $: { 'android:name': 'com.google.firebase.MESSAGING_EVENT' } }],
        }],
      },
    ]);
  });

  it('deduplicates stale generated markers in either mode', () => {
    const input = manifest();
    input.manifest.application[0].service.push(
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.reactlibrary.MessageService', 'tools:node': 'remove' } }
    );

    const result = updateAndroidManifest(input, options('mapp'));

    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.push.fcm.MappMessagingService', 'tools:node': 'remove' } },
      expect.objectContaining({
        $: expect.objectContaining({ 'android:name': 'com.reactlibrary.MessageService' }),
      }),
    ]);
  });

  it('adds required permissions and gates geofencing permissions', () => {
    const withoutGeofencing = updateAndroidManifest(manifest(), options('mapp'));
    expect(withoutGeofencing.manifest['uses-permission']).toEqual([
      { $: { 'android:name': 'android.permission.RECEIVE_BOOT_COMPLETED' } },
      { $: { 'android:name': 'android.permission.INTERNET' } },
      { $: { 'android:name': 'android.permission.POST_NOTIFICATIONS' } },
    ]);

    const withGeofencing = updateAndroidManifest(manifest(), options('mapp', true));
    expect(withGeofencing.manifest['uses-permission']).toEqual(expect.arrayContaining([
      { $: { 'android:name': 'android.permission.ACCESS_FINE_LOCATION' } },
      { $: { 'android:name': 'android.permission.ACCESS_BACKGROUND_LOCATION' } },
    ]));
  });
});
