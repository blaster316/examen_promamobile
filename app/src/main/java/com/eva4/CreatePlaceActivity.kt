package com.eva4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eva4.databinding.ActivityCreatePlaceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatePlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePlaceBinding
    private lateinit var placeDao: PlaceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DataBase.getDatabase(this)
        placeDao = db.placeDao()

        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            savePlace()
        }

        binding.btnLocation.setOnClickListener {
            openMap()
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
            binding.edtLatLong.setText(""+latitude+","+longitude)
        }
    }

    private fun savePlace() {
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

        if(name.isEmpty()) {
            binding.edtPlaceName.error =  getString(R.string.error_message)
            return
        }

        if(imageRef.isEmpty()) {
            binding.edtImgRef.error =  getString(R.string.error_message)
            return
        }
        if(latLong.isEmpty()) {
            binding.edtLatLong.error =  getString(R.string.error_message)
            return
        }
        if(costText.isEmpty()) {
            binding.edtCost.error =  getString(R.string.error_message)
            return
        }
        if(comment.isEmpty()) {
            binding.edtComment.error =  getString(R.string.error_message)
            return
        }


        val cost = costText.toDoubleOrNull() ?: 0.0
        val costTrans = costTransText.toDoubleOrNull() ?: 0.0

        val place = Place(
            name = name,
            imageRef = imageRef,
            latLong = latLong,
            cost = cost,
            costTrans = costTrans,
            comment = comment
        )

        lifecycleScope.launch(Dispatchers.IO) {
            placeDao.insertPlace(place)
            lifecycleScope.launch (Dispatchers.Main){
                Toast.makeText(this@CreatePlaceActivity, getString(R.string.sitio_agregado), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
