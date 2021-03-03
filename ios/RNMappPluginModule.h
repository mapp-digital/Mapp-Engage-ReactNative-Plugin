#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <AppoxeeSDK/AppoxeeSDK.h>
#import <AppoxeeInapp/AppoxeeInapp.h>
#import <AppoxeeLocationServices/AppoxeeLocationManager.h>
#import <UserNotifications/UNUserNotificationCenter.h>

@interface RNMappPluginModule : NSObject <RCTBridgeModule,AppoxeeInappDelegate, AppoxeeNotificationDelegate, AppoxeeLocationManagerDelegate >

@end
