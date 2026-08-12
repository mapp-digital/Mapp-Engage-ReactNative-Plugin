import { updatePushHandling } from '../src/android';

const manifest = () => ({
  manifest: {
    $: { 'xmlns:android': 'http://schemas.android.com/apk/res/android' },
    application: [{
      $: { 'android:name': '.MainApplication' },
      service: [{ $: { 'android:name': 'com.example.CustomMessagingService' } }],
    }],
  },
});

describe('Android push ownership manifest changes', () => {
  it('adds only an exact, idempotent removal marker in custom mode', () => {
    const result = updatePushHandling(manifest(), 'custom');
    updatePushHandling(result, 'custom');
    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
      { $: { 'android:name': 'com.reactlibrary.MessageService', 'tools:node': 'remove' } },
    ]);
    expect(result.manifest.$['xmlns:tools']).toBe('http://schemas.android.com/tools');
  });

  it('removes stale markers when switching back to Mapp mode', () => {
    const result = updatePushHandling(updatePushHandling(manifest(), 'custom'), 'mapp');
    expect(result.manifest.application[0].service).toEqual([
      { $: { 'android:name': 'com.example.CustomMessagingService' } },
    ]);
  });
});

