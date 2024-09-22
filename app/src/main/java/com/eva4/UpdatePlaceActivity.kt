package com.eva4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eva4.databinding.ActivityUpdatePlaceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdatePlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePlaceBinding
    private lateinit var placeDao: PlaceDao
    private var placeId: String = ""
    private lateinit var place: Place

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdatePlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DataBase.getDatabase(this)
        placeDao = db.placeDao()


        placeId = intent.getStringExtra("placeId")!!


        loadPlaceData(placeId)

        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            updatePlace()
        }

        binding.btnLocation.setOnClickListener {
            openMap()
        }
    }

    private fun loadPlaceData(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            place = placeDao.getPlaceById(id)


            lifecycleScope.launch(Dispatchers.Main) {
                binding.edtPlaceName.setText(place.name)
                binding.edtImgRef.setText(place.imageRef)
                binding.edtLatLong.setText(place.latLong)
                binding.edtCost.setText(place.cost.toString())
                binding.edtCostTrans.setText(place.costTrans.toString())
                binding.edtComment.setText(place.comment)
            }
        }
    }

    private fun openMap() {
        val intent = Intent(this, MapsActivity::class.java)
        openMapLauncher.launch(intent)
    }

    private val openMapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val latitude = result.data?.getDoubleExtra("latitude", 0.0)
            val longitude = result.data?.getDoubleExtra("longitude", 0.0)
            binding.edtLatLong.setText("$latitude,$longitude")
        }
    }

    private fun updatePlace() {

        binding.edtPlaceName.error = null
        binding.edtImgRef.error = null
        binding.edtLatLong.error = null
        binding.edtCost.error = null
        binding.edtCostTrans.error = null
        binding.edtComment.error = null

        val name = binding.edtPlaceName.text.toString().trim()
        val imageRef = binding.edtImgRef.text.toString().trim()
        val latLong = binding.edtLatLong.text.toString().trim()
        val costText = binding.edtCost.text.toString().trim()
        val costTransText = binding.edtCostTrans.text.toString().trim()
        val comment = binding.edtComment.text.toString().trim()

        if (name.isEmpty()) {
            binding.edtPlaceName.error = getString(R.string.error_message)
            return
        }

        if (imageRef.isEmpty()) {
            binding.edtImgRef.error = getString(R.string.error_message)
            return
        }

        if (latLong.isEmpty()) {
            binding.edtLatLong.error = getString(R.string.error_message)
            return
        }

        if (costText.isEmpty()) {
            binding.edtCost.error = getString(R.string.error_message)
            return
        }

        if (comment.isEmpty()) {
            binding.edtComment.error = getString(R.string.error_message)
            return
        }

        val cost = costText.toDoubleOrNull() ?: 0.0
        val costTrans = costTransText.toDoubleOrNull() ?: 0.0


        place.name = name
        place.imageRef = imageRef
        place.latLong = latLong
        place.cost = cost
        place.costTrans = costTrans
        place.comment = comment


        lifecycleScope.launch(Dispatchers.IO) {
            placeDao.updatePlace(place)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@UpdatePlaceActivity, getString(R.string.sitio_actualizado), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}