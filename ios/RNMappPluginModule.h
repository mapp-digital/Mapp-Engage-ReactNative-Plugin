#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "AppoxeeSDK.h"
#import "AppoxeeInapp.h"
#import "AppoxeeLocationManager.h"
#import <UserNotifications/UNUserNotificationCenter.h>
#if RCT_NEW_ARCH_ENABLED
#import <RNMappPlugin/RNMappPlugin.h>
#endif

@interface RNMappPluginModule : NSObject <RCTBridgeModule,AppoxeeInappDelegate, AppoxeeNotificationDelegate, AppoxeeLocationManagerDelegate
#if RCT_NEW_ARCH_ENABLED
, NativeRNMappPluginModuleSpec
#endif
>

@end
