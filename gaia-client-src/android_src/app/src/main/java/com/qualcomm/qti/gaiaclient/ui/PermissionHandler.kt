/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * To handle permissions checks and requests for a category.
 *
 * @param category       the category to handle permission for
 * @param run            the function this handler should run when it is safe to do so, aka when the permission is
 *                       granted.
 */
abstract class PermissionHandler(val category: Permissions.Categories, val run: () -> Unit) {

    private val started: AtomicBoolean = AtomicBoolean(false)

    private val observer: Observer<PermissionState> = Observer { state -> onPermissionStateChanged(state) }

    abstract val viewModel: PermissionsViewModel

    abstract val owner: LifecycleOwner

    /**
     * Checks whether the category permissions have been granted, prior to run the function:
     * Launches the permission request if it has not been granted: the result will be provided via the given observer.
     * Launches an error dialog if it has been permanently denied.
     * Runs the given function if it has been granted.
     *
     * @param activity  the permission activity to use immediately to check permissions status and display any
     *                  immediate error/warning.
     */
    fun requestOrRun(activity: PermissionsActivity) {
        if (category.isGranted(activity)) {
            run()
            return
        }

        if (viewModel.getState(category).value == PermissionState.DENIED_PERMANENTLY) {
            activity.showPermissionsError(category)
            return
        }

        activity.requestCategoryPermissions(category)

        start()
    }

    /**
     * Receives the result when the permission state is updated for this category after requesting the permission(s)
     * to be granted.
     * This function runs the <code>run</code> function if the permission was granted.
     */
    private fun onPermissionStateChanged(state: PermissionState) {
        stop() // permission result received.
        if (state === PermissionState.GRANTED) {
            run()
        }
    }

    private fun start() {
        if (started.compareAndSet(false, true)) {
            viewModel.getState(category).observe(owner, observer)
        }
    }

    private fun stop() {
        if (started.compareAndSet(true, false)) {
            viewModel.getState(category).removeObserver(observer)
        }
    }

}
