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

#### Android unit tests (JUnit)
```bash
cd android && ./gradlew test
```
