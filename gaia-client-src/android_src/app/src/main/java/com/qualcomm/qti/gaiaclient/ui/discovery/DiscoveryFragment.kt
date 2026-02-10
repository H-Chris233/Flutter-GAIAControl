/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType
import com.qualcomm.qti.gaiaclient.databinding.FragmentDiscoveryBinding
import com.qualcomm.qti.gaiaclient.repository.connection.Device
import com.qualcomm.qti.gaiaclient.ui.Navigator
import com.qualcomm.qti.gaiaclient.ui.PermissionHandler
import com.qualcomm.qti.gaiaclient.ui.Permissions.Categories.BLUETOOTH
import com.qualcomm.qti.gaiaclient.ui.Permissions.Categories.BLUETOOTH_DISCOVERY
import com.qualcomm.qti.gaiaclient.ui.PermissionsActivity
import com.qualcomm.qti.gaiaclient.ui.PermissionsViewModel
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.DeviceViewData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoveryFragment : Fragment() {

    private val viewModel: DiscoveryViewModel by viewModels()
    private lateinit var binding: FragmentDiscoveryBinding

    private val backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // this prevents the app to close: this fragment is the navigation starting point
        }
    }

    private lateinit var permissionsViewModel: PermissionsViewModel // owner is activity => cannot use by viewModels()
    private val permissionHandlerDiscovery = object : PermissionHandler(BLUETOOTH_DISCOVERY, {
        val context = activity?.applicationContext
        if (context != null) {
            viewModel.refreshDevices(context, DiscoveryType.DISCOVERED)
        }
    }) {
        override val viewModel: PermissionsViewModel
            get() = permissionsViewModel
        override val owner: LifecycleOwner
            get() = this@DiscoveryFragment
    }

    private val permissionHandlerBluetooth = object : PermissionHandler(BLUETOOTH, {
        val context = activity?.applicationContext
        if (context != null) {
            viewModel.refreshDevices(context, DiscoveryType.CONNECTED)
            viewModel.refreshDevices(context, DiscoveryType.PAIRED)

            val activity = activity as PermissionsActivity?
            if (activity != null) {
                permissionHandlerDiscovery.requestOrRun(activity)
            }
        }
    }) {
        override val viewModel: PermissionsViewModel
            get() = permissionsViewModel
        override val owner: LifecycleOwner
            get() = this@DiscoveryFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = activity ?: return
        activity.onBackPressedDispatcher.addCallback(this, backPressedCallback)
        permissionsViewModel = ViewModelProvider(activity)[PermissionsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        val adapter = DiscoveryDevicesAdapter { deviceData: DeviceViewData -> navigateToConnection(deviceData) }
        binding.devicesList.adapter = adapter
        binding.swipeRefreshLayout.setOnRefreshListener { refreshList() }
        subscribeUI(adapter)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun subscribeUI(adapter: DiscoveryDevicesAdapter) {
        val owner = viewLifecycleOwner
        viewModel.itemsList.observe(owner) { adapter.submitList(it) }
        viewModel.isRefreshing.observe(owner) { updateRefreshingState(it) }
        viewModel.message.observe(owner) { binding.message = it }
    }

    private fun updateRefreshingState(isRefreshing: Boolean?) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing != null && isRefreshing
    }

    private fun navigateToConnection(deviceData: DeviceViewData) {
        val device = deviceData.device
        Navigator.navigate(binding.root, DiscoveryFragmentDirections.toConnection(Device(device)))
    }

    private fun refreshList() {
        val activity = activity as PermissionsActivity? ?: return

        permissionHandlerBluetooth.requestOrRun(activity)
    }
}
