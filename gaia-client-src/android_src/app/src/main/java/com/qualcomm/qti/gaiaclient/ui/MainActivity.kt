/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.qualcomm.qti.gaiaclient.BuildConfig
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.databinding.ActivityMainBinding
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import com.qualcomm.qti.gaiaclient.ui.common.SnackMessage
import dagger.hilt.android.AndroidEntryPoint

private val NAVIGATION_WITHOUT_BACK_ARROW = arrayOf(
    R.id.destination_discovery,
    R.id.destination_connection,
    R.id.destination_information,
    R.id.destination_settings,
    R.id.destination_upgrade
)

@AndroidEntryPoint
class MainActivity : PermissionsActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var snackbar: Snackbar? = null
    private val observers: DataObservers = DataObservers()

    private val launcher =
        registerForActivityResult(StartActivityForResult()) { viewModel.setBluetoothDialogActive(false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init toolbar
        setSupportActionBar(findViewById(R.id.top_bar))

        // init view model
        observers.start()

        // init navigation controller
        initNavigationController()
    }

    override fun onStart() {
        super.onStart()

        // Launch the permission request on app start: Bluetooth is required for the whole usage of the app
        if (!Permissions.Categories.BLUETOOTH.isGranted(this)) {
            requestCategoryPermissions(Permissions.Categories.BLUETOOTH)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateTitle(title: String?) {
        val bar = supportActionBar
        if (bar != null) {
            bar.title = title
        }
    }

    override fun showPermissionsError(category: Permissions.Categories) {
        val message = getString(category.errorMessage)
        val listener = View.OnClickListener { navigateToPermissionsSettings() }
        viewModel.snackMessage =
            SnackMessage(message, Snackbar.LENGTH_INDEFINITE, R.string.button_go_to_settings, listener)
    }

    private fun initNavigationController() {
        val navController = findNavController(this, R.id.nav_host_fragment)
        binding.navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
            onDestinationChanged(destination)
        }

        // Set top level destinations - those will not have a back arrow on the app bar
        val set: Set<Int> = HashSet(listOf(*NAVIGATION_WITHOUT_BACK_ARROW))
        val appBarConfiguration: AppBarConfiguration = AppBarConfiguration.Builder(set).build()
        setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    private fun updateBottomMenuVisibility(visibility: Int) {
        val old = binding.navView.visibility
        if (old != visibility) {
            binding.navView.visibility = visibility
            // forces the snack bar to be regenerated
            if (snackbar != null) {
                onSnackMessage(viewModel.snackMessage)
            }
        }
    }

    private fun onDestinationChanged(destination: NavDestination) {
        viewModel.onDestinationChanged(destination.id)
    }

    private fun updateIconBarVisibility(show: Boolean) {
        val bar = supportActionBar ?: return

        // Restore elevation as some fragments sets it to 0
        bar.elevation = 11f
        bar.setIcon(if (show) R.drawable.ic_app_logo else 0)
        if (show) {
            // we add the version of the application to the tile bar
            bar.title =
                applicationContext.getString(R.string.title_version, BuildConfig.VERSION_NAME)
        }
    }

    /**
     * To act dependently to the new bluetooth dialog state: if disabled this method calls the system to
     * enable the Bluetooth. This triggers a system dialog to ask the user to do so.
     */
    private fun onBluetoothRequestStateChanged(shouldRequest: Boolean?) {
        if (shouldRequest == null) {
            return
        }
        if (shouldRequest && Permissions.Categories.BLUETOOTH.isGranted(this)) {
            requestBluetooth()
        }
    }

    private fun requestBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        viewModel.setBluetoothDialogActive(true)
        launcher.launch(enableBtIntent)
    }

    private fun onSnackMessage(message: SnackMessage?) {
        if (snackbar != null) {
            snackbar!!.dismiss()
            snackbar = null
        }
        if (message == null) {
            return
        }
        snackbar = Snackbar.make(binding.root, message.message, message.length)
        if (viewModel.isNavigationVisible) {
            snackbar!!.anchorView = binding.navView
        }
        snackbar!!.setAction(message.action) { view: View? ->
            snackbar = null
            if (message.hasAction()) {
                message.clickListener.onClick(view)
            }
        }
        snackbar!!.show()
    }

    private fun onPermissionStateChanged(state: PermissionState) {
        when (state) {
            PermissionState.GRANTED ->                 // granted: can use Bluetooth
                viewModel.updateBluetoothState(applicationContext)
            PermissionState.DENIED ->                  // denied (only once): ask again.
                requestCategoryPermissions(Permissions.Categories.BLUETOOTH)
            PermissionState.DENIED_PERMANENTLY -> {    // denied permanently: launch an error snack bar
                showPermissionsError(Permissions.Categories.BLUETOOTH)
            }
        }
    }

    private inner class DataObservers : Observers() {
        private val navigationObserver = Observer { visibility: Int -> updateBottomMenuVisibility(visibility) }
        private val iconBarVisibilityObserver = Observer { show: Boolean -> updateIconBarVisibility(show) }
        private val bluetoothRequestObserver = Observer { state: Boolean? -> onBluetoothRequestStateChanged(state) }
        private val snackMessageObserver = Observer { message: SnackMessage? -> onSnackMessage(message) }
        private val bluetoothPermissionObserver =
            Observer { state: PermissionState -> this@MainActivity.onPermissionStateChanged(state) }

        override fun registerObservers() {
            viewModel.observeNavigationVisibility(this@MainActivity, navigationObserver)
            viewModel.observeIconBarVisibility(this@MainActivity, iconBarVisibilityObserver)
            viewModel.observeBluetoothRequestState(this@MainActivity, bluetoothRequestObserver)
            viewModel.observeSnackMessage(this@MainActivity, snackMessageObserver)
            permissionViewModel.getState(Permissions.Categories.BLUETOOTH)
                .observe(this@MainActivity, bluetoothPermissionObserver)
        }

        override fun unregisterObservers() {
            // never used: the owner is the activity, when killed the observers would be removed
        }
    }
}
