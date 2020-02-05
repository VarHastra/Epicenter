package com.github.varhastra.epicenter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val IO_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()
val NETWORKING_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()

private val dbDispatcher = IO_EXECUTOR.asCoroutineDispatcher()

val Dispatchers.DB get() = dbDispatcher

fun ioThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}

fun networkingThread(f: () -> Unit) {
    NETWORKING_EXECUTOR.execute(f)
}