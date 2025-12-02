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
