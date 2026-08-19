# Migrating from 1.4.2 to 2.0.0

This guide covers upgrading from the latest published 1.4.x release, 1.4.2. The public JavaScript method names and signatures remain available, but 2.0.0 changes platform requirements, Android dependency resolution, push ownership, and the behavior of several existing methods.

Read [Breaking changes in 2.0.0](BREAKING_CHANGES.md) first to determine which changes affect your application.

## Migration checklist

1. Raise the application deployment target to iOS 15.1 or newer.
2. Use JDK 21 for the supported Android build baseline and remove dependency pins that conflict with the versions below.
3. If using Expo, configure the plugin and replace Expo Go with a development build.
4. Review calls to geofence permission and token APIs for their new prompt and rejection behavior.
5. If the application owns a `FirebaseMessagingService`, forward messages and tokens through `MappPushHelper`.
6. Replace deprecated methods using the migration table below.
7. Verify Android inbox read, unread, and deleted operations against a real Mapp message.

## Platform and build requirements

- The minimum iOS deployment target is now 15.1 instead of 10.0. Applications targeting an older iOS version must raise their deployment target before installing 2.0.0.
- Android still requires a minimum SDK of 24, but the plugin exports strict compatibility constraints for Kotlin 2.1.20, coroutines 1.11.0, AndroidX Core 1.18.0, WorkManager 2.10.5, Lifecycle 2.10.0, and Play Services Location 21.3.0. Remove conflicting application-level pins or align them with these versions.
- JDK 21 is the supported Android build JDK for the tested Expo SDK 57, AGP 8.12, and API 36 baseline.
- Expo Go is not supported. Expo applications must use CNG with `expo-dev-client` and create a new native build after changing plugin options.

## Existing methods with changed behavior

| Method | 1.4.2 behavior | 2.0.0 behavior and migration impact |
| --- | --- | --- |
| `Mapp.requestGeofenceLocationPermission()` | Android only checked whether permissions had already been granted. | Android now displays the foreground-location request and then the background-location request when needed. Call it from an appropriate user action and expect system dialogs. iOS now has a New Architecture implementation as well. |
| `Mapp.getToken()` | A failed Android Firebase task could crash while its result was read. The iOS implementation was missing. | Android rejects with `FCM_REGISTRATION_FAILED`; consumers must handle promise rejection. iOS rejects with `APNS_TOKEN_UNAVAILABLE` because Mapp auto-integration owns the APNs token. |
| `Mapp.setToken(token)` | The iOS bridge did not settle the declared promise. | iOS resolves `true` for a base64-encoded native APNs token and rejects with `INVALID_APNS_TOKEN` for invalid input. An Expo push token is not a native APNs token. |
| `Mapp.inAppMarkAsRead(templateId, eventId)` | Android no-op. | Android fetches the Mapp Engage 7.1.2 inbox message and updates it to `READ`. |
| `Mapp.inAppMarkAsUnRead(templateId, eventId)` | Android no-op. | Android fetches the inbox message and updates it to `UNREAD`. |
| `Mapp.inAppMarkAsDeleted(templateId, eventId)` | Android no-op. | Android fetches the inbox message and updates it to `DELETED`. |
| `Mapp.engage(...)` | iOS JavaScript called the private native `autoengage` and `engageInapp` methods separately. | All platforms use the public native `engage` entry point. On iOS it still initializes both push and in-app, using `AppoxeeConfig.plist` as the credential source of truth. |
| iOS event listeners | Events emitted before a JavaScript listener was attached were dropped. | Up to 50 cold-start events are buffered and delivered after a listener attaches. Consumers should tolerate receiving an initial queued event. |

For the Android inbox methods, `eventId` remains accepted for source compatibility but Mapp Engage 7.1.2 identifies and fetches the message using `templateId`.

## Deprecated methods and replacements

| Deprecated method | Replacement |
| --- | --- |
| `Mapp.engage2()` | `Mapp.engage(sdkKey, googleProjectId, server, appId, tenantId)` |
| `Mapp.startGeoFencing()` | `await Mapp.startGeofencing()` |
| `Mapp.stopGeoFencing()` | `await Mapp.stopGeofencing()` |
| Direct `NativeModules.RNMappPluginModule.autoengage(...)` or `engageInapp(...)` calls | `Mapp.engage(...)` |

The direct `autoengage` and `engageInapp` bridge methods were never part of the documented JavaScript API and are no longer exported separately.

## New Android native push API

Applications that own a custom `FirebaseMessagingService` can now use `com.reactlibrary.MappPushHelper`. These methods run without a React Native JavaScript runtime and replace JavaScript forwarding from background or terminated callbacks:

| New method | Purpose | Replaces in a native Firebase service |
| --- | --- | --- |
| `MappPushHelper.initialize(application)` | Safely engages Mapp on the main looper and waits for the bounded initialization attempt. | Direct background calls to `Appoxee.engage(...)`. |
| `MappPushHelper.isMappMessage(remoteMessage)` | Returns whether a native `RemoteMessage` belongs to Mapp. | Converting the message to JSON and calling `Mapp.isPushFromMapp(...)`. |
| `MappPushHelper.handleMessage(application, remoteMessage)` | Initializes Mapp when needed and forwards a Mapp message. It returns `true` only when the message was handled by Mapp. | `Mapp.setRemoteMessage(...)` for background and terminated delivery. |
| `MappPushHelper.handleNewToken(application, token)` | Initializes Mapp when needed and forwards a new FCM token. | Calling `Mapp.setToken(...)` from a Firebase token callback. |
| `MappPushHelper.waitUntilReady()` | Waits for the already-engaged SDK to become ready for a bounded period. | Application-owned polling of `Mapp.isReady()`. |

`Mapp.setRemoteMessage(...)`, `Mapp.isPushFromMapp(...)`, and `Mapp.setToken(...)` remain supported when JavaScript is guaranteed to be running. They are not reliable replacements for native Firebase callbacks while the application is backgrounded or terminated.

## Expo configuration

Add the package to the Expo `plugins` array and supply the required iOS Mapp values. The default Android `pushHandling` mode is `"mapp"` and requires `expo.android.googleServicesFile`.

Use `pushHandling: "custom"` when the application or another library owns Firebase callbacks. The config plugin then removes both the plugin and Mapp SDK messaging services so the application service is the sole owner.

See the [Expo configuration example](README.md#expo-cng) for all options.

## Native Android push migration example

```java
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.reactlibrary.MappPushHelper;

public final class ApplicationMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (!MappPushHelper.handleMessage(getApplication(), message)) {
            // Handle non-Mapp messages.
        }
    }

    @Override
    public void onNewToken(String token) {
        MappPushHelper.handleNewToken(getApplication(), token);
    }
}
```

For Expo, rebuild the native projects after changing the plugin configuration:

```bash
npx expo prebuild --clean
npx expo run:android
npx expo run:ios
```
