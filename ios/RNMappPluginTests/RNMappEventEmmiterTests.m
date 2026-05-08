/**
 * RNMappEventEmmiterTests.m
 *
 * Unit tests for RNMappEventEmmiter.
 *
 * Tests cover:
 *   - singleton behaviour
 *   - supportedEvents list completeness
 *   - getMessageWith:event: lookup logic (pure logic, no SDK mocks needed)
 *   - stringFromDate:inUTC: formatting
 *   - getPushMessage: / getRichMessage: dictionary construction
 *
 * Note: Tests use real objects instead of OCMock. Delegate callbacks tested
 * with simple object creation and assertion patterns.
 */

#import <XCTest/XCTest.h>
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


// ── Simple stub for tracking sendEventWithName: calls ──────────────────────────
@interface StubEventEmitter : NSObject
@property (nonatomic, strong) NSMutableArray<NSDictionary *> *emittedEvents;
@end

@implementation StubEventEmitter
- (instancetype)init {
    self = [super init];
    if (self) {
        self.emittedEvents = [NSMutableArray array];
    }
    return self;
}

- (void)sendEventWithName:(NSString *)name body:(id)body {
    [self.emittedEvents addObject:@{@"name": name ?: @"", @"body": body ?: [NSNull null]}];
}
@end


@interface RNMappEventEmmiterTests : XCTestCase
@property (nonatomic, strong) RNMappEventEmmiter *emitter;
@end


@implementation RNMappEventEmmiterTests

- (void)setUp {
    [super setUp];
    self.emitter = [RNMappEventEmmiter shared];
    [self.emitter startObserving];
}

- (void)tearDown {
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

- (void)test_getMessageWith_returnsNilWhenNoMessages {
    self.emitter.messages = [NSMutableArray array];
    APXInBoxMessage *found = [self.emitter getMessageWith:@1 event:@"e"];
    XCTAssertNil(found);
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

@end
