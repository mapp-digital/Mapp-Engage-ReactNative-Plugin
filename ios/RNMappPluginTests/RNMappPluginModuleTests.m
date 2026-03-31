/**
 * RNMappPluginModuleTests.m
 *
 * Unit tests for RNMappPluginModule.
 *
 * Uses OCMock to mock the Appoxee, AppoxeeInapp, and AppoxeeLocationManager
 * singletons so that no real SDK calls are made. Each test verifies:
 *   - the correct SDK method is called
 *   - the correct parameters are forwarded
 *   - Promise resolve/reject is called with the correct value
 *
 * Setup: see ios/RNMappPluginTests/README.md
 */

#import <XCTest/XCTest.h>
#import <OCMock/OCMock.h>

#import "RNMappPluginModule.h"
#import "RNMappEventEmmiter.h"

// ── Expose private helpers for white-box testing ─────────────────────────────
@interface RNMappPluginModule (Testing)
- (SERVER)getServerKeyFor:(NSString *)name;
- (INAPPSERVER)getInappServerKeyFor:(NSString *)name;
- (NSDictionary *)deviceInfo:(APXClientDevice *)device;
@end
// ─────────────────────────────────────────────────────────────────────────────


@interface RNMappPluginModuleTests : XCTestCase

@property (nonatomic, strong) RNMappPluginModule *module;

// Class mocks intercept [Appoxee shared], [AppoxeeInapp shared], etc.
@property (nonatomic, strong) id mockAppoxee;
@property (nonatomic, strong) id mockAppoxeeInapp;
@property (nonatomic, strong) id mockLocationManager;
@property (nonatomic, strong) id mockEventEmitter;

@end


@implementation RNMappPluginModuleTests

- (void)setUp {
    [super setUp];

    self.module = [[RNMappPluginModule alloc] init];

    // Stub [Appoxee shared] to return a mock instance
    self.mockAppoxee = OCMClassMock([Appoxee class]);
    OCMStub([self.mockAppoxee shared]).andReturn(self.mockAppoxee);

    // Stub [AppoxeeInapp shared]
    self.mockAppoxeeInapp = OCMClassMock([AppoxeeInapp class]);
    OCMStub([self.mockAppoxeeInapp shared]).andReturn(self.mockAppoxeeInapp);

    // Stub [AppoxeeLocationManager shared]
    self.mockLocationManager = OCMClassMock([AppoxeeLocationManager class]);
    OCMStub([self.mockLocationManager shared]).andReturn(self.mockLocationManager);

    // Stub [RNMappEventEmmiter shared] — needed by engage/autoengage
    self.mockEventEmitter = OCMClassMock([RNMappEventEmmiter class]);
    OCMStub([self.mockEventEmitter shared]).andReturn(self.mockEventEmitter);
}

- (void)tearDown {
    [self.mockAppoxee stopMocking];
    [self.mockAppoxeeInapp stopMocking];
    [self.mockLocationManager stopMocking];
    [self.mockEventEmitter stopMocking];
    [super tearDown];
}

// =============================================================================
// getServerKeyFor: — pure-logic, no mocks needed
// =============================================================================

- (void)test_getServerKeyFor_L3_returnsL3 {
    XCTAssertEqual([self.module getServerKeyFor:@"L3"], L3);
}

- (void)test_getServerKeyFor_EMC_returnsEMC {
    XCTAssertEqual([self.module getServerKeyFor:@"EMC"], EMC);
}

- (void)test_getServerKeyFor_EMC_US_returnsEMC_US {
    XCTAssertEqual([self.module getServerKeyFor:@"EMC_US"], EMC_US);
}

- (void)test_getServerKeyFor_CROC_returnsCROC {
    XCTAssertEqual([self.module getServerKeyFor:@"CROC"], CROC);
}

- (void)test_getServerKeyFor_TEST_returnsTEST {
    XCTAssertEqual([self.module getServerKeyFor:@"TEST"], TEST);
}

- (void)test_getServerKeyFor_TEST55_returnsTEST55 {
    XCTAssertEqual([self.module getServerKeyFor:@"TEST55"], TEST55);
}

- (void)test_getServerKeyFor_unknownString_defaultsToTEST {
    // Unknown server names fall through to the default TEST
    XCTAssertEqual([self.module getServerKeyFor:@"BOGUS"], TEST);
}

- (void)test_getServerKeyFor_emptyString_defaultsToTEST {
    XCTAssertEqual([self.module getServerKeyFor:@""], TEST);
}

// =============================================================================
// getInappServerKeyFor: — pure-logic, no mocks needed
// =============================================================================

- (void)test_getInappServerKeyFor_L3_returnsl3 {
    XCTAssertEqual([self.module getInappServerKeyFor:@"L3"], l3);
}

- (void)test_getInappServerKeyFor_EMC_returnseMC {
    XCTAssertEqual([self.module getInappServerKeyFor:@"EMC"], eMC);
}

- (void)test_getInappServerKeyFor_EMC_US_returnseMC_US {
    XCTAssertEqual([self.module getInappServerKeyFor:@"EMC_US"], eMC_US);
}

- (void)test_getInappServerKeyFor_CROC_returnscROC {
    XCTAssertEqual([self.module getInappServerKeyFor:@"CROC"], cROC);
}

- (void)test_getInappServerKeyFor_TEST_returnstEST {
    XCTAssertEqual([self.module getInappServerKeyFor:@"TEST"], tEST);
}

- (void)test_getInappServerKeyFor_TEST55_returnstEST55 {
    XCTAssertEqual([self.module getInappServerKeyFor:@"TEST55"], tEST55);
}

- (void)test_getInappServerKeyFor_unknownString_defaultsToTEST {
    XCTAssertEqual([self.module getInappServerKeyFor:@"BOGUS"], tEST);
}

// =============================================================================
// deviceInfo: — pure-logic, constructs NSDictionary from APXClientDevice
// =============================================================================

- (void)test_deviceInfo_populatesAllExpectedKeys {
    id mockDevice = OCMClassMock([APXClientDevice class]);
    OCMStub([mockDevice udid]).andReturn(@"test-udid");
    OCMStub([mockDevice sdkVersion]).andReturn(@"6.0.0");
    OCMStub([mockDevice locale]).andReturn(@"en_US");
    OCMStub([mockDevice timeZone]).andReturn(@"Europe/Berlin");
    OCMStub([mockDevice hardwearType]).andReturn(@"iPhone14,2");
    OCMStub([mockDevice osVersion]).andReturn(@"17.0");
    OCMStub([mockDevice osName]).andReturn(@"iOS");

    NSDictionary *result = [self.module deviceInfo:mockDevice];

    XCTAssertEqualObjects(result[@"udid"],        @"test-udid");
    XCTAssertEqualObjects(result[@"sdkVersion"],  @"6.0.0");
    XCTAssertEqualObjects(result[@"locale"],      @"en_US");
    XCTAssertEqualObjects(result[@"timezone"],    @"Europe/Berlin");
    XCTAssertEqualObjects(result[@"deviceModel"], @"iPhone14,2");
    XCTAssertEqualObjects(result[@"osVersion"],   @"17.0");
    XCTAssertEqualObjects(result[@"osName"],      @"iOS");
}

- (void)test_deviceInfo_returnsNonNilDictionary {
    id mockDevice = OCMClassMock([APXClientDevice class]);
    OCMStub([mockDevice udid]).andReturn(@"u");
    OCMStub([mockDevice sdkVersion]).andReturn(@"1");
    OCMStub([mockDevice locale]).andReturn(@"en");
    OCMStub([mockDevice timeZone]).andReturn(@"UTC");
    OCMStub([mockDevice hardwearType]).andReturn(@"iPhone");
    OCMStub([mockDevice osVersion]).andReturn(@"16");
    OCMStub([mockDevice osName]).andReturn(@"iOS");

    XCTAssertNotNil([self.module deviceInfo:mockDevice]);
}

// =============================================================================
// engage:projectId:cepUrl:appID:tenantID:
// =============================================================================

- (void)test_engage_callsEngageWithLaunchOptionsOnAppoxee {
    OCMExpect([self.mockAppoxee
               engageWithLaunchOptions:nil
               andDelegate:[OCMArg any]
               andSDKID:@"mySdkKey"
               with:L3]);

    [self.module engage:@"mySdkKey"
              projectId:@"projectId"
                 cepUrl:@"L3"
                  appID:@"appId"
               tenantID:@"tenantId"];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_engage_resolvesServerCorrectly_EMC {
    OCMExpect([self.mockAppoxee
               engageWithLaunchOptions:nil
               andDelegate:[OCMArg any]
               andSDKID:[OCMArg any]
               with:EMC]);

    [self.module engage:@"key" projectId:@"p" cepUrl:@"EMC" appID:@"a" tenantID:@"t"];

    OCMVerifyAll(self.mockAppoxee);
}

// =============================================================================
// logOut:
// =============================================================================

- (void)test_logOut_true_callsLogoutWithOptinTrue {
    OCMExpect([self.mockAppoxee logoutWithOptin:YES]);

    [self.module logOut:YES];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_logOut_false_callsLogoutWithOptinFalse {
    OCMExpect([self.mockAppoxee logoutWithOptin:NO]);

    [self.module logOut:NO];

    OCMVerifyAll(self.mockAppoxee);
}

// =============================================================================
// isReady:
// =============================================================================

- (void)test_isReady_resolvesTrue_whenSdkIsReady {
    OCMStub([self.mockAppoxee isReady]).andReturn(YES);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve called"];

    [self.module isReady:^(id result) {
        XCTAssertEqualObjects(result, @YES);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_isReady_resolvesFalse_whenSdkNotReady {
    OCMStub([self.mockAppoxee isReady]).andReturn(NO);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve called"];

    [self.module isReady:^(id result) {
        XCTAssertEqualObjects(result, @NO);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

// =============================================================================
// isDeviceRegistered: (alias for isReady on iOS)
// =============================================================================

- (void)test_isDeviceRegistered_resolvesTrue_whenSdkIsReady {
    OCMStub([self.mockAppoxee isReady]).andReturn(YES);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module isDeviceRegistered:^(id result) {
        XCTAssertEqualObjects(result, @YES);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

// =============================================================================
// setAlias: / setAliasWithResend:withResendAttributes:
// =============================================================================

- (void)test_setAlias_callsSetDeviceAliasOnSdk {
    OCMExpect([self.mockAppoxee
               setDeviceAlias:@"myAlias"
               withCompletionHandler:[OCMArg any]]);

    [self.module setAlias:@"myAlias"];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setAliasWithResend_trueFlag_callsSdkWithTrue {
    OCMExpect([self.mockAppoxee
               setDeviceAlias:@"myAlias"
               withResendAttributes:YES
               withCompletionHandler:[OCMArg any]]);

    [self.module setAliasWithResend:@"myAlias" withResendAttributes:YES];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setAliasWithResend_falseFlag_callsSdkWithFalse {
    OCMExpect([self.mockAppoxee
               setDeviceAlias:@"myAlias"
               withResendAttributes:NO
               withCompletionHandler:[OCMArg any]]);

    [self.module setAliasWithResend:@"myAlias" withResendAttributes:NO];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_getAlias_resolvesWithAliasReturnedBySdk {
    NSString *expectedAlias = @"device-alias-123";
    OCMStub([self.mockAppoxee
             getDeviceAliasWithCompletionHandler:[OCMArg invokeBlockWithArgs:
                                                  [NSNull null], expectedAlias, nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module getAlias:^(id result) {
        XCTAssertEqualObjects(result, expectedAlias);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_getAlias_rejectsWhenSdkReturnsError {
    NSError *sdkError = [NSError errorWithDomain:@"AppoxeeError" code:1 userInfo:nil];
    OCMStub([self.mockAppoxee
             getDeviceAliasWithCompletionHandler:[OCMArg invokeBlockWithArgs:
                                                  sdkError, [NSNull null], nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"reject"];

    [self.module getAlias:^(id result) {
        XCTFail(@"resolve should not be called");
    } rejecter:^(NSString *code, NSString *message, NSError *error) {
        XCTAssertEqualObjects(code, @"GET_ALIAS_ERROR");
        [exp fulfill];
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_removeDeviceAlias_callsSdkRemoveAlias {
    OCMExpect([self.mockAppoxee
               removeDeviceAliasWithCompletionHandler:[OCMArg any]]);

    [self.module removeDeviceAlias];

    OCMVerifyAll(self.mockAppoxee);
}

// =============================================================================
// setPushEnabled: / isPushEnabled:
// =============================================================================

- (void)test_setPushEnabled_true_callsDisablePushWithFalse {
    // setPushEnabled:YES means enabled=YES → disablePush:NO
    OCMExpect([self.mockAppoxee
               disablePushNotifications:NO
               withCompletionHandler:[OCMArg any]]);

    [self.module setPushEnabled:YES];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setPushEnabled_false_callsDisablePushWithTrue {
    OCMExpect([self.mockAppoxee
               disablePushNotifications:YES
               withCompletionHandler:[OCMArg any]]);

    [self.module setPushEnabled:NO];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_isPushEnabled_resolvesWithSdkValue {
    OCMStub([self.mockAppoxee
             isPushEnabled:[OCMArg invokeBlockWithArgs:
                            [NSNull null], @YES, nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module isPushEnabled:^(id result) {
        XCTAssertEqualObjects(result, @YES);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_isPushEnabled_rejectsOnError {
    NSError *sdkError = [NSError errorWithDomain:@"AppoxeeError" code:2 userInfo:nil];
    OCMStub([self.mockAppoxee
             isPushEnabled:[OCMArg invokeBlockWithArgs:
                            sdkError, [NSNull null], nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"reject"];

    [self.module isPushEnabled:^(id result) {
        XCTFail(@"resolve should not be called");
    } rejecter:^(NSString *code, NSString *message, NSError *error) {
        XCTAssertEqualObjects(code, @"PUSH_STATUS_ERROR");
        [exp fulfill];
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

// =============================================================================
// setPostponeNotificationRequest: / setShowNotificationsAtForeground:
// =============================================================================

- (void)test_setPostponeNotificationRequest_true_callsSdk {
    OCMExpect([self.mockAppoxee setPostponeNotificationRequest:YES]);
    [self.module setPostponeNotificationRequest:YES];
    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setPostponeNotificationRequest_false_callsSdk {
    OCMExpect([self.mockAppoxee setPostponeNotificationRequest:NO]);
    [self.module setPostponeNotificationRequest:NO];
    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setShowNotificationsAtForeground_true_callsSdk {
    OCMExpect([self.mockAppoxee setShowNotificationsOnForeground:YES]);
    [self.module setShowNotificationsAtForeground:YES];
    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setShowNotificationsAtForeground_false_callsSdk {
    OCMExpect([self.mockAppoxee setShowNotificationsOnForeground:NO]);
    [self.module setShowNotificationsAtForeground:NO];
    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_showNotificationAlertView_callsSdk {
    OCMExpect([self.mockAppoxee showNotificationAlertDialog]);
    [self.module showNotificationAlertView];
    OCMVerifyAll(self.mockAppoxee);
}

// =============================================================================
// setAttribute: / setAttributeInt: / setAttributes: / incrementNumericKey:
// =============================================================================

- (void)test_setAttribute_callsSetStringValueOnSdk {
    OCMExpect([self.mockAppoxee
               setStringValue:@"red"
               forKey:@"color"
               withCompletionHandler:[OCMArg any]]);

    [self.module setAttribute:@"color" value:@"red"];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setAttributeInt_callsSetNumberValueOnSdk {
    NSNumber *value = @42;
    OCMExpect([self.mockAppoxee
               setNumberValue:value
               forKey:@"age"
               withCompletionHandler:[OCMArg any]]);

    [self.module setAttributeInt:@"age" value:value];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_setAttributes_callsSetCustomAttributesOnSdk {
    NSDictionary *attrs = @{@"key": @"value"};
    OCMExpect([self.mockAppoxee
               setCustomAttributtes:attrs
               withCompletionHandler:[OCMArg any]]);

    [self.module setAttributes:attrs];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_incrementNumericKey_callsSdkWithCorrectKeyAndValue {
    NSNumber *value = @5;
    OCMExpect([self.mockAppoxee
               incrementNumericKey:@"visits"
               byNumericValue:value
               withCompletionHandler:[OCMArg any]]);

    [self.module incrementNumericKey:@"visits" value:value];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_getAttributes_callsSdkAndResolvesData {
    NSArray *keys = @[@"color", @"age"];
    NSDictionary *returnedData = @{@"get": @{@"color": @"red"}};
    OCMStub([self.mockAppoxee
             getCustomAttributes:keys
             withCompletionHandler:[OCMArg invokeBlockWithArgs:
                                    [NSNull null], returnedData, nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module getAttributes:keys and:^(id result) {
        XCTAssertEqualObjects(result, returnedData[@"get"]);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_getAttributeStringValue_stringValue_resolvesString {
    NSDictionary *returnedData = @{@"color": @"red"};
    OCMStub([self.mockAppoxee
             fetchCustomFieldByKey:@"color"
             withCompletionHandler:[OCMArg invokeBlockWithArgs:
                                    [NSNull null], returnedData, nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module getAttributeStringValue:@"color"
                                resolver:^(id result) {
        XCTAssertEqualObjects(result, @"red");
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_getAttributeStringValue_numberValue_resolvesStringRepresentation {
    NSDictionary *returnedData = @{@"age": @30};
    OCMStub([self.mockAppoxee
             fetchCustomFieldByKey:@"age"
             withCompletionHandler:[OCMArg invokeBlockWithArgs:
                                    [NSNull null], returnedData, nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module getAttributeStringValue:@"age"
                                resolver:^(id result) {
        XCTAssertEqualObjects(result, @"30");
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_getAttributeStringValue_rejectsOnError {
    NSError *sdkError = [NSError errorWithDomain:@"AppoxeeError" code:3 userInfo:nil];
    OCMStub([self.mockAppoxee
             fetchCustomFieldByKey:[OCMArg any]
             withCompletionHandler:[OCMArg invokeBlockWithArgs:
                                    sdkError, [NSNull null], nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"reject"];

    [self.module getAttributeStringValue:@"key"
                                resolver:^(id result) {
        XCTFail(@"resolve should not be called");
    } rejecter:^(NSString *code, NSString *message, NSError *error) {
        XCTAssertEqualObjects(code, @"GET_ATTRIBUTE_FAIL");
        [exp fulfill];
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

// =============================================================================
// addTag: / removeTag: / getTags:
// =============================================================================

- (void)test_addTag_callsAddTagsToDeviceOnSdk {
    OCMExpect([self.mockAppoxee
               addTagsToDevice:@[@"sports"]
               withCompletionHandler:[OCMArg any]]);

    [self.module addTag:@"sports"];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_removeTag_callsRemoveTagsFromDeviceOnSdk {
    OCMExpect([self.mockAppoxee
               removeTagsFromDevice:@[@"sports"]
               withCompletionHandler:[OCMArg any]]);

    [self.module removeTag:@"sports"];

    OCMVerifyAll(self.mockAppoxee);
}

- (void)test_getTags_resolvesWithTagArrayFromSdk {
    NSArray *tags = @[@"sports", @"news"];
    OCMStub([self.mockAppoxee
             fetchDeviceTags:[OCMArg invokeBlockWithArgs:
                              [NSNull null], tags, nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module getTags:^(id result) {
        XCTAssertEqualObjects(result, tags);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_getTags_rejectsOnError {
    NSError *sdkError = [NSError errorWithDomain:@"AppoxeeError" code:4 userInfo:nil];
    OCMStub([self.mockAppoxee
             fetchDeviceTags:[OCMArg invokeBlockWithArgs:
                              sdkError, [NSNull null], nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"reject"];

    [self.module getTags:^(id result) {
        XCTFail(@"resolve should not be called");
    } rejecter:^(NSString *code, NSString *message, NSError *error) {
        XCTAssertEqualObjects(code, @"GET_TAGS_FAIL");
        [exp fulfill];
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

// =============================================================================
// getDeviceInfo:
// =============================================================================

- (void)test_getDeviceInfo_resolvesDeviceDictionary {
    id mockDevice = OCMClassMock([APXClientDevice class]);
    OCMStub([mockDevice udid]).andReturn(@"u");
    OCMStub([mockDevice sdkVersion]).andReturn(@"1");
    OCMStub([mockDevice locale]).andReturn(@"en");
    OCMStub([mockDevice timeZone]).andReturn(@"UTC");
    OCMStub([mockDevice hardwearType]).andReturn(@"iPhone");
    OCMStub([mockDevice osVersion]).andReturn(@"16");
    OCMStub([mockDevice osName]).andReturn(@"iOS");

    OCMStub([self.mockAppoxee
             deviceInformationwithCompletionHandler:[OCMArg invokeBlockWithArgs:
                                                     [NSNull null], mockDevice, nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module getDeviceInfo:^(id result) {
        XCTAssertNotNil(result);
        XCTAssertNotNil(((NSDictionary *)result)[@"udid"]);
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_getDeviceInfo_rejectsOnError {
    NSError *sdkError = [NSError errorWithDomain:@"AppoxeeError" code:5 userInfo:nil];
    OCMStub([self.mockAppoxee
             deviceInformationwithCompletionHandler:[OCMArg invokeBlockWithArgs:
                                                     sdkError, [NSNull null], nil]]);
    XCTestExpectation *exp = [self expectationWithDescription:@"reject"];

    [self.module getDeviceInfo:^(id result) {
        XCTFail(@"resolve should not be called");
    } rejecter:^(NSString *code, NSString *message, NSError *error) {
        XCTAssertEqualObjects(code, @"GET_DEVICE_INFO_ERROR");
        [exp fulfill];
    }];

    [self waitForExpectationsWithTimeout:1 handler:nil];
}

// =============================================================================
// In-App: engageInapp: / fetchInboxMessage: / fetchLatestInboxMessage: / triggerInApp:
// =============================================================================

- (void)test_engageInapp_callsEngageWithDelegateOnAppoxeeInapp {
    OCMExpect([self.mockAppoxeeInapp
               engageWithDelegate:[OCMArg any]
               with:l3]);

    [self.module engageInapp:@"L3"];

    OCMVerifyAll(self.mockAppoxeeInapp);
}

- (void)test_fetchInboxMessage_callsFetchAPXInBoxMessages {
    OCMExpect([self.mockAppoxeeInapp fetchAPXInBoxMessages]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module fetchInboxMessage:^(id result) {
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    OCMVerifyAll(self.mockAppoxeeInapp);
    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_fetchLatestInboxMessage_callsFetchAPXInBoxMessages {
    OCMExpect([self.mockAppoxeeInapp fetchAPXInBoxMessages]);
    XCTestExpectation *exp = [self expectationWithDescription:@"resolve"];

    [self.module fetchLatestInboxMessage:^(id result) {
        [exp fulfill];
    } rejecter:^(NSString *c, NSString *m, NSError *e) {
        XCTFail(@"reject should not be called");
    }];

    OCMVerifyAll(self.mockAppoxeeInapp);
    [self waitForExpectationsWithTimeout:1 handler:nil];
}

- (void)test_triggerInApp_callsReportInteractionEventWithName {
    OCMExpect([self.mockAppoxeeInapp
               reportInteractionEventWithName:@"promo_banner"
               andAttributes:nil]);

    [self.module triggerInApp:@"promo_banner"];

    OCMVerifyAll(self.mockAppoxeeInapp);
}

// =============================================================================
// inAppMarkAsRead: / inAppMarkAsUnRead: / inAppMarkAsDeleted:
// (These delegate to the message object retrieved from RNMappEventEmmiter)
// =============================================================================

- (void)test_inAppMarkAsRead_callsMarkAsReadOnMessage {
    id mockMessage = OCMClassMock([APXInBoxMessage class]);
    OCMStub([self.mockEventEmitter getMessageWith:@1 event:@"e1"]).andReturn(mockMessage);
    OCMExpect([mockMessage markAsRead]);

    [self.module inAppMarkAsRead:@1 event:@"e1"];

    OCMVerifyAll(mockMessage);
}

- (void)test_inAppMarkAsRead_doesNotCrashWhenMessageNotFound {
    OCMStub([self.mockEventEmitter getMessageWith:[OCMArg any] event:[OCMArg any]]).andReturn(nil);

    // Should not throw when message is nil (guarded by `if (message)`)
    XCTAssertNoThrow([self.module inAppMarkAsRead:@999 event:@"nonexistent"]);
}

- (void)test_inAppMarkAsUnRead_callsMarkAsUnreadOnMessage {
    id mockMessage = OCMClassMock([APXInBoxMessage class]);
    OCMStub([self.mockEventEmitter getMessageWith:@2 event:@"e2"]).andReturn(mockMessage);
    OCMExpect([mockMessage markAsUnread]);

    [self.module inAppMarkAsUnRead:@2 event:@"e2"];

    OCMVerifyAll(mockMessage);
}

- (void)test_inAppMarkAsDeleted_callsMarkAsDeletedOnMessage {
    id mockMessage = OCMClassMock([APXInBoxMessage class]);
    OCMStub([self.mockEventEmitter getMessageWith:@3 event:@"e3"]).andReturn(mockMessage);
    OCMExpect([mockMessage markAsDeleted]);

    [self.module inAppMarkAsDeleted:@3 event:@"e3"];

    OCMVerifyAll(mockMessage);
}

// =============================================================================
// startGeoFencing / stopGeoFencing
// =============================================================================

- (void)test_startGeoFencing_callsEnableLocationMonitoring {
    OCMExpect([self.mockLocationManager enableLocationMonitoring]);
    [self.module startGeoFencing];
    OCMVerifyAll(self.mockLocationManager);
}

- (void)test_stopGeoFencing_callsDisableLocationMonitoring {
    OCMExpect([self.mockLocationManager disableLocationMonitoring]);
    [self.module stopGeoFencing];
    OCMVerifyAll(self.mockLocationManager);
}

@end
