# react-native-mapp-plugin

Mapp Engage native SDK integration for React Native CLI and Expo development builds.

## Upgrading from 1.4.x

Version 2.0 raises the minimum iOS target to 15.1 and changes Android dependency constraints, permission handling, push ownership, and inbox status updates.

Review the [breaking changes](BREAKING_CHANGES.md) and follow the [1.4.2 to 2.0.0 migration guide](MIGRATION_2.0.md) before upgrading.

## Expo (CNG)

Expo Go is not supported because it cannot load this package's custom native code. Use Continuous Native Generation (CNG) with `expo-dev-client`; generated `android/` and `ios/` directories do not need manual changes.

```bash
npx expo install react-native-mapp-plugin expo-dev-client
```

Configure the installed package by name in `app.json` (values shown are examples):

```json
{
  "expo": {
    "name": "Mapp app",
    "slug": "mapp-app",
    "newArchEnabled": true,
    "ios": {
      "bundleIdentifier": "com.example.mappapp"
    },
    "android": {
      "package": "com.example.mappapp",
      "googleServicesFile": "./google-services.json"
    },
    "plugins": [
      [
        "react-native-mapp-plugin",
        {
          "android": {
            "enableGeofencing": false,
            "pushHandling": "mapp"
          },
          "ios": {
            "appId": "MAPP_APP_ID",
            "dmcSystemId": 123,
            "sdkKey": "MAPP_SDK_KEY",
            "isEu": true,
            "inAppServerUrl": "MAPP_INAPP_SERVER_URL",
            "openLandingPageInsideApp": false,
            "customFields": ["customString", "customNumber", "customDate"],
            "mediaTimeout": 5,
            "enableGeofencing": false
          }
        }
      ]
    ]
  }
}
```

The Android package must match the Firebase Android application in `google-services.json`. For iOS, configure an APNs-enabled App ID, matching bundle identifier, and Apple/EAS signing credentials. Values embedded in app config and native resources are public application configuration; do not put service-account keys or signing secrets there.

The Firebase file is customer-owned configuration. Point Expo's built-in `expo.android.googleServicesFile` field at it; this plugin does not copy, generate, or modify `google-services.json`.

Generate and run development builds:

```bash
npx expo prebuild --clean
npx expo run:android
npx expo run:ios

# Cloud builds, after configuring EAS
eas build --profile development --platform all
eas build --profile preview --platform all
eas build --profile production --platform all
```

Changing plugin options or native dependencies requires a new binary. JavaScript-only changes may use EAS Update.

### Initialization

Register event listeners at application startup, then initialize Mapp. On iOS the generated `AppoxeeConfig.plist` is the credential source of truth; the existing `engage` arguments remain relevant to Android.

```js
import { Mapp, MappEventEmitter } from 'react-native-mapp-plugin';

const events = new MappEventEmitter();
const subscription = events.addListener('com.mapp.deep_link_received', event => {
  // Route the deep link.
});

await Mapp.engage('ANDROID_SDK_KEY', 'FCM_PROJECT_ID', 'EMC', 'APP_ID', 'TENANT_ID');
```

Always await `Mapp.engage(...)` before calling APIs that use the native Mapp singleton. The promise resolves after native engagement and bridge setup complete; use `Mapp.onInitCompletedListener()` or `Mapp.isReady()` when a feature specifically requires the SDK's later ready state.

### Android push ownership

`pushHandling: "mapp"` is the default. It requires `expo.android.googleServicesFile` and retains `com.reactlibrary.MessageService` as the sole normal-priority Mapp FCM callback owner. The config plugin removes the Mapp SDK v7 service (`com.appoxee.shared.MappMessagingService`) from the merged app manifest.

Use `pushHandling: "custom"` when another integration, such as a client-owned `FirebaseMessagingService`, owns callbacks. The plugin removes both its `MessageService` and the Mapp SDK service so the consumer service owns callbacks. Repeated prebuilds and switching modes clean up stale generated markers. From native Android code, use `com.reactlibrary.MappPushHelper`:

```java
@Override public void onMessageReceived(RemoteMessage message) {
  if (!MappPushHelper.handleMessage(getApplication(), message)) {
    // Handle non-Mapp messages here.
  }
}

@Override public void onNewToken(String token) {
  MappPushHelper.handleNewToken(getApplication(), token);
}
```

`Mapp.setRemoteMessage(...)` remains available when JavaScript is guaranteed to be alive. The native helper is required for reliable background and terminated delivery. `expo-notifications` coexistence must use custom ownership and explicit native forwarding; successful manifest merging alone does not forward payloads.

### Geofencing

Set `enableGeofencing` on each platform that needs it. Android then adds fine/background location permissions and `Mapp.requestGeofenceLocationPermission()` requests foreground permission before background permission. On iOS, also supply non-empty `locationWhenInUsePermission` and `locationAlwaysPermission` messages. Only request location access when your user-facing feature and store policy justify it.

### iOS rich push

The Expo config plugin creates a `MappNotificationService` Notification Service Extension with the bundle identifier `<expo.ios.bundleIdentifier>.mappnotificationservice`. The extension reads the public Mapp `ios_apx_media` payload key, downloads the media, and attaches it to the notification. It targets iOS 10 and does not require an App Group, an additional CocoaPod, or React Native code.

The extension is also declared in Expo's experimental EAS app-extension metadata so EAS can prepare its signing credentials. Regenerate the iOS project after changing the application bundle identifier.

### Tested compatibility

| Component | Tested baseline |
| --- | --- |
| Expo SDK | 57 |
| React Native | 0.86 (Expo SDK 57) |
| New Architecture | Enabled |
| Android min / compile / target SDK | 24 / 36 / 36 |
| Android Gradle Plugin / Kotlin / JDK | 8.12 / 2.1.20 / 21 |
| iOS deployment target | 16.4 (library minimum: 15.1) |
| Mapp Android SDK | 7.1.2 |
| Mapp iOS SDKs | Vendored xcframeworks in this package |

Mapp Engage Android 7.1.2 currently publishes Android dependencies newer than the Expo SDK 57 toolchain can consume. This release exports bounded compatibility constraints for AndroidX Core 1.18.0, WorkManager 2.10.5, Lifecycle 2.10.0, Play Services Location 21.3.0, and Kotlin stdlib 2.1.20. These pins can be removed after the Mapp Android publication adopts the Expo-compatible versions.

Coroutines are intentionally different: Mapp's native in-app UI was compiled against kotlinx-coroutines 1.11.0 and calls an ABI absent from 1.10.x. The plugin therefore exports the coroutines 1.11.0 BOM and strict constraints. Do not downgrade coroutines to 1.10.x; dismissing or replacing a native in-app message can otherwise crash with `Job.cancel$default` `NoSuchMethodError`.

Use JDK 21 for Android builds on this baseline. The config plugin does not alter a consumer's Gradle daemon JVM configuration.

The separately maintained [sample application](https://github.com/MappCloud/React-Native-Test-Application/) is the physical-device integration consumer. Record its tested revision here when its Expo CNG migration is released.

## React Native CLI

```bash
npm install react-native-mapp-plugin
cd ios && pod install
```

Modern React Native autolinking discovers the Android package and CocoaPod automatically. Do not run `react-native link`, edit `settings.gradle`, or add `compile project(...)`.

No `MainActivity` or `MainApplication` edit is required. The native module is registered by autolinking. In Expo projects, the config plugin applies Android permissions, the Mapp messaging service, and the push receiver during prebuild without modifying consumer Gradle files. React Native CLI projects receive the same declarations through the library manifest merge. On iOS, CocoaPods discovers `RNMappPlugin.podspec` automatically; do not add the pod manually.

For a manually maintained iOS native project, add Push Notifications, Remote Notifications background mode, and (only if needed) Location Updates, then include an `AppoxeeConfig.plist` in the application target. Expo clients should use the config plugin above instead.

Basic usage:

```js
import { Mapp } from 'react-native-mapp-plugin';

await Mapp.engage('SDK_KEY', 'FCM_PROJECT_ID', 'EMC', 'APP_ID', 'TENANT_ID');
```

See the [Mapp integration documentation](https://mapp-wiki.atlassian.net/wiki/spaces/MIC/pages/1154875400/React+Native+Integration+for+Mapp+Cloud) for the full JavaScript API and native Mapp configuration values.
