// @flow
'use strict';

import {
  NativeModules,
  NativeEventEmitter,
  Platform
} from 'react-native';

const RNMappPluginModule = NativeModules.RNMappPluginModule;

class MappEventEmitter extends NativeEventEmitter {

  constructor() {
    super(RNMappPluginModule);
  }

  addListener(eventType: string, listener: Function, context: ?Object): EmitterSubscription {
    if (Platform.OS === 'android') {
        RNMappPluginModule.addAndroidListener(eventType);
    }
    return super.addListener(eventType, listener, context);
  }

  removeAllListeners(eventType: string) {
    if (Platform.OS === 'android') {
      const count = this.listeners(eventType).length;
        RNMappPluginModule.removeAndroidListeners(count);
    }

    super.removeAllListeners(eventType);
  }

  removeSubscription(subscription: EmitterSubscription) {
    if (Platform.OS === 'android') {
        RNMappPluginModule.removeAndroidListeners(1);
    }
    super.removeSubscription(subscription);
  }
}

module.exports = MappEventEmitter;
