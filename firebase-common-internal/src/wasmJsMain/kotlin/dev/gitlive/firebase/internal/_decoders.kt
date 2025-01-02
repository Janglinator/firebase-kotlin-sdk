/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

public actual fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor, polymorphicIsNested: Boolean): CompositeDecoder = when (descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> decodeAsMap(false)
    StructureKind.LIST -> decodeAsList()
    StructureKind.MAP -> (value as JsonObject).entries.map { it.toPair() }.toTypedArray().let {
        FirebaseCompositeDecoder(
            it.size,
            settings,
        ) { desc, index ->
            it[index / 2].run {
                if (index % 2 == 0) {
                    val key = first
                    if (desc.getElementDescriptor(index).kind == PrimitiveKind.STRING) {
                        key
                    } else {
                        Json.parseToJsonElement(key)
                    }
                } else {
                    second
                }
            }
        }
    }

    is PolymorphicKind -> decodeAsMap(polymorphicIsNested)
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

public actual fun getPolymorphicType(value: Any?, discriminator: String): String =
    (value as JsonObject)[discriminator]?.jsonPrimitive?.content ?: ""

private fun FirebaseDecoder.decodeAsList(): CompositeDecoder = (value as List<*>).let {
    FirebaseCompositeDecoder(it.size, settings) { _, index -> it[index] }
}

private fun FirebaseDecoder.decodeAsMap(isNestedPolymorphic: Boolean): CompositeDecoder = (value as JsonObject).let { json ->
    FirebaseClassDecoder(json.size, settings, { json[it] != null }) { desc, index ->
        if (isNestedPolymorphic) {
            if (desc.getElementName(index) == "value") {
                json
            } else {
                json[desc.getElementName(index)]
            }
        } else {
            json[desc.getElementName(index)]
        }
    }
}