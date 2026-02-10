/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.getfiles

data class Filter(
    val trueTags: Array<String>? = null,
    val falseTags: Array<String>? = null,
    val createdAfter: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Filter

        if (trueTags != null) {
            if (other.trueTags == null) return false
            if (!trueTags.contentEquals(other.trueTags)) return false
        } else if (other.trueTags != null) return false
        if (falseTags != null) {
            if (other.falseTags == null) return false
            if (!falseTags.contentEquals(other.falseTags)) return false
        } else if (other.falseTags != null) return false
        if (createdAfter != other.createdAfter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trueTags?.contentHashCode() ?: 0
        result = 31 * result + (falseTags?.contentHashCode() ?: 0)
        result = 31 * result + (createdAfter?.hashCode() ?: 0)
        return result
    }

}
