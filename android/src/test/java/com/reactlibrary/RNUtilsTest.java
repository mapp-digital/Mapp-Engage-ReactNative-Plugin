package com.reactlibrary;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for RNUtils.readableMapToJson() — the only method in RNUtils whose output
 * is a plain JSONObject (no React Native JNI required).
 *
 * jsonToWritableMap() and jsonArrayToWritableArray() call new WritableNativeMap() /
 * new WritableNativeArray() on their first line, which requires the React Native JNI
 * library. Those methods cannot be tested in plain JUnit without Robolectric.
 *
 * Requires Mockito (org.mockito:mockito-core:5.14.2) to mock ReadableMap.
 */
public class RNUtilsTest {

    // -------------------------------------------------------------------------
    // readableMapToJson — null / empty guards
    // -------------------------------------------------------------------------

    @Test
    public void readableMapToJson_nullInput_returnsNull() {
        assertNull(RNUtils.readableMapToJson(null));
    }

    @Test
    public void readableMapToJson_emptyMap_returnsNull() {
        ReadableMap map = mock(ReadableMap.class);
        ReadableMapKeySetIterator iterator = mock(ReadableMapKeySetIterator.class);
        when(map.keySetIterator()).thenReturn(iterator);
        when(iterator.hasNextKey()).thenReturn(false);

        assertNull(RNUtils.readableMapToJson(map));
    }

    // -------------------------------------------------------------------------
    // readableMapToJson — type branches
    // -------------------------------------------------------------------------

    @Test
    public void readableMapToJson_stringValue() throws JSONException {
        ReadableMap map = mockMapWithSingleEntry("name", ReadableType.String);
        when(map.getString("name")).thenReturn("Alice");

        JSONObject result = RNUtils.readableMapToJson(map);
        assertNotNull(result);
        assertEquals("Alice", result.getString("name"));
    }

    @Test
    public void readableMapToJson_booleanValue() throws JSONException {
        ReadableMap map = mockMapWithSingleEntry("flag", ReadableType.Boolean);
        when(map.getBoolean("flag")).thenReturn(true);

        JSONObject result = RNUtils.readableMapToJson(map);
        assertNotNull(result);
        assertTrue(result.getBoolean("flag"));
    }

    @Test
    public void readableMapToJson_intValue() throws JSONException {
        ReadableMap map = mockMapWithSingleEntry("count", ReadableType.Number);
        when(map.getInt("count")).thenReturn(42);

        JSONObject result = RNUtils.readableMapToJson(map);
        assertNotNull(result);
        assertEquals(42, result.getInt("count"));
    }

    @Test
    public void readableMapToJson_nullValue() throws JSONException {
        ReadableMap map = mockMapWithSingleEntry("key", ReadableType.Null);

        JSONObject result = RNUtils.readableMapToJson(map);
        assertNotNull(result);
        assertTrue(result.isNull("key"));
    }

    @Test
    public void readableMapToJson_nestedMap() throws JSONException {
        // Outer map: { "nested": <innerMap> }
        ReadableMap innerMap = mockMapWithSingleEntry("innerKey", ReadableType.String);
        when(innerMap.getString("innerKey")).thenReturn("innerValue");

        ReadableMap outerMap = mockMapWithSingleEntry("nested", ReadableType.Map);
        when(outerMap.getMap("nested")).thenReturn(innerMap);

        JSONObject result = RNUtils.readableMapToJson(outerMap);
        assertNotNull(result);
        JSONObject nested = result.getJSONObject("nested");
        assertNotNull(nested);
        assertEquals("innerValue", nested.getString("innerKey"));
    }

    @Test
    public void readableMapToJson_multipleKeys() throws JSONException {
        ReadableMapKeySetIterator iterator = mock(ReadableMapKeySetIterator.class);
        when(iterator.hasNextKey()).thenReturn(true, true, false);
        when(iterator.nextKey()).thenReturn("a", "b");

        ReadableMap map = mock(ReadableMap.class);
        when(map.keySetIterator()).thenReturn(iterator);
        when(map.getType("a")).thenReturn(ReadableType.String);
        when(map.getType("b")).thenReturn(ReadableType.Number);
        when(map.getString("a")).thenReturn("hello");
        when(map.getInt("b")).thenReturn(99);

        JSONObject result = RNUtils.readableMapToJson(map);
        assertNotNull(result);
        assertEquals("hello", result.getString("a"));
        assertEquals(99, result.getInt("b"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a mock ReadableMap with exactly one key of the given type.
     * Callers should stub the appropriate get*() method after calling this.
     */
    private ReadableMap mockMapWithSingleEntry(String key, ReadableType type) {
        ReadableMapKeySetIterator iterator = mock(ReadableMapKeySetIterator.class);
        when(iterator.hasNextKey()).thenReturn(true, false);
        when(iterator.nextKey()).thenReturn(key);

        ReadableMap map = mock(ReadableMap.class);
        when(map.keySetIterator()).thenReturn(iterator);
        when(map.getType(key)).thenReturn(type);
        return map;
    }
}
