package com.example.testnavigation

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.testnavigation.ui.carte.CarteFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorStepDetector: Sensor
    private lateinit var sensorRot: Sensor

    //Taile d'un pas en metre
    val taillePas = 0.5

    // Trim (du à l'erreur de mon téléphone dans l'azimuth)
    val azimuthTrim : Double = -0.7

    var fragParam : Fragment? = null
    var fragCarte : Fragment? = null
    var fragCalib : Fragment? = null

    var enregistrement : Boolean = true
    var mapOrientation : LinkedHashMap<Long, Double> = LinkedHashMap(5000)
    var listPosition: ArrayList<LatLng> = ArrayList<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_calibration, R.id.navigation_carte, R.id.navigation_parametres))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //
        listPosition.add(LatLng(49.41130417614944, 2.8305347697658036))

        // Initilalisation des capteurs STEP_DETECTOR et ROTATION_VECTOR
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorRot = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        // Activation des listeners
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!=null){
            sensorManager.registerListener(this, sensorRot, SensorManager.SENSOR_DELAY_FASTEST)
            Log.i("Rot", "Sensor activé")

        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null){
            sensorManager.registerListener(this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST)
            Log.i("Pas", "Sensor activé")
        }
    }

    override fun onSensorChanged(se: SensorEvent) {
        val currentFragment : Fragment? = supportFragmentManager.fragments.last()?.childFragmentManager?.fragments?.get(0)

        if(se.sensor.type == Sensor.TYPE_STEP_DETECTOR){
                Log.i("Pas", "Pas détecté")

                if(currentFragment == fragCarte){
                    val horodatage = se.timestamp/50000000
                    val orientation : Double = mapOrientation[horodatage]!!
                    val nouvellePosition : LatLng = calculerNouvellePosition(listPosition.last(), orientation + azimuthTrim, taillePas)
                    listPosition.add(nouvellePosition)

                    (fragCarte as CarteFragment).miseAJourAffichage()
                    try {
                        // Récupérer textview du fragment
                        val textLat : TextView = findViewById(R.id.valeur_latitude)
                        val textLong : TextView = findViewById(R.id.valeur_longitude)
                        // Changer sa valeur
                        textLat.setText(listPosition.last().latitude.toString())
                        textLong.setText(listPosition.last().longitude.toString())
                    } catch (e: Exception) {
                    }
                }

            }

            val orientationTriple = FloatArray(3)
            val rotationMat = FloatArray(9)

            if(se.sensor.type == Sensor.TYPE_ROTATION_VECTOR){
                // Matrice d'orientation tridimensionnelle
                SensorManager.getRotationMatrixFromVector(rotationMat, se.values)
                // Orientation unidimensionnelle, plan du sol (autour de l'axe -Z)
                val orientation : Double =
                    SensorManager.getOrientation(rotationMat, orientationTriple)[0].toDouble()
                mapOrientation.put(se.timestamp / 50000000, orientation)

                // Utilisé pour le débug
                //Log.i("Rot", (se.timestamp/50000000).toString())
                //Log.i("mapSize", mapOrientation.size.toString())
                try {
                    // Récupérer textview du fragment
                    val text : TextView = findViewById(R.id.valeur_rotation)
                    // Changer sa valeur
                    text.setText((mapOrientation[se.timestamp / 50000000]?.plus(azimuthTrim)).toString())
                } catch (e: Exception) {
                }
            }
    }

    private fun calculerNouvellePosition(anciennePos: LatLng, orientation: Double, taillePas: Double): LatLng {
        // https://stackoverflow.com/a/39540339
        // https://en.wikipedia.org/wiki/Geographic_coordinate_system
        val nouvelleLat : Double = anciennePos.latitude + (taillePas * cos(orientation) /111320)
        val nouvelleLong : Double = anciennePos.longitude + (taillePas * sin(orientation) * (360/(cos(anciennePos.latitude) * 40075000)))
        return LatLng(nouvelleLat, nouvelleLong)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this, sensorStepDetector)
        sensorManager.unregisterListener(this, sensorRot)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, sensorRot, SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun updateFragParam(f: Fragment){
        fragParam = f
    }

    fun updateFragCarte(f: Fragment){
        fragCarte = f
    }

    fun updateFragCalib(f: Fragment){
        fragCalib = f
    }
}