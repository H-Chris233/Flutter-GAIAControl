/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Filter
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSetType
import com.qualcomm.qti.gaiaclient.databinding.FragmentMusicProcessingBinding
import com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing.plotting.EQFilterBank
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min

@AndroidEntryPoint
class MusicProcessingFragment : Fragment() {

    private var mViewModel: MusicProcessingViewModel? = null
    private var _mBinding: FragmentMusicProcessingBinding? = null
    private val mBinding: FragmentMusicProcessingBinding
        get() {
            return _mBinding!!
        }

    private var bandViews: List<LinearLayout> = listOf()
    private var bandSliders: List<VerticalSeekBar> = listOf()
    private var bandFreqTextViews: List<TextView> = listOf()

    private var presetRows: List<LinearLayout> = listOf()
    private var allPresetButtons: List<Button> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(
            MusicProcessingViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _mBinding = FragmentMusicProcessingBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
        bandViews = emptyList()
        bandSliders = emptyList()
        bandFreqTextViews = emptyList()
        presetRows = emptyList()
        allPresetButtons = emptyList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.errorTextView.visibility = View.VISIBLE
        mBinding.eqContainer.visibility = View.GONE
        mBinding.errorTextView.text = getString(R.string.eq_please_wait)

        // Find all the slider components and set up
        bandViews = childViewsOfView(mBinding.sliderHorizontalContainer).filterIsInstance<LinearLayout>()
        setUpSliders()
        setUpPresetButtons()
        hideAllPresetButtons()

        subscribeUI()
    }

    override fun onResume() {
        super.onResume()
        mViewModel?.fetchData()
    }

    private fun subscribeUI() {
        mViewModel?.setObservers(viewLifecycleOwner, this::updateUI)
    }

    private fun childViewsOfView(view: ViewGroup): List<View> {
        val children = mutableListOf<View>()
        for (index in 0 until view.childCount) {
            val child = view.getChildAt(index)
            children.add(child)
        }
        return children
    }

    private fun setUpSliders() {
        val sliders = mutableListOf<VerticalSeekBar>()
        val textViews = mutableListOf<TextView>()
        for (bandView in bandViews) {
            for (index in 0 until bandView.childCount) {
                val child = bandView.getChildAt(index)
                if (child is VerticalSeekBar) {
                    sliders.add(child)
                    child.gain = 0.0
                    child.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            // To prevent flooding the Gaia Device we only send when the finger is lifted
                            val verticalSlider = seekBar as VerticalSeekBar
                            val gain = verticalSlider.gain
                            val sliderIndex = bandSliders.indexOf(verticalSlider)
                            if (sliderIndex >= 0) {
                                mViewModel?.setBandGain(sliderIndex, gain)
                            }
                        }
                    })
                } else if (child is TextView) {
                    // Text
                    textViews.add(child)
                }
            }
        }

        bandSliders = sliders
        bandFreqTextViews = textViews

        // set up the labelled button to reset the sliders to 0
        mBinding.labels.button0.setOnClickListener { mViewModel?.setAllGains(0.0) }
    }

    private fun setUpPresetButtons() {
        presetRows = childViewsOfView(mBinding.presetRowsContainerView).filterIsInstance<LinearLayout>()

        val buttons = mutableListOf<Button>()
        for (row in presetRows) {
            for (index in 0 until row.childCount) {
                val child = row.getChildAt(index)
                if (child is Button) {
                    buttons.add(child)
                    child.setOnClickListener {
                        val btnIndex = allPresetButtons.indexOf(it)
                        if (btnIndex >= 0) {
                            mViewModel?.selectPresetAtIndex(btnIndex)
                        }
                    }
                }
            }
        }
        allPresetButtons = buttons
    }

    private fun hideAllPresetButtons() {
        for (button in allPresetButtons) {
            button.visibility = View.INVISIBLE
            val parent = button.parent as LinearLayout
            parent.visibility = View.GONE
        }
    }

    private fun filterAllowsGainModification(filter: Filter): Boolean {
        when (filter) {
            Filter.BYPASS -> {
                return false
            }
            Filter.LOW_PASS_1 -> {
                return false
            }
            Filter.HIGH_PASS_1 -> {
                return false
            }
            Filter.ALL_PASS_1 -> {
                return false
            }
            Filter.LOW_SHELF_1 -> {
                return true
            }
            Filter.HIGH_SHELF_1 -> {
                return true
            }
            Filter.TILT_1 -> {
                return true
            }

            Filter.LOW_PASS_2 -> {
                return false
            }
            Filter.HIGH_PASS_2 -> {
                return false
            }
            Filter.ALL_PASS_2 -> {
                return false
            }
            Filter.LOW_SHELF_2 -> {
                return true
            }
            Filter.HIGH_SHELF_2 -> {
                return true
            }
            Filter.TILT_2 -> {
                return true
            }

            Filter.PARAMETRIC_EQUALIZER -> {
                return true
            }
        }
    }

    private fun getPresetName(preset: PreSet): String {
        return when (preset.type) {
            PreSetType.OFF -> getString(R.string.eq_preset_off)
            PreSetType.USER -> getString(R.string.eq_preset_user)
            else -> when (preset.value) {
                // provides suggested labels related to our product demos
                1 -> getString(R.string.eq_preset_rock)
                2 -> getString(R.string.eq_preset_pop)
                3 -> getString(R.string.eq_preset_classical)
                4 -> getString(R.string.eq_preset_techno)
                5 -> getString(R.string.eq_preset_rnb)
                6 -> getString(R.string.eq_preset_ambient)
                7 -> getString(R.string.eq_preset_metal)
                8 -> getString(R.string.eq_preset_funk)
                9 -> getString(R.string.eq_preset_speech)
                else -> getString(R.string.eq_preset_numbered, preset.value)
            }
        }
    }

    private fun updateUI(eqInfo: MusicProcessingViewData) {
        val connected = eqInfo.deviceConnected
        val eqEnabled = eqInfo.eqEnabled

        if (!connected || !eqEnabled) {
            mBinding.errorTextView.visibility = View.VISIBLE
            mBinding.eqContainer.visibility = View.GONE

            if (!connected) {
                mBinding.errorTextView.text = getString(R.string.eq_error_device_not_connected)
            } else {
                mBinding.errorTextView.text = getString(R.string.eq_error_not_enabled)
            }
            return
        }

        mBinding.errorTextView.visibility = View.GONE
        mBinding.eqContainer.visibility = View.VISIBLE

        val presets = eqInfo.presets
        val selectedPreset = eqInfo.selectedPreset

        // Draw preset buttons

        hideAllPresetButtons()
        val maxIndex = min(presets.size, allPresetButtons.size)

        for (index in 0 until maxIndex) {
            val button = allPresetButtons[index]
            val preset = presets[index]
            val buttonTitle = getPresetName(preset)
            val isCurrent = selectedPreset?.value == preset.value
            button.text = buttonTitle
            button.visibility = View.VISIBLE
            button.isSelected = isCurrent

            val parent = button.parent as LinearLayout
            parent.visibility = View.VISIBLE
        }

        // Custom EQ

        if (selectedPreset != null) {
            val isCustom = selectedPreset.type == PreSetType.USER

            val visibilityForUseEQ = if (isCustom) View.VISIBLE else View.INVISIBLE
            mBinding.sliderHorizontalContainer.visibility = visibilityForUseEQ
            mBinding.graphView.visibility = visibilityForUseEQ
            mBinding.labels.labelsContainer.visibility = visibilityForUseEQ

            val userBands = eqInfo.orderedUserBands

            // If not user not selected then the graph just shows flat.
            val bank = if (isCustom) EQFilterBank(userBands) else EQFilterBank()
            mBinding.graphView.filterBank = bank

            for (index in bandViews.indices) {
                val slider = bandSliders[index]
                val text = bandFreqTextViews[index]
                val bandView = bandViews[index]

                val bandPresent = index < userBands.size

                bandView.visibility = if (bandPresent) View.VISIBLE else View.GONE
                slider.isEnabled = isCustom
                if (bandPresent) {
                    slider.isEnabled = isCustom && filterAllowsGainModification(userBands[index].filter)
                    val gain = if (isCustom) userBands[index].gain else 0.0
                    slider.gain = gain
                    text.text = renderFrequency(userBands[index].frequency)
                }
            }
        }
    }

    private fun renderFrequency(frequency: Int): String {
        if (frequency < 999) {
            return frequency.toString()
        }

        val thousands = frequency.toDouble() / 1000.0
        if (frequency % 1000 == 0) {
            return getString(R.string.eq_frequency_k_param, thousands.toInt().toString())
        } else {
            return getString(R.string.eq_frequency_k_param, String.format("%.1f", thousands))
        }

    }
}
