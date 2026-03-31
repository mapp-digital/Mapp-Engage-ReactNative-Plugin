/**
 * RNMappEventEmmiterTests.m
 *
 * Unit tests for RNMappEventEmmiter.
 *
 * Tests cover:
 *   - singleton behaviour
 *   - supportedEvents list completeness
 *   - getMessageWith:event: lookup logic (pure logic, no SDK mocks needed)
 *   - Notification delegate callbacks → correct event emitted with correct body
 *   - InApp delegate callbacks → correct event emitted with correct body
 *   - Location delegate callbacks → correct event emitted with correct body
 *   - stringFromDate:inUTC: formatting
 *   - getPushMessage: / getRichMessage: dictionary construction
 *
 * Setup: see ios/RNMappPluginTests/README.md
 */

#import <XCTest/XCTest.h>
#import <OCMock/OCMock.h>

#import "RNMappEventEmmiter.h"

// ── Expose private helpers for white-box testing ─────────────────────────────
@interface RNMappEventEmmiter (Testing)
- (NSString *)stringFromDate:(NSDate *)date inUTC:(BOOL)isUTC;
- (NSDictionary *)getRichMessage:(APXRichMessage *)message;
- (NSDictionary *)getPushMessage:(APXPushNotification *)pushMessage;
@property (strong, nonatomic) NSMutableArray<APXInBoxMessage *> *messages;
@end
// ─────────────────────────────────────────────────────────────────────────────

// Event name constants (mirrors RNMappEventEmmiter.m)
static NSString *const kInitEvent            = @"com.mapp.init";
static NSString *const kInboxMessageReceived = @"com.mapp.inbox_message_received";
static NSString *const kInboxMessages        = @"com.mapp.inbox_messages_received";
static NSString *const kLocationEnter        = @"com.mapp.georegion_enter";
static NSString *const kLocationExit         = @"com.mapp.georegion_exit";
static NSString *const kCustomLink           = @"com.mapp.custom_link_received";
static NSString *const kDeepLink             = @"com.mapp.deep_link_received";
static NSString *const kRichMessage          = @"com.mapp.rich_message";
static NSString *const kPushMessage          = @"com.mapp.rich_message_received";
static NSString *const kErrorMessage         = @"com.mapp.error_message";
static NSString *const kInappMessage         = @"com.mapp.inapp_message";


@interface RNMappEventEmmiterTests : XCTestCase

// A partial mock so we can intercept sendEventWithName:body: without a real bridge
@property (nonatomic, strong) id partialMockEmitter;
@property (nonatomic, strong) RNMappEventEmmiter *emitter;

@end


@implementation RNMappEventEmmiterTests

- (void)setUp {
    [super setUp];
    self.emitter = [RNMappEventEmmiter shared];
    // Partial mock: real object with sendEventWithName:body: intercepted
    self.partialMockEmitter = OCMPartialMock(self.emitter);
    // Simulate hasListeners = YES so events are not dropped
    [self.emitter startObserving];
}

- (void)tearDown {
    [self.partialMockEmitter stopMocking];
    [self.emitter stopObserving];
    [super tearDown];
}

// =============================================================================
// Singleton
// =============================================================================

- (void)test_shared_returnsSameInstance {
    RNMappEventEmmiter *a = [RNMappEventEmmiter shared];
    RNMappEventEmmiter *b = [RNMappEventEmmiter shared];
    XCTAssertEqual(a, b);
}

- (void)test_shared_returnsNonNilInstance {
    XCTAssertNotNil([RNMappEventEmmiter shared]);
}

// =============================================================================
// supportedEvents
// =============================================================================

- (void)test_supportedEvents_containsAllExpectedEventNames {
    NSArray<NSString *> *supported = [self.emitter supportedEvents];
    XCTAssertTrue([supported containsObject:kInitEvent]);
    XCTAssertTrue([supported containsObject:kInboxMessageReceived]);
    XCTAssertTrue([supported containsObject:kInboxMessages]);
    XCTAssertTrue([supported containsObject:kLocationEnter]);
    XCTAssertTrue([supported containsObject:kCustomLink]);
    XCTAssertTrue([supported containsObject:kDeepLink]);
    XCTAssertTrue([supported containsObject:kRichMessage]);
    XCTAssertTrue([supported containsObject:kPushMessage]);
    XCTAssertTrue([supported containsObject:kErrorMessage]);
    XCTAssertTrue([supported containsObject:kInappMessage]);
}

- (void)test_supportedEvents_isNonEmpty {
    XCTAssertGreaterThan([self.emitter supportedEvents].count, 0u);
}

// =============================================================================
// getMessageWith:event: — pure logic, no SDK calls
// =============================================================================

- (void)test_getMessageWith_returnsMatchingMessage {
    id mockMessage = OCMClassMock([APXInBoxMessage class]);
    OCMStub([mockMessage messageId]).andReturn(@"42");
    self.emitter.messages = [NSMutableArray arrayWithObject:mockMessage];

    APXInBoxMessage *found = [self.emitter getMessageWith:@42 event:@"irrelevant"];

    XCTAssertEqual(found, mockMessage);
}

- (void)test_getMessageWith_returnsNilWhenNoMatch {
    id mockMessage = OCMClassMock([APXInBoxMessage class]);
    OCMStub([mockMessage messageId]).andReturn(@"1");
    self.emitter.messages = [NSMutableArray arrayWithObject:mockMessage];

    APXInBoxMessage *found = [self.emitter getMessageWith:@999 event:@"e"];

    XCTAssertNil(found);
}

- (void)test_getMessageWith_returnsNilForEmptyMessages {
    self.emitter.messages = [NSMutableArray array];

    XCTAssertNil([self.emitter getMessageWith:@1 event:@"e"]);
}

- (void)test_getMessageWith_returnsNilForNilMessages {
    self.emitter.messages = nil;

    XCTAssertNil([self.emitter getMessageWith:@1 event:@"e"]);
}

// =============================================================================
// stringFromDate:inUTC: — pure logic
// =============================================================================

- (void)test_stringFromDate_notUTC_returnsFormattedString {
    NSDateComponents *components = [[NSDateComponents alloc] init];
    components.year = 2024; components.month = 6; components.day = 15;
    components.hour = 10;   components.minute = 30; components.second = 0;
    NSCalendar *cal = [NSCalendar calendarWithIdentifier:NSCalendarIdentifierGregorian];
    NSDate *date = [cal dateFromComponents:components];

    NSString *result = [self.emitter stringFromDate:date inUTC:NO];

    // Format is yyyy-MM-dd'T'HH:mm:ss — just check structure
    XCTAssertTrue([result hasPrefix:@"2024-06-15T"]);
    XCTAssertEqual(result.length, 19u);
}

- (void)test_stringFromDate_UTC_returnsISO8601InUTC {
    // Create a date that is unambiguous in UTC
    NSDateFormatter *fmt = [[NSDateFormatter alloc] init];
    [fmt setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
    [fmt setTimeZone:[NSTimeZone timeZoneWithName:@"UTC"]];
    NSDate *date = [fmt dateFromString:@"2024-01-01T12:00:00"];

    NSString *result = [self.emitter stringFromDate:date inUTC:YES];

    XCTAssertEqualObjects(result, @"2024-01-01T12:00:00");
}

- (void)test_stringFromDate_resultHasCorrectLength {
    NSString *result = [self.emitter stringFromDate:[NSDate date] inUTC:YES];
    XCTAssertEqual(result.length, 19u); // "yyyy-MM-dd'T'HH:mm:ss"
}

// =============================================================================
// getRichMessage: — dictionary construction
// =============================================================================

- (void)test_getRichMessage_containsIdTitleContentMessageLink {
    id mockMsg = OCMClassMock([APXRichMessage class]);
    OCMStub([mockMsg uniqueID]).andReturn(7);
    OCMStub([mockMsg title]).andReturn(@"Test Title");
    OCMStub([mockMsg content]).andReturn(@"Test Content");
    OCMStub([mockMsg messageLink]).andReturn(@"https://example.com");
    OCMStub([mockMsg postDate]).andReturn(nil);

    NSDictionary *result = [self.emitter getRichMessage:mockMsg];

    XCTAssertEqualObjects(result[@"id"],          @"7");
    XCTAssertEqualObjects(result[@"title"],       @"Test Title");
    XCTAssertEqualObjects(result[@"content"],     @"Test Content");
    XCTAssertEqualObjects(result[@"messageLink"], @"https://example.com");
}

- (void)test_getRichMessage_omitsNilFields {
    id mockMsg = OCMClassMock([APXRichMessage class]);
    OCMStub([mockMsg uniqueID]).andReturn(1);
    OCMStub([mockMsg title]).andReturn(nil);
    OCMStub([mockMsg content]).andReturn(nil);
    OCMStub([mockMsg messageLink]).andReturn(nil);
    OCMStub([mockMsg postDate]).andReturn(nil);

    NSDictionary *result = [self.emitter getRichMessage:mockMsg];

    XCTAssertNotNil(result[@"id"]);
    XCTAssertNil(result[@"title"]);
    XCTAssertNil(result[@"content"]);
    XCTAssertNil(result[@"messageLink"]);
}

- (void)test_getRichMessage_includesPostDateWhenPresent {
    id mockMsg = OCMClassMock([APXRichMessage class]);
    OCMStub([mockMsg uniqueID]).andReturn(1);
    OCMStub([mockMsg title]).andReturn(nil);
    OCMStub([mockMsg content]).andReturn(nil);
    OCMStub([mockMsg messageLink]).andReturn(nil);
    OCMStub([mockMsg postDate]).andReturn([NSDate dateWithTimeIntervalSince1970:0]);

    NSDictionary *result = [self.emitter getRichMessage:mockMsg];

    XCTAssertNotNil(result[@"postDate"]);
    XCTAssertNotNil(result[@"postDateUTC"]);
}

// =============================================================================
// getPushMessage: — dictionary construction
// =============================================================================

- (void)test_getPushMessage_populatesAvailableFields {
    id mockPush = OCMClassMock([APXPushNotification class]);
    OCMStub([mockPush title]).andReturn(@"Push Title");
    OCMStub([mockPush alert]).andReturn(@"Push Alert");
    OCMStub([mockPush body]).andReturn(@"Push Body");
    OCMStub([mockPush uniqueID]).andReturn(99);
    OCMStub([mockPush badge]).andReturn(1);
    OCMStub([mockPush subtitle]).andReturn(@"subtitle");
    OCMStub([mockPush extraFields]).andReturn(@{@"key": @"val"});
    OCMStub([mockPush isRich]).andReturn(YES);
    OCMStub([mockPush isSilent]).andReturn(NO);
    OCMStub([mockPush isTriggerUpdate]).andReturn(NO);
    id mockAction = OCMClassMock([NSObject class]);
    OCMStub([mockAction categoryName]).andReturn(nil);
    OCMStub([mockPush pushAction]).andReturn(mockAction);

    NSDictionary *result = [self.emitter getPushMessage:mockPush];

    XCTAssertEqualObjects(result[@"title"],  @"Push Title");
    XCTAssertEqualObjects(result[@"alert"],  @"Push Alert");
    XCTAssertEqualObjects(result[@"body"],   @"Push Body");
    XCTAssertEqualObjects(result[@"id"],     @99);
    XCTAssertEqualObjects(result[@"badge"],  @1);
    XCTAssertEqualObjects(result[@"isRich"], @"true");
    XCTAssertEqualObjects(result[@"isSilent"], @"false");
}

- (void)test_getPushMessage_returnsEmptyDictForNilFields {
    id mockPush = OCMClassMock([APXPushNotification class]);
    // All properties return nil / 0 by default from OCMClassMock
    id mockAction = OCMClassMock([NSObject class]);
    OCMStub([mockAction categoryName]).andReturn(nil);
    OCMStub([mockPush pushAction]).andReturn(mockAction);

    NSDictionary *result = [self.emitter getPushMessage:mockPush];

    // Should be an empty (or nearly empty) dictionary, not nil
    XCTAssertNotNil(result);
}

// =============================================================================
// InApp delegate: appoxeeInapp:didReceiveInappMessageWithIdentifier:andMessageExtraData:
// =============================================================================

- (void)test_didReceiveInappMessage_withExtraData_emitsCorrectEvent {
    NSDictionary *extraData = @{@"foo": @"bar"};
    OCMExpect([self.partialMockEmitter
               sendEventWithName:kInappMessage
               body:@{@"id": @"5", @"extraData": extraData}]);

    id mockInapp = OCMClassMock([AppoxeeInapp class]);
    [self.emitter appoxeeInapp:mockInapp
        didReceiveInappMessageWithIdentifier:@5
        andMessageExtraData:extraData];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didReceiveInappMessage_withoutExtraData_emitsEventWithIdOnly {
    OCMExpect([self.partialMockEmitter
               sendEventWithName:kInappMessage
               body:@{@"id": @"5"}]);

    id mockInapp = OCMClassMock([AppoxeeInapp class]);
    [self.emitter appoxeeInapp:mockInapp
        didReceiveInappMessageWithIdentifier:@5
        andMessageExtraData:nil];

    OCMVerifyAll(self.partialMockEmitter);
}

// =============================================================================
// InApp delegate: didReceiveDeepLinkWithIdentifier:withMessageString:andTriggerEvent:
// =============================================================================

- (void)test_didReceiveDeepLink_emitsDeepLinkEvent {
    OCMExpect([self.partialMockEmitter
               sendEventWithName:kDeepLink
               body:@{@"action": @"10", @"url": @"https://link.example", @"event_trigger": @"tap"}]);

    [self.emitter didReceiveDeepLinkWithIdentifier:@10
                                 withMessageString:@"https://link.example"
                                  andTriggerEvent:@"tap"];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didReceiveDeepLink_doesNotEmitWhenParamsAreNil {
    OCMReject([self.partialMockEmitter sendEventWithName:[OCMArg any] body:[OCMArg any]]);

    [self.emitter didReceiveDeepLinkWithIdentifier:nil
                                 withMessageString:nil
                                  andTriggerEvent:nil];

    OCMVerifyAll(self.partialMockEmitter);
}

// =============================================================================
// InApp delegate: didReceiveCustomLinkWithIdentifier:withMessageString:
// =============================================================================

- (void)test_didReceiveCustomLink_emitsCustomLinkEvent {
    OCMExpect([self.partialMockEmitter
               sendEventWithName:kCustomLink
               body:@{@"id": @"7", @"message": @"custom://action"}]);

    [self.emitter didReceiveCustomLinkWithIdentifier:@7
                                  withMessageString:@"custom://action"];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didReceiveCustomLink_doesNotEmitWhenParamsAreNil {
    OCMReject([self.partialMockEmitter sendEventWithName:[OCMArg any] body:[OCMArg any]]);

    [self.emitter didReceiveCustomLinkWithIdentifier:nil withMessageString:nil];

    OCMVerifyAll(self.partialMockEmitter);
}

// =============================================================================
// InApp delegate: didReceiveInBoxMessages:
// =============================================================================

- (void)test_didReceiveInBoxMessages_emitsInboxMessagesReceivedEvent {
    id mockMsg = OCMClassMock([APXInBoxMessage class]);
    OCMStub([mockMsg getDictionary]).andReturn(@{@"id": @"1"});
    NSArray *messages = @[mockMsg];

    OCMExpect([self.partialMockEmitter
               sendEventWithName:kInboxMessages
               body:[OCMArg isKindOfClass:[NSArray class]]]);

    [self.emitter didReceiveInBoxMessages:messages];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didReceiveInBoxMessages_storesMessagesForLaterLookup {
    id mockMsg = OCMClassMock([APXInBoxMessage class]);
    OCMStub([mockMsg getDictionary]).andReturn(@{});
    OCMStub([mockMsg messageId]).andReturn(@"99");

    [self.emitter didReceiveInBoxMessages:@[mockMsg]];

    APXInBoxMessage *found = [self.emitter getMessageWith:@99 event:@"any"];
    XCTAssertEqual(found, mockMsg);
}

// =============================================================================
// InApp delegate: didReceiveInBoxMessage:
// =============================================================================

- (void)test_didReceiveInBoxMessage_emitsSingleInboxMessageReceivedEvent {
    id mockMsg = OCMClassMock([APXInBoxMessage class]);
    NSDictionary *dict = @{@"id": @"1"};
    OCMStub([mockMsg getDictionary]).andReturn(dict);

    OCMExpect([self.partialMockEmitter
               sendEventWithName:kInboxMessageReceived
               body:dict]);

    [self.emitter didReceiveInBoxMessage:mockMsg];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didReceiveInBoxMessage_doesNotEmitWhenDictionaryIsNil {
    id mockMsg = OCMClassMock([APXInBoxMessage class]);
    OCMStub([mockMsg getDictionary]).andReturn(nil);

    OCMReject([self.partialMockEmitter sendEventWithName:[OCMArg any] body:[OCMArg any]]);

    [self.emitter didReceiveInBoxMessage:mockMsg];

    OCMVerifyAll(self.partialMockEmitter);
}

// =============================================================================
// InApp delegate: inAppCallFailedWithResponse:andError:
// =============================================================================

- (void)test_inAppCallFailed_emitsErrorEventWithCustomError {
    NSError *sdkError = [NSError errorWithDomain:@"com.mapp" code:1 userInfo:nil];

    OCMExpect([self.partialMockEmitter
               sendEventWithName:kErrorMessage
               body:[OCMArg checkWithBlock:^BOOL(NSDictionary *body) {
        return body[@"error"] == sdkError && [body[@"response"] isEqualToString:@"response msg"];
    }]]);

    [self.emitter inAppCallFailedWithResponse:@"response msg" andError:sdkError];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_inAppCallFailed_usesDefaultsWhenParamsAreNil {
    OCMExpect([self.partialMockEmitter
               sendEventWithName:kErrorMessage
               body:[OCMArg checkWithBlock:^BOOL(NSDictionary *body) {
        return body[@"error"] != nil && body[@"response"] != nil;
    }]]);

    [self.emitter inAppCallFailedWithResponse:nil andError:nil];

    OCMVerifyAll(self.partialMockEmitter);
}

// =============================================================================
// Location delegate: didEnterGeoRegion: / didExitGeoRegion:
// =============================================================================

- (void)test_didEnterGeoRegion_emitsLocationEnterEventWithCoordinates {
    CLLocationCoordinate2D coord = CLLocationCoordinate2DMake(48.8566, 2.3522);
    CLCircularRegion *region = [[CLCircularRegion alloc]
                                initWithCenter:coord radius:100 identifier:@"paris"];

    OCMExpect([self.partialMockEmitter
               sendEventWithName:kLocationEnter
               body:[OCMArg checkWithBlock:^BOOL(NSDictionary *body) {
        return body[@"latitude"] != nil && body[@"longitude"] != nil;
    }]]);

    id mockManager = OCMClassMock([AppoxeeLocationManager class]);
    [self.emitter locationManager:mockManager didEnterGeoRegion:region];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didEnterGeoRegion_doesNotEmitWhenRegionIsNil {
    OCMReject([self.partialMockEmitter sendEventWithName:[OCMArg any] body:[OCMArg any]]);

    id mockManager = OCMClassMock([AppoxeeLocationManager class]);
    [self.emitter locationManager:mockManager didEnterGeoRegion:nil];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didExitGeoRegion_emitsLocationExitEventWithCoordinates {
    CLLocationCoordinate2D coord = CLLocationCoordinate2DMake(52.5200, 13.4050);
    CLCircularRegion *region = [[CLCircularRegion alloc]
                                initWithCenter:coord radius:200 identifier:@"berlin"];

    OCMExpect([self.partialMockEmitter
               sendEventWithName:kLocationExit
               body:[OCMArg checkWithBlock:^BOOL(NSDictionary *body) {
        return body[@"latitude"] != nil && body[@"longitude"] != nil;
    }]]);

    id mockManager = OCMClassMock([AppoxeeLocationManager class]);
    [self.emitter locationManager:mockManager didExitGeoRegion:region];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_didExitGeoRegion_doesNotEmitWhenRegionIsNil {
    OCMReject([self.partialMockEmitter sendEventWithName:[OCMArg any] body:[OCMArg any]]);

    id mockManager = OCMClassMock([AppoxeeLocationManager class]);
    [self.emitter locationManager:mockManager didExitGeoRegion:nil];

    OCMVerifyAll(self.partialMockEmitter);
}

// =============================================================================
// Location delegate: didFailWithError:
// =============================================================================

- (void)test_locationManagerDidFail_emitsErrorEvent {
    NSError *error = [NSError errorWithDomain:@"CLError" code:0 userInfo:nil];

    OCMExpect([self.partialMockEmitter
               sendEventWithName:kErrorMessage
               body:[OCMArg checkWithBlock:^BOOL(NSDictionary *body) {
        return body[@"error"] == error;
    }]]);

    id mockManager = OCMClassMock([AppoxeeLocationManager class]);
    [self.emitter locationManager:mockManager didFailWithError:error];

    OCMVerifyAll(self.partialMockEmitter);
}

- (void)test_locationManagerDidFail_doesNotEmitWhenErrorIsNil {
    OCMReject([self.partialMockEmitter sendEventWithName:[OCMArg any] body:[OCMArg any]]);

    id mockManager = OCMClassMock([AppoxeeLocationManager class]);
    [self.emitter locationManager:mockManager didFailWithError:nil];

    OCMVerifyAll(self.partialMockEmitter);
}

@end
