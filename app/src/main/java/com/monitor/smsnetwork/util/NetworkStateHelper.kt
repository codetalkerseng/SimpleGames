package com.monitor.smsnetwork.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import androidx.core.app.ActivityCompat

class NetworkStateHelper(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun getCurrentNetworkType(): String {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                "WIFI"
            }
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                when (telephonyManager.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSDPA -> "HSPA"
                    TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                    TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                    TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                    TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                    TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO"
                    else -> "MOBILE"
                }
            }
            else -> "NONE"
        }
    }

    fun getSignalStrength(): Int {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return -1
        }

        try {
            val cellInfoList = telephonyManager.allCellInfo
            if (cellInfoList != null && cellInfoList.isNotEmpty()) {
                val cellInfo = cellInfoList[0]
                return when (cellInfo) {
                    is CellInfoLte -> cellInfo.cellSignalStrength.dbm
                    is CellInfoGsm -> cellInfo.cellSignalStrength.dbm
                    is CellInfoWcdma -> cellInfo.cellSignalStrength.dbm
                    else -> -1
                }
            }
        } catch (e: Exception) {
            // Ignore exceptions
        }
        return -1
    }

    fun getSignalLevel(): Int {
        val strength = getSignalStrength()
        return when {
            strength >= -70 -> 4
            strength >= -85 -> 3
            strength >= -100 -> 2
            strength >= -110 -> 1
            else -> 0
        }
    }

    fun isWifiCallingActive(): Boolean {
        // Note: Detecting WiFi calling is complex and may not be fully reliable
        // This is a best-effort approach
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Check if currently on WiFi and phone is in call
                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
                val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

                // You might need to check call state as well for more accuracy
                return isWifi && telephonyManager.callState != TelephonyManager.CALL_STATE_IDLE
            }
        } catch (e: Exception) {
            // Ignore
        }
        return false
    }

    fun isRoaming(): Boolean {
        return telephonyManager.isNetworkRoaming
    }

    fun getWifiSsid(): String? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo?.ssid?.removeSurrounding("\"")
    }

    fun getWifiFrequency(): Int? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val wifiInfo = wifiManager.connectionInfo
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wifiInfo?.frequency
        } else {
            null
        }
    }

    fun getCellId(): Int? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        try {
            val cellLocation = telephonyManager.cellLocation
            return when (cellLocation) {
                is GsmCellLocation -> cellLocation.cid
                is CdmaCellLocation -> cellLocation.baseStationId
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun getLac(): Int? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        try {
            val cellLocation = telephonyManager.cellLocation
            return if (cellLocation is GsmCellLocation) {
                cellLocation.lac
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun isAirplaneModeOn(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }
}
