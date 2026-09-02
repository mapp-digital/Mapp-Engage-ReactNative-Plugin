#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "AppoxeeSDK.h"
#import "AppoxeeInapp.h"
#import "AppoxeeLocationManager.h"
#import <UserNotifications/UNUserNotificationCenter.h>
#if RCT_NEW_ARCH_ENABLED
#import <ReactCommon/RCTTurboModule.h>
#if __has_include(<RNMappPluginSpec/RNMappPluginSpec.h>)
#import <RNMappPluginSpec/RNMappPluginSpec.h>
#define RNMAPP_HAS_TURBO_MODULE_SPEC 1
#elif __has_include(<RNMappPlugin/RNMappPlugin.h>)
#import <RNMappPlugin/RNMappPlugin.h>
#define RNMAPP_HAS_TURBO_MODULE_SPEC 1
#elif __has_include("RNMappPlugin.h")
#import "RNMappPlugin.h"
#define RNMAPP_HAS_TURBO_MODULE_SPEC 1
#else
#define RNMAPP_HAS_TURBO_MODULE_SPEC 0
#endif
#endif

@interface RNMappPluginModule : NSObject <RCTBridgeModule,AppoxeeInappDelegate, AppoxeeNotificationDelegate, AppoxeeLocationManagerDelegate
#if RCT_NEW_ARCH_ENABLED && RNMAPP_HAS_TURBO_MODULE_SPEC
, NativeRNMappPluginModuleSpec
#endif
>

@end
