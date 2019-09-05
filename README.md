# react-native-video-google-dai

## Getting started

`$ npm install react-native-video-google-dai --save`

### Mostly automatic installation

`$ react-native link react-native-video-google-dai`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-video-google-dai` and add `VideoGoogleDai.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libVideoGoogleDai.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.VideoGoogleDaiPackage;` to the imports at the top of the file
  - Add `new VideoGoogleDaiPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-video-google-dai'
  	project(':react-native-video-google-dai').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-video-google-dai/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-video-google-dai')
  	```


## Usage
```javascript
import VideoGoogleDai from 'react-native-video-google-dai';

// TODO: What to do with the module?
VideoGoogleDai;
```
