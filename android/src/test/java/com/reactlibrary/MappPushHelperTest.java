package com.reactlibrary;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.appoxee.Appoxee;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
@LooperMode(LooperMode.Mode.PAUSED)
public class MappPushHelperTest {
    private Application application;
    private MockedStatic<Appoxee> mockedAppoxee;
    private ExecutorService executor;

    @Before
    public void setUp() {
        application = RuntimeEnvironment.getApplication();
        mockedAppoxee = mockStatic(Appoxee.class);
        executor = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() throws Exception {
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        mockedAppoxee.close();
    }

    @Test
    public void initializeOnMainThread_engagesImmediatelyOnMainThread() {
        AtomicBoolean invokedOnMain = new AtomicBoolean(false);
        mockedAppoxee.when(() -> Appoxee.engage(any(), isNull())).thenAnswer(invocation -> {
            invokedOnMain.set(Looper.myLooper() == Looper.getMainLooper());
            return null;
        });

        assertTrue(MappPushHelper.initialize(application));

        assertTrue(invokedOnMain.get());
    }

    @Test
    public void initializeFromWorker_engagesOnMainLooperAndWaitsForCompletion() throws Exception {
        Handler handler = mock(Handler.class);
        CountDownLatch posted = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable operation = invocation.getArgument(0);
            boolean accepted = new Handler(Looper.getMainLooper()).post(operation);
            posted.countDown();
            return accepted;
        }).when(handler).post(any(Runnable.class));
        mockedAppoxee.when(() -> Appoxee.engage(any(), isNull())).thenAnswer(invocation -> {
            assertEquals(Looper.getMainLooper(), Looper.myLooper());
            return null;
        });

        Future<Boolean> initialized = executor.submit(() ->
                MappPushHelper.initialize(application, handler, 1_000));
        assertTrue(posted.await(1, TimeUnit.SECONDS));
        assertFalse(initialized.isDone());
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        assertTrue(initialized.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void handlersReturnFalseWhenInitializationThrows() {
        mockedAppoxee.when(() -> Appoxee.engage(any(), isNull()))
                .thenThrow(new IllegalStateException("not ready"));

        assertFalse(MappPushHelper.handleMessage(application, mock(RemoteMessage.class)));
        assertFalse(MappPushHelper.handleNewToken(application, "token"));
    }

    @Test
    public void handlersReturnFalseWhenMainLooperDispatchTimesOut() throws Exception {
        Handler handler = mock(Handler.class);
        when(handler.post(any(Runnable.class))).thenReturn(true);
        RemoteMessage message = mock(RemoteMessage.class);

        Future<Boolean> messageResult = executor.submit(() ->
                MappPushHelper.handleMessage(application, message, handler, 20));
        assertFalse(messageResult.get(1, TimeUnit.SECONDS));

        Future<Boolean> tokenResult = executor.submit(() ->
                MappPushHelper.handleNewToken(application, "token", handler, 20));
        assertFalse(tokenResult.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void interruptionIsPreservedWhileWaitingForMainLooper() throws Exception {
        Handler handler = mock(Handler.class);
        when(handler.post(any(Runnable.class))).thenReturn(true);

        Future<Boolean> interrupted = executor.submit(() -> {
            Thread.currentThread().interrupt();
            boolean initialized = MappPushHelper.initialize(application, handler, 1_000);
            return !initialized && Thread.currentThread().isInterrupted();
        });

        assertTrue(interrupted.get(1, TimeUnit.SECONDS));
    }
}
