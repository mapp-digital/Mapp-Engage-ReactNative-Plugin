# Breaking changes in 2.0.0

Version 2.0.0 is largely source-compatible with 1.4.2, but it is not fully backward-compatible at the platform and behavioral levels. Not every client will need application code changes.

No documented JavaScript method was removed or renamed, and existing method parameters remain unchanged. The major version is required because some existing builds, native integrations, and runtime assumptions can break—most notably the increased minimum iOS version.

## Changes that may affect 1.4.2 clients

### Minimum iOS deployment target

The minimum supported iOS deployment target is now 15.1 instead of 10.0.

Applications targeting an older iOS version cannot install or build the pod until their deployment target is raised to iOS 15.1 or newer. This platform-support removal is a breaking change even when the application does not need JavaScript changes.

### Android dependency resolution

The plugin exports strict compatibility constraints for the supported Mapp Engage 7.1.2 and Expo SDK 57 runtime:

- Kotlin 2.1.20
- Coroutines 1.11.0
- AndroidX Core 1.18.0
- WorkManager 2.10.5
- Lifecycle 2.10.0
- Play Services Location 21.3.0

Gradle may fail dependency resolution when an application explicitly requires incompatible versions. Applications already resolving compatible versions do not need a change.

### Custom Android Firebase handling

The plugin removes the Mapp SDK Firebase service and uses `com.reactlibrary.MessageService` as the default Mapp push owner.

Applications that use the default plugin service do not need to migrate their Firebase callbacks. Applications that own a custom `FirebaseMessagingService` must select custom push ownership and forward callbacks through the native helper:

```java
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
```

For Expo, set `android.pushHandling` to `"custom"`. Successful manifest merging alone does not forward messages to Mapp.

### Geofence permission behavior

In 1.4.2, `Mapp.requestGeofenceLocationPermission()` only checked the current Android permission state. In 2.0.0, it requests foreground location and then background location when required, which can display system dialogs.

Call this method from an appropriate user interaction and only after explaining why the application needs location access.

### Firebase token failure handling

On Android, `Mapp.getToken()` now rejects with `FCM_REGISTRATION_FAILED` when Firebase token registration fails. In 1.4.2, reading the failed Firebase task result could crash.

Consumers awaiting the token should handle rejection:

```js
try {
  const token = await Mapp.getToken();
} catch (error) {
  // Handle unavailable FCM registration.
}
```

On iOS, `Mapp.getToken()` rejects with `APNS_TOKEN_UNAVAILABLE` because Mapp auto-integration owns the native APNs token.

### Inbox status methods now update Mapp

The following Android methods were no-ops in 1.4.2:

```js
Mapp.inAppMarkAsRead(templateId, eventId);
Mapp.inAppMarkAsUnRead(templateId, eventId);
Mapp.inAppMarkAsDeleted(templateId, eventId);
```

In 2.0.0, they call Mapp Engage 7.1.2 to fetch the inbox message and update its real server-side status to `READ`, `UNREAD`, or `DELETED`. This is the intended behavior, but it introduces an observable network and backend side effect.

The legacy `eventId` argument remains accepted for source compatibility. Mapp Engage 7.1.2 identifies the message using `templateId`.

### Undocumented iOS native methods

Applications directly calling these undocumented bridge methods will break because they are no longer exported separately:

```js
NativeModules.RNMappPluginModule.autoengage(...);
NativeModules.RNMappPluginModule.engageInapp(...);
```

Use the documented public method instead:

```js
Mapp.engage(sdkKey, googleProjectId, server, appId, tenantId);
```

On iOS, `Mapp.engage(...)` still initializes push and in-app functionality. `AppoxeeConfig.plist` is the credential source of truth.

## Changes that are not immediate breaks

- `Mapp.engage2()` remains available but is deprecated in favor of `Mapp.engage(...)`.
- `Mapp.startGeoFencing()` remains available but is deprecated in favor of `Mapp.startGeofencing()`.
- `Mapp.stopGeoFencing()` remains available but is deprecated in favor of `Mapp.stopGeofencing()`.
- `Mapp.setRemoteMessage()`, `Mapp.isPushFromMapp()`, and `Mapp.setToken()` remain supported while JavaScript is guaranteed to be running. Use `MappPushHelper` instead for native background or terminated Firebase callbacks.
- Android minimum SDK remains 24.
- React Native peer requirement remains `>=0.84`.
- Node.js requirement remains `>=20.19.4`.

## Who can upgrade without source changes?

An application may not need source changes when all of the following are true:

- Its iOS deployment target is already 15.1 or newer.
- Its Android dependencies do not conflict with the exported constraints.
- It uses the plugin's default Android Mapp push service instead of a custom `FirebaseMessagingService`.
- It already handles promise rejection from `Mapp.getToken()`.
- It expects inbox status methods to update the real Mapp message status.
- It does not call undocumented native bridge methods directly.

Even in that case, test push delivery, permission prompts, and inbox status changes on physical devices before releasing the upgrade.

For concrete upgrade steps and replacement examples, continue with the [1.4.2 to 2.0.0 migration guide](MIGRATION_2.0.md).
