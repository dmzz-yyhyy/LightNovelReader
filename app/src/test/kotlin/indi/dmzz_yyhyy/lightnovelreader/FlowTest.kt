package indi.dmzz_yyhyy.lightnovelreader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

fun getFlow() = flow {
    emit("ciallo")
    delay(3.seconds)
    emit("end")
}

fun main() {
    println("Main start")
    runBlocking(Dispatchers.IO) {
        getFlow().collect {
            println(it)
        }
    }
    println("Main end")
}