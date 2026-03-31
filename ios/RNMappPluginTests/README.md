# iOS Native Tests — Setup Guide

## Prerequisites

- macOS with full **Xcode** installed (not just Command Line Tools)
- **CocoaPods** installed (`sudo gem install cocoapods` or `brew install cocoapods`)

Verify:
```bash
xcodebuild -version   # must show Xcode version, not "command line tools"
pod --version
```

---

## One-time setup

### 1. Add a test target to the Xcode project

Open the project in Xcode:
```bash
open ios/RNMappPlugin.xcodeproj
```

In Xcode:
1. **File → New → Target…**
2. Choose **Unit Testing Bundle**
3. Name it `RNMappPluginTests`
4. Set **Target to be Tested** to `RNMappPlugin`
5. Confirm — Xcode creates `RNMappPluginTests/` with a default `.m` file

Delete the generated default file — our test files are already in `ios/RNMappPluginTests/`.

### 2. Add the test files to the target

In Xcode's Project Navigator:
1. Right-click `RNMappPluginTests` group → **Add Files to "RNMappPlugin"…**
2. Select both:
   - `RNMappPluginModuleTests.m`
   - `RNMappEventEmmiterTests.m`
3. Make sure **Target Membership** is set to `RNMappPluginTests` (checkbox checked)

### 3. Add OCMock via CocoaPods

> Note: OCMock's Swift Package uses unsafe build flags. Xcode blocks those
> packages in test targets unless you explicitly allow unsafe flags (not
> available in some Xcode versions). CocoaPods is the most reliable path here.

Create (or update) `ios/Podfile`:

```ruby
platform :ios, '13.0'

target 'RNMappPlugin' do
  # existing dependencies ...
end

target 'RNMappPluginTests' do
  inherit! :search_paths
  pod 'OCMock', '~> 3.9'
end
```

Then install:
```bash
cd ios
pod install
```

This generates `RNMappPlugin.xcworkspace`. **Always open the `.xcworkspace`** from now on, not the `.xcodeproj`:
```bash
open ios/RNMappPlugin.xcworkspace
```

### 4. Configure the test target's header search paths

In the test target's **Build Settings**:
- **Header Search Paths** → add:
  ```
  $(SRCROOT)/../node_modules/react-native/React
  $(SRCROOT)/Frameworks/AppoxeeSDK.xcframework/ios-arm64_x86_64-simulator/AppoxeeSDK.framework/Headers
  $(SRCROOT)/Frameworks/AppoxeeInapp.xcframework/ios-arm64_x86_64-simulator/AppoxeeInapp.framework/Headers
  $(SRCROOT)/Frameworks/AppoxeeLocationServices.xcframework/ios-arm64_x86_64-simulator/AppoxeeLocationServices.framework/Headers
  ```

---

## Running the tests

### From Xcode
`Product → Test` (⌘U)

### From the command line
```bash
cd ios
xcodebuild test \
  -workspace RNMappPlugin.xcworkspace \
  -scheme RNMappPluginTests \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
  | xcpretty
```

Install `xcpretty` for readable output:
```bash
gem install xcpretty
```

---

## Test file overview

### RNMappPluginModuleTests.m — 50 tests

| Group | Tests |
|---|---|
| `getServerKeyFor:` | L3, EMC, EMC_US, CROC, TEST, TEST55, unknown→TEST, empty→TEST |
| `getInappServerKeyFor:` | l3, eMC, eMC_US, cROC, tEST, tEST55, unknown→tEST |
| `deviceInfo:` | all keys populated, returns non-nil |
| `engage:` | SDK called with correct sdkKey and server enum |
| `logOut:` | true/false forwarded to SDK |
| `isReady:` | resolves true/false from SDK |
| `isDeviceRegistered:` | resolves isReady value |
| `setAlias:` / `setAliasWithResend:` | correct alias and resend flag forwarded |
| `getAlias:` | resolves alias string / rejects with GET_ALIAS_ERROR |
| `removeDeviceAlias` | SDK called |
| `setPushEnabled:` | inverts flag for disablePushNotifications: |
| `isPushEnabled:` | resolves value / rejects with PUSH_STATUS_ERROR |
| `setPostponeNotificationRequest:` | true/false forwarded |
| `setShowNotificationsAtForeground:` | true/false forwarded |
| `showNotificationAlertView` | SDK called |
| `setAttribute:value:` | key+value forwarded |
| `setAttributeInt:value:` | key+number forwarded |
| `setAttributes:` | dictionary forwarded |
| `incrementNumericKey:value:` | key+number forwarded |
| `getAttributes:and:` | resolves data[@"get"] |
| `getAttributeStringValue:` | resolves string / number as string / rejects |
| `addTag:` / `removeTag:` | single-element array forwarded |
| `getTags:` | resolves array / rejects with GET_TAGS_FAIL |
| `getDeviceInfo:` | resolves dictionary / rejects with GET_DEVICE_INFO_ERROR |
| `engageInapp:` | AppoxeeInapp engaged with correct server |
| `fetchInboxMessage:` / `fetchLatestInboxMessage:` | fetchAPXInBoxMessages called |
| `triggerInApp:` | reportInteractionEventWithName: called |
| `inAppMarkAsRead/UnRead/Deleted:` | correct method called on message / no crash when nil |
| `startGeoFencing` / `stopGeoFencing` | enable/disable called on AppoxeeLocationManager |

### RNMappEventEmmiterTests.m — 35 tests

| Group | Tests |
|---|---|
| Singleton | same instance returned twice, non-nil |
| `supportedEvents` | all 10 event name strings present, list non-empty |
| `getMessageWith:event:` | returns matching / nil for no-match / nil for empty / nil for nil array |
| `stringFromDate:inUTC:` | correct format, correct UTC result, length=19 |
| `getRichMessage:` | id/title/content/messageLink populated; nil fields omitted; postDate included when present |
| `getPushMessage:` | title/alert/body/id/badge/isRich populated; returns non-nil for nil fields |
| `didReceiveInappMessage` | correct event + body with/without extraData |
| `didReceiveDeepLink` | correct event + body; no emit on nil params |
| `didReceiveCustomLink` | correct event + body; no emit on nil params |
| `didReceiveInBoxMessages` | inbox_messages_received emitted; messages stored for lookup |
| `didReceiveInBoxMessage` | inbox_message_received emitted; no emit on nil dictionary |
| `inAppCallFailed` | error_message emitted with error+response; defaults used when nil |
| `didEnterGeoRegion` | georegion_enter emitted with lat/lon; no emit on nil |
| `didExitGeoRegion` | georegion_exit emitted with lat/lon; no emit on nil |
| `locationManager:didFailWithError:` | error_message emitted; no emit on nil error |
