package com.example.testnavigation


import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.testnavigation.databinding.ActivityMainBinding
import com.example.testnavigation.ui.carte.CarteFragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : AppCompatActivity(), SensorEventListener {
    // Binding pour trouver les element du XML
    lateinit var binding: ActivityMainBinding

    //Capteur
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorStepDetector: Sensor
    private lateinit var sensorRot: Sensor

    //GPS Calibration
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLocationRequest: LocationRequest? = null
    // Boucle pour le GPS pour le calibration
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                positionCourante = location
                miseAJourCalibration()
            }
        }
    }

    //Pour la barre de nav du bas
    private var fragmentActif : Int = 1
    var fragParam : androidx.fragment.app.Fragment? = null
    var fragCarte : androidx.fragment.app.Fragment? = null
    var fragCalib : androidx.fragment.app.Fragment? = null

    //Taile d'un pas en metre
    var taillePas : Float = 0.7F

    // Trim (du à l'erreur de mon téléphone dans l'azimuth)
    var azimuthTrim : Double = 0.0

    // Différent bool d'état
    var etatActivee : Boolean = false
    var etatInit : Boolean = false
    var calibrageActif : Boolean = false

    // Les données
    private var mapOrientation : LinkedHashMap<Long, Double> = LinkedHashMap(5000)
    var listPosition: ArrayList<LatLng> = ArrayList()

    //Liste des azimuth lors de la calib
    private var listAzimuthCalib : ArrayList<Double> = ArrayList()

    // Pour la calibration
    lateinit var positionCourante : Location
    lateinit var positionDepart : Location
    var nbrPasCalib : Int = 0
    var distanceCalib : Float = 0.0F




    // Creation de l'app
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        bottomNav?.setupWithNavController(navController)
        //val navView: BottomNavigationView = findViewById(R.id.nav_view)
        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment

        //val navController = navHostFragment.navController
        //NavigationUI.setupActionBarWithNavController(this, navController)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_calibration, R.id.navigation_carte, R.id.navigation_parametres))

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        */

        //
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        demanderActualisationGPS();

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            verifierPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    "Localisation précise",
                    102
            )
        }
        else{
            fusedLocationClient.lastLocation.addOnCompleteListener { taskLocation ->
                if (taskLocation.isSuccessful && taskLocation.result != null) {
                    var location = taskLocation.result
                    positionCourante = location
                }
            }
        }

        //listPosition.add(LatLng(49.41130417614944, 2.8305347697658036))

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
            sensorManager.registerListener(
                    this,
                    sensorStepDetector,
                    SensorManager.SENSOR_DELAY_FASTEST
            )
            Log.i("Pas", "Sensor activé")
        }
    }

    // A chaque pas détecté ou nouvelle valeur de rotation
    override fun onSensorChanged(se: SensorEvent) {
        if(se.sensor.type == Sensor.TYPE_STEP_DETECTOR  && (etatActivee || calibrageActif)){
            //Partie DeadReckoning Carte
            if(etatActivee) {
                val horodatage = se.timestamp / 50000000
                try {
                    val orientation: Double = mapOrientation[horodatage]!!
                    val nouvellePosition: LatLng = calculerNouvellePosition(
                            listPosition.last(),
                            orientation + azimuthTrim,
                            taillePas
                    )
                    listPosition.add(nouvellePosition)

                    Log.i("Pas", "mise a jour affichage")


                    if (fragmentActif == 2)
                            (fragCarte as CarteFragment).actualiserAffichage()
                    val textLat: TextView = findViewById(R.id.valeur_latitude)
                    val textLong: TextView = findViewById(R.id.valeur_longitude)
                    // Changer sa valeur
                    textLat.setText(listPosition.last().latitude.toString())
                    textLong.setText(listPosition.last().longitude.toString())

                }catch (e: java.lang.Exception){

                }
            }


            // Partie Calibration
            if(calibrageActif){
                nbrPasCalib++
            }
        }

        val orientationTriple = FloatArray(3)
        val rotationMat = FloatArray(9)

        if(se.sensor.type == Sensor.TYPE_ROTATION_VECTOR && (etatActivee || calibrageActif)){
                // Matrice d'orientation tridimensionnelle
                SensorManager.getRotationMatrixFromVector(rotationMat, se.values)
                // Orientation unidimensionnelle, plan du sol (autour de l'axe -Z)
                val orientation : Double = SensorManager.getOrientation(rotationMat, orientationTriple)[0].toDouble()
                if(etatActivee)
                    mapOrientation.put(se.timestamp / 50000000, orientation)
                if(calibrageActif)
                    listAzimuthCalib.add(orientation)

                // Utilisé pour le débug
                //Log.i("Rot", (se.timestamp/50000000).toString())
                //Log.i("mapSize", mapOrientation.size.toString())

                // Mise a jour affichage Parametres
                try {
                    // Récupérer textview du fragment
                    val text : TextView = findViewById(R.id.valeur_rotation)
                    // Changer sa valeur
                    text.setText(orientation.plus(azimuthTrim).toString())
                } catch (e: Exception) {
                }
            }
    }

    // ALgorithme de calcul de la nouvelle position
    private fun calculerNouvellePosition(anciennePos: LatLng, orientation: Double, taillePas: Float): LatLng {
        // https://stackoverflow.com/a/39540339
        // https://en.wikipedia.org/wiki/Geographic_coordinate_system
        val nouvelleLat : Double = anciennePos.latitude + (taillePas * cos(orientation) /111320)
        val nouvelleLong : Double = anciennePos.longitude + (taillePas * sin(orientation) * (360/(cos(
                anciennePos.latitude
        ) * 40075000)))
        return LatLng(nouvelleLat, nouvelleLong)
    }

    // Calcul de l'angle d'orientation entre deux positions GPS
    private fun calculAngleGPS(loc1 : Location, loc2 : Location): Double {
        // Merci à https://stackoverflow.com/a/15516993
        val dLon: Double = loc1.longitude - loc2.longitude
        val y = Math.sin(dLon) * Math.cos(loc2.latitude)
        val x = Math.cos(loc1.latitude) * Math.sin(loc2.latitude) - Math.sin(loc1.latitude) * Math.cos(loc2.latitude) * Math.cos(dLon)
        var brng = Math.atan2(y, x)
        return brng
    }

    // Verifie les permissions
    fun verifierPermission(permission: String, name: String, resquestCode: Int){
        when{
            ContextCompat.checkSelfPermission(this.applicationContext, permission) == PackageManager.PERMISSION_GRANTED ->{
                Toast.makeText(
                        this.applicationContext,
                        "La permission " + name + " est accordée",
                        Toast.LENGTH_SHORT
                ).show()
            }
            else -> ActivityCompat.requestPermissions(this, arrayOf(permission), resquestCode)
        }
    }

    //Boucle de GPS
    fun demanderActualisationGPS() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 500 // two minute interval
        mLocationRequest!!.fastestInterval = 250
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
            )
        }
    }

    // Lors d'un nouveau pas ou d'une nouvelle position GPS, mise à jour des valeurs de calibration
    private fun miseAJourCalibration() {
        try {
            Log.i("Frag", fragmentActif.toString())
            if(fragmentActif == 1)
                findViewById<TextView>(R.id.coordCourante).setText(positionCourante.latitude.toString() + " / " + positionCourante.longitude.toString())
            Log.i("Frag", fragmentActif.toString())

            if (calibrageActif) {
                distanceCalib = positionCourante.distanceTo(positionDepart)
                taillePas = (distanceCalib/nbrPasCalib)
                val moyenneAzimuth = listAzimuthCalib.average()
                val moyenneAzimuthGPS = calculAngleGPS(positionCourante, positionDepart)
                Log.i("Azimuth", "capteur "+moyenneAzimuth.toString())
                Log.i("Azimuth", "Gps ma fonction "+moyenneAzimuthGPS.toString())
                var azimuthTest : Double = positionCourante.bearing.toDouble()
                azimuthTest = ((azimuthTest * PI.toFloat() / 180) - PI)
                Log.i("Azimuth", "Gps bearing to "+azimuthTest.toString())
                azimuthTrim =  azimuthTest - moyenneAzimuth
                if(fragmentActif == 1){
                    findViewById<TextView>(R.id.nbrTaillePas).setText(taillePas.toString())
                    findViewById<TextView>(R.id.nbrPas).setText(nbrPasCalib.toString())
                    findViewById<TextView>(R.id.nbrAzimuth).setText(azimuthTrim.toString())
                    findViewById<TextView>(R.id.distance).setText(distanceCalib.toString())
                }
                if(fragmentActif == 3){
                    findViewById<TextView>(R.id.azimuthGPSfonction).setText(moyenneAzimuthGPS.toString())
                    findViewById<TextView>(R.id.azimuthBearing).setText(azimuthTest.toString())
                    findViewById<TextView>(R.id.azimuthMoyen).setText(moyenneAzimuth.toString())
                }
            }



        } catch (e: java.lang.Exception) {
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    // Fonction obligatoire par Android mais inutile
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    // Fonction perso pour connaitre le fragment actif
    fun updateFragParam(f: Fragment){
        fragParam = f
    }
    fun updateFragCarte(f: Fragment){
        fragCarte = f
    }
    fun updateFragCalib(f: Fragment){
        fragCalib = f
    }
    fun updateFragmentActif(i: Int){
        fragmentActif = i
    }

    // A la fin de l'appli
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this, sensorStepDetector)
        sensorManager.unregisterListener(this, sensorRot)
    }

    // Pause l'appli
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, sensorStepDetector)
        sensorManager.unregisterListener(this, sensorRot)
    }

    // Quand on revient sur l'appl
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, sensorRot, SensorManager.SENSOR_DELAY_FASTEST)
    }


}