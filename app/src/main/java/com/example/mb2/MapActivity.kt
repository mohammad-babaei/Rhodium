package com.example.mb2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var infoViewModel: CellInfoViewModel
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        infoViewModel = ViewModelProvider(this).get(CellInfoViewModel::class.java)
        infoViewModel.allInfos.observe(this, Observer { words ->
            // Update the list of markers
            words?.let { updateMarkers(it) }
        })


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMyLocationEnabled(true)
    }

    private fun getMarkerColor(cellInfo: CellInfo) : Float {
        var color: Float
        val type = cellInfo.type
        when(type) {
            "LTE" -> {
                if (cellInfo.strength.toInt() >= -80) color = BitmapDescriptorFactory.HUE_BLUE
                else if (cellInfo.strength.toInt() >= -90) color = BitmapDescriptorFactory.HUE_GREEN
                else if (cellInfo.strength.toInt() >= -100) color = BitmapDescriptorFactory.HUE_YELLOW
                else if (cellInfo.strength.toInt() >= -110) color = BitmapDescriptorFactory.HUE_ORANGE
                else color = BitmapDescriptorFactory.HUE_RED

            }
            "UMTS" -> {
                if (cellInfo.strength.toInt() >= -70) color = BitmapDescriptorFactory.HUE_BLUE
                else if (cellInfo.strength.toInt() >= -85) color = BitmapDescriptorFactory.HUE_GREEN
                else if (cellInfo.strength.toInt() >= -100) color = BitmapDescriptorFactory.HUE_YELLOW
                else if (cellInfo.strength.toInt() >= -110) color = BitmapDescriptorFactory.HUE_ORANGE
                else color = BitmapDescriptorFactory.HUE_RED
            }
            "GSM" -> {
                if (cellInfo.strength.toInt() >= -70) color = BitmapDescriptorFactory.HUE_BLUE
                else if (cellInfo.strength.toInt() >= -85) color = BitmapDescriptorFactory.HUE_GREEN
                else if (cellInfo.strength.toInt() >= -100) color = BitmapDescriptorFactory.HUE_YELLOW
                else if (cellInfo.strength.toInt() >= -110) color = BitmapDescriptorFactory.HUE_ORANGE
                else color = BitmapDescriptorFactory.HUE_RED
            }
            else -> color = BitmapDescriptorFactory.HUE_RED
        }
        return color
    }

    private fun updateMarkers(infos: List<CellInfo>) {
        for (cellInfo in infos) {
            val pos = LatLng(cellInfo.altitude, cellInfo.longitude)
            val color = getMarkerColor(cellInfo)
            val snip = "Strength: " + cellInfo.strength + ", " +
                       "PLMN: " + cellInfo.mcc + cellInfo.mnc
            val marker = mMap.addMarker(MarkerOptions().icon(
                BitmapDescriptorFactory.defaultMarker(color)).position(
                pos).title(cellInfo.type).snippet(snip))
        }
    }
}