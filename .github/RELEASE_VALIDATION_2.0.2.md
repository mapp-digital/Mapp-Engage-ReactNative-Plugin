# Android acceptance and release checks for 2.0.2

Tracks [SDK-2246](https://mapp.atlassian.net/browse/SDK-2246), [SDK-2145](https://mapp.atlassian.net/browse/SDK-2145) and [SDK-2146](https://mapp.atlassian.net/browse/SDK-2146).

This is a checklist, not a test-result report. Run builds and device checks manually. Leave each item unchecked until its evidence is attached to the relevant ticket.

## Evidence to record

Record the plugin commit/package version, resolved native SDK version, consumer app commit, RN/Expo and Android toolchain versions, device model/OS, push ownership mode, date and result. Attach relevant test reports, channel settings before/after, and session request counts. Keep credentials and full push payloads out of shared evidence.

For the actual v6 upgrade, also record the starting APK's native SDK version, application ID and signing-certificate fingerprint. Plugin 1.4.2 and 2.0.1 both use native SDK 7.1.2; upgrading between those plugin versions is not evidence of a native v6-to-v7 migration.

## Automated plugin checks

The existing **Expo support validation** workflow runs the JavaScript/package checks and Android tests, dependency verification and compilation. It can also be started manually for a selected branch. Use JDK 21 for Android checks.

- [ ] Record a successful workflow run for the exact release commit.
- [ ] Confirm the JavaScript/config-plugin tests and package-content check passed.
- [ ] Confirm `MappEngagementDispatcherTest`, `MappPushHelperTest`, `RNMappPluginModuleTest` and `RNMappPluginModuleApiSignatureTest` passed.
- [ ] Confirm `verifyExpo57RuntimeDependencies` passed with native SDK 7.1.4, preserving the existing dependency constraints.

For a focused manual run from the repository root after installing dependencies:

```sh
cd android
./gradlew testDebugUnitTest \
  --tests com.reactlibrary.MappEngagementDispatcherTest \
  --tests com.reactlibrary.MappPushHelperTest \
  --tests com.reactlibrary.RNMappPluginModuleTest \
  --tests com.reactlibrary.RNMappPluginModuleApiSignatureTest \
  --no-daemon --console=plain
./gradlew verifyExpo57RuntimeDependencies --no-daemon --console=plain
```

The RN unit tests mock the native SDK. Use native SDK regression results for instance reuse, channel selection and lifecycle behavior, and the consumer checks below for delivery through a real RN app. Do not report mock-based tests as physical-device or signed-upgrade evidence.

## Published native artifact and consumer compatibility

- [ ] Verify that both the 7.1.4 POM and AAR are available from Maven Central. A local Maven installation is insufficient.

```sh
curl --fail --silent --show-error --head \
  https://repo.maven.apache.org/maven2/com/mapp/sdk/engage-android/7.1.4/engage-android-7.1.4.pom
curl --fail --silent --show-error --head \
  https://repo.maven.apache.org/maven2/com/mapp/sdk/engage-android/7.1.4/engage-android-7.1.4.aar
```

- [ ] Build a representative supported RN/Expo consumer using the candidate package and the published native artifact, without a local SDK substitution. Record the package source and resolved dependency graph. Use a clean CI runner without a populated Maven-local repository to establish remote resolution.
- [ ] Confirm the consumer resolves `com.mapp.sdk:engage-android:7.1.4`; inspect its actual Android runtime configuration with Gradle `dependencyInsight` and retain the output.
- [ ] Check the merged manifest: plugin-owned mode has the plugin messaging service as the sole normal-priority Mapp FCM owner; custom mode retains only the application's intended owner and forwards through `MappPushHelper`. Do not add a second Mapp service.
- [ ] Confirm notification permission handling, foreground/silent behavior and representative iOS initialization/push behavior still work. Record the consumer and build used.

## Channel scenarios — SDK-2246

Capture existing channel IDs and their settings before sending notifications. In every scenario, verify the posted notification's actual channel ID, not only whether a popup appeared. Repeat after process restart and after another push.

| Starting installation | Expected result |
| --- | --- |
| Actual v6-based APK, only `channel_id_001` present; upgrade in place to the candidate with the same application ID/signing key | Legacy channel remains selected, all settings retained, no v7 channel created. |
| Existing v7 installation, only `${packageName}_CHANNEL_ID` present | Existing v7 channel and settings retained. |
| Both legacy and v7 channels exist | v7 wins even when legacy is HIGH and v7 is DEFAULT, LOW or blocked. |
| Neither channel exists | v7 channel created at HIGH and used for posting. |
| User-modified or application-precreated selected channel | Importance, blocked state, sound, vibration, badges and other configured settings remain unchanged. |

- [ ] Complete the actual signed v6 upgrade without uninstalling or clearing app data between the old and new APKs. Test legacy HIGH, reduced importance and blocked settings on separate runs/devices.
- [ ] Complete the existing-v7 and both-channel cases, including DEFAULT, LOW and blocked v7 channels.
- [ ] Complete fresh channel creation and confirm HIGH. Record notification permission/DND/device settings when checking popup behavior.
- [ ] Repeat with a user-modified/application-precreated channel and verify its name/settings survive repeated access, pushes and process restart. Do not delete and recreate the channel during the preservation check.
- [ ] Exercise real foreground/background delivery for the supported plugin-owned and custom-forwarding paths. Muted/blocked channels must remain muted/blocked; existing DEFAULT channels must not be promoted.

If a popup is absent, check the selected channel, notification permission, DND and device settings before attributing it to migration. Mapp dashboard channel IDs are separate; no dashboard changes are required.

## Cold background receipt and interactions — SDK-2145

Use a registered consumer with working credentials, notification permission and native startup configuration. A truly cold native path needs saved native options or valid `AppoxeeConfig` defaults; JavaScript must not be required to supply credentials while the process is stopped.

1. Launch once, await readiness and confirm the intended messaging service/receiver configuration.
2. Send the app to the background, then terminate its background process. For example, use `adb shell am kill <applicationId>` and verify that the process has exited. Do not use Android's **Force stop** or `am force-stop`; that changes delivery conditions. See the [Android activity-manager command reference](https://developer.android.com/tools/adb#am).
3. Keep the app closed and send a real Mapp push through the configured native Firebase path. Record whether JS started, and verify native receipt, receiver setup and the selected notification channel without relying on a JS `engage()` call.
4. Repeat receipt independently for notification tap, action and dismissal where supported. Verify callback handling, reporting and navigation after the app/JS starts. If an interaction is intended to be delivered to JS, record that behavior separately from native receiver execution.
5. Exercise native token forwarding through `onNewToken`/`MappPushHelper`, without manually starting JS, and confirm registration reaches the SDK. Record how token refresh was triggered.

- [ ] Background receipt and interactions passed after process termination in plugin-owned mode.
- [ ] Equivalent native forwarding passed in custom ownership mode.
- [ ] Standard, test-server, deprecated `engage2()` and blocking/native entry points register the receiver; initialization errors do not report success.
- [ ] Native token forwarding passed without a running JS context.

## Session reuse, retry and reconfiguration — SDK-2146

- [ ] In one process, exercise JS engagement, a push callback and a token callback with unchanged configuration. End one foreground session and confirm exactly one activation/session report; distinguish unrelated token/device-update requests from session reports.
- [ ] Retry a failed initialization and confirm recovery without accumulating lifecycle callbacks.
- [ ] Change configuration explicitly, wait for SDK readiness, then verify subsequent calls use the new configuration and one session end still produces one report. Existing observer subscriptions and receiver registration remain valid. Do not count cancellation of old-channel work as a successful new-channel operation.
- [ ] Attach the native SDK lifecycle regression report, including the three-engagement/one-report cases, and record the separate RN consumer results.

## Package release and availability — SDK-2246

- [ ] Commit/merge the intended RN changes and finalize the 2.0.2 changelog date only when releasing. Preserve the existing 2.0.1 history.
- [ ] Run the existing **Publish to npm** workflow in `dry-run` mode for the release commit and review its checks/package contents.
- [ ] After the required validation is complete, the release owner runs the normal authorized publish process for 2.0.2.
- [ ] Verify the exact package is downloadable, not only that a publish job succeeded:

```sh
npm view react-native-mapp-plugin@2.0.2 version dist.tarball dist.integrity
# Run in an empty temporary directory so the tarball is easy to identify.
npm pack react-native-mapp-plugin@2.0.2 --ignore-scripts
tar -xOf react-native-mapp-plugin-2.0.2.tgz package/android/build.gradle
tar -xOf react-native-mapp-plugin-2.0.2.tgz package/package.json
```

- [ ] Confirm the downloaded package declares version 2.0.2 and native dependency 7.1.4, and the intended npm dist-tag points to it.
- [ ] Smoke-test a clean consumer installation from that published npm package against Maven Central before sharing availability with the client.
- [ ] Add the actual validation/release evidence to SDK-2246 and the corresponding device/regression evidence to SDK-2145 and SDK-2146. Do not close acceptance criteria on this checklist alone.
