#import "RNMappPluginModule.h"
#import "RNMappEventEmmiter.h"
#if RCT_NEW_ARCH_ENABLED
#import <ReactCommon/RCTTurboModule.h>
#endif


@implementation RNMappPluginModule

#if RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNMappPluginModuleSpecJSI>(params);
}
#endif


- (void)setBridge:(RCTBridge *)bridge {
    [RNMappEventEmmiter shared].bridge = bridge;
}

- (RCTBridge *)bridge {
    return [RNMappEventEmmiter shared].bridge;
}

- (void)setCallableJSModules:(RCTCallableJSModules *)callableJSModules {
    [RNMappEventEmmiter shared].callableJSModules = callableJSModules;
}

- (RCTCallableJSModules *)callableJSModules {
    return [RNMappEventEmmiter shared].callableJSModules;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(addListener:(NSString *)eventName) {
    [[RNMappEventEmmiter shared] addListener:eventName];
}

RCT_EXPORT_METHOD(removeListeners:(NSInteger)count) {
    [[RNMappEventEmmiter shared] removeListeners:count];
}

// Cross-platform TurboModule methods that need iOS-specific behavior or have no
// meaningful iOS equivalent. Keeping them here makes the generated spec safe to
// invoke under the New Architecture instead of relying on legacy interop.
RCT_EXPORT_METHOD(requestGeofenceLocationPermission:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if (status == kCLAuthorizationStatusNotDetermined) {
        [[[CLLocationManager alloc] init] requestAlwaysAuthorization];
    }
    resolve(@(status == kCLAuthorizationStatusAuthorizedAlways));
}

RCT_EXPORT_METHOD(requestPostNotificationPermission:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[UNUserNotificationCenter currentNotificationCenter]
        requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound)
        completionHandler:^(BOOL granted, NSError *error) {
            if (error) reject(@"NOTIFICATION_PERMISSION_ERROR", @"Unable to request notification permission", error);
            else resolve(@(granted));
        }];
}

RCT_EXPORT_METHOD(setRemoteMessage:(NSString *)msgJson resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    // iOS delivery is handled by APNs/Appoxee auto-integration, not FCM RemoteMessage JSON.
    resolve(@NO);
}

RCT_EXPORT_METHOD(isPushFromMapp:(NSString *)msgJson resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    resolve(@NO);
}

RCT_EXPORT_METHOD(getToken:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    reject(@"APNS_TOKEN_UNAVAILABLE", @"Mapp auto-integration owns the native APNs token on iOS", nil);
}

RCT_EXPORT_METHOD(engage2) {}

RCT_EXPORT_METHOD(engageTestServer:(NSString *)cepUrl sdkKey:(NSString *)sdkKey googleProjectId:(NSString *)projectId server:(NSString *)server appID:(NSString *)appID tenantID:(NSString *)tenantID) {
    [self engage:sdkKey googleProjectId:projectId server:server appID:appID tenantID:tenantID];
}

RCT_EXPORT_METHOD(onInitCompletedListener:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    resolve(@([[Appoxee shared] isReady]));
}

RCT_EXPORT_METHOD(setAttributeBoolean:(NSString *)key value:(BOOL)value) {
    [[Appoxee shared] setNumberValue:@(value) forKey:key withCompletionHandler:nil];
}

RCT_EXPORT_METHOD(removeAttribute:(NSString *)attribute) {
    // The vendored iOS SDK has no single-field removal API.
}

RCT_EXPORT_METHOD(getDeviceDmcInfo:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self getDeviceInfo:resolve reject:reject];
}

RCT_EXPORT_METHOD(lockScreenOrientation:(NSInteger)orientation) {}

RCT_EXPORT_METHOD(startGeofencing:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[AppoxeeLocationManager shared] enableLocationMonitoring];
    resolve(@"started");
}

RCT_EXPORT_METHOD(stopGeofencing:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[AppoxeeLocationManager shared] disableLocationMonitoring];
    resolve(@"stopped");
}

RCT_EXPORT_METHOD(triggerStatistic:(NSInteger)templateId originalEventId:(NSString *)originalEventId trackingKey:(NSString *)trackingKey displayMillis:(NSInteger)displayMillis reason:(NSString *)reason link:(NSString *)link) {}
RCT_EXPORT_METHOD(addAndroidListener:(NSString *)eventName) {}
RCT_EXPORT_METHOD(removeAndroidListeners:(NSInteger)count) {}

#pragma mark Exported methods - Notifications

RCT_EXPORT_METHOD(engage: (NSString *)sdkKey googleProjectId: (NSString *)projectId server:(NSString *)server appID:(NSString *)appID tenantID:(NSString *)tenantID) {
    SERVER serv = [self getServerKeyFor:server];
    [[Appoxee shared] engageAndAutoIntegrateWithLaunchOptions:nil andDelegate:[RNMappEventEmmiter shared] with:serv];
    [[Appoxee shared] addObserver: [RNMappEventEmmiter shared] forKeyPath:@"isReady" options:NSKeyValueObservingOptionNew context:nil];
    [[AppoxeeInapp shared] engageWithDelegate:[RNMappEventEmmiter shared] with:[self getInappServerKeyFor:server]];
}

RCT_EXPORT_METHOD(getAlias:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] getDeviceAliasWithCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError == nil && data != nil) {
            resolve(data);
        } else {
            reject(@"GET_ALIAS_ERROR", @"Failed to get alias", appoxeeError);
        }
    }];
}

RCT_EXPORT_METHOD(setAlias:(NSString *) alias resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] setDeviceAlias:alias withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError != nil) {
            reject(@"SET_ALIAS_ERROR", @"Failed to set alias", appoxeeError);
        } else {
            resolve(@YES);
        }
    }];
}

RCT_EXPORT_METHOD(setAliasWithResend:(NSString *) alias resendCustomAttributes:(BOOL) resendAttributes resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] setDeviceAlias:alias withResendAttributes:resendAttributes withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError != nil) {
            reject(@"SET_ALIAS_ERROR", @"Failed to set alias", appoxeeError);
        } else {
            resolve(@YES);
        }
    }];
}

RCT_EXPORT_METHOD(setToken:(NSString *) token resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    NSData *deviceToken = [[NSData alloc] initWithBase64EncodedString:token options:0];
    if (!deviceToken) {
        reject(@"INVALID_APNS_TOKEN", @"setToken expects a base64-encoded native APNs device token, not an Expo push token", nil);
        return;
    }
    [[Appoxee shared] didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
    resolve(@YES);
}

RCT_EXPORT_METHOD(removeDeviceAlias) {
    [[Appoxee shared] removeDeviceAliasWithCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
            if (appoxeeError != nil) {
                NSLog(@"%@", appoxeeError.debugDescription);
            }
    }];
}

RCT_EXPORT_METHOD(logOut: (BOOL) pushEnabled) {
    [[Appoxee shared] logoutWithOptin:pushEnabled];
}

RCT_EXPORT_METHOD(isReady:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    resolve(@([[Appoxee shared] isReady]));
}

RCT_EXPORT_METHOD(isPushEnabled:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] isPushEnabled:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError == nil) {
            resolve(data);
        } else {
            reject(@"PUSH_STATUS_ERROR", @"Failed to get push status", appoxeeError);
        }
    }];
}

RCT_EXPORT_METHOD(setPushEnabled: (BOOL) enabled) {
    [[Appoxee shared] disablePushNotifications: !enabled withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}

RCT_EXPORT_METHOD(setPostponeNotificationRequest: (BOOL) postpone) {
    [[Appoxee shared] setPostponeNotificationRequest:postpone];
}

RCT_EXPORT_METHOD(setShowNotificationsAtForeground: (BOOL) value) {
    [[Appoxee shared] setShowNotificationsOnForeground:value];
}

RCT_EXPORT_METHOD(showNotificationAlertView) {
    [[Appoxee shared] showNotificationAlertDialog];
}

RCT_EXPORT_METHOD(incrementNumericKey: (NSString *) key value: (NSNumber *) number) {
    [[Appoxee shared] incrementNumericKey:key byNumericValue:number withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}

RCT_EXPORT_METHOD(setAttributes: (NSDictionary *)attributes resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)  {
    [[Appoxee shared] setCustomAttributtes:attributes withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError) {
            reject(@"SET_ATTRIBUTES_ERROR", @"Failed to set attributes", appoxeeError);
        } else {
            resolve(@YES);
        }
    }];
}

RCT_EXPORT_METHOD(getAttributes: (NSArray *)attributes resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)  {
    [[Appoxee shared] getCustomAttributes:attributes withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
        resolve((NSDictionary*)data[@"get"]);
    }];
}
                  
RCT_EXPORT_METHOD(setAttribute: (NSString *)key value: (NSString *) value)  {
    [[Appoxee shared] setStringValue:value forKey:key withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if(appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}

RCT_EXPORT_METHOD(setAttributeInt: (NSString *)key value: (NSInteger) value) {
    [[Appoxee shared] setNumberValue:@(value) forKey:key withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if(appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}

RCT_EXPORT_METHOD(removeTag: (NSString *) tag) {
    [[Appoxee shared] removeTagsFromDevice: @[tag] withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if(appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}

RCT_EXPORT_METHOD(addTag: (NSString *) tag) {
    [[Appoxee shared] addTagsToDevice:@[tag] withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if(appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}

RCT_EXPORT_METHOD(getTags:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] fetchDeviceTags:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[NSArray class]]) {
            NSArray *deviceTags = (NSArray *)data;
            resolve(deviceTags);
        } else {
            reject(@"GET_TAGS_FAIL", @"Failed to get tags", appoxeeError);
        }
    }];
}

RCT_EXPORT_METHOD(getDeviceInfo:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] deviceInformationwithCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[APXClientDevice class]]) {
            APXClientDevice *device = (APXClientDevice *)data;
            NSDictionary *deviceData = [self deviceInfo:device];
            resolve(deviceData);
        } else {
            reject(@"GET_DEVICE_INFO_ERROR", @"Failed to get device information", appoxeeError);
        }
    }];
}

RCT_EXPORT_METHOD(getAttributeStringValue: (NSString *) key resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] fetchCustomFieldByKey:key withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        NSLog(@"%@", data);
        if (!appoxeeError && [data isKindOfClass:[NSDictionary class]]) {
            NSDictionary *dictionary = (NSDictionary *)data;
            NSLog(@"%@", dictionary);
            NSString *key = [[dictionary allKeys] firstObject];
            id value = dictionary[key]; // can be of the following types: NSString || NSNumber || NSDate
            NSLog(@"%@", value);
            if ([value isKindOfClass: [NSString class]]) {
                NSLog(@"value is string %@", value);
                resolve(value);
            } else if ([value isKindOfClass: [NSNumber class]]) {
                NSLog(@"value is number %@", value);
                NSString *str = ((NSNumber *)value).stringValue;
                resolve(str);
            } else if ([value isKindOfClass: [NSDate class]]) {
                NSLog(@"value is date %@", value);
                NSDate *date = (NSDate *)value;
                NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
                [formatter setDateFormat: @"dd MMM yyyy HH:mm"];
                NSString *stringFromDate = [formatter stringFromDate:date];
                resolve(stringFromDate);
            } else {
                NSLog(@"value is non of types!");
            }
        } else {
            reject(@"GET_ATTRIBUTE_FAIL", @"Failed to get atribute string value", appoxeeError);
        }
        
    }];
}

RCT_EXPORT_METHOD(removeBadgeNumber) {
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber: 0];
}

RCT_EXPORT_METHOD(clearNotifications) {
    [[UNUserNotificationCenter currentNotificationCenter] removeAllDeliveredNotifications];
}

RCT_EXPORT_METHOD(clearNotification: (NSInteger) index ){
    [[UNUserNotificationCenter currentNotificationCenter] removeDeliveredNotificationsWithIdentifiers: @[[NSString stringWithFormat:@"%ld", (long)index]]];
}

#pragma mark Exported methods - Inapp

RCT_EXPORT_METHOD(fetchInboxMessage: (RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[AppoxeeInapp shared] fetchAPXInBoxMessages];
    resolve(@"Fetching, set event listener for iOS");
}

RCT_EXPORT_METHOD(fetchLatestInboxMessage: (RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[AppoxeeInapp shared] fetchAPXInBoxMessages];
    resolve(@"Fetching, set event listener for iOS");
}

RCT_EXPORT_METHOD(triggerInApp: (NSString *) event) {
    [[AppoxeeInapp shared] reportInteractionEventWithName:event andAttributes:nil];
}

RCT_EXPORT_METHOD(inAppMarkAsRead: (NSInteger) templateId eventId: (NSString * _Nonnull) eventId) {
    APXInBoxMessage *message = [[RNMappEventEmmiter shared] getMessageWith:@(templateId) event:eventId];
    if (message) {
        [message markAsRead];
    }
}

RCT_EXPORT_METHOD(inAppMarkAsUnRead: (NSInteger) templateId eventId: (NSString * _Nonnull) eventId) {
    APXInBoxMessage *message = [[RNMappEventEmmiter shared] getMessageWith:@(templateId) event:eventId];
    if (message) {
        [message markAsUnread];
    }
}

RCT_EXPORT_METHOD(inAppMarkAsDeleted: (NSInteger) templateId eventId: (NSString * _Nonnull) eventId) {
    APXInBoxMessage *message = [[RNMappEventEmmiter shared] getMessageWith:@(templateId) event:eventId];
    if (message) {
        [message markAsDeleted];
    }
}

RCT_EXPORT_METHOD(isDeviceRegistered:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    resolve(@([[Appoxee shared] isReady]));
}

#pragma mark Exported methods - Location Manager

RCT_EXPORT_METHOD(startGeoFencing) {
    [[AppoxeeLocationManager shared] enableLocationMonitoring];
}

RCT_EXPORT_METHOD(stopGeoFencing) {
    [[AppoxeeLocationManager shared] disableLocationMonitoring];
}

#pragma mark Helpers


- (SERVER)getServerKeyFor: (NSString *) name {
    if ([name isEqualToString:@"L3"]) {
        return L3;
    }
    if ([name isEqualToString:@"EMC"]) {
        return EMC;
    }
    if ([name isEqualToString:@"EMC_US"]) {
        return EMC_US;
    }
    if ([name isEqualToString:@"CROC"]) {
        return CROC;
    }
    if ([name isEqualToString:@"TEST"]) {
        return TEST;
    }
    if ([name isEqualToString:@"TEST55"]) {
        return TEST55;
    }
    return TEST;
}

- (INAPPSERVER) getInappServerKeyFor: (NSString *) name {
    if ([name isEqualToString:@"L3"]) {
        return l3;
    }
    if ([name isEqualToString:@"EMC"]) {
        return eMC;
    }
    if ([name isEqualToString:@"EMC_US"]) {
        return eMC_US;
    }
    if ([name isEqualToString:@"CROC"]) {
        return cROC;
    }
    if ([name isEqualToString:@"TEST"]) {
        return tEST;
    }
    if ([name isEqualToString:@"TEST55"]) {
        return tEST55;
    }
    return tEST;
}

- (NSDictionary *) deviceInfo: (APXClientDevice *) device {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject: device.udid forKey:@"udid"];
    [dict setObject: device.sdkVersion forKey:@"sdkVersion"];
    [dict setObject:device.locale forKey:@"locale"];
    [dict setObject:device.timeZone forKey:@"timezone"];
    [dict setObject:device.hardwearType forKey:@"deviceModel"];
    [dict setObject:device.osVersion forKey:@"osVersion"];
    [dict setObject:device.osName forKey:@"osName"];
    return dict;
}

- (NSDictionary *) inboxMessage: (APXInBoxMessage *) message {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    return dict;
}


@end
