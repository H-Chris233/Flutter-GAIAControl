/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.category

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.Slider
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.databinding.DialogStatisticsSettingsBinding
import com.qualcomm.qti.gaiaclient.databinding.FragmentStatisticsListBinding
import com.qualcomm.qti.gaiaclient.ui.MainActivity
import com.qualcomm.qti.gaiaclient.ui.common.MessageData
import com.qualcomm.qti.gaiaclient.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val MAXIMUM_REFRESH_RATE_IN_MS = 10000
private const val MINIMUM_REFRESH_RATE_IN_MS = 500
private const val DEFAULT_REFRESH_RATE_IN_MS = 5000

private const val NO_STATISTICS_MESSAGE_TIMER_MS: Long = 500

@AndroidEntryPoint
class StatisticsStatisticsFragment : Fragment() {
    private var viewModel: StatisticsStatisticsViewModel? = null
    private var _binding: FragmentStatisticsListBinding? = null
    private val binding: FragmentStatisticsListBinding
        get() {
            return _binding!!
        }

    private var adapter: StatisticsStatisticsAdapter? = null
    private var settingsDialogBinding: DialogStatisticsSettingsBinding? = null

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val shareFileLauncher = registerForActivityResult(StartActivityForResult()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[StatisticsStatisticsViewModel::class.java]

        val args = arguments ?: return
        val id = StatisticsStatisticsFragmentArgs.fromBundle(args).category
        viewModel?.setCategoryID(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsListBinding.inflate(inflater, container, false)
        adapter = StatisticsStatisticsAdapter(
            viewModel?.categoryID?.value,
            viewModel?.isSupported(QTILFeature.XPAN) == true,
            { viewModel?.toggleXpanTransport() },
            { viewModel?.setXpanTransport(it) },
            { viewModel?.setAPConnectionState(it) }
        )
        binding.statisticList.adapter = adapter
        val layoutManager = LinearLayoutManager(binding.statisticList.context)
        binding.statisticList.layoutManager = layoutManager

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        settingsDialogBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.statisticsActionButton.visibility = View.VISIBLE
        binding.statisticsActionButton.text = getString(R.string.statistic_category_settings)
        binding.statisticsActionButton.setOnClickListener {
            this.onRequestSettings()
        }
        binding.statisticsActionButton.isActivated = false

        subscribeUI()
    }

    override fun onResume() {
        super.onResume()
        viewModel?.resume()

        // display a message if no data was received
        handler.postDelayed(
            { showNoAvailableStatisticsMessage(!(viewModel?.isFetched ?: false)) },
            NO_STATISTICS_MESSAGE_TIMER_MS
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel?.pause()
    }

    private fun subscribeUI() {
        viewModel?.viewData?.observe(viewLifecycleOwner, ::updateView)
        viewModel?.title?.observe(viewLifecycleOwner, ::updateTitle)
    }

    private fun updateView(updated: StatisticsStatisticsViewData) {
        adapter?.submitList(updated.statistics)
        updateSettingsDialogView()
        showNoAvailableStatisticsMessage(updated.statistics.isEmpty())
    }

    private fun updateTitle(title: String) {
        if (activity is MainActivity && title.isNotEmpty()) {
            (activity as MainActivity).updateTitle(title)
        }
    }

    private fun onRequestSettings() {
        val context = context ?: return

        settingsDialogBinding = createSettingsDialogView()
        updateSettingsDialogView()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.statistic_category_settings)
        builder.setView(settingsDialogBinding?.root)
        builder.setCancelable(true)
        builder.setOnDismissListener { settingsDialogBinding = null }

        val alert = builder.create()
        alert.create()
        alert.show()
    }

    private fun createSettingsDialogView(): DialogStatisticsSettingsBinding {
        val dialogBinding = DialogStatisticsSettingsBinding.inflate(LayoutInflater.from(context))

        dialogBinding.bounds =
            RefreshRateBounds(MINIMUM_REFRESH_RATE_IN_MS.toFloat(), MAXIMUM_REFRESH_RATE_IN_MS.toFloat())

        dialogBinding.refreshRateSlider.setLabelFormatter { value: Float ->
            getString(R.string.statistic_refresh_rate_value, RefreshRate(value).rateInSeconds)
        }
        dialogBinding.refreshRateSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel?.setRefreshRate(slider.value.toLong())
                updateSettingsDialogView()
            }
        })

        dialogBinding.recordButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val vm = viewModel ?: return
                if (vm.isRecording()) {
                    val file = vm.stopRecording()
                    if (file != null) {
                        startShareFileActivity(file)
                    }
                } else {
                    vm.startRecording()
                }

                updateSettingsDialogView()
            }
        })

        return dialogBinding
    }

    private fun startShareFileActivity(file: File) {
        val context = context ?: return

        val intent = Intent(Intent.ACTION_SEND)

        // add logs file
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val uri = FileUtils.getUriFromFile(context, file, FileUtils.FILE_PROVIDER_AUTHORITY)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "*/*" // type not recognised by Android -> using "anything"

        // start system sharing action
        val shareIntent = Intent.createChooser(intent, getString(R.string.stats_share_intent_title))

        shareFileLauncher.launch(shareIntent)
    }

    private fun updateSettingsDialogView() {
        val dialogBinding = settingsDialogBinding ?: return

        val refreshRate = viewModel?.refreshRateMsecs() ?: DEFAULT_REFRESH_RATE_IN_MS
        dialogBinding.rate = RefreshRate(refreshRate.toFloat())

        val isRecording = viewModel?.isRecording() ?: false
        dialogBinding.isRecording = isRecording
        dialogBinding.recordButton.isActivated = isRecording
    }

    private fun showNoAvailableStatisticsMessage(show: Boolean) {
        if (show != binding.message?.show) {
            binding.message = MessageData(
                show,
                getString(R.string.statistics_none_title),
                getString(R.string.statistics_none_message)
            )
        }
    }
}
