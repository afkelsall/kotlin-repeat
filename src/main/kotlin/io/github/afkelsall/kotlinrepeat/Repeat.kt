package io.github.afkelsall.kotlinrepeat

import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class Repeat<T> {

    private var timeout: Long = 0
    private var timeUnit: TimeUnit = TimeUnit.SECONDS
    private var onTimeoutFunction: ((T) -> Unit)? = null

    private lateinit var repeatStatement: (Int) -> T

    private var sleepTime: Long = 100
    private var sleepTimeUnit: TimeUnit = TimeUnit.MILLISECONDS

    private var maxRepeats = -1

    fun statement(repeatStatement: (repeatCount: Int) -> T): Repeat<T> {
        this.repeatStatement = repeatStatement
        return this
    }

    fun until(untilStatement: (T) -> Boolean): T {
        var repeatCounter = 0
        var value = repeatStatement(repeatCounter)

        val startTime = LocalDateTime.now()

        while (!untilStatement(value)) {

            if (sleepTime > 0) {
                Thread.sleep(sleepTimeUnit.toMillis(sleepTime))
            }

            if (timeout > 0) {
                if (Duration.between(startTime, LocalDateTime.now()).toMillis() >= timeUnit.toMillis(timeout)) {
                    onTimeoutFunction?.invoke(value)
                    return value
                }
            }

            if (maxRepeats in 0..repeatCounter) return value

            repeatCounter++
            value = repeatStatement(repeatCounter)
        }

        return value
    }

    fun withSleep(sleepTime: Long, sleepTimeUnit: TimeUnit = TimeUnit.SECONDS): Repeat<T> {
        this.sleepTime = sleepTime
        this.sleepTimeUnit = sleepTimeUnit
        return this
    }

    fun withTimeout(timeout: Long, timeUnit: TimeUnit): Repeat<T> {
        this.timeout = timeout
        this.timeUnit = timeUnit
        return this
    }

    fun onTimeout(function: (T) -> Unit): Repeat<T> {
        this.onTimeoutFunction = function
        return this
    }

    fun maximumRepeats(maxRepeats: Int): Repeat<T> {
        this.maxRepeats = maxRepeats
        return this
    }

    fun untilTimeout(timeout: Long, timeUnit: TimeUnit): T {
        withTimeout(timeout, timeUnit)
        return until { false }
    }
}