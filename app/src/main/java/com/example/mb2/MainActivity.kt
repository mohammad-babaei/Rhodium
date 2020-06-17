package com.example.mb2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import android.provider.Settings


class MainActivity : AppCompatActivity() {
    private lateinit var infoViewModel: CellInfoViewModel
    private var current_location: Location? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = InfoListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val start_sample_btn = findViewById<Button>(R.id.start_sample)


        infoViewModel = ViewModelProvider(this).get(CellInfoViewModel::class.java)
        infoViewModel.allInfos.observe(this, Observer { words ->
            // Update the cached copy of the words in the adapter.
            words?.let { adapter.setWords(it) }
        })

        //start sample button listener
        start_sample_btn.setOnClickListener {
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                        val mainHandler = Handler(Looper.getMainLooper())

                        mainHandler.post(object : Runnable {
                            override fun run() {
                                minusOneSecond(tm)
                                mainHandler.postDelayed(this, 5000)
                            }
                        })

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) {
                    }
                }).check()
        }

    }

    @SuppressLint("MissingPermission")
    fun minusOneSecond(tm: TelephonyManager){

        var strength: String = ""
        var gsm_rssi: String = ""
        var umts_rscp: String = ""
        var lte_rsrq: String = ""
        var lte_rsrp: String = ""
        var lte_cqi: String = ""
        var mcc: String = ""
        var mnc: String = ""
        var lac: String = ""
        var tac: String = ""


        var typee: String = ""
        val infos = tm.allCellInfo
        if (infos.size == 0){
            Toast.makeText(this@MainActivity, "No Signal", Toast.LENGTH_SHORT).show()
        }

        try {
            val cellInfo = tm.allCellInfo[0]

            val networkOperator = tm.networkOperator

            if (cellInfo is CellInfoGsm)
            {
                val cellSignalStrengthGsm: CellSignalStrengthGsm = cellInfo.cellSignalStrength
                val cellIdentityGsm: CellIdentityGsm = cellInfo.cellIdentity
                mcc = cellIdentityGsm.mcc.toString()
                mnc = cellIdentityGsm.mnc.toString()
                lac = cellIdentityGsm.lac.toString()
                strength = cellSignalStrengthGsm.dbm.toString()
                gsm_rssi = cellSignalStrengthGsm.asuLevel.toString()
                typee = "GSM"

            }
            if (cellInfo is CellInfoWcdma)
            {
                val cellSignalStrengthWcdma: CellSignalStrengthWcdma = cellInfo.cellSignalStrength
                val cellIdentityWcdma: CellIdentityWcdma = cellInfo.cellIdentity
                strength = cellSignalStrengthWcdma.dbm.toString()
                mcc = cellIdentityWcdma.mcc.toString()
                mnc = cellIdentityWcdma.mnc.toString()
                lac = cellIdentityWcdma.lac.toString()
                umts_rscp = cellSignalStrengthWcdma.asuLevel.toString()
                typee = "UMTS"

            }
            if (cellInfo is CellInfoLte)
            {
                val cellSignalStrengthLte: CellSignalStrengthLte = cellInfo.cellSignalStrength
                val cellIdentityLte: CellIdentityLte = cellInfo.cellIdentity
                mcc = cellIdentityLte.mcc.toString()
                mnc = cellIdentityLte.mnc.toString()
                tac = cellIdentityLte.tac.toString()
                strength = cellSignalStrengthLte.dbm.toString()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    lte_rsrq = cellSignalStrengthLte.rsrq.toString()
                    lte_rsrp = cellSignalStrengthLte.rsrp.toString()
                    lte_cqi = cellSignalStrengthLte.cqi.toString()
                    cellSignalStrengthLte.rsrp.toString()
                }
                typee = "LTE"

            }
            if (cellInfo is CellInfoCdma)
            {
                val cellSignalStrengthCdma: CellSignalStrengthCdma = cellInfo.cellSignalStrength
                strength = cellSignalStrengthCdma.dbm.toString()
                umts_rscp = cellSignalStrengthCdma.asuLevel.toString()
                typee = "UMTS"

            }
        }
        catch (e: IndexOutOfBoundsException) {
            Toast.makeText(this@MainActivity, "No Signal", Toast.LENGTH_SHORT).show()
        }
        finally {
            requestNewLocationData()
            if (current_location != null)
            {
                val info = CellInfo(mcc = mcc, mnc = mnc, tac = tac, lac = lac, type = typee, gsm_rssi = gsm_rssi, umts_rscp = umts_rscp, lte_rsrq = lte_rsrq, lte_rsrp = lte_rsrp, lte_cqi = lte_cqi, strength = strength, longitude = current_location!!.longitude, altitude = current_location!!.latitude, time = System.currentTimeMillis())
                val a = infoViewModel.insert(info)
//                val mss = "$strength $typee \n latitude: ${current_location?.latitude.toString()}, longitude: ${current_location?.longitude.toString()}"
//                Toast.makeText(this@MainActivity, mss, Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
//            Toast.makeText(this@MainActivity, "latitude: ${mLastLocation.latitude.toString()}, longitude: ${mLastLocation.longitude.toString()}", Toast.LENGTH_SHORT).show()
            current_location = mLastLocation
        }
    }


//    fun getNetworkGeneration(context: Context): String? {
//        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        return when (telephonyManager.networkType) {
//            TelephonyManager.NETWORK_TYPE_UNKNOWN -> null
//
//            TelephonyManager.NETWORK_TYPE_GPRS,
//            TelephonyManager.NETWORK_TYPE_EDGE,
//            TelephonyManager.NETWORK_TYPE_CDMA,
//            TelephonyManager.NETWORK_TYPE_1xRTT,
//            TelephonyManager.NETWORK_TYPE_IDEN,
//            TelephonyManager.NETWORK_TYPE_GSM -> "2G"
//
//            TelephonyManager.NETWORK_TYPE_UMTS,
//            TelephonyManager.NETWORK_TYPE_EVDO_0,
//            TelephonyManager.NETWORK_TYPE_EVDO_A,
//            TelephonyManager.NETWORK_TYPE_HSDPA,
//            TelephonyManager.NETWORK_TYPE_HSUPA,
//            TelephonyManager.NETWORK_TYPE_HSPA,
//            TelephonyManager.NETWORK_TYPE_EVDO_B,
//            TelephonyManager.NETWORK_TYPE_EHRPD,
//            TelephonyManager.NETWORK_TYPE_HSPAP,
//            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
//
//            TelephonyManager.NETWORK_TYPE_LTE,
//            TelephonyManager.NETWORK_TYPE_IWLAN -> "4G"
//
//            TelephonyManager.NETWORK_TYPE_NR -> "5G"
//
//            else -> null
//        }
//    }
}
