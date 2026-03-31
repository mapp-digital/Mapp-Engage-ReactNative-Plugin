#import "RNMappReactStubs.h"

@implementation RCTBridge
@end

@implementation RCTCallableJSModules
@end

@implementation RCTEventEmitter

- (NSArray<NSString *> *)supportedEvents {
    return @[];
}

- (void)sendEventWithName:(NSString *)name body:(id)body {
}

- (void)addListener:(NSString *)eventName {
}

- (void)removeListeners:(NSInteger)count {
}

@end
