package com.github.varhastra.epicenter

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val IO_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()
val NETWORKING_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()

fun ioThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}

fun networkingThread(f: () -> Unit) {
    NETWORKING_EXECUTOR.execute(f)
}