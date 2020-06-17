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
        val iranPosition = LatLng(32.364811, 53.799715)
        mMap.addMarker(MarkerOptions().position(iranPosition).title("IRAN!"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(iranPosition))
    }

    private fun getMarkerColor(cellInfo: CellInfo) : Float {
        var color: Float
        val type = cellInfo.type
        when(type) {
            "LTE" -> {
                if (cellInfo.lte_rsrq.toInt() > -5 && cellInfo.lte_rsrp.toInt() > -84) color = 0F
                else if (cellInfo.lte_rsrq.toInt() > -9 && cellInfo.lte_rsrp.toInt() > -102) color = 120F
                else if (cellInfo.lte_rsrq.toInt() > -12 && cellInfo.lte_rsrp.toInt() > -111) color = 60F
                else if (cellInfo.lte_rsrq.toInt() < -12 && cellInfo.lte_rsrp.toInt() < -111) color = 240F
                else color = 359F

            }
            "UMTS" -> {
                if (cellInfo.strength.toInt() >= -70) color = 0f
                else if (cellInfo.strength.toInt() >= -85) color = 0f
                else if (cellInfo.strength.toInt() >= -100) color = 0f
                else if (cellInfo.strength.toInt() >= -110) color = 0f
                else color = 0f
            }
            "GSM" -> {
                if (cellInfo.strength.toInt() >= -70) color = 0f
                else if (cellInfo.strength.toInt() >= -85) color = 0f
                else if (cellInfo.strength.toInt() >= -100) color = 0f
                else if (cellInfo.strength.toInt() >= -110) color = 0f
                else color = 0f
            }
            else -> color = 0f
        }

        Log.i("MapActivity", color.toString());
        Log.i("MapActivity", cellInfo.longitude.toString()+ " " + cellInfo.altitude.toString())
        return color
    }

    private fun updateMarkers(infos: List<CellInfo>) {
        for (cellInfo in infos) {
            val pos = LatLng(cellInfo.altitude, cellInfo.longitude)
            val color = getMarkerColor(cellInfo)
            mMap.addMarker(MarkerOptions().icon(
                BitmapDescriptorFactory.defaultMarker(color)).position(
                pos).title(cellInfo.type).snippet("strength: " + cellInfo.strength))
        }
    }
}