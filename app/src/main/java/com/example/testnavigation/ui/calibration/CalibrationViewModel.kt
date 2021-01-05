package com.example.testnavigation.ui.calibration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalibrationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Fragment Calibration"
    }
    val text: LiveData<String> = _text
}