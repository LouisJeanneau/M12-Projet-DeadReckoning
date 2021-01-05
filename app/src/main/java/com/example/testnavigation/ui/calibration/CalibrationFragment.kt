package com.example.testnavigation.ui.calibration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testnavigation.MainActivity
import com.example.testnavigation.R

class CalibrationFragment : Fragment() {

    //private lateinit var calibrationViewModel: CalibrationViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //calibrationViewModel = ViewModelProvider(this).get(CalibrationViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_calibration, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
       //calibrationViewModel.text.observe(viewLifecycleOwner, Observer { textView.text = it })
        (activity as MainActivity).updateFragCalib(this)
        return root
    }
}