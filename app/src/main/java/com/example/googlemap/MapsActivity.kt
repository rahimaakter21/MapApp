package com.example.googlemap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.googlemap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private  lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    private  lateinit var locationCallback: LocationCallback

    companion object{
    private  const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            locationCallback = object : LocationCallback() {

                override fun onLocationResult(locationResult: LocationResult) {

                    locationResult ?: return

                    for (location in locationResult.locations) {
                        updateMapLocation(location)

                    }
                }
            }
        }






    private fun updateMapLocation(location: Location?) {
        val currentLatlng = LatLng(location?.latitude?:0.0, location?.longitude?:0.0)

        mMap.addMarker(MarkerOptions().position(currentLatlng).title("my current location "))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng,15F))

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableMyLocation()

    }

    private fun enableMyLocation() {

        // Check if the location permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // If permission is already granted, enable the location layer
            mMap.isMyLocationEnabled = true
            // Get the last known location and update the map
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    updateMapLocation(it)
                }
            }
            startLocationUpdates()
        }
    }
    private  fun startLocationUpdates(){
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000)

        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(3000)
        .setMaxUpdateDelayMillis(1000)
        .build()

        if(ContextCompat.checkSelfPermission(
            this,
                Manifest.permission.ACCESS_FINE_LOCATION
                )== PackageManager.PERMISSION_GRANTED
            ) {
               fusedLocationProviderClient.requestLocationUpdates(
                   locationRequest,
                   locationCallback,
                    null
               )
         }
    }

    override   fun onRequestPermissionsResult(

            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray

    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    enableMyLocation()
                }
            }

        }
    override  fun onPause(){

        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }
    override fun onResume() {

        startLocationUpdates()
        super.onResume()
    }
}