package com.reactlibrary;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import com.facebook.react.uimanager.ViewManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNMappPluginPackage implements ReactPackage {

  @NonNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
    return Collections.singletonList(new RNMappPluginModule(reactContext));
  }

  @NonNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }

  public ReactModuleInfoProvider getReactModuleInfoProvider() {
    return () -> {
      Map<String, ReactModuleInfo> moduleInfos = new HashMap<>();

      moduleInfos.put(
              RNMappPluginModule.NAME,
              new ReactModuleInfo(
                      RNMappPluginModule.NAME,
                      RNMappPluginModule.NAME,
                      false, // canOverrideExistingModule
                      false, // needsEagerInit
                      true,  // hasConstants
                      false, // isCxxModule
                      true   // isTurboModule -> THIS IS IMPORTANT
              )
      );

      return moduleInfos;
    };
  }
}

