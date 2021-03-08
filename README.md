
# react-native-mapp-plugin

## Getting started
[Site](https://mapp.com/) |
[Docs](https://mapp-wiki.atlassian.net/wiki/spaces/MIC/pages/1154875400/React+Native+Integration+for+Mapp+Cloud) 
|[TestApk](https://github.com/MappCloud/React-Native-Test-Application/) 

`$ npm install https://github.com/MappCloud/React-native-plugin.git --save`

**The project is not yet on NPM. Until then, only github integration works **


### Mostly automatic installation

For the old version of RN, 
`$ react-native link react-native-mapp-plugin`

### Manual installation


#### iOS

1) Install pods
```
cd ios && pod install
```

2) Add the following capabilities for your application target:
  - Push Notification
  - Background Modes > Remote Notifications
  - Background Modes > Location updates

3) Create a plist `AppoxeeConfig.plist` and include it in your application’s target:
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>inapp</key>
    <dict>
        <key>custom_fields</key>
        <array>
            <string>customString</string>
            <string>customNumber</string>
            <string>customDate</string>
        </array>
        <key>media_timeout</key>
        <integer>5</integer>
    </dict>
    <key>sdk</key>
    <dict>
        <key>app_id</key>
        <string>your app id</string>
        <key>dmc_system_id</key>
        <integer>your dmc id</integer>
        <key>sdk_key</key>
        <string>your sdk key</string>
        <key>is_eu</key>
        <true/>
        <key>open_landing_page_inside_app</key>
        <false/>
        <key>jamie_url</key>
        <string>your inapp server url</string>
        <key>apx_open_url_internal</key>
        <string>YES</string>
    </dict>
</dict>
</plist>
```

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNMappPluginPackage;` to the imports at the top of the file
  - Add `new RNMappPluginPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-mapp-plugin'
  	project(':react-native-mapp-plugin').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-mapp-plugin/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-mapp-plugin')
  	```

## Usage
```javascript
import Mapp from 'react-native-mapp-plugin';

// TODO: What to do with the module?
Mapp;
```
  
