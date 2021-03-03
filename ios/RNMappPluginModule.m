#import "RNMappPluginModule.h"
#import "RNMappEventEmmiter.h"


@implementation RNMappPluginModule


- (void)setBridge:(RCTBridge *)bridge {
    [RNMappEventEmmiter shared].bridge = bridge;
}

- (RCTBridge *)bridge {
    return [RNMappEventEmmiter shared].bridge;
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

#pragma mark Exported methods - Notifications

RCT_EXPORT_METHOD(engage: (NSString *)sdkKey projectId: (NSString *)projectId cepUrl:(NSString *)cepUrl appID:(NSString *)appID tenantID:(NSString *)tenantID) {
    SERVER serv = [self getServerKeyFor:cepUrl];
    [[Appoxee shared] engageWithLaunchOptions:nil andDelegate:[RNMappEventEmmiter shared] andSDKID:sdkKey with: serv];
}

RCT_REMAP_METHOD(autoengage,engage:(NSString *) server) {
    SERVER serv = [self getServerKeyFor:server];
    [[Appoxee shared] engageAndAutoIntegrateWithLaunchOptions:nil andDelegate:[RNMappEventEmmiter shared] with:serv];
    [[Appoxee shared] addObserver: [RNMappEventEmmiter shared] forKeyPath:@"isReady" options:NSKeyValueObservingOptionNew context:nil];
}

RCT_EXPORT_METHOD(getAlias:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] getDeviceAliasWithCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError == nil && data != nil) {
            resolve(data);
        } else {
            reject(@"GET_ALIAS_ERROR", @"Failed to get alias", appoxeeError);
        }
    }];
}

RCT_EXPORT_METHOD(setAlias:(NSString *) alias) {
    [[Appoxee shared] setDeviceAlias:alias withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
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

RCT_EXPORT_METHOD(isReady:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    resolve(@([[Appoxee shared] isReady]));
}

RCT_EXPORT_METHOD(isPushEnabled:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
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
RCT_EXPORT_METHOD(incrementNumericKey: (NSString *) key value: (NSNumber *) number) {
    [[Appoxee shared] incrementNumericKey:key byNumericValue:number withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}
                  
RCT_EXPORT_METHOD(setAttributeString: (NSString *)key value: (NSString *) value)  {
    [[Appoxee shared] setStringValue:value forKey:key withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if(appoxeeError != nil) {
            NSLog(@"%@", appoxeeError.debugDescription);
        }
    }];
}

RCT_EXPORT_METHOD(setAttributeInt: (NSString *)key value: (NSNumber *) value) {
    [[Appoxee shared] setNumberValue:value forKey:key withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
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

RCT_EXPORT_METHOD(getTags:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] fetchDeviceTags:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[NSArray class]]) {
            NSArray *deviceTags = (NSArray *)data;
            resolve(deviceTags);
        } else {
            reject(@"GET_TAGS_FAIL", @"Failed to get tags", appoxeeError);
        }
    }];
}

RCT_EXPORT_METHOD(getDeviceInfo:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
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

RCT_EXPORT_METHOD(getAttributeStringValue: (NSString *) key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[Appoxee shared] fetchCustomFieldByKey:key withCompletionHandler:^(NSError * _Nullable appoxeeError, id  _Nullable data) {
        if (!appoxeeError && [data isKindOfClass:[NSDictionary class]]) {
            NSDictionary *dictionary = (NSDictionary *)data;
            NSString *key = [[dictionary allKeys] firstObject];
            id value = dictionary[key]; // can be of the following types: NSString || NSNumber || NSDate
            if ([value isKindOfClass: [NSString class]]) {
                resolve(value);
            } else if ([value isKindOfClass: [NSNumber class]]) {
                NSString *str = ((NSNumber *)value).stringValue;
                resolve(str);
            } else if ([value isKindOfClass: [NSDate class]]) {
                NSDate *date = (NSDate *)value;
                NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
                [formatter setDateFormat: @"dd MMM yyyy HH:mm"];
                NSString *stringFromDate = [formatter stringFromDate:date];
                resolve(stringFromDate);
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

RCT_EXPORT_METHOD(clearNotification: (NSNumber *) index ){
    [[UNUserNotificationCenter currentNotificationCenter] removeDeliveredNotificationsWithIdentifiers: @[[index stringValue]]];
}

#pragma mark Exported methods - Inapp

RCT_REMAP_METHOD(engageInapp,engageInapp:(NSString *) server) {
    INAPPSERVER serv = [self getInappServerKeyFor:server];
    [[AppoxeeInapp shared] engageWithDelegate:[RNMappEventEmmiter shared] with:serv];
}

RCT_EXPORT_METHOD(fetchInboxMessage: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [[AppoxeeInapp shared] fetchAPXInBoxMessages];
    resolve(@"Fetching, set event listener for iOS");
}

RCT_EXPORT_METHOD(triggerInApp: (NSString *) event) {
    [[AppoxeeInapp shared] reportInteractionEventWithName:event andAttributes:nil];
}

RCT_EXPORT_METHOD(inAppMarkAsRead: (NSNumber *) templateId event: (NSString *) eventId) {
    APXInBoxMessage *message = [[RNMappEventEmmiter shared] getMessageWith:templateId event:eventId];
    if (message) {
        [message markAsRead];
    }
}

RCT_EXPORT_METHOD(inAppMarkAsUnRead: (NSNumber *) templateId event: (NSString *) eventId) {
    APXInBoxMessage *message = [[RNMappEventEmmiter shared] getMessageWith:templateId event:eventId];
    if (message) {
        [message markAsUnread];
    }
}

RCT_EXPORT_METHOD(inAppMarkAsDeleted: (NSNumber *) templateId event: (NSString *) eventId) {
    APXInBoxMessage *message = [[RNMappEventEmmiter shared] getMessageWith:templateId event:eventId];
    if (message) {
        [message markAsDeleted];
    }
}

RCT_EXPORT_METHOD(isDeviceRegistered:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
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
    if ([name containsString:@"L3"]) {
        return L3;
    }
    if ([name containsString:@"EMC"]) {
        return EMC;
    }
    if ([name containsString:@"EMC_US"]) {
        return EMC_US;
    }
    if ([name containsString:@"CROC"]) {
        return CROC;
    }
    if ([name containsString:@"TEST"]) {
        return TEST;
    }
    if ([name containsString:@"TEST55"]) {
        return TEST55;
    }
    return TEST;
}

- (INAPPSERVER) getInappServerKeyFor: (NSString *) name {
    if ([name containsString:@"L3"]) {
        return l3;
    }
    if ([name containsString:@"EMC"]) {
        return eMC;
    }
    if ([name containsString:@"EMC_US"]) {
        return eMC_US;
    }
    if ([name containsString:@"CROC"]) {
        return cROC;
    }
    if ([name containsString:@"TEST"]) {
        return tEST;
    }
    if ([name containsString:@"TEST55"]) {
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

