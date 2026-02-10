/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.categories

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.databinding.FragmentStatisticsListBinding
import com.qualcomm.qti.gaiaclient.ui.Navigator
import com.qualcomm.qti.gaiaclient.ui.common.MessageData
import com.qualcomm.qti.gaiaclient.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val NO_CATEGORIES_MESSAGE_TIMER_MS: Long = 500

@AndroidEntryPoint
class StatisticsCategoriesFragment : Fragment() {
    private val viewModel: StatisticsCategoriesViewModel by viewModels()
    private var _binding: FragmentStatisticsListBinding? = null
    private val binding: FragmentStatisticsListBinding
        get() {
            return _binding!!
        }
    private var adapter: StatisticsCategoriesAdapter? = null

    private val shareFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsListBinding.inflate(inflater, container, false)
        adapter = StatisticsCategoriesAdapter {
            Navigator.navigate(
                binding.root,
                StatisticsCategoriesFragmentDirections.toStatisticsCategory(CategoryID((it.identifier)))
            )
        }

        binding.statisticList.adapter = adapter
        val layoutManager = LinearLayoutManager(context)
        binding.statisticList.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        binding.statisticList.addItemDecoration(dividerItemDecoration)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.statisticsActionButton.visibility = View.GONE
        binding.statisticsActionButton.text = getString(R.string.statistic_categories_stop_recording)
        binding.statisticsActionButton.setOnClickListener {
            this.stopAllRecording()
        }
        binding.statisticsActionButton.isActivated = true

        subscribeUI()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchData()

        // display a message if no data was received
        handler.postDelayed(
            { showNoAvailableStatisticsMessage(!(viewModel.isFetched)) },
            NO_CATEGORIES_MESSAGE_TIMER_MS
        )
    }

    private fun subscribeUI() {
        viewModel.viewData.observe(viewLifecycleOwner, this::updateUI)
    }

    private fun stopAllRecording() {
        val files = viewModel.stopAllRecording()
        shareFiles(files)
    }

    private fun shareFiles(files: List<File>) {
        val context = context ?: return
        val uris = ArrayList<Uri>(files.map { file ->
            return@map FileUtils.getUriFromFile(context, file, FileUtils.FILE_PROVIDER_AUTHORITY)
        })

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)

        // add logs files
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.type = "*/*" // type not recognised by Android -> using "anything"

        // start system sharing action
        val shareIntent = Intent.createChooser(intent, getString(R.string.stats_share_intent_title))

        shareFileLauncher.launch(shareIntent)
    }

    private fun updateUI(updated: StatisticsCategoriesViewData) {
        val context = context ?: return
        binding.statisticsActionButton.visibility = if (updated.isRecording) View.VISIBLE else View.GONE
        val transformed = updated.categoriesInfo.map {
            CategoryViewData(it.category.getTitle(context), it.recordingStatus, it.category)
        }
        adapter?.submitList(transformed)

        showNoAvailableStatisticsMessage(transformed.isEmpty())
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
