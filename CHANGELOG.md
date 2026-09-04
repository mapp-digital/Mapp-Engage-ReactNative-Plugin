## Version 2.0.1 (Unpublished)

***Bug Fixes***

- iOS: `Mapp.onInitCompletedListener()` now returns a `Promise<boolean>` instead of `null`.
- iOS: When the SDK is already initialized, `Mapp.onInitCompletedListener()` resolves immediately with `true`.
- iOS: When the SDK is still initializing, `Mapp.onInitCompletedListener()` waits for the existing `com.mapp.init` native event and removes its one-time listener after resolving.
- Android: Preserved the existing native `AppoxeeObserver` implementation and behavior of `Mapp.onInitCompletedListener()`.
- Android/iOS: Preserved the public `Mapp.onInitCompletedListener(): Promise<boolean>` API while providing consistent initialization-waiting behavior on both platforms.

***Tests***

- Added bridge coverage for both iOS readiness paths: already initialized and waiting for the native initialization event.
- Updated platform-dispatch coverage to verify that Android continues to use the native listener and iOS uses its existing initialization event.

## Version 2.0.0 (2026/09/03)

***Breaking Changes***

- Raised the minimum iOS deployment target from 10.0 to 15.1.
- Exported strict Android compatibility constraints for the supported Expo SDK 57 toolchain and Mapp Engage 7.1.2 runtime ABI.
- Changed Android geofence permission handling, FCM service ownership, and inbox status updates.
- Deprecated `engage2()`, `startGeoFencing()`, and `stopGeoFencing()` in favor of their supported replacements.
- Review the [breaking changes](BREAKING_CHANGES.md), then follow the [1.4.2 to 2.0.0 migration guide](MIGRATION_2.0.md) for required application changes.

***Bug Fixes***

- `Mapp.engage(...)` is now awaitable so singleton-dependent calls can safely run after native initialization; engagement failures reject instead of being logged silently.
- Android: Failed Firebase token registration now rejects with `FCM_REGISTRATION_FAILED` instead of crashing while reading a failed task result.
- Android: All Mapp engage calls run on the main looper. Background Firebase callbacks wait for a bounded engage attempt and safely return failure after SDK errors, timeout, or interruption.
- Android/Expo: Mapp and custom push ownership now remove the SDK v7 Firebase service and remain idempotent when prebuild runs repeatedly or changes mode.
- Android/Expo: The config plugin now writes required permissions, the Mapp messaging service, and the push receiver through `withAndroidManifest`; Firebase configuration remains customer-owned through `expo.android.googleServicesFile`.
- Android: Exported Expo SDK 57 compatibility constraints stabilize WorkManager, Lifecycle, AndroidX Core, Play Services Location, and Kotlin stdlib for API 36/AGP 8.12/Kotlin 2.1.20 builds.
- Android: Coroutines remain aligned at 1.11.0 to preserve the Mapp 7.1.2 native in-app dismissal ABI.
- iOS/Expo: The config plugin now creates and embeds a standalone Notification Service Extension for Mapp rich-push media from `ios_apx_media`, with EAS app-extension metadata and no App Group or extra Pod.
- iOS/Expo: Verified that Expo SDK 57 CocoaPods autolinking discovers `RNMappPlugin.podspec` and processes `RNMappPlugin` under the New Architecture.

***Compatibility***

- Verified baseline: Expo SDK 57, React Native 0.86, New Architecture, Android API 36, AGP 8.12, Kotlin 2.1.20, and JDK 21.

## Version 1.4.2

***Dependency Updates***

- Android: Aligned with Mapp Engage Android SDK 7.1.2.

## Version 1.4.1

***Dependency Updates***

- Android: Aligned with Mapp Engage Android SDK 7.1.1.
- iOS: Aligned with Mapp Engage Inapp iOS SDK 6.0.11, which provides improved in-app statistics.

***Architecture***

- Android: Updated plugin to support New Architecture (TurboModule).

## Version 1.4.0

***Bug Fixes***

- Android: `requestPostNotificationPermission` now requests the runtime notification permission instead of only checking its current state.
- Android: If the runtime prompt is unavailable or the permission is blocked, the plugin now opens the app notification settings screen and resolves the promise in every path.

***Dependency Updates***

- Aligned with Mapp Engage Android SDK 7.0.2.

## Version 1.3.6

***Dependency Updates***

-Aligned with Mapp Engage iOS SDK 6.1.3

## Version 1.3.5

***Bug Fixes***

-iOS: Fixed an issue where App Store links from Push messages could not be opened.

-iOS: Fixed a bug where calling setAlias with resendAttributes = true did not resend cached custom attributes as expected.

***Dependency Updates***

-Aligned with Mapp Engage iOS SDK 6.1.2 and Mapp Engage Inapp iOS SDK 6.0.9.

**Note:**
-This release ensures correct link handling from Push messages and restores expected behavior when resending cached attributes during alias updates on iOS. The update is fully backward compatible and recommended for all iOS integrations using alias functionality.

# Version 1.3.4
- Updated native Mapp SDK versions; for Android - 6.1.3; for iOS - 6.1.1;
**Bug Fixes**
- Android: Fixed an issue where notificationMode settings had no effect — setting the mode to BACKGROUND_ONLY or SILENT_ONLY previously displayed push messages even when the application was in the foreground.
- Android: Fixed a bug where the device’s cached state was not properly invalidated after logout, which caused certain SDK methods to malfunction.
- Android: Fixed an issue where device fingerprint information was not updated when changes occurred.
- iOS: Updated internal implementation to remove usage of deprecated iOS APIs.
- iOS: Links from Push and In-App messages continue to open and function as expected.

**Dependency Updates (Android)**
```
*com.google.code.gson:gson: 2.13.2
*com.google.dagger:dagger: 2.57.2
*com.google.firebase:firebase-bom: 34.4.0
*com.github.bumptech.glide:compiler: 5.0.5
*androidx.appcompat:appcompat: 1.7.1
*androidx.webkit:webkit: 1.14.0
*androidx.work:work-runtime: 2.10.5
*androidx.concurrent:concurrent-futures: 1.3.0
```

# Version 1.3.2
- Resolved an issue introduced in version 1.3.1 where changes to the setAlias function declaration caused compatibility problems. The function has been updated to ensure backward compatibility.

# Version 1.3.1
*** Features ***
- Updated react native version to 0.81.4
- Updated Gradle to version 8.14.2
- android minSdk updated to version 24
- android targetSdk updated to version 36

### Android requrements
* minimum required node version is 20.19.4
* minimum requred Gradle version 8.14.2

### iOS Requirements

* Additional flag needs to be added under the buildSettings -> OTHER_CPLUSPLUSFLAGS
    -DFOLLY_CFG_NO_COROUTINES=1,
    -DFOLLY_HAVE_CLOCK_GETTIME=1
* Mapp SDK 6.0.10 -> 6.1.0
* Mapp Geolocation -> 6.0.7
