# react-native-mapp-plugin

Mapp Engage native SDK integration for React Native CLI and Expo development builds.

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

Mapp.engage('ANDROID_SDK_KEY', 'FCM_PROJECT_ID', 'EMC', 'APP_ID', 'TENANT_ID');
```

### Android push ownership

`pushHandling: "mapp"` is the default. It requires `expo.android.googleServicesFile` and retains `com.reactlibrary.MessageService` as the FCM callback owner.

Use `pushHandling: "custom"` when another integration, such as a client-owned `FirebaseMessagingService`, owns callbacks. The plugin adds a manifest merger rule that removes only `com.reactlibrary.MessageService`. From native Android code, use `com.reactlibrary.MappPushHelper`:

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

### Tested compatibility

| Component | Tested baseline |
| --- | --- |
| Expo SDK | 56 |
| React Native | 0.85 (Expo SDK 56) |
| New Architecture | Enabled |
| Android min / compile / target SDK | 24 / 36 / 36 |
| iOS deployment target | 16.4 (library minimum: 15.1) |
| Mapp Android SDK | 7.1.2 |
| Mapp iOS SDKs | Vendored xcframeworks in this package |

The separately maintained [sample application](https://github.com/MappCloud/React-Native-Test-Application/) is the physical-device integration consumer. Record its tested revision here when its Expo CNG migration is released.

## React Native CLI

```bash
npm install react-native-mapp-plugin
cd ios && pod install
```

Modern React Native autolinking discovers the Android package and CocoaPod automatically. Do not run `react-native link`, edit `settings.gradle`, or add `compile project(...)`.

For a manually maintained iOS native project, add Push Notifications, Remote Notifications background mode, and (only if needed) Location Updates, then include an `AppoxeeConfig.plist` in the application target. Expo clients should use the config plugin above instead.

Basic usage:

```js
import { Mapp } from 'react-native-mapp-plugin';

Mapp.engage('SDK_KEY', 'FCM_PROJECT_ID', 'EMC', 'APP_ID', 'TENANT_ID');
```

See the [Mapp integration documentation](https://mapp-wiki.atlassian.net/wiki/spaces/MIC/pages/1154875400/React+Native+Integration+for+Mapp+Cloud) for the full JavaScript API and native Mapp configuration values.
