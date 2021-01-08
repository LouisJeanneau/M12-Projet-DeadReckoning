package com.example.testnavigation.ui.carte

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.testnavigation.MainActivity
import com.example.testnavigation.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng


class CarteFragment : Fragment(), GoogleMap.OnMapClickListener {
    // Esthetique
    private val tailleCercle: Double = 0.4

    // Ref de la map
    private var handleMap : GoogleMap? = null

    // Liste de cercle
    private val listCercle = mutableListOf<Circle>()

    //private lateinit var carteViewModel: CarteViewModel

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        handleMap = googleMap

        /*
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                (activity as MainActivity).listPosition.first(), 95f
            )
        )
         */

        //Montre le point GPS de l'utilisateur ou rappelle d'accorder la permission
        if (ActivityCompat.checkSelfPermission(
                (activity as MainActivity),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                (activity as MainActivity),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                requireActivity().applicationContext,
                "Veuillez accordez la permission GPS dans les paramètres",
                Toast.LENGTH_SHORT
            ).show()
        }
        else{
            googleMap.isMyLocationEnabled = true
        }

        googleMap.moveCamera(CameraUpdateFactory.zoomTo(150f))
        try{
            val temp = (activity as MainActivity).positionCourante
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(temp.latitude, temp.longitude)))
        }catch (e: Exception){
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(49.41130417614944, 2.8305347697658036)))
        }

        googleMap.setIndoorEnabled(true)
        googleMap.setOnMapClickListener{
            validerPositionInitiale(it)
            (activity as MainActivity).etatInit = true
            miseAJourBouton()
        }
        //Si il y a eu changement de fragment, cela remet les points du parcours
        initAffichage()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //carteViewModel = ViewModelProvider(this).get(CarteViewModel::class.java)


        val root = inflater.inflate(R.layout.fragment_carte, container, false)
        return root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        (activity as MainActivity).updateFragCarte(this)
        (activity as MainActivity).updateFragmentActif(2)


        // Bouton start non clickable tant qu'on a pas une pos initiale
        val buttonStart : Button = requireView().findViewById(R.id.buttonStart)
        val buttonStop : Button = requireView().findViewById(R.id.buttonStop)
        val buttonPosInit : Button = requireView().findViewById(R.id.buttonValiderPosDepart)
        //
        miseAJourBouton()


        buttonPosInit.setOnClickListener {
            (activity as MainActivity).fusedLocationClient.lastLocation.addOnCompleteListener {
                taskLocation ->
                if (taskLocation.isSuccessful && taskLocation.result != null) {
                    var location = taskLocation.result
                    validerPositionInitiale((LatLng(location.latitude, location.longitude)))
                }
            }
            (activity as MainActivity).etatInit = true
            miseAJourBouton()
        }
        buttonStart.setOnClickListener{
            (activity as MainActivity).etatActivee = true
            miseAJourBouton()
        }

        buttonStop.setOnClickListener {
            (activity as MainActivity).etatActivee = false
            miseAJourBouton()
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        initAffichage()
    }

    private fun miseAJourBouton(){
        val init = (activity as MainActivity).etatInit
        val activee = (activity as MainActivity).etatActivee
        val buttonStart : Button = requireView().findViewById(R.id.buttonStart)
        val buttonStop : Button = requireView().findViewById(R.id.buttonStop)
        val buttonPosInit : Button = requireView().findViewById(R.id.buttonValiderPosDepart)
        //
        if(activee){
            buttonStop.setBackgroundColor(resources.getColor(R.color.primaryColor))
            buttonStart.setBackgroundColor(resources.getColor(R.color.gris))
        }
        else{
            if(init){
                buttonStart.setBackgroundColor(resources.getColor(R.color.primaryColor))
            }
            else{
                buttonStart.setBackgroundColor(resources.getColor(R.color.gris))
            }
            buttonStop.setBackgroundColor(resources.getColor(R.color.gris))
        }

    }

    private fun initAffichage(){
        for (position in (activity as MainActivity).listPosition){
            var circleOptions : CircleOptions = CircleOptions().center(position).fillColor(R.color.pointInter).strokeWidth(0f).radius(tailleCercle)
            if(position == (activity as MainActivity).listPosition.first()){
                circleOptions = CircleOptions().center(position).fillColor(R.color.pointDepart).strokeWidth(0f).radius(tailleCercle)
            }
            else if(position == (activity as MainActivity).listPosition.last()){
                circleOptions = CircleOptions().center(position).fillColor(R.color.pointDernier).strokeWidth(0f).radius(tailleCercle)
            }
            try {
                val circle: Circle = handleMap!!.addCircle(circleOptions)
                listCercle.add(circle)
            } catch (e: Exception) {

            }
        }
    }


    fun actualiserAffichage(){
        val position = (activity as MainActivity).listPosition.last()
        var circleOptions = CircleOptions().center(position).fillColor(R.color.pointDernier).strokeWidth(
            0f
        ).radius(tailleCercle)
        val circle: Circle = handleMap!!.addCircle(circleOptions)
        if(position != (activity as MainActivity).listPosition.first())
            listCercle.last().setFillColor(R.color.pointInter)
        listCercle.add(circle)
    }

    @SuppressLint("MissingPermission")
    fun validerPositionInitiale(position : LatLng){
        if((activity as MainActivity).etatActivee == false){
        (activity as MainActivity).etatInit = true
        (activity as MainActivity).listPosition.clear()
        (activity as MainActivity).listPosition.add(position)
        for(circle in listCercle){
            circle.remove()
        }
        listCercle.clear()
        initAffichage()
        }
    }

    override fun onMapClick(p0: LatLng?) {
        if (p0 != null) {
            validerPositionInitiale(p0)
        }
    }
}