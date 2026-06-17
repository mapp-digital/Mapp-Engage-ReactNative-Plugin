package com.reactlibrary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.BaseReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;

import java.util.HashMap;
import java.util.Map;

public class RNMappPluginPackage extends BaseReactPackage {

  @Override
  @Nullable
  public NativeModule getModule(@NonNull String name, @NonNull ReactApplicationContext reactContext) {
    if (RNMappPluginModule.NAME.equals(name)) {
      return new RNMappPluginModule(reactContext);
    }
    return null;
  }

  @NonNull
  @Override
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
                      false, // isCxxModule
                      true   // isTurboModule -> THIS IS IMPORTANT
              )
      );

      return moduleInfos;
    };
  }
}
