package com.eva4

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.DiscretePathEffect
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eva4.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var placeDao: PlaceDao
    private lateinit var placeAdapter: PlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        placeDao = DataBase.getDatabase(this).placeDao()
        setupRecyclerView()
        loadPlaces()

        binding.btnAdd.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceAdapter(
            mutableListOf(),
            onDeleteClick = { idPlace -> deletePlace(idPlace) },
            onEditClick = { idPlace -> editPlace(idPlace) },
            onLocationClick = { idPlace -> updateLocation(idPlace) },
            openDetail = { idPlace -> openPlace(idPlace) }
        )
        binding.rvPlaces.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = placeAdapter
        }
    }

    private fun openPlace(idPlace: String) {
        val intent = Intent(this, PlaceDetailActivity::class.java)
        intent.putExtra("idPlace", idPlace)
        startActivity(intent)
    }

    private fun loadPlaces() {
        lifecycleScope.launch(Dispatchers.IO) {
            val places = placeDao.getAllPlaces()
            lifecycleScope.launch(Dispatchers.Main) {
                placeAdapter.update(places)
            }
        }
    }

    private fun deletePlace(idPlace: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.eliminar_lugar))
            .setMessage(getString(R.string.estar_seguro_eliminar))
            .setPositiveButton(getString(R.string.eliminar)) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val place = placeDao.getPlaceById(idPlace)
                    placeDao.deletePlace(place)
                    lifecycleScope.launch(Dispatchers.Main) {
                        loadPlaces()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()

    }

    private fun editPlace(idPlace: String) {
        val intent = Intent(this, UpdatePlaceActivity::class.java)
        intent.putExtra("placeId", idPlace)
        startActivity(intent)
    }

    private fun updateLocation(idPlace: String) {
    }

    fun openCreatePlace() {
        startActivity(Intent(this, CreatePlaceActivity::class.java))
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            val manager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            } else {

                openCreatePlace()
            }
        }
    }

    var locationPermissionRequest =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback<Map<String, Boolean>> { result: Map<String, Boolean> ->
                val fineLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                )
                val coarseLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false
                )
                if (fineLocationGranted != null && fineLocationGranted) {

                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    onBackPressed()
                    Toast.makeText(
                        this,
                        "Se necesitan los permisos completos para continuar",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    onBackPressedDispatcher.onBackPressed()
                    Toast.makeText(
                        this,
                        "Se necesitan los permisos para continuar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Tu GPS esta desactivado, deseas habilitarlo??")
            .setCancelable(false)
            .setPositiveButton("Si") { dialog, id ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                onBackPressedDispatcher.onBackPressed()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onResume() {
        loadPlaces()
        super.onResume()
    }
}
