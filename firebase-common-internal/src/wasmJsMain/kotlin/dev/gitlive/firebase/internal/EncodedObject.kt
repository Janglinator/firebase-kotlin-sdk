package dev.gitlive.firebase.internal

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

public val EncodedObject.js: JsonObject
    get() = buildJsonObject {
        getRaw().entries.forEach { (key, value) ->
            put(key, Json.encodeToJsonElement(value))
        }
    }

@PublishedApi
internal actual fun Any.asNativeMap(): Map<*, *>? {
    val json = when (this) {
        is Number, is Boolean, is String, is Collection<*>, is Array<*> -> null
        is Map<*, *> -> {
            if (keys.all { it is String }) {
                buildJsonObject {
                    mapKeys { (key, _) -> key as String }.forEach { (key, value) ->
                        put(key, Json.encodeToJsonElement(value))
                    }
                }
            } else {
                null
            }
        }
        else -> this as? JsonObject
    } ?: return null

    val mutableMap = mutableMapOf<String, Any?>()
    for (key in json.keys) {
        mutableMap[key] = json[key]
    }
    return mutableMap.toMap()
}