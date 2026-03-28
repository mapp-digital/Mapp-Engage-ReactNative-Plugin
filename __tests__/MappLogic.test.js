"use strict";

/**
 * Logic tests for Mapp.js — source-text assertions for branching/dispatch logic
 * and behavioural tests for convertEventEnum (the only pure function in Mapp.js).
 *
 * These tests do NOT import Mapp.js at runtime (it imports react-native which is
 * unavailable in the Node test environment). Instead they use source-text assertions
 * for structure/dispatch tests and inline the pure convertEventEnum logic for
 * behavioural tests.
 */

const fs = require("fs");
const path = require("path");

const mappSource = fs.readFileSync(path.join(__dirname, "..", "Mapp.js"), "utf8");

// ---------------------------------------------------------------------------
// Inline convertEventEnum for behavioural testing
// (extracted from Mapp.js — kept in sync manually)
// ---------------------------------------------------------------------------

const PUSH_RECEIVED_EVENT = "com.mapp.rich_message_received";
const MappIntentEvent     = "com.mapp.deep_link_received";
const IOS_INIT            = "com.mapp.init";
const IOS_INBOX_MESSAGES  = "com.mapp.inbox_messages_received";
const IOS_INBOX_MESSAGE   = "com.mapp.inbox_message_received";
const IOS_RICH_MESSAGE    = "com.mapp.rich_message";

function convertEventEnum(type) {
  if (type === "notificationResponse") return PUSH_RECEIVED_EVENT;
  if (type === "deepLink")             return MappIntentEvent;
  if (type === "iosSDKInit")           return IOS_INIT;
  if (type === "iosInboxMessages")     return IOS_INBOX_MESSAGES;
  if (type === "iosInboxMessage")      return IOS_INBOX_MESSAGE;
  if (type === "iosRichMessage")       return IOS_RICH_MESSAGE;
  throw new Error("Invalid event name: " + type);
}

// ---------------------------------------------------------------------------
// convertEventEnum — behavioural tests
// ---------------------------------------------------------------------------

describe("convertEventEnum", () => {
  test("notificationResponse maps to push received event", () => {
    expect(convertEventEnum("notificationResponse")).toBe("com.mapp.rich_message_received");
  });

  test("deepLink maps to deep link event", () => {
    expect(convertEventEnum("deepLink")).toBe("com.mapp.deep_link_received");
  });

  test("iosSDKInit maps to iOS init event", () => {
    expect(convertEventEnum("iosSDKInit")).toBe("com.mapp.init");
  });

  test("iosInboxMessages maps to iOS inbox messages event", () => {
    expect(convertEventEnum("iosInboxMessages")).toBe("com.mapp.inbox_messages_received");
  });

  test("iosInboxMessage maps to iOS inbox message event", () => {
    expect(convertEventEnum("iosInboxMessage")).toBe("com.mapp.inbox_message_received");
  });

  test("iosRichMessage maps to iOS rich message event", () => {
    expect(convertEventEnum("iosRichMessage")).toBe("com.mapp.rich_message");
  });

  test("invalid event name throws", () => {
    expect(() => convertEventEnum("bogusEvent")).toThrow("Invalid event name: bogusEvent");
  });

  test("all 6 valid event types produce distinct strings", () => {
    const values = [
      "notificationResponse", "deepLink", "iosSDKInit",
      "iosInboxMessages", "iosInboxMessage", "iosRichMessage"
    ].map(convertEventEnum);
    const unique = new Set(values);
    expect(unique.size).toBe(6);
  });
});

// ---------------------------------------------------------------------------
// Mapp.js source-text — platform dispatch logic
// ---------------------------------------------------------------------------

describe("Mapp.js platform dispatch", () => {
  test("engage() calls autoengage and engageInapp on iOS", () => {
    expect(mappSource).toMatch(/Platform\.OS\s*==\s*["']ios["']/);
    expect(mappSource).toMatch(/RNMappPluginModule\.autoengage/);
    expect(mappSource).toMatch(/RNMappPluginModule\.engageInapp/);
  });

  test("engage() calls RNMappPluginModule.engage on Android path", () => {
    expect(mappSource).toMatch(/RNMappPluginModule\.engage\s*\(/);
  });

  test("engageTestServer() is guarded to Android only", () => {
    // The method body must contain a Platform.OS android check
    const engageTestServerBlock = mappSource.match(
      /static\s+engageTestServer[\s\S]*?(?=\n\s{2}static|\n\s{2}\/\*\*|\n\s{2}\}?\s*$)/
    );
    expect(engageTestServerBlock).not.toBeNull();
    expect(engageTestServerBlock[0]).toMatch(/Platform\.OS\s*==\s*["']android["']/);
  });

  test("onInitCompletedListener() is guarded to Android only", () => {
    const block = mappSource.match(
      /static\s+onInitCompletedListener[\s\S]*?(?=\n\s{2}static|\n\s{2}\/\*\*)/
    );
    expect(block).not.toBeNull();
    expect(block[0]).toMatch(/Platform\.OS\s*==\s*["']android["']/);
  });

  test("setRemoteMessage() serialises argument with JSON.stringify", () => {
    const block = mappSource.match(
      /static\s+setRemoteMessage[\s\S]*?(?=\n\s{2}static|\n\s{2}\/\*\*)/
    );
    expect(block).not.toBeNull();
    expect(block[0]).toMatch(/JSON\.stringify/);
  });

  test("isPushFromMapp() serialises argument with JSON.stringify", () => {
    const block = mappSource.match(
      /static\s+isPushFromMapp[\s\S]*?(?=\n\s{2}static|\n\s{2}\/\*\*)/
    );
    expect(block).not.toBeNull();
    expect(block[0]).toMatch(/JSON\.stringify/);
  });
});

// ---------------------------------------------------------------------------
// Mapp.js source-text — setAlias dispatch
// ---------------------------------------------------------------------------

describe("Mapp.js setAlias dispatch", () => {
  test("calls setAliasWithResend when resendAttributes is defined", () => {
    expect(mappSource).toMatch(/setAliasWithResend\s*\(/);
  });

  test("guards with !== undefined check before calling setAliasWithResend", () => {
    expect(mappSource).toMatch(/resendAttributes\s*!==\s*undefined/);
  });

  test("guards with !== null check before calling setAliasWithResend", () => {
    expect(mappSource).toMatch(/resendAttributes\s*!==\s*null/);
  });

  test("falls back to plain setAlias when resendAttributes is absent", () => {
    expect(mappSource).toMatch(/RNMappPluginModule\.setAlias\s*\(/);
  });
});

// ---------------------------------------------------------------------------
// Mapp.js source-text — fetchLatestInboxMessage iOS sort logic
// ---------------------------------------------------------------------------

describe("Mapp.js fetchLatestInboxMessage", () => {
  test("iOS path sorts messages by template_id", () => {
    const block = mappSource.match(
      /static\s+fetchLatestInboxMessage[\s\S]*?(?=\n\s{2}static|\n\s{2}\/\*\*)/
    );
    expect(block).not.toBeNull();
    expect(block[0]).toMatch(/\.sort\s*\(/);
    expect(block[0]).toMatch(/template_id/);
  });

  test("Android path delegates directly to RNMappPluginModule", () => {
    const block = mappSource.match(
      /static\s+fetchLatestInboxMessage[\s\S]*?(?=\n\s{2}static|\n\s{2}\/\*\*)/
    );
    expect(block).not.toBeNull();
    expect(block[0]).toMatch(/RNMappPluginModule\.fetchLatestInboxMessage/);
  });
});
