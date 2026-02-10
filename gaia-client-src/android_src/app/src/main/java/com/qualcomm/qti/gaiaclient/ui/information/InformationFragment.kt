/*
 * ************************************************************************************************
 * * © 2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.information

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.CrossUpdateVersion
import com.qualcomm.qti.gaiaclient.databinding.FragmentInformationBinding
import com.qualcomm.qti.gaiaclient.ui.common.progress.DialogViewData
import com.qualcomm.qti.gaiaclient.ui.information.dialog.DeviceInfoDialogOption
import com.qualcomm.qti.gaiaclient.ui.information.dialog.Options
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InformationFragment : Fragment() {

    private val viewModel: InformationViewModel by viewModels()
    private lateinit var binding: FragmentInformationBinding

    private var dialog: AlertDialog? = null

    private val backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // this prevents the navigation to come back to another screen while it is upgrading
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = activity ?: return
        activity.onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInformationBinding.inflate(inflater)
        subscribeUI()
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        dismissDialog()
    }

    private fun subscribeUI() {
        viewModel.informationViewData.observe(viewLifecycleOwner) { data: InformationViewData -> updateData(data) }
        viewModel.crossUpdateVersion.observe(viewLifecycleOwner) { it?.let { onCrossUpdateVersion(it) } }
    }

    private fun onCrossUpdateVersion(crossUpdateVersion: CrossUpdateVersion) {
        crossUpdateVersion.let {
            val dialogId: Int = 0
            val viewData = DialogViewData<Int, Options>(
                dialogId,
                getString(R.string.cross_update_version_dialog_title),
                getString(
                    R.string.cross_update_version_dialog_message, crossUpdateVersion.major, crossUpdateVersion
                        .minor, crossUpdateVersion.ps
                )
            )
            viewData.isCancelable = false
            viewData.neutralOption = DeviceInfoDialogOption.OKAY
            showDialog(viewData)

        }
    }

    private fun showDialog(data: DialogViewData<Int, Options>) {
        dismissDialog() // only one dialog at a time

        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        builder.setMessage(data.message)
            .setTitle(data.title)
            .setCancelable(data.isCancelable)
        val neutralOption = data.neutralOption
        if (neutralOption != null) {
            builder.setNeutralButton(
                neutralOption.getLabel(context)
            ) { dialog: DialogInterface?, which: Int ->
                // Nothing
            }
        }
        dialog = builder.create()
        dialog?.show()
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun updateData(data: InformationViewData) {
        binding.information = data
    }
}
