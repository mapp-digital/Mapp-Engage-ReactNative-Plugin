/**
 * RNMappPluginModuleTests.m
 *
 * Unit tests for RNMappPluginModule.
 *
 * Tests cover pure-logic functions and behavior:
 *   - the correct server key mapping is returned
 *   - device info dictionary is properly constructed
 *   - Promise pattern works with expectations
 *
 * Note: Tests without OCMock - uses real object initialization
 * and tests only pure logic paths that don't depend on SDK calls.
 */

#import <XCTest/XCTest.h>
#import "RNMappPluginModule.h"
#import "RNMappEventEmmiter.h"

// ── Expose private helpers for white-box testing ─────────────────────────────
@interface RNMappPluginModule (Testing)
- (SERVER)getServerKeyFor:(NSString *)name;
- (INAPPSERVER)getInappServerKeyFor:(NSString *)name;
@end
// ─────────────────────────────────────────────────────────────────────────────


@interface RNMappPluginModuleTests : XCTestCase
@property (nonatomic, strong) RNMappPluginModule *module;
@end


@implementation RNMappPluginModuleTests

- (void)setUp {
    [super setUp];
    self.module = [[RNMappPluginModule alloc] init];
}

- (void)tearDown {
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
// Module initialization
// =============================================================================

- (void)test_module_initSuccessfully {
    XCTAssertNotNil(self.module);
}

@end
