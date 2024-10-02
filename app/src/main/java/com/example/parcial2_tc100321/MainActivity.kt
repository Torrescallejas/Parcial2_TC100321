package com.example.parcial2_tc100321

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import android.content.Intent
import android.provider.MediaStore

class MainActivity : AppCompatActivity() {

    private val LOCATION_REQUEST_CODE = 100
    private val MEDIA_REQUEST_CODE = 101
    private val NOTIFICATION_REQUEST_CODE = 102
    private val CHANNEL_ID = "notification_channel"

    // FusedLocationProviderClient para obtener la ubicación
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val buttonOpenGallery = findViewById<Button>(R.id.button_open_gallery)

        // Crear el canal de notificación (necesario para Android 8.0 o superior)
        createNotificationChannel()

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Ajustes de ventanas para Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar botones
        val buttonOpenMap = findViewById<Button>(R.id.button_open_map)
        val buttonMedia = findViewById<Button>(R.id.button_media)

        // Acción para abrir el mapa
        buttonOpenMap.setOnClickListener {
            openMapFragment()
        }

        // Acción para solicitar permiso de multimedia solo cuando se presiona el botón
        buttonMedia.setOnClickListener {
            requestMediaPermission()
        }

        buttonOpenGallery.setOnClickListener {
            openGalleryIfPermitted()
        }

        // Solicitar permisos
        requestLocationPermission()
        requestNotificationPermission() // Se solicita el permiso de notificaciones al inicio
    }

    // Función para abrir la galería si el permiso ha sido concedido
    private fun openGalleryIfPermitted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            // El permiso ha sido concedido, abrir la galería
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivity(intent)
        } else {
            // Si no está concedido, se pide el permiso
            Toast.makeText(this, "Permiso de multimedia no concedido", Toast.LENGTH_SHORT).show()
            requestMediaPermission()
        }
    }

    private fun openMapFragment() {
        // Crear y reemplazar el fragmento dentro del contenedor
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_container, MapFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        // Obtener la ubicación después de abrir el fragmento del mapa
        getLastLocation()
    }

    // Función para obtener la última ubicación
    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener<Location?> { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(this, "Latitud: $latitude, Longitud: $longitude", Toast.LENGTH_SHORT).show()

                    // Aquí se obtiene la instancia del MapFragment y se actualiza la ubicación
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.main_container) as? MapFragment
                    mapFragment?.updateLocation(latitude, longitude) // Actualiza la ubicación en el mapa
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para solicitar permiso de ubicación
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para solicitar permisos de archivos multimedia (solo se ejecuta cuando el botón es presionado)
    private fun requestMediaPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), MEDIA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Permiso de multimedia concedido", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para solicitar permiso de notificaciones
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_REQUEST_CODE)
        } else {
            // Si el permiso ya está concedido, enviar la notificación
            sendNotification()
        }
    }

    // Crear el canal de notificación para Android 8.0 y superior
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal de notificación"
            val descriptionText = "Canal para notificaciones de agradecimiento"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Registrar el canal
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Función para enviar una notificación de agradecimiento
    private fun sendNotification() {
        // Verificar si tenemos el permiso de notificaciones
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("¡Gracias!")
                .setContentText("Gracias por conceder el permiso de notificaciones.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                // Enviar la notificación con un ID único
                notify(1, builder.build())
            }
        } else {
            // Solicitar el permiso si no lo tiene
            requestNotificationPermission()
        }
    }

    // Manejo de la respuesta a los permisos solicitados
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                    getLastLocation() // Obtener la ubicación si se concede el permiso
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                }
            }
            MEDIA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de multimedia concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de multimedia denegado", Toast.LENGTH_SHORT).show()
                }
            }
            NOTIFICATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
                    sendNotification() // Enviar la notificación después de aceptar el permiso
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}