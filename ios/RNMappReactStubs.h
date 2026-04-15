#ifndef RNMappReactStubs_h
#define RNMappReactStubs_h

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol RCTBridgeModule <NSObject>
@optional
+ (BOOL)requiresMainQueueSetup;
@end

@interface RCTBridge : NSObject
@end

@interface RCTCallableJSModules : NSObject
@end

@interface RCTEventEmitter : NSObject
@property (nonatomic, weak) RCTBridge *bridge;
@property (nonatomic, strong) RCTCallableJSModules *callableJSModules;

- (NSArray<NSString *> *)supportedEvents;
- (void)sendEventWithName:(NSString *)name body:(id)body;
- (void)addListener:(NSString *)eventName;
- (void)removeListeners:(NSInteger)count;
@end

typedef void (^RCTPromiseResolveBlock)(id _Nullable result);
typedef void (^RCTPromiseRejectBlock)(NSString *code, NSString *message, NSError * _Nullable error);

#define RCT_EXPORT_MODULE(...)
#define RCT_EXPORT_METHOD(method) - (void)method
#define RCT_REMAP_METHOD(js_name, method) - (void)method

NS_ASSUME_NONNULL_END

#endif
