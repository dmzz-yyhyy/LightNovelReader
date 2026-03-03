@file:Suppress("unused")

package indi.dmzz_yyhyy.lightnovelreader.utils

import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.concurrent.withLock

class AdvancedPrioritySemaphore(permits: Int) {
    private val lock = ReentrantLock(true)
    private val condition = lock.newCondition()
    @OptIn(ExperimentalAtomicApi::class)
    private val sequenceGenerator = AtomicLong(0)
    private val waitingQueue = PriorityBlockingQueue<WaitingRequest>()
    private var available = permits

    data class WaitingRequest(
        val priority: Int,
        val sequenceNumber: Long,
        val permitsRequested: Int
    ) : Comparable<WaitingRequest> {
        override fun compareTo(other: WaitingRequest): Int {
            return if (this.priority != other.priority) {
                other.priority - this.priority // 数字越大优先级越高
            } else {
                this.sequenceNumber.compareTo(other.sequenceNumber)
            }
        }
    }

    fun acquire(priority: Int = 0) {
        acquire(1, priority)
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun acquire(permits: Int, priority: Int = 0) {
        lock.withLock {
            val request = WaitingRequest(priority, sequenceGenerator.fetchAndIncrement(), permits)
            waitingQueue.add(request)

            try {
                while (!canAcquire(request)) {
                    condition.await()
                }

                available -= permits
                waitingQueue.remove(request)

                // 检查是否可以唤醒其他等待的线程
                checkAndSignalNext()
            } catch (e: InterruptedException) {
                waitingQueue.remove(request)
                checkAndSignalNext()
                throw e
            }
        }
    }

    fun release() {
        release(1)
    }

    fun release(permits: Int) {
        lock.withLock {
            available += permits
            checkAndSignalNext()
        }
    }

    fun tryAcquire(permits: Int, priority: Int = 0): Boolean {
        lock.withLock {
            return if (canAcquireImmediately(permits, priority)) {
                available -= permits
                true
            } else {
                false
            }
        }
    }

    private fun canAcquire(request: WaitingRequest): Boolean {
        // 只有当这个请求是队列中优先级最高的且资源足够时才允许获取
        return waitingQueue.peek() == request && available >= request.permitsRequested
    }

    private fun canAcquireImmediately(permits: Int, priority: Int): Boolean {
        if (available < permits) return false

        // 检查是否有更高优先级的请求在等待
        val highestWaiting = waitingQueue.peek()
        return highestWaiting == null || priority > highestWaiting.priority
    }

    private fun checkAndSignalNext() {
        val nextRequest = waitingQueue.peek()
        if (nextRequest != null && available >= nextRequest.permitsRequested) {
            condition.signalAll()
        }
    }

    fun availablePermits(): Int {
        lock.withLock {
            return available
        }
    }

    fun queueLength(): Int {
        lock.withLock {
            return waitingQueue.size
        }
    }
}