#ifndef RNMappAppoxeeStubs_h
#define RNMappAppoxeeStubs_h

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, SERVER) {
    L3 = 0,
    EMC,
    EMC_US,
    CROC,
    TEST,
    TEST55
};

typedef NS_ENUM(NSInteger, INAPPSERVER) {
    l3 = 0,
    eMC,
    eMC_US,
    cROC,
    tEST,
    tEST55
};

@class Appoxee;
@class AppoxeeInapp;
@class AppoxeeLocationManager;
@class APXPushNotification;
@class APXRichMessage;
@class APXInBoxMessage;
@class APXClientDevice;

@protocol AppoxeeInappDelegate <NSObject>
@optional
- (void)appoxeeInapp:(AppoxeeInapp *)appoxeeInapp didReceiveInappMessageWithIdentifier:(NSNumber *)identifier andMessageExtraData:(NSDictionary<NSString *, id> * _Nullable)messageExtraData;
- (void)didReceiveDeepLinkWithIdentifier:(NSNumber *)identifier withMessageString:(NSString *)message andTriggerEvent:(NSString *)triggerEvent;
- (void)didReceiveCustomLinkWithIdentifier:(NSNumber *)identifier withMessageString:(NSString *)message;
- (void)didReceiveInBoxMessages:(NSArray *)messages;
- (void)didReceiveInBoxMessage:(APXInBoxMessage * _Nullable)message;
- (void)inAppCallFailedWithResponse:(NSString * _Nullable)response andError:(NSError * _Nullable)error;
@end

@protocol AppoxeeNotificationDelegate <NSObject>
@optional
- (void)appoxee:(Appoxee *)appoxee handledRemoteNotification:(APXPushNotification *)pushNotification andIdentifer:(NSString *)actionIdentifier;
- (void)appoxee:(Appoxee *)appoxee handledRichContent:(APXRichMessage *)richMessage didLaunchApp:(BOOL)didLaunch;
@end

@protocol AppoxeeLocationManagerDelegate <NSObject>
@optional
- (void)locationManager:(AppoxeeLocationManager *)manager didFailWithError:(NSError * _Nullable)error;
- (void)locationManager:(AppoxeeLocationManager *)manager didEnterGeoRegion:(CLCircularRegion *)geoRegion;
- (void)locationManager:(AppoxeeLocationManager *)manager didExitGeoRegion:(CLCircularRegion *)geoRegion;
@end

@interface Appoxee : NSObject
@property (nonatomic, assign, readonly) BOOL isReady;

+ (instancetype)shared;
- (void)engageWithLaunchOptions:(NSDictionary * _Nullable)launchOptions
                    andDelegate:(id<AppoxeeNotificationDelegate>)delegate
                      andSDKID:(NSString *)sdkID
                           with:(SERVER)server;
- (void)engageAndAutoIntegrateWithLaunchOptions:(NSDictionary * _Nullable)launchOptions
                                    andDelegate:(id<AppoxeeNotificationDelegate>)delegate
                                           with:(SERVER)server;
- (void)addObserver:(NSObject *)observer forKeyPath:(NSString *)keyPath options:(NSKeyValueObservingOptions)options context:(void * _Nullable)context;
- (void)removeObserver:(NSObject *)observer forKeyPath:(NSString *)keyPath;
- (void)getDeviceAliasWithCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)setDeviceAlias:(NSString *)alias withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)setDeviceAlias:(NSString *)alias withResendAttributes:(BOOL)resendAttributes withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)removeDeviceAliasWithCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)logoutWithOptin:(BOOL)pushEnabled;
- (void)isPushEnabled:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)disablePushNotifications:(BOOL)disable withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)setPostponeNotificationRequest:(BOOL)postpone;
- (void)setShowNotificationsOnForeground:(BOOL)value;
- (void)showNotificationAlertDialog;
- (void)incrementNumericKey:(NSString *)key byNumericValue:(NSNumber *)number withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)setCustomAttributtes:(NSDictionary *)attributes withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)getCustomAttributes:(NSArray *)attributes withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)setStringValue:(NSString *)value forKey:(NSString *)key withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)setNumberValue:(NSNumber *)value forKey:(NSString *)key withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)removeTagsFromDevice:(NSArray *)tags withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)addTagsToDevice:(NSArray *)tags withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)fetchDeviceTags:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)deviceInformationwithCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
- (void)fetchCustomFieldByKey:(NSString *)key withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler;
@end

@interface AppoxeeInapp : NSObject
+ (instancetype)shared;
- (void)engageWithDelegate:(id<AppoxeeInappDelegate>)delegate with:(INAPPSERVER)server;
- (void)fetchAPXInBoxMessages;
- (void)reportInteractionEventWithName:(NSString *)event andAttributes:(NSDictionary * _Nullable)attributes;
@end

@interface AppoxeeLocationManager : NSObject
+ (instancetype)shared;
- (void)setDelegate:(id<AppoxeeLocationManagerDelegate>)delegate;
- (void)enableLocationMonitoring;
- (void)disableLocationMonitoring;
@end

@interface APXPushAction : NSObject
@property (nonatomic, copy, nullable) NSString *categoryName;
@end

@interface APXPushNotification : NSObject
@property (nonatomic, copy, nullable) NSString *title;
@property (nonatomic, copy, nullable) NSString *alert;
@property (nonatomic, copy, nullable) NSString *body;
@property (nonatomic, assign) NSInteger uniqueID;
@property (nonatomic, assign) NSInteger badge;
@property (nonatomic, copy, nullable) NSString *subtitle;
@property (nonatomic, strong, nullable) NSDictionary *extraFields;
@property (nonatomic, assign) BOOL isRich;
@property (nonatomic, assign) BOOL isSilent;
@property (nonatomic, assign) BOOL isTriggerUpdate;
@property (nonatomic, strong, nullable) APXPushAction *pushAction;
@end

@interface APXRichMessage : NSObject
@property (nonatomic, assign) NSInteger uniqueID;
@property (nonatomic, copy, nullable) NSString *title;
@property (nonatomic, copy, nullable) NSString *content;
@property (nonatomic, copy, nullable) NSString *messageLink;
@property (nonatomic, strong, nullable) NSDate *postDate;
@end

@interface APXInBoxMessage : NSObject
@property (nonatomic, copy, nullable) NSString *messageId;
- (NSDictionary * _Nullable)getDictionary;
- (void)markAsRead;
- (void)markAsUnread;
- (void)markAsDeleted;
@end

@interface APXClientDevice : NSObject
@property (nonatomic, copy, nullable) NSString *udid;
@property (nonatomic, copy, nullable) NSString *sdkVersion;
@property (nonatomic, copy, nullable) NSString *locale;
@property (nonatomic, copy, nullable) NSString *timeZone;
@property (nonatomic, copy, nullable) NSString *hardwearType;
@property (nonatomic, copy, nullable) NSString *osVersion;
@property (nonatomic, copy, nullable) NSString *osName;
@end

NS_ASSUME_NONNULL_END

#endif
