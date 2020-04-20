package com.example.transactionhistoryexercise

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber

class DisplayLocationActivity: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private lateinit var latLng: LatLng

    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_displaylocation)

        //Setup actionBar
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setLogo(R.drawable.icon_home_as_up_icon)
            setDisplayUseLogoEnabled(true)
            title = getString(R.string.find_us)
        }

        latLng = intent.getParcelableExtra(LOCATION)!!
        name = intent.getStringExtra(NAMEOFATM)!!

        Timber.i("Lat = ${latLng.latitude}; Lon = ${latLng.longitude}")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker on the map and move the camera
        map.addMarker(MarkerOptions().position(latLng).title(name).icon(
            BitmapDescriptorFactory.fromResource(R.drawable.marker_atm_commbank)))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }
}