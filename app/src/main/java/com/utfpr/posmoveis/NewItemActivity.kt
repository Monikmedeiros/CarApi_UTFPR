package com.utfpr.posmoveis

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import com.utfpr.posmoveis.databinding.ActivityNewItemBinding
import com.utfpr.posmoveis.model.Item
import com.utfpr.posmoveis.model.Place
import com.utfpr.posmoveis.service.RetrofitClient
import com.utfpr.posmoveis.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import com.utfpr.posmoveis.service.Result.Success
import com.utfpr.posmoveis.service.Result.Error
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.uuid.Uuid

class NewItemActivity : AppCompatActivity(), OnMapReadyCallback  {
    private lateinit var binding: ActivityNewItemBinding
    private lateinit var mMap: GoogleMap

    private var selectedMarker: Marker? = null
    private lateinit var imageUri: Uri

    private var imageFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            binding.imageUrl.setText("Imagem Obtida")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        requestLocationPermission()
        setupGoogleMap()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.saveCar.setOnClickListener {
            saveItem()
        }

        binding.takePictureCar.setOnClickListener {
            takePicture()
        }
    }

    private fun takePicture(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }

    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        // quando abrir a camera armazena a imagem no uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {

        // define o nome do arquivo
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // guarda a imagem temporaria
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(
            this,
            "com.utfpr.posmoveis.fileprovider",
            imageFile!!
        )
    }



    @SuppressLint("MissingPermission")
    private fun requestLocationPermission() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Se o usuário permitiu a localização, obtenha a última localização, caso contrário, seguimos sem localização exata.
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLong = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            }
        }
    }
    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        getDeviceLocation();
        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            )
        }

    }

    override fun onRequestPermissionsResult(
        // verificae se a permissão de localização foi concedida

        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // tenho a permissão de localização vou carregar a minha localização
                    loadCurrentLocation()
                } else {
                    Toast.makeText(
                        this,
                        R.string.location_permission_denied,
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
        }
    }

    private fun getDeviceLocation() {
        // verificar a permissão de localização
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadCurrentLocation()
        } else {
            // solicitar a permissão de localização
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }


    }

    @SuppressLint("MissingPermission")// como ja verifiquei que temos a permissão posso chamar o metodo sem problema
    private fun loadCurrentLocation() {

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // buscar a localização do dispositivo
        fusedLocationClient
    }
        private fun saveItem() {
            if (!validateFields()) return


            uploadImageToFirebase()


        }

    private fun saveData(){
        val itemPosition = selectedMarker?.position?.let {
            Place(
                it.latitude,
                it.longitude
            )
        }



        CoroutineScope(Dispatchers.IO).launch {
            val id = (1000..9999).random().toString()
            val item = Item(
                id = id,
                binding.imageUrl.text.toString(),
                binding.year.text.toString(),
                binding.name.text.toString(),
                licence = binding.licence.text.toString(),
                place = itemPosition
            )
            val result = safeApiCall { RetrofitClient.apiService.addItem(item) }


            withContext(Dispatchers.Main) {
                when (result) {
                    is com.utfpr.posmoveis.service.Result.Success -> handleOnSuccess()
                    is com.utfpr.posmoveis.service.Result.Error -> handleOnError()
                }
            }
        }
    }

    private fun uploadImageToFirebase(){
        // apenas se eu tiver o arquivo
        imageFile?.let {


            // inicializar Firebase storage
            val storageRef = FirebaseStorage.getInstance().reference

            // criar uma referencia unica para a imagem
            val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

            // converter o bitmap em um Bytemap
            val baos = ByteArrayOutputStream()
            val imageBitmap = BitmapFactory.decodeFile(it.path)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val data = baos.toByteArray()
            //desabilitar controles durante o upload
            onLoadImage(true)
            imageRef.putBytes(data)
                .addOnFailureListener {
                    onLoadImage(false)
                    Toast.makeText(this, "Falha ao realizar o Upload para o Firebase", Toast.LENGTH_LONG).show()
                }

                .addOnSuccessListener {
                    onLoadImage(false)
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // atribui a uri de download do firebase ao text para salvar
                        binding.imageUrl.setText(uri.toString())

                        // salvar apenas quando tiver a url
                        saveData()
                    }
                }
        }

    }


    fun onLoadImage(isLoading: Boolean) {
        binding.loadImageProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.takePictureCar.isEnabled = !isLoading
        binding.saveCar.isEnabled = !isLoading
    }


    private fun handleOnError() {
        Toast.makeText(
            this@NewItemActivity,
            R.string.error_add_item,
            Toast.LENGTH_SHORT
        ).show()
    }
    private fun handleOnSuccess() {
        Toast.makeText(
            this,
            R.string.success_add_item,
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    private fun validateFields(): Boolean {
        var isValid = false
        if (binding.name.text.isNullOrBlank()) {
            binding.name.error = getString(R.string.required_field)
            isValid = true
        }
        if (binding.licence.text.isNullOrBlank()) {
            binding.licence.error = getString(R.string.required_field)
            isValid = true
        }
        if (binding.year.text.isNullOrBlank()) {
            binding.year.error = getString(R.string.required_field)
            isValid = true
        }
        if (binding.imageUrl.text.isNullOrBlank()) {
            binding.imageUrl.error = getString(R.string.required_field)
            isValid = true
        }

      return!isValid
    }


    companion object {

        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        fun newIntent(context: Context): Intent {
            return Intent(context, NewItemActivity::class.java)
        }
    }
}
