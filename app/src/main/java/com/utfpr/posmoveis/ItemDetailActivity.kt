package com.utfpr.posmoveis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utfpr.posmoveis.databinding.ActivityItemDetailBinding
import com.utfpr.posmoveis.model.Item
import com.utfpr.posmoveis.service.Result
import com.utfpr.posmoveis.service.RetrofitClient
import com.utfpr.posmoveis.service.safeApiCall
import com.utfpr.posmoveis.ui.loadUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.utfpr.posmoveis.model.ItemValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityItemDetailBinding

    private lateinit var itemValue: ItemValue

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        loadItem()
       setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (::itemValue.isInitialized) {
            // Se o item já estiver carregado, carregue-o no mapa
            loadItemInGoogleMap()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
       binding.deleteCar.setOnClickListener {
            deleteItem()
        }
       binding.editCar.setOnClickListener {
            editItem()
        }
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getItem(itemId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        itemValue = result.data
                        handleSuccess()
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.error_fetch_item,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleSuccess() {
        binding.textViewName.text = itemValue.value.name
        binding.textViewYear.text = itemValue.value.year
        binding.textViewLicence.setText(itemValue.value.licence)
        binding.imageViewCar.loadUrl(itemValue.value.imageUrl)

      loadItemInGoogleMap()
    }

    private fun loadItemInGoogleMap() {
        if (!::mMap.isInitialized) return
        itemValue.value.place?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val location = LatLng(it.lat, it.long)
            mMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("teste")
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    15f
                )
            )
        }
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleteItem(itemValue.id) }

           withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> handleSuccessDelete()

                    is Result.Error -> {
                        Toast.makeText(
                           this@ItemDetailActivity,
                            R.string.error_delete,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun editItem() {

    val newLicence = binding.textViewLicence.text.toString()
        if (newLicence.isEmpty()) {
            Toast.makeText(this, "A placa não pode ser vazia", Toast.LENGTH_SHORT).show()
            return
        }


        CoroutineScope(Dispatchers.IO).launch {

        val updatedItemValue = itemValue.value.copy(licence = newLicence)

        val result = safeApiCall {
                RetrofitClient.apiService.updateItem(
                    itemValue.id,
                    updatedItemValue
                )
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.success_update,
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }

                    is Result.Error -> {
                        Toast.makeText(

                            this@ItemDetailActivity,
                            R.string.error_update,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun handleSuccessDelete() {
        Toast.makeText(
            this,
            R.string.success_delete,
            Toast.LENGTH_LONG
        ).show()
        finish()
    }

    companion object {
        const val ARG_ID = "arg_id"

        fun newIntent(context: Context, itemId: String): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
        }
    }
}