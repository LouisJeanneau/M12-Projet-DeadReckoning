package com.example.testnavigation.ui.parametres

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testnavigation.MainActivity
import com.example.testnavigation.R

class ParametresFragment : Fragment() {
    val ACTIVITY_RQ = 101
    private lateinit var parametresViewModel: ParametresViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        parametresViewModel = ViewModelProvider(this).get(ParametresViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_parametres, container, false)
        val textView: TextView = root.findViewById(R.id.valeur_rotation)
        val button : Button = root.findViewById(R.id.button)

        (activity as MainActivity).updateFragParam(this)

        parametresViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        //Permission
        when {
            activity?.let {
                ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACTIVITY_RECOGNITION
                )
            } == PackageManager.PERMISSION_GRANTED -> {
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
            }
        }

        //Bouton permission
        buttonTaps(button)
        return root
    }

    fun buttonTaps(buttonPermission: Button){
        buttonPermission.setOnClickListener{
            checkForPermissions(android.Manifest.permission.ACTIVITY_RECOGNITION, "Activity", ACTIVITY_RQ)
        }
    }

    private fun checkForPermissions(permission: String, name: String, resquestCode: Int){
        when{
            ContextCompat.checkSelfPermission(requireActivity().applicationContext, permission) == PackageManager.PERMISSION_GRANTED ->{
                Toast.makeText(requireActivity().applicationContext, "La permission est accordée", Toast.LENGTH_SHORT).show()
            }
            else -> ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), resquestCode)
        }
    }
}