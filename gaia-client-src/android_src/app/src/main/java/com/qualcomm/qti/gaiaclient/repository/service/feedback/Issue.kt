/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.feedback

data class Issue(val title: String, val id: String, val link: String) {
    constructor() : this("", "", "")
}
