/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.ArrayMap
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.qualcomm.qti.gaiaclient.R

private const val TAG: String = "PermissionsActivity"

abstract class PermissionsActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    val permissionViewModel: PermissionsViewModel by viewModels()

    private val launchers = ArrayMap<Permissions.Categories, ActivityResultLauncher<Array<String>>>()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // only called for normal permissions
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // anomaly
        if (permissions.size < grantResults.size) return

        // only invoked for a category
        val category = Permissions.Categories.valueFrom(requestCode) ?: return
        var state: PermissionState = PermissionState.GRANTED

        for (i in 0..permissions.size) {
            if (category.contains(permissions[i])) {
                val result = if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    PermissionState.GRANTED else PermissionState.DENIED
                state = if (state == PermissionState.GRANTED) result else state
            }
        }

        // update category state
        onPermissionsStateChanged(category, state)
    }

    /**
     * Checks if the permissions of the category have been granted.
     * If they have not, the function attempts to launch the permission request.
     * Shows the appropriate request rationale if necessary.
     *
     * @return True if the permission request was launched, false if the permission is granted and/or not required.
     */
    fun requestCategoryPermissions(category: Permissions.Categories): Boolean {
        if (category.isGranted(this)) {
            // permissions already granted
            return false
        }
        if (!permissionViewModel.getToken(category).compareAndSet(false, true)) {
            // already a permission request in progress
            return true
        }
        return if (category.isRuntime) {
            requestRuntimePermissions(category)
        } else {
            requestNormalPermissions(category)
            false
        }
    }

    abstract fun showPermissionsError(category: Permissions.Categories)

    fun navigateToPermissionsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", application.packageName, null)
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        application.startActivity(intent)
    }

    private fun requestRuntimePermissions(category: Permissions.Categories): Boolean {
        val launcher = launchers[category]

        if (launcher == null) {
            Log.w(TAG, "[requestRuntimePermissions] no launcher")
            // unexpected
            // no permissions to request
            permissionViewModel.getToken(category).set(false)
            return false
        }

        if (requirePermissionRationale(category)) {
            // must explain to the user why the permissions are needed
            showPermissionRequestRationale(launcher, category)
        } else {
            launcher.launch(category.ids)
        }
        return true
    }

    private fun initActivityResultLauncher(category: Permissions.Categories) {
        if (category.isRuntime) {
            val launcher = registerForActivityResult(
                RequestMultiplePermissions()
            ) { areGranted: Map<String, Boolean> ->
                run {
                    permissionViewModel.getToken(category).set(false)
                    onPermissionsResult(category, areGranted)
                }
            }
            launchers[category] = launcher
        }
    }

    private fun containsKeys(permissionsStateMap: Map<String, Boolean>, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (!permissionsStateMap.containsKey(permission)) {
                return false
            }
        }
        return true
    }

    private fun arePermissionsGranted(
        permissionsStateMap: Map<String, Boolean>,
        category: Permissions.Categories
    ): Boolean {
        for (permission in category.ids) {
            val state = permissionsStateMap[permission]
            if (state == null || !state) {
                return false
            }
        }
        return true
    }

    private fun requestNormalPermissions(category: Permissions.Categories) {
        // no need to inform the user, we directly request the permissions
        ActivityCompat.requestPermissions(this, category.ids, category.requestCode)
    }

    private fun onPermissionsResult(category: Permissions.Categories, granted: Map<String, Boolean>) {
        if (!containsKeys(granted, category.ids)) {
            // If null, the request was wrong.
            return
        }
        if (arePermissionsGranted(granted, category)) {
            // Set permissions as 'Granted'
            onPermissionsStateChanged(category, PermissionState.GRANTED)
        } else {
            if (requirePermissionRationale(category)) {
                // Set permissions as 'Denied'
                onPermissionsStateChanged(category, PermissionState.DENIED)
            } else {
                // Set permissions as 'Don't ask again'
                onPermissionsStateChanged(category, PermissionState.DENIED_PERMANENTLY)
            }
        }
    }

    private fun requirePermissionRationale(category: Permissions.Categories): Boolean {
        for (permission in category.ids) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true
            }
        }
        return false
    }

    private fun showPermissionRequestRationale(
        launcher: ActivityResultLauncher<Array<String>>,
        category: Permissions.Categories
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.alert_permissions_title)
        builder.setMessage(category.rationaleMessage)
        builder.setCancelable(false) // the user cannot cancel the dialog with native back button
        builder.setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
            launcher.launch(category.ids)
        }
        builder.setNegativeButton(R.string.button_cancel) { _: DialogInterface?, _: Int -> }
        builder.show()
    }

    private fun onPermissionsStateChanged(category: Permissions.Categories, state: PermissionState) {
        permissionViewModel.getToken(category).set(false)
        permissionViewModel.setState(category, state)
    }

    /* init launchers to request permissions */
    init {
        for (category in Permissions.Categories.values()) {
            initActivityResultLauncher(category)
        }
    }
}
