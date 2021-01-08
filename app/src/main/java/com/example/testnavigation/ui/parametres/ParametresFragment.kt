package com.example.testnavigation.ui.parametres

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.testnavigation.MainActivity
import com.example.testnavigation.R

class ParametresFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_parametres, container, false)
        val textView: TextView = root.findViewById(R.id.valeur_rotation)


        (activity as MainActivity).updateFragParam(this)
        (activity as MainActivity).updateFragmentActif(3)

        //Permission
        when {
            activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACTIVITY_RECOGNITION) } == PackageManager.PERMISSION_GRANTED -> { }
            else -> {
                Toast.makeText(requireActivity().applicationContext, "N'oublie pas d'accorder les permissions !", Toast.LENGTH_SHORT).show()
            }
        }

        //Bouton permission capteur
        val buttonCapteur : Button = root.findViewById(R.id.buttonSensor)
        buttonCapteur.setOnClickListener{
            (activity as MainActivity).verifierPermission(android.Manifest.permission.ACTIVITY_RECOGNITION, "Activity", 101)
        }
        // Bouton permission gps
        val buttonGPS : Button = root.findViewById(R.id.buttonGPS)
        buttonGPS.setOnClickListener{
            (activity as MainActivity).verifierPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, "Localisation précise", 102)
            (activity as MainActivity).verifierPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION, "Localisation", 103)
        }
        return root
    }
}