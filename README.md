# AppUpdater
Custom app updater for android apps

[![](https://jitpack.io/v/elliot619/AppUpdater.svg)](https://jitpack.io/#elliot619/AppUpdater)

This library is a custom Android's app updater, that works with Android O and previous versions.
This library main feature is that, if there is an update for the app, it downloads the APK trough the DownloadManager and also starts the PackageManager automatically to handle the downloaded APK, just how the mechanism works for the Play Store Apps.

First step to use this is to add this repository in you project build.gradle:
  ```
  maven { url 'https://jitpack.io' }
  ```

Then you need to include this dependency in you app build.gradle:
    ```
compile 'com.github.elliot619:AppUpdater:-SNAPSHOT'
```

If you have problems on merge of manifests, add this code to the end of you app build.gradle (You can put any version you need for the support library):

```
configurations.all {
    resolutionStrategy.eachDependency { details ->
        def requested = details.requested
        if (requested.group == 'com.android.support') {
            if (!requested.name.startsWith("multidex")) {
                details.useVersion '25.3.1'
            }
        }
    }
}
```

To check for the updates, this library looks for a JSON file hosted in a server that contains this structure:
```
{"currentVersion":"2.0","apkUrl":"http://www.domain.com/appFile.apk"}
```

To make the comparison and decide if there is an available update, the plugin checks for the versionName of the app defined in it's build.gradle against the currentVersion in the JSON file.

To use this plugin you need to add this line whatever you need it:
```
new AppUpdater(MyActivity.this).sendNetworkUpdateAppRequest("http://www.domain.com/fileDescriptor.json");
```

The translations for this are in english, but you can override them by putting this strings in your res/strings.xml:

```
<string name="app_name">AppUpdater</string>
<string name="update_available_title">Update available</string>
<string name="update_available_msg">Press OK to update this app</string>
<string name="update_available_subtitle">Click here to install</string>
<string name="update_error">"Thre was an error while updating, please try again later.</string>
<string name="update_provider_name">Your host</string>
```
