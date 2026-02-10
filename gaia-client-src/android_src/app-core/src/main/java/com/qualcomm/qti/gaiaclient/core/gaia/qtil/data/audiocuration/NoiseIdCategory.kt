/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

import java.util.Objects

private val CATEGORIES: List<NoiseIdCategory> =
    listOf(NoiseIdCategory.CategoryA, NoiseIdCategory.CategoryB, NoiseIdCategory.NotApplicable)

sealed class NoiseIdCategory(open val id: Int) {
    object CategoryA : NoiseIdCategory(0x00)
    object CategoryB : NoiseIdCategory(0x01)
    object NotApplicable : NoiseIdCategory(0xFF)
    class Undefined(value: Int) : NoiseIdCategory(value)

    companion object {
        @JvmStatic
        fun valueOf(value: Int): NoiseIdCategory {
            return CATEGORIES.firstOrNull { it.id == value } ?: Undefined(value)
        }
    }

    override fun toString(): String {
        return when (this) {
            is Undefined -> "Undefined($id)"
            else -> this.javaClass.simpleName
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return this is Undefined && other is Undefined && id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}
