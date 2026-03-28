### Clean all caches
```bash
./clean.sh
```

### Generate codegen
```bash
npx react-native codegen
```

### Rebuild node_modules
```bash
npm install #or yarn install
```

### Run application
```bash
npx react-native run-android
```
or
```bash
npx react-native run-ios
```

### Run tests

#### JavaScript tests (Jest)
```bash
npm test
```

Runs all 3 test suites (101 tests total):
- `MappApiSurface.test.js` — API surface contract (method existence)
- `MappLogic.test.js` — platform dispatch logic, convertEventEnum behaviour
- `MappBridge.test.js` — native bridge calls and parameter passing

#### Android unit tests (JUnit)
```bash
cd android && ./gradlew test
```

Runs all 53 tests across 5 test classes:
- `RNMappPluginModuleApiSignatureTest` — @ReactMethod signature contract
- `ResolveServerTest` — server name resolution logic (v7 fix)
- `IsMappPushTest` — Mapp push detection JSON logic
- `GetRemoteMessageTest` — JSON → RemoteMessage parsing
- `RNUtilsTest` — ReadableMap → JSONObject conversion
- `PushNotificationEventTest` — push event name constant
