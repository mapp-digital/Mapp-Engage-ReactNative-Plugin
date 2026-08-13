import { updatePushHandling } from '../src/android';

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
    const result = updatePushHandling(manifest(), 'custom');
    updatePushHandling(result, 'custom');
    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.push.fcm.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.reactlibrary.MessageService', 'tools:node': 'remove' } },
    ]);
    expect(result.manifest.$['xmlns:tools']).toBe('http://schemas.android.com/tools');
  });

  it('removes the stale plugin marker but retains SDK removal when switching to Mapp mode', () => {
    const result = updatePushHandling(updatePushHandling(manifest(), 'custom'), 'mapp');
    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.push.fcm.MappMessagingService', 'tools:node': 'remove' } },
    ]);
  });

  it('deduplicates stale generated markers in either mode', () => {
    const input = manifest();
    input.manifest.application[0].service.push(
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.reactlibrary.MessageService', 'tools:node': 'remove' } }
    );

    const result = updatePushHandling(input, 'mapp');

    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
      { $: { 'android:name': 'com.appoxee.shared.MappMessagingService', 'tools:node': 'remove' } },
      { $: { 'android:name': 'com.appoxee.push.fcm.MappMessagingService', 'tools:node': 'remove' } },
    ]);
  });
});
