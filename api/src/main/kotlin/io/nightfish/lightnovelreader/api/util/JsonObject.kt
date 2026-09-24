package io.nightfish.lightnovelreader.api.util

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

/**
 * 返回一个空的[JsonObject]
 *
 * @return 空的[JsonObject]
 *
 * @since Api 2
 */
fun JsonObject.Companion.empty() = buildJsonObject { }