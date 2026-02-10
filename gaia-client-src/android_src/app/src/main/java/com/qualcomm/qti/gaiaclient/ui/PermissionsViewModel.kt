/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val _states: MutableMap<Permissions.Categories, StickyLiveData<PermissionState>> = mutableMapOf()

    private val tokens: MutableMap<Permissions.Categories, AtomicBoolean> = mutableMapOf()

    fun getState(category: Permissions.Categories): LiveData<PermissionState> {
        return _states.getOrPut(category) { StickyLiveData() }
    }

    fun setState(category: Permissions.Categories, state: PermissionState) {
        val liveData = getState(category)
        (liveData as StickyLiveData).postValue(state)
    }

    fun getToken(category: Permissions.Categories): AtomicBoolean {
        return tokens.getOrPut(category) { AtomicBoolean(false) }
    }
}
