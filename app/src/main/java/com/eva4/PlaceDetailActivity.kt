package com.eva4

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.eva4.databinding.ActivityPlaceDetailBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceDetailBinding
    private lateinit var placeDao: PlaceDao
    private lateinit var photoSavedDao : PhotoSavedDao
    private lateinit var photosAdapter: PhotoAdapterSave
    private lateinit var osMapView: MapView
    private  var idPlace: String ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        placeDao = DataBase.getDatabase(this).placeDao()
        photoSavedDao = DataBase.getDatabase(this).photoSavedDao()

        idPlace = intent.getStringExtra("idPlace") ?: return
        osMapView = binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }

        initPlace()

        binding.btnEdit.setOnClickListener {
            editPlace(intent.getStringExtra("idPlace")!!)
        }
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPhoto.setOnClickListener {
            openCamera()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog { deletePlace(idPlace!!) }
        }
    }

    private fun initPlace() {
        lifecycleScope.launch (Dispatchers.IO){
            val exchangeRate = RetrofitInstance.api.getExchangeRate().dolar_intercambio.valor
            val place = placeDao.getPlaceById(idPlace!!)
            val photos = photoSavedDao.getPhotosByPlaceId(idPlace!!)
            lifecycleScope.launch (Dispatchers.Main){
                updateUI(place, exchangeRate, photos.toMutableList())
            }
        }
    }
    private fun editPlace(idPlace: String) {
        val intent = Intent(this, UpdatePlaceActivity::class.java)
        intent.putExtra("placeId", idPlace)
        startActivity(intent)
    }

    private fun updateUI(place: Place, exchangeRate: Double, photos: MutableList<PhotosSaved>) {
        binding.textNamePlace.text = place.name
        binding.textCost.text = "${place.cost } CLP"+"-"+"${String.format("%.2f",place.cost/exchangeRate)}"+"USD"
        binding.textTrans.text = "${place.costTrans } CLP"+"-"+"${String.format("%.2f",place.costTrans/exchangeRate)}"+"USD"
        binding.textComments.text = place.comment
        binding.imgPlace.load(place.imageRef)

        photosAdapter = PhotoAdapterSave(photos)
        binding.rvPhotosSave.adapter = photosAdapter

        val (lat, long) = place.latLong.split(",").map { it.toDouble() }
        val startPoint = GeoPoint(lat, long)
        val marker = Marker(osMapView).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(this@PlaceDetailActivity, R.drawable.baseline_location_pin_24)
            title = place.name
        }
        osMapView.controller.setZoom(15.0)
        osMapView.controller.setCenter(startPoint)
        osMapView.overlays.add(marker)
    }

    private fun openCamera() {
        ImagePicker.with(this)
            .compress(1024)
            .cameraOnly()
            .maxResultSize(
                800,
                800
            )
            .createIntent { intent ->
                cameraLaunch.launch(intent)
            }
    }

    private val cameraLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                savePhoto(imageUri)
            }
        }
    }

    private fun savePhoto(uri: Uri) {
        val idPlace = intent.getStringExtra("idPlace") ?: return
        val photo = PhotosSaved(idPlace = idPlace, imgUriString = uri.toString())
        lifecycleScope.launch(Dispatchers.IO) {
            photoSavedDao.insertPhoto(photo)
            lifecycleScope.launch(Dispatchers.Main) {
                photosAdapter.addPhoto(photo)
            }
        }
    }

    private fun showDeleteConfirmationDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.eliminar_lugar))
            .setMessage(getString(R.string.estar_seguro_eliminar))
            .setPositiveButton(getString(R.string.eliminar)) { _, _ -> onConfirm() }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun deletePlace(idPlace: String) {
        lifecycleScope.launch {
            val place = placeDao.getPlaceById(idPlace)
            placeDao.deletePlace(place)
            finish()
        }
    }

    override fun onResume() {
        if(idPlace != null) initPlace()
        super.onResume()
    }
}