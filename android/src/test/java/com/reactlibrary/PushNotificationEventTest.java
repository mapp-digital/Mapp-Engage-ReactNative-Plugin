package com.reactlibrary;

import com.appoxee.shared.MappPush;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests for PushNotificationEvent.
 *
 * getName() is pure — returns a constant, no JNI or SDK needed.
 * getBody() delegates to RNUtils.getPushMessageToJSon() which calls new WritableNativeMap()
 * and therefore requires JNI — excluded here.
 */
public class PushNotificationEventTest {

    private static final String EXPECTED_EVENT_NAME = "com.mapp.rich_message_received";

    @Test
    public void getName_alwaysReturnsPushReceivedEvent() {
        MappPush mappPush = mock(MappPush.class);
        PushNotificationEvent event = new PushNotificationEvent(mappPush, "onPushReceived");
        assertEquals(EXPECTED_EVENT_NAME, event.getName());
    }

    @Test
    public void getName_sameConstantRegardlessOfType() {
        MappPush mappPush = mock(MappPush.class);

        // All event types share the same event name channel — the "type" field is in the body
        for (String type : new String[]{"onPushReceived", "onPushOpened", "onPushDismissed",
                "onSilentPush", "onPushButtonClicked", "onRichPush"}) {
            PushNotificationEvent event = new PushNotificationEvent(mappPush, type);
            assertEquals(
                    "Event name must be constant for type: " + type,
                    EXPECTED_EVENT_NAME,
                    event.getName()
            );
        }
    }
}
