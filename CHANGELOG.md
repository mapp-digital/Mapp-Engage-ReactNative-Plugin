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
