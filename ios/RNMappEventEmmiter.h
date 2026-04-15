//
//  RNMappEventEmmiter.h
//  react-native-apoxee-plugin
//
//  Created by Miroljub Stoilkovic on 19/02/2021.
//

#import <Foundation/Foundation.h>

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

#if __has_include(<React/RCTEventEmitter.h>)
#import <React/RCTEventEmitter.h>
#else
#import "RNMappReactStubs.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface RNMappEventEmmiter : RCTEventEmitter <AppoxeeInappDelegate, AppoxeeNotificationDelegate, AppoxeeLocationManagerDelegate >

@property (nonatomic, weak) RCTBridge *bridge;

+ (nullable instancetype) shared;
- (APXInBoxMessage *) getMessageWith: (NSNumber *) templateId event: (NSString *) eventId;
- (void)startObserving;
- (void)stopObserving;

@end

NS_ASSUME_NONNULL_END
