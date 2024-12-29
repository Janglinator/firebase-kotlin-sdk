/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.Duration.Companion.minutes

actual fun runTest(test: suspend CoroutineScope.() -> Unit) =
    kotlinx.coroutines.test.runTest(timeout = 5.minutes) { test() }

actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) {
    kotlinx.coroutines.test.runTest { action() }
}

actual fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any = buildJsonObject {
    pairs.forEach { (key, value) ->
        put((key as? String) ?: Json.encodeToString(key), Json.encodeToJsonElement(value))
    }
}

actual fun nativeListOf(vararg elements: Any?): Any = elements

actual fun nativeAssertEquals(expected: Any?, actual: Any?) {
    kotlin.test.assertEquals(Json.encodeToString(expected), Json.encodeToString(actual))
}
