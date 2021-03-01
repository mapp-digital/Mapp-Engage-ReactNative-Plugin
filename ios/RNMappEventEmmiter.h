//
//  RNMappEventEmmiter.h
//  react-native-apoxee-plugin
//
//  Created by Miroljub Stoilkovic on 19/02/2021.
//

#import <Foundation/Foundation.h>
#import <AppoxeeSDK/AppoxeeSDK.h>
#import <AppoxeeInapp/AppoxeeInapp.h>
#import <AppoxeeLocationServices/AppoxeeLocationManager.h>
#import <React/RCTEventEmitter.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNMappEventEmmiter : RCTEventEmitter <AppoxeeInappDelegate, AppoxeeNotificationDelegate, AppoxeeLocationManagerDelegate >

//@property (nonatomic, weak) RCTBridge *bridge;

+ (nullable instancetype) shared;
- (APXInBoxMessage *) getMessageWith: (NSNumber *) templateId event: (NSString *) eventId;

@end

NS_ASSUME_NONNULL_END
