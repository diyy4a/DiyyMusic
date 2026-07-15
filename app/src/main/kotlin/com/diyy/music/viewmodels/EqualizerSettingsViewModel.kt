package com.diyy.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diyy.music.eq.data.EQProfileRepository
import com.diyy.music.eq.data.ParametricEQBand
import com.diyy.music.eq.data.SavedEQProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EqualizerSettingsViewModel @Inject constructor(
    private val repository: EQProfileRepository,
) : ViewModel() {

    val activeProfile: StateFlow<SavedEQProfile?> = repository.activeProfile

    val presets: List<SavedEQProfile> = listOf(
        preset(
            id = "diyy_eq_balanced",
            name = "Balanced",
            preamp = -1.5,
            gains = listOf(1.5, 0.8, 0.0, -0.4, 0.0, 0.7, 1.0, 1.2),
        ),
        preset(
            id = "diyy_eq_bass",
            name = "Bass Boost",
            preamp = -5.0,
            gains = listOf(5.0, 4.2, 2.4, 0.4, -0.6, 0.0, 0.4, 0.5),
        ),
        preset(
            id = "diyy_eq_vocal",
            name = "Vocal",
            preamp = -3.0,
            gains = listOf(-1.0, -0.8, 0.0, 1.0, 2.6, 3.0, 1.4, 0.2),
        ),
        preset(
            id = "diyy_eq_bright",
            name = "Bright",
            preamp = -4.0,
            gains = listOf(-1.2, -0.8, -0.4, 0.0, 0.8, 1.8, 3.0, 4.0),
        ),
    )

    fun selectPreset(id: String) {
        viewModelScope.launch {
            if (id == FLAT_ID) {
                repository.setActiveProfile(null)
                return@launch
            }
            val profile = presets.firstOrNull { it.id == id } ?: return@launch
            repository.saveProfile(profile)
            repository.setActiveProfile(profile.id)
        }
    }

    fun updateBandGain(index: Int, gain: Double) {
        val current = activeProfile.value ?: return
        if (index !in current.bands.indices) return
        viewModelScope.launch {
            val updated = current.copy(
                bands = current.bands.mapIndexed { bandIndex, band ->
                    if (bandIndex == index) band.copy(gain = gain.coerceIn(-12.0, 12.0)) else band
                },
            )
            repository.saveProfile(updated)
            repository.setActiveProfile(updated.id)
        }
    }

    fun updatePreamp(preamp: Double) {
        val current = activeProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(preamp = preamp.coerceIn(-12.0, 6.0))
            repository.saveProfile(updated)
            repository.setActiveProfile(updated.id)
        }
    }

    fun resetActiveProfile() {
        val current = activeProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                preamp = 0.0,
                bands = current.bands.map { it.copy(gain = 0.0) },
            )
            repository.saveProfile(updated)
            repository.setActiveProfile(updated.id)
        }
    }

    companion object {
        const val FLAT_ID = "flat"
        private val frequencies = listOf(60.0, 120.0, 250.0, 500.0, 1000.0, 2500.0, 6000.0, 12000.0)

        private fun preset(
            id: String,
            name: String,
            preamp: Double,
            gains: List<Double>,
        ) = SavedEQProfile(
            id = id,
            name = name,
            deviceModel = "DiyyMusic preset",
            bands = frequencies.zip(gains).map { (frequency, gain) ->
                ParametricEQBand(
                    frequency = frequency,
                    gain = gain,
                    q = 1.15,
                )
            },
            preamp = preamp,
            source = "DiyyMusic",
            rig = "Built-in",
            isCustom = false,
        )
    }
}
