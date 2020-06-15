package com.example.mb2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cell_info_table")
class CellInfo(@PrimaryKey(autoGenerate = true) val id: Int? = null,
               val type: String,
               val gsm_rssi: String,
               val umts_rscp: String,
               val lte_rsrq: String,
               val lte_rsrp: String,
               val lte_cqi: String,
               val strength: String,
               val mcc: String,
               val mnc: String,
               val lac: String,
               val tac: String,
               val longitude: Double,
               val altitude: Double,
               val time: Long

)