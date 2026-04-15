## Version 1.4.0

***Bug Fixes***

- Android: `requestPostNotificationPermission` now requests the runtime notification permission instead of only checking its current state.
- Android: If the runtime prompt is unavailable or the permission is blocked, the plugin now opens the app notification settings screen and resolves the promise in every path.

***Dependency Updates***

- Aligned with Mapp Engage Android SDK 7.0.2.

## Version 1.3.6

***Improvements***

-iOS: Updated bundled Mapp Engage SDK binaries to version 6.1.3 (AppoxeeSDK.xcframework).

-iOS: Updated CocoaPods integration to use vendored `.xcframework` and bundled resources directly from this plugin.

-iOS: Removed direct CocoaPods dependencies on `MappSDK`, `MappSDKInapp`, and `MappSDKGeotargeting` from the podspec to keep native SDK versions aligned with the plugin package.

***Compatibility***

-No JavaScript API changes.

-No Android SDK changes in this release.

**Note:**
-This is a packaging and dependency-alignment release focused on iOS integration consistency. No breaking changes are expected.

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
