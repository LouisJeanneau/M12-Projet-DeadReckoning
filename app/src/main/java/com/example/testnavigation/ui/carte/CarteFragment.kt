package com.example.testnavigation.ui.carte

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.testnavigation.MainActivity
import com.example.testnavigation.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions


class CarteFragment : Fragment() {
    // Esthetique
    private val tailleCercle: Double = 0.4
    val colorPointDepart = Color.parseColor("#8a8a8a")
    val colorPointInter = Color.parseColor("#4eaacf")
    val colorPointDernier = Color.parseColor("#36c794")
    val colorPointClick = Color.parseColor("#8f56d1")

    // Ref de la map
    var handleMap : GoogleMap? = null

    // Liste de cercle
    val listCircle = mutableListOf<Circle>()

    //private lateinit var carteViewModel: CarteViewModel

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
        //on ajoute un marqueur dans le coin de PG1
        //map.addMarker(new MarkerOptions().position(Depart_PG).title("Marker_PG"));
        //on centre la cam sur le marqueur et on zoome
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                (activity as MainActivity).listPosition.first(),
                95f
            )
        )
        initAffichage()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //carteViewModel = ViewModelProvider(this).get(CarteViewModel::class.java)
        (activity as MainActivity).updateFragCarte(this)

        val root = inflater.inflate(R.layout.fragment_carte, container, false)

        //val buttonStart : Button = requireView().findViewById(R.id.buttonStart)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        initAffichage()
    }

    fun initAffichage(){
        for (position in (activity as MainActivity).listPosition){
            var circleOptions : CircleOptions = CircleOptions().center(position).fillColor(
                colorPointInter
            ).strokeWidth(0f).radius(tailleCercle)
            if(position == (activity as MainActivity).listPosition.first()){
                circleOptions = CircleOptions().center(position).fillColor(colorPointDepart).strokeWidth(
                    0f
                ).radius(tailleCercle)
            }
            else if(position == (activity as MainActivity).listPosition.last()){
                circleOptions = CircleOptions().center(position).fillColor(colorPointDernier).strokeWidth(
                    0f
                ).radius(tailleCercle)
            }
            try {
                val circle: Circle = handleMap!!.addCircle(circleOptions)
                listCircle.add(circle)
            } catch (e: Exception) {

            }
        }
    }


    fun miseAJourAffichage(){
        val position = (activity as MainActivity).listPosition.last()
        var circleOptions = CircleOptions().center(position).fillColor(colorPointDernier).strokeWidth(
            0f
        ).radius(tailleCercle)
        val circle: Circle = handleMap!!.addCircle(circleOptions)
        if(position != (activity as MainActivity).listPosition.first())
            listCircle.last().setFillColor(colorPointInter)
        listCircle.add(circle)
    }
}