#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "AppoxeeSDK.h"
#import "AppoxeeInapp.h"
#import "AppoxeeLocationManager.h"
#import <UserNotifications/UNUserNotificationCenter.h>

@interface RNMappPluginModule : NSObject <RCTBridgeModule,AppoxeeInappDelegate, AppoxeeNotificationDelegate, AppoxeeLocationManagerDelegate >

@end
