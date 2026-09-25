package indi.dmzz_yyhyy.lightnovelreader.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.EmptyCoroutineContext

class PriorityDispatcherTest {
    @Test
    fun nestedScopesCompleteAtCapacityWithoutExceedingRequestLimit() = runBlocking {
        val dispatcher = PriorityDispatcher(5, backendDispatcher = Dispatchers.Unconfined)
        // Detached requests let the timeout report a deadlock without hanging runBlocking cleanup.
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val entered = Channel<Unit>(Channel.UNLIMITED)
        val release = CompletableDeferred<Unit>()
        val active = AtomicInteger()
        val peak = AtomicInteger()
        try {
            val requests = List(10) {
                scope.async(start = CoroutineStart.UNDISPATCHED) {
                    withContext(dispatcher) {
                        peak.accumulateAndGet(active.incrementAndGet(), ::maxOf)
                        try {
                            coroutineScope {
                                withContext(Dispatchers.IO) {
                                    entered.send(Unit)
                                    release.await()
                                }
                            }
                        } finally {
                            active.decrementAndGet()
                        }
                    }
                }
            }
            val enqueued = CompletableDeferred<Unit>()
            dispatcher.dispatch(EmptyCoroutineContext, Runnable { enqueued.complete(Unit) })
            withTimeout(2_000) {
                enqueued.await()
                repeat(5) { entered.receive() }
            }
            assertEquals(5, active.get())
            release.complete(Unit)
            assertNotNull("Nested scopes must resume while all request slots are occupied",
                withTimeoutOrNull(2_000) { requests.awaitAll() })
            assertEquals(5, peak.get())
            assertEquals(0, active.get())
        } finally {
            scope.cancel()
            withTimeoutOrNull(1_000) { dispatcher.close() }
        }
    }

    @Test
    fun cancellingNestedScopeRunsFinallyAndReleasesRequestSlot() = runBlocking {
        val dispatcher = PriorityDispatcher(1)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val entered = CompletableDeferred<Unit>()
        val finished = CompletableDeferred<Unit>()
        try {
            val request = scope.async {
                withContext(dispatcher) {
                    try {
                        coroutineScope {
                            withContext(Dispatchers.IO) {
                                entered.complete(Unit)
                                awaitCancellation()
                            }
                        }
                    } finally {
                        finished.complete(Unit)
                    }
                }
            }
            withTimeout(2_000) { entered.await() }
            val next = scope.async(start = CoroutineStart.UNDISPATCHED) {
                withContext(dispatcher) { "next request" }
            }
            request.cancel()
            assertNotNull("Cancellation must resume the nested scope and execute finally",
                withTimeoutOrNull(2_000) { finished.await(); request.join() })
            assertEquals("next request", withTimeoutOrNull(2_000) { next.await() })
        } finally {
            scope.cancel()
            withTimeoutOrNull(1_000) { dispatcher.close() }
        }
    }
}
