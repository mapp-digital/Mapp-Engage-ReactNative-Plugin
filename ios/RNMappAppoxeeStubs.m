#import "RNMappAppoxeeStubs.h"

@implementation Appoxee

+ (instancetype)shared {
    return [[self alloc] init];
}

- (void)engageWithLaunchOptions:(NSDictionary * _Nullable)launchOptions
                    andDelegate:(id<AppoxeeNotificationDelegate>)delegate
                      andSDKID:(NSString *)sdkID
                           with:(SERVER)server {
}

- (void)engageAndAutoIntegrateWithLaunchOptions:(NSDictionary * _Nullable)launchOptions
                                    andDelegate:(id<AppoxeeNotificationDelegate>)delegate
                                           with:(SERVER)server {
}

- (void)addObserver:(NSObject *)observer forKeyPath:(NSString *)keyPath options:(NSKeyValueObservingOptions)options context:(void * _Nullable)context {
}

- (void)removeObserver:(NSObject *)observer forKeyPath:(NSString *)keyPath {
}

- (void)getDeviceAliasWithCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)setDeviceAlias:(NSString *)alias withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)setDeviceAlias:(NSString *)alias withResendAttributes:(BOOL)resendAttributes withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
}

- (void)removeDeviceAliasWithCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)logoutWithOptin:(BOOL)pushEnabled {
}

- (void)isPushEnabled:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)disablePushNotifications:(BOOL)disable withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)setPostponeNotificationRequest:(BOOL)postpone {
}

- (void)setShowNotificationsOnForeground:(BOOL)value {
}

- (void)showNotificationAlertDialog {
}

- (void)incrementNumericKey:(NSString *)key byNumericValue:(NSNumber *)number withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)setCustomAttributtes:(NSDictionary *)attributes withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)getCustomAttributes:(NSArray *)attributes withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)setStringValue:(NSString *)value forKey:(NSString *)key withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)setNumberValue:(NSNumber *)value forKey:(NSString *)key withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)removeTagsFromDevice:(NSArray *)tags withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)addTagsToDevice:(NSArray *)tags withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)fetchDeviceTags:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)deviceInformationwithCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

- (void)fetchCustomFieldByKey:(NSString *)key withCompletionHandler:(void (^)(NSError * _Nullable appoxeeError, id _Nullable data))completionHandler {
}

@end

@implementation AppoxeeInapp

+ (instancetype)shared {
    return [[self alloc] init];
}

- (void)engageWithDelegate:(id<AppoxeeInappDelegate>)delegate with:(INAPPSERVER)server {
}

- (void)fetchAPXInBoxMessages {
}

- (void)reportInteractionEventWithName:(NSString *)event andAttributes:(NSDictionary * _Nullable)attributes {
}

@end

@implementation AppoxeeLocationManager

+ (instancetype)shared {
    return [[self alloc] init];
}

- (void)setDelegate:(id<AppoxeeLocationManagerDelegate>)delegate {
}

- (void)enableLocationMonitoring {
}

- (void)disableLocationMonitoring {
}

@end

@implementation APXPushAction
@end

@implementation APXPushNotification
@end

@implementation APXRichMessage
@end

@implementation APXInBoxMessage

- (NSDictionary * _Nullable)getDictionary {
    return nil;
}

- (void)markAsRead {
}

- (void)markAsUnread {
}

- (void)markAsDeleted {
}

@end

@implementation APXClientDevice
@end
