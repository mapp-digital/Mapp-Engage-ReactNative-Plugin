package com.reactlibrary;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.appoxee.Appoxee;
import com.appoxee.internal.network.Call;
import com.appoxee.shared.AppoxeeOptions;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.google.firebase.messaging.RemoteMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
@LooperMode(LooperMode.Mode.PAUSED)
public class MappEngagementDispatcherTest {
    private Application application;
    private MockedStatic<Appoxee> statics;
    private ExecutorService executor;
    private final AtomicReference<Appoxee> installed = new AtomicReference<>();
    private final List<AppoxeeOptions> attempts = new ArrayList<>();
    private final List<Appoxee> instances = new ArrayList<>();
    private boolean failNextEngage;
    private boolean readyOnEngage;

    @Before
    public void setUp() {
        application = RuntimeEnvironment.getApplication();
        executor = Executors.newSingleThreadExecutor();
        statics = mockStatic(Appoxee.class);
        statics.when(Appoxee::instance).thenAnswer(invocation -> {
            if (installed.get() == null) throw new NullPointerException("not engaged");
            return installed.get();
        });
        statics.when(() -> Appoxee.engage(any(), nullable(AppoxeeOptions.class)))
                .thenAnswer(invocation -> {
                    assertEquals(Looper.getMainLooper(), Looper.myLooper());
                    attempts.add(invocation.getArgument(1));
                    if (failNextEngage) {
                        failNextEngage = false;
                        throw new IllegalStateException("initialization failed");
                    }
                    if (installed.get() == null) installed.set(newSdk());
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private Appoxee newSdk() {
        Appoxee sdk = mock(Appoxee.class);
        when(sdk.isReady()).thenReturn(readyOnEngage);
        when(sdk.isPushMessageFromMapp(any())).thenReturn(true);
        when(sdk.updateFirebaseToken(anyString())).thenReturn(mock(Call.class));
        instances.add(sdk);
        return sdk;
    }

    @After
    public void tearDown() throws Exception {
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        statics.close();
    }

    private AppoxeeOptions options(String key) {
        return new AppoxeeOptions(AppoxeeOptions.Server.L3, key, "app", "tenant");
    }

    private boolean engage(AppoxeeOptions options) {
        return MappEngagementDispatcher.engageBlocking(application, options);
    }

    @Test
    public void jsPushAndTokenShareReceiverSetupAndDelegateReuseToNativeSdk() {
        readyOnEngage = true;
        ReactApplicationContext context = mock(ReactApplicationContext.class);
        when(context.getApplicationContext()).thenReturn(application);
        RNMappPluginModule module = new RNMappPluginModule(context);
        Promise promise = mock(Promise.class);
        module.engage("key", "project", "L3", "app", "tenant", promise);
        verify(promise).resolve(true);
        assertTrue(MappPushHelper.handleMessage(application, mock(RemoteMessage.class)));
        assertTrue(MappPushHelper.handleNewToken(application, "token"));

        assertEquals(3, attempts.size());
        verify(installed.get(), never()).subscribe(any());
        verify(installed.get(), times(3)).setPushBroadcast(MyPushBroadcastReceiver.class);
        verify(installed.get()).handlePushMessage(any());
        verify(installed.get()).updateFirebaseToken("token");
    }

    @Test
    public void coldPushAndTokenWorkWithoutCreatingAReactContext() {
        readyOnEngage = true;
        assertTrue(MappPushHelper.handleMessage(application, mock(RemoteMessage.class)));
        assertTrue(MappPushHelper.handleNewToken(application, "token"));
        assertEquals(2, attempts.size());
        assertNull(attempts.get(0));
        verify(installed.get(), times(2)).setPushBroadcast(MyPushBroadcastReceiver.class);
        verify(installed.get(), never()).subscribe(any());
    }

    @Test
    public void sameAndChangedOptionsAreForwardedToTheNativeReuseBoundary() {
        AppoxeeOptions a = options("a");
        AppoxeeOptions b = options("b");
        assertTrue(engage(a));
        assertTrue(engage(a));
        assertTrue(engage(b));
        assertTrue(engage(null));
        assertEquals(4, attempts.size());
        assertSame(a, attempts.get(0));
        assertSame(a, attempts.get(1));
        assertSame(b, attempts.get(2));
        assertNull(attempts.get(3));
    }

    @Test
    public void synchronousFailureReturnsFalseAndDoesNotPreventTheNextAttempt() {
        failNextEngage = true;
        AppoxeeOptions requested = options("a");
        assertFalse(engage(requested));
        assertTrue(engage(requested));
        assertEquals(2, attempts.size());
        verify(installed.get()).setPushBroadcast(MyPushBroadcastReceiver.class);
    }

    @Test
    public void asyncCompletionRegistersReceiverBeforeCallbacksWithoutWaitingForReadiness() {
        Runnable afterEngage = mock(Runnable.class);
        MappEngagementDispatcher.EngagementCallback callback =
                mock(MappEngagementDispatcher.EngagementCallback.class);
        MappEngagementDispatcher.engageAsync(application, options("a"), afterEngage, callback);
        org.mockito.InOrder order = inOrder(installed.get(), afterEngage, callback);
        order.verify(installed.get()).setPushBroadcast(MyPushBroadcastReceiver.class);
        order.verify(afterEngage).run();
        order.verify(callback).onSuccess();
        assertFalse(installed.get().isReady());
    }

    @Test
    public void asyncWorkerDispatchesSetupAndSuccessOnMain() throws Exception {
        MappEngagementDispatcher.EngagementCallback callback =
                mock(MappEngagementDispatcher.EngagementCallback.class);
        doAnswer(invocation -> {
            assertEquals(Looper.getMainLooper(), Looper.myLooper());
            return null;
        }).when(callback).onSuccess();
        executor.submit(() -> MappEngagementDispatcher.engageAsync(
                application, options("a"), null, callback)).get(1, TimeUnit.SECONDS);
        verifyNoInteractions(callback);
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        verify(installed.get()).setPushBroadcast(MyPushBroadcastReceiver.class);
        verify(callback).onSuccess();
    }

    @Test
    public void receiverFailureDoesNotReportSuccessAndRegistrationCanBeRetried() {
        assertTrue(engage(options("a")));
        doThrow(new IllegalStateException("receiver failed")).doNothing()
                .when(installed.get()).setPushBroadcast(MyPushBroadcastReceiver.class);
        MappEngagementDispatcher.EngagementCallback callback =
                mock(MappEngagementDispatcher.EngagementCallback.class);
        Runnable afterEngage = mock(Runnable.class);
        MappEngagementDispatcher.engageAsync(application, options("a"), afterEngage, callback);
        verify(callback).onFailure(any(IllegalStateException.class));
        verify(callback, never()).onSuccess();
        verifyNoInteractions(afterEngage);
        assertTrue(engage(null));
        assertEquals(3, attempts.size());
    }

    @Test
    public void timedOutQueuedConfigurationCannotRunLater() throws Exception {
        assertTrue(engage(options("a")));
        Handler handler = mock(Handler.class);
        AtomicReference<Runnable> queued = new AtomicReference<>();
        doAnswer(invocation -> {
            queued.set(invocation.getArgument(0));
            return true;
        }).when(handler).post(any(Runnable.class));
        assertFalse(executor.submit(() -> MappEngagementDispatcher.engageBlocking(
                application, options("b"), handler, 20)).get(1, TimeUnit.SECONDS));
        verify(handler).removeCallbacks(queued.get());
        queued.get().run(); // Even if removal loses a race, the abandoned request does nothing.
        assertTrue(engage(options("a")));
        assertEquals(2, attempts.size());
        assertEquals("a", attempts.get(1).getSdkKey());
    }

    @Test
    public void interruptedQueuedRequestIsAbandonedAndInterruptFlagPreserved() throws Exception {
        Handler handler = mock(Handler.class);
        AtomicReference<Runnable> queued = new AtomicReference<>();
        doAnswer(invocation -> {
            queued.set(invocation.getArgument(0));
            return true;
        }).when(handler).post(any(Runnable.class));
        assertTrue(executor.submit(() -> {
            Thread.currentThread().interrupt();
            boolean result = MappEngagementDispatcher.engageBlocking(application, options("b"), handler, 500);
            boolean preserved = Thread.currentThread().isInterrupted();
            Thread.interrupted();
            return !result && preserved;
        }).get(1, TimeUnit.SECONDS));
        queued.get().run();
        assertTrue(attempts.isEmpty());
        verify(handler).removeCallbacks(queued.get());
    }

    @Test
    public void timedOutRequestThatAlreadyStartedDoesNotRemoveRunningOperation() throws Exception {
        Handler handler = mock(Handler.class);
        AtomicReference<Runnable> queued = new AtomicReference<>();
        java.util.concurrent.CountDownLatch posted = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
        doAnswer(invocation -> {
            queued.set(invocation.getArgument(0));
            posted.countDown();
            assertTrue(started.await(1, TimeUnit.SECONDS));
            return true;
        }).when(handler).post(any(Runnable.class));
        java.util.concurrent.Future<Boolean> result = executor.submit(() ->
                MappEngagementDispatcher.engageBlocking(application, options("a"), handler, 20));
        assertTrue(posted.await(1, TimeUnit.SECONDS));
        statics.when(() -> Appoxee.engage(any(), nullable(AppoxeeOptions.class)))
                .thenAnswer(invocation -> {
                    started.countDown();
                    // The main operation has started; let the waiting worker time out before it finishes.
                    assertFalse(result.get(1, TimeUnit.SECONDS));
                    attempts.add(invocation.getArgument(1));
                    installed.set(newSdk());
                    return null;
                });
        queued.get().run();
        assertTrue(engage(options("a")));
        assertEquals(2, attempts.size());
        verify(handler, never()).removeCallbacks(any(Runnable.class));
    }

    @Test
    public void rejectedQueuePostReturnsFailure() throws Exception {
        Handler handler = mock(Handler.class);
        when(handler.post(any(Runnable.class))).thenReturn(false);
        assertFalse(executor.submit(() -> MappEngagementDispatcher.engageBlocking(
                application, options("a"), handler, 500)).get(1, TimeUnit.SECONDS));
        assertTrue(attempts.isEmpty());
    }
}
