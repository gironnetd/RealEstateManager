package com.openclassrooms.realestatemanager.util

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NetworkConnectionLiveData
@Inject
constructor(val context: Context) : LiveData<Boolean>() {

    private var connectivityManager: ConnectivityManager
            = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

    override fun onActive() {
        super.onActive()
        TODO("Not yet implemented")
    }

    override fun onInactive() {
        super.onInactive()
        TODO("Not yet implemented")
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkAvailableRequest() {
        TODO("Not yet implemented")
    }

    private fun createNetworkCallback(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    TODO("Not yet implemented")
                }

                override fun onLost(network: Network) {
                    TODO("Not yet implemented")
                }
            }
            return connectivityManagerCallback
        } else {
            throw IllegalAccessError("Should not happened")
        }
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            TODO("Not yet implemented")
        }
    }
}