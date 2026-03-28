package com.reactlibrary;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for RNUtils.readableMapToJson().
 *
 * The output is a JSONObject whose put() calls are stubbed by the Android test
 * framework (returnDefaultValues = true), so we cannot assert field values.
 * We verify the structural contract instead:
 *   - null input  → null return
 *   - empty map   → null return
 *   - non-empty map → non-null JSONObject (method executed without exception)
 *
 * jsonToWritableMap() and jsonArrayToWritableArray() call new WritableNativeMap() /
 * new WritableNativeArray() on their first line, which requires the React Native JNI.
 * Those methods cannot be tested in plain JUnit without Robolectric.
 *
 * Requires Mockito (org.mockito:mockito-core) to mock ReadableMap.
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
    // readableMapToJson — non-null return for each type branch
    //
    // We cannot assert field values because JSONObject.put() is stubbed by the
    // Android test framework. We verify that the method returns non-null
    // (i.e. it reached the return statement without throwing) for each type.
    // -------------------------------------------------------------------------

    @Test
    public void readableMapToJson_stringValue_returnsNonNull() {
        ReadableMap map = mockMapWithSingleEntry("name", ReadableType.String);
        when(map.getString("name")).thenReturn("Alice");

        assertNotNull(RNUtils.readableMapToJson(map));
    }

    @Test
    public void readableMapToJson_booleanValue_returnsNonNull() {
        ReadableMap map = mockMapWithSingleEntry("flag", ReadableType.Boolean);
        when(map.getBoolean("flag")).thenReturn(true);

        assertNotNull(RNUtils.readableMapToJson(map));
    }

    @Test
    public void readableMapToJson_intValue_returnsNonNull() {
        ReadableMap map = mockMapWithSingleEntry("count", ReadableType.Number);
        when(map.getInt("count")).thenReturn(42);

        assertNotNull(RNUtils.readableMapToJson(map));
    }

    @Test
    public void readableMapToJson_nullValue_returnsNonNull() {
        ReadableMap map = mockMapWithSingleEntry("key", ReadableType.Null);

        assertNotNull(RNUtils.readableMapToJson(map));
    }

    @Test
    public void readableMapToJson_nestedMap_returnsNonNull() {
        ReadableMap innerMap = mockMapWithSingleEntry("innerKey", ReadableType.String);
        when(innerMap.getString("innerKey")).thenReturn("innerValue");

        ReadableMap outerMap = mockMapWithSingleEntry("nested", ReadableType.Map);
        when(outerMap.getMap("nested")).thenReturn(innerMap);

        assertNotNull(RNUtils.readableMapToJson(outerMap));
    }

    @Test
    public void readableMapToJson_multipleKeys_returnsNonNull() {
        ReadableMapKeySetIterator iterator = mock(ReadableMapKeySetIterator.class);
        when(iterator.hasNextKey()).thenReturn(true, true, false);
        when(iterator.nextKey()).thenReturn("a", "b");

        ReadableMap map = mock(ReadableMap.class);
        when(map.keySetIterator()).thenReturn(iterator);
        when(map.getType("a")).thenReturn(ReadableType.String);
        when(map.getType("b")).thenReturn(ReadableType.Number);
        when(map.getString("a")).thenReturn("hello");
        when(map.getInt("b")).thenReturn(99);

        assertNotNull(RNUtils.readableMapToJson(map));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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
