/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.files

import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData
import com.qualcomm.qti.gaiaclient.ui.upgrade.tags.TagsAdapter
import com.qualcomm.qti.gaiaclient.utils.FILE_DATE_PATTERN
import com.qualcomm.qti.gaiaclient.utils.format
import com.qualcomm.qti.gaiaclient.utils.ifNullOrEmpty
import com.qualcomm.qti.gaiaclient.utils.parseIsoDate

class FileViewData(val file: FileData) : ListAdapterItemData {

    val createdOn: String = file.createdOn.parseIsoDate().format(FILE_DATE_PATTERN).ifNullOrEmpty { file.createdOn }
    val tags: Array<String> = file.tags
    val tagsAdapter = TagsAdapter(file.tags)

    fun title(defaultValue: String): String = file.title.ifNullOrEmpty { defaultValue }
    fun description(defaultValue: String): String = file.description.ifNullOrEmpty { defaultValue }

    override fun isSameContent(other: ListAdapterItemData?): Boolean = equals(other)

    override fun isSameItem(other: ListAdapterItemData?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileViewData
        return file.id != other.file.id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileViewData

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}
