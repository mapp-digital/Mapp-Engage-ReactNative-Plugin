
# react-native-mapp-plugin

## Getting started

`$ npm install react-native-mapp-plugin --save`

### Mostly automatic installation

`$ react-native link react-native-mapp-plugin`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-mapp-plugin` and add `RNMappPlugin.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNMappPlugin.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

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

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNMappPlugin.sln` in `node_modules/react-native-mapp-plugin/windows/RNMappPlugin.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Mapp.Plugin.RNMappPlugin;` to the usings at the top of the file
  - Add `new RNMappPluginPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNMappPlugin from 'react-native-mapp-plugin';

// TODO: What to do with the module?
RNMappPlugin;
```
  