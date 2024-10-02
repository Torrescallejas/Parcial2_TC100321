package com.example.parcial2_tc100321

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    private lateinit var mapView: MapView

    // Variable para almacenar la posición inicial
    private var startPoint: GeoPoint? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Configuración de OSMDroid
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))

        // Inicialización del MapView
        mapView = view.findViewById(R.id.mapView)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Ajustar la posición inicial del mapa
        if (startPoint != null) {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(startPoint)

            // Añadir un marcador en el mapa
            val marker = Marker(mapView)
            marker.position = startPoint!!
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Tu ubicación"
            mapView.overlays.add(marker)
        }

        return view
    }

    // Método para actualizar la ubicación en el mapa
    fun updateLocation(latitude: Double, longitude: Double) {
        startPoint = GeoPoint(latitude, longitude) // Actualiza el startPoint con la nueva ubicación
        if (mapView != null) {
            mapView.controller.setCenter(startPoint) // Centra el mapa en la nueva ubicación
            mapView.overlays.clear() // Limpiar los marcadores existentes
            val marker = Marker(mapView)
            marker.position = startPoint!!
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Tu ubicación"
            mapView.overlays.add(marker) // Añade un nuevo marcador
            mapView.invalidate() // Redibuja el mapa para mostrar los cambios
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}