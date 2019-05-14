
package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RNMappPluginModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private Map<Callback, String> mFeedSubscriberMap = new ConcurrentHashMap<>();
  private Map<Callback, Boolean> mCallbackWasCalledMap = new ConcurrentHashMap<>();
  public RNMappPluginModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNMappPlugin";
  }


  private void reportResultWithCallback(Callback callback, String error, Object result) {
    if (callback != null) {
      if (error != null) {
        callback.invoke(error);
      } else {
        callback.invoke(null, result);
      }
    }
  }
}