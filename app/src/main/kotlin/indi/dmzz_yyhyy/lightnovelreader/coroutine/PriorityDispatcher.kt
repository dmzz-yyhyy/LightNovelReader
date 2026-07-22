package indi.dmzz_yyhyy.lightnovelreader.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


@OptIn(InternalCoroutinesApi::class)
class PriorityDispatcher(
    private val maxConcurrency: Int,
    private val defaultPriority: Int = 0,
    startPaused: Boolean = false,
    backendDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CoroutineDispatcher() {
    init {
        require(maxConcurrency > 0) { "maxConcurrency must be greater than 0" }
    }

    private val controlScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val workerScope = CoroutineScope(SupervisorJob() + backendDispatcher)
    private val commands = Channel<Command>(Channel.UNLIMITED)
    private val closed = CompletableDeferred<Unit>()

    init {
        controlScope.launch {
            runCoordinator(startPaused)
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val priority = context[Priority]?.value ?: defaultPriority
        val job = context[Job]
        check(commands.trySend(Command.Enqueue(priority, block, job)).isSuccess) {
            "dispatcher is closed"
        }
    }

    suspend fun close() {
        if (!closed.isCompleted) {
            commands.send(Command.Shutdown)
        }
        closed.await()
    }

    private suspend fun runCoordinator(startPaused: Boolean) {
        val readyTasks = TaskHeap()
        val pendingStarts = TaskHeap()
        val knownJobs = mutableSetOf<Job>()
        val activeJobs = mutableSetOf<Job>()
        var paused = startPaused
        var acceptingTasks = true
        var nextSequence = 0L

        fun enqueue(priority: Int, block: Runnable, job: Job?) {
            val task = ScheduledTask(
                priority = priority,
                sequence = nextSequence++,
                block = block,
                job = job,
            )

            if (job == null) {
                readyTasks.add(task)
                return
            }

            if (job !in knownJobs) {
                knownJobs += job
                job.invokeOnCompletion {
                    commands.trySend(Command.JobCompleted(job))
                }
                pendingStarts.add(task)
                return
            }

            if (job in activeJobs) {
                readyTasks.add(task)
            } else {
                pendingStarts.add(task)
            }
        }

        fun launchTask(task: ScheduledTask) {
            val taskJob = task.job
            if (taskJob != null) {
                activeJobs += taskJob
            }

            workerScope.launch {
                runCatching { task.block.run() }
                    .onFailure(Throwable::printStackTrace)
            }
        }

        fun launchReadyTasks() {
            if (paused) {
                return
            }

            while (readyTasks.isNotEmpty()) {
                launchTask(readyTasks.removeFirst())
            }

            while (activeJobs.size < maxConcurrency && pendingStarts.isNotEmpty()) {
                launchTask(pendingStarts.removeFirst())
                while (readyTasks.isNotEmpty()) {
                    launchTask(readyTasks.removeFirst())
                }
            }
        }

        for (command in commands) {
            when (command) {
                is Command.Enqueue -> {
                    if (acceptingTasks) {
                        enqueue(command.priority, command.block, command.job)
                    }
                }

                is Command.JobCompleted -> {
                    activeJobs -= command.job
                    knownJobs -= command.job
                }

                Command.Pause -> paused = true
                Command.Resume -> paused = false
                Command.Shutdown -> acceptingTasks = false
            }

            launchReadyTasks()

            if (!acceptingTasks && activeJobs.isEmpty() && pendingStarts.isEmpty() && readyTasks.isEmpty()) {
                break
            }
        }

        commands.close()
        workerScope.cancel()
        controlScope.cancel()
        closed.complete(Unit)
    }

    class Priority(val value: Int) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<Priority>
    }

    private sealed interface Command {
        data class Enqueue(
            val priority: Int,
            val block: Runnable,
            val job: Job?,
        ) : Command

        data class JobCompleted(val job: Job) : Command
        data object Pause : Command
        data object Resume : Command
        data object Shutdown : Command
    }

    private data class ScheduledTask(
        val priority: Int,
        val sequence: Long,
        val block: Runnable,
        val job: Job?,
    )

    private class TaskHeap {
        private val items = mutableListOf<ScheduledTask>()

        fun add(task: ScheduledTask) {
            items += task
            siftUp(items.lastIndex)
        }

        fun removeFirst(): ScheduledTask {
            val first = items.first()
            val last = items.removeAt(items.lastIndex)
            if (items.isNotEmpty()) {
                items[0] = last
                siftDown()
            }
            return first
        }

        fun isEmpty(): Boolean = items.isEmpty()

        fun isNotEmpty(): Boolean = items.isNotEmpty()

        private fun siftUp(startIndex: Int) {
            var index = startIndex
            while (index > 0) {
                val parentIndex = (index - 1) / 2
                if (items[parentIndex] >= items[index]) {
                    return
                }
                items.swap(parentIndex, index)
                index = parentIndex
            }
        }

        private fun siftDown(startIndex: Int = 0) {
            var index = startIndex
            while (true) {
                val left = index * 2 + 1
                val right = left + 1
                if (left >= items.size) {
                    return
                }

                var best = left
                if (right < items.size && items[right] > items[left]) {
                    best = right
                }

                if (items[index] >= items[best]) {
                    return
                }

                items.swap(index, best)
                index = best
            }
        }

        private fun MutableList<ScheduledTask>.swap(first: Int, second: Int) {
            val tmp = this[first]
            this[first] = this[second]
            this[second] = tmp
        }

        private operator fun ScheduledTask.compareTo(other: ScheduledTask): Int {
            val priorityComparison = priority.compareTo(other.priority)
            return if (priorityComparison != 0) {
                priorityComparison
            } else {
                other.sequence.compareTo(sequence)
            }
        }
    }
}
