package com.example.testnavigation.ui.calibration

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.testnavigation.MainActivity
import com.example.testnavigation.R


class CalibrationFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_calibration, container, false)
        (activity as MainActivity).updateFragCalib(this)
        (activity as MainActivity).updateFragmentActif(1)

        val switch : SwitchCompat = root.findViewById(R.id.switchCalibration)
        val affichageNbrPas : TextView = root.findViewById(R.id.nbrPas)
        val affichageDistance : TextView = root.findViewById(R.id.distance)
        val affichageTaille : TextView = root.findViewById(R.id.nbrTaillePas)
        val affichageAzimuth : TextView = root.findViewById(R.id.nbrAzimuth)
        val calibManuelleTaille : TextView = root.findViewById(R.id.calibManuelleTaillePas)
        val calibManuelleAzimuth : TextView = root.findViewById(R.id.calibManuelleAzimuth)

        affichageDistance.setText((activity as MainActivity).distanceCalib.toString())
        affichageNbrPas.setText((activity as MainActivity).nbrPasCalib.toString())
        affichageTaille.setText((activity as MainActivity).taillePas.toString())
        affichageAzimuth.setText((activity as MainActivity).azimuthTrim.toString())
        calibManuelleTaille.setText((activity as MainActivity).taillePas.toString())
        calibManuelleAzimuth.setText((activity as MainActivity).azimuthTrim.toString())

        (activity as MainActivity).demanderActualisationGPS()

        switch.setOnCheckedChangeListener { _, isChecked ->
            (activity as MainActivity).calibrageActif = isChecked
            if(isChecked){
                //On commence la calibration
                    var continuer = true
                if (ActivityCompat.checkSelfPermission((activity as MainActivity), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((activity as MainActivity), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireActivity().applicationContext, "Veuillez accordez la permission GPS dans les paramètres", Toast.LENGTH_SHORT).show()
                    switch.setChecked(false)
                    continuer = false
                }
                if(continuer){
                    (activity as MainActivity).positionDepart = (activity as MainActivity).positionCourante
                    (activity as MainActivity).nbrPasCalib = 0
                    (activity as MainActivity).distanceCalib = 0.0F
                }
            }
        }

        calibManuelleTaille.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                (activity as MainActivity).taillePas = v.text.toString().toFloat()
                (activity as MainActivity).findViewById<TextView>(R.id.nbrTaillePas).setText((activity as MainActivity).taillePas.toString())
                false
            } else {
                false
            }
        }
        calibManuelleAzimuth.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                (activity as MainActivity).azimuthTrim = v.text.toString().toDouble()
                (activity as MainActivity).findViewById<TextView>(R.id.nbrAzimuth).setText((activity as MainActivity).azimuthTrim.toString())
                false
            } else {
                false
            }
        }



        return root
    }
}