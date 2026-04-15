#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#else
#import "RNMappReactStubs.h"
#endif

#if __has_include(<AppoxeeSDK/AppoxeeSDK.h>)
#import <AppoxeeSDK/AppoxeeSDK.h>
#elif __has_include("AppoxeeSDK.h")
#import "AppoxeeSDK.h"
#else
#import "RNMappAppoxeeStubs.h"
#endif

#if __has_include(<AppoxeeInapp/AppoxeeInapp.h>)
#import <AppoxeeInapp/AppoxeeInapp.h>
#elif __has_include("AppoxeeInapp.h")
#import "AppoxeeInapp.h"
#else
#import "RNMappAppoxeeStubs.h"
#endif

#if __has_include(<AppoxeeLocationServices/AppoxeeLocationManager.h>)
#import <AppoxeeLocationServices/AppoxeeLocationManager.h>
#elif __has_include("AppoxeeLocationManager.h")
#import "AppoxeeLocationManager.h"
#else
#import "RNMappAppoxeeStubs.h"
#endif

#import <UserNotifications/UNUserNotificationCenter.h>

@interface RNMappPluginModule : NSObject <RCTBridgeModule,AppoxeeInappDelegate, AppoxeeNotificationDelegate, AppoxeeLocationManagerDelegate >

- (void)addListener:(NSString *)eventName;
- (void)removeListeners:(NSInteger)count;

- (void)engage:(NSString *)sdkKey projectId:(NSString *)projectId cepUrl:(NSString *)cepUrl appID:(NSString *)appID tenantID:(NSString *)tenantID;
- (void)engage:(NSString *)server;

- (void)getAlias:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)setAlias:(NSString *)alias;
- (void)setAliasWithResend:(NSString *)alias withResendAttributes:(BOOL)resendAttributes;
- (void)setToken:(NSString *)token;
- (void)removeDeviceAlias;
- (void)logOut:(BOOL)pushEnabled;
- (void)isReady:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)isPushEnabled:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)setPushEnabled:(BOOL)enabled;
- (void)setPostponeNotificationRequest:(BOOL)postpone;
- (void)setShowNotificationsAtForeground:(BOOL)value;
- (void)showNotificationAlertView;
- (void)incrementNumericKey:(NSString *)key value:(NSNumber *)number;
- (void)setAttributes:(NSDictionary *)attributes;
- (void)getAttributes:(NSArray *)attributes and:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)setAttribute:(NSString *)key value:(NSString *)value;
- (void)setAttributeInt:(NSString *)key value:(NSNumber *)value;
- (void)removeTag:(NSString *)tag;
- (void)addTag:(NSString *)tag;
- (void)getTags:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)getDeviceInfo:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)getAttributeStringValue:(NSString *)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)removeBadgeNumber;
- (void)clearNotifications;
- (void)clearNotification:(NSNumber *)index;

- (void)engageInapp:(NSString *)server;
- (void)fetchInboxMessage:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)fetchLatestInboxMessage:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;
- (void)triggerInApp:(NSString *)event;
- (void)inAppMarkAsRead:(NSNumber *)templateId event:(NSString *)eventId;
- (void)inAppMarkAsUnRead:(NSNumber *)templateId event:(NSString *)eventId;
- (void)inAppMarkAsDeleted:(NSNumber *)templateId event:(NSString *)eventId;
- (void)isDeviceRegistered:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

- (void)startGeoFencing;
- (void)stopGeoFencing;

@end
