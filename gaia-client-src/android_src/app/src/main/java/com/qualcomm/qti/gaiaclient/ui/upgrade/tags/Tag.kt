/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.tags

import androidx.annotation.ColorRes
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.R

private val TAGS = setOf(Tag.T1, Tag.T2)

sealed class Tag(
    val id: String,
    @ColorRes val backgroundColor: Int,
    @ColorRes val textColor: Int
) {
    object T1 : Tag("T1", R.color.tag_1_background, R.color.tag_1_text)
    object T2 : Tag("T2", R.color.tag_2_background, R.color.tag_2_text)
    class Other(tag: String) : Tag(tag, R.color.tag_3_background, R.color.tag_3_text)

    companion object {
        fun valueOf(tag: String): Tag {
            return TAGS.find { it.id == tag } ?: Other(tag)
        }

        fun valuesFrom(values: Array<String>): Array<Tag> {
            val tags = mutableSetOf<Tag>()
            values.forEach { tags.add(valueOf(it)) }
            return tags.sortedBy { if (it is Other) TAGS.size else TAGS.indexOf(it) }.toTypedArray()
        }

        fun entries(): Pair<Array<String>, Array<String>> {
            val values = mutableSetOf<String>()
            TAGS.forEach { values.add(it.id) }
            return Pair(values.toTypedArray(), values.toTypedArray())
        }
    }
}
