package com.eva4

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

class MapsActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private lateinit var marker: Marker
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().setUserAgentValue(packageName)

        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        requestLocation()

        marker = Marker(map).apply {
            icon = getDrawable(R.drawable.baseline_location_pin_24)
            map.controller.setZoom(8.0)
            map.overlays.add(this)

        }

        val mapClickOverlay = object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                val projection = mapView?.projection
                val geoPoint = projection?.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint
                selectedLatitude = geoPoint.latitude
                selectedLongitude = geoPoint.longitude
                updateMarker(geoPoint)
                return true
            }
        }

        map.overlays.add(mapClickOverlay)

        val btnSave: Button = findViewById(R.id.btn_save_location)
        btnSave.setOnClickListener {
            if (selectedLatitude != null && selectedLongitude != null) {
                val resultIntent = Intent().apply {
                    putExtra("latitude", selectedLatitude)
                    putExtra("longitude", selectedLongitude)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLocation = GeoPoint(it.latitude, it.longitude)
                        map.controller.setZoom(20.0)
                        map.controller.setCenter(currentLocation)
                        updateMarker(currentLocation)
                        selectedLatitude = it.latitude
                        selectedLongitude = it.longitude
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }

    private fun updateMarker(geoPoint: GeoPoint) {
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 100
    }
}
