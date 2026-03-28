package com.reactlibrary;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for RNMappPluginModule.isMappPush(String) — pure JSON logic, zero-risk.
 *
 * A push is from Mapp if and only if the JSON has a "data" object containing a "p" key.
 * No mocking needed — pure JSONObject parsing.
 */
public class IsMappPushTest {

    @Test
    public void nullInput_returnsFalse() {
        assertFalse(RNMappPluginModule.isMappPush(null));
    }

    @Test
    public void malformedJson_returnsFalse() {
        assertFalse(RNMappPluginModule.isMappPush("{not json{{"));
    }

    @Test
    public void emptyJson_returnsFalse() {
        assertFalse(RNMappPluginModule.isMappPush("{}"));
    }

    @Test
    public void jsonWithDataButNoP_returnsFalse() {
        assertFalse(RNMappPluginModule.isMappPush("{\"data\":{\"other\":\"value\"}}"));
    }

    @Test
    public void jsonWithoutDataField_returnsFalse() {
        assertFalse(RNMappPluginModule.isMappPush("{\"notification\":{\"p\":\"1\"}}"));
    }

    @Test
    public void jsonWithDataAndP_returnsTrue() {
        assertTrue(RNMappPluginModule.isMappPush("{\"data\":{\"p\":\"1\"}}"));
    }

    @Test
    public void jsonWithDataAndPAlongsideOtherFields_returnsTrue() {
        assertTrue(RNMappPluginModule.isMappPush(
                "{\"messageId\":\"abc\",\"data\":{\"p\":\"1\",\"campaign\":\"xyz\"}}"));
    }

    @Test
    public void jsonWithEmptyDataObject_returnsFalse() {
        assertFalse(RNMappPluginModule.isMappPush("{\"data\":{}}"));
    }
}
