package com.openclassrooms.realestatemanager.util

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NetworkConnectionLiveData
@Inject
constructor(val context: Context) : LiveData<Boolean>() {

    private var connectivityManager: ConnectivityManager
            = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    init {
        compositeDisposable.add(Single.fromCallable { Utils.isInternetAvailable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isInternetAvailable ->
                    value = isInternetAvailable
                    compositeDisposable.clear()
                })
    }

    override fun onActive() {
        super.onActive()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                connectivityManager.registerDefaultNetworkCallback(createNetworkCallback())

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> lollipopNetworkAvailableRequest()

            else -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    context.registerReceiver(networkReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
                }
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } else {
            context.unregisterReceiver(networkReceiver);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkAvailableRequest() {
        val builder = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        connectivityManager.registerNetworkCallback(builder.build(), createNetworkCallback())
    }

    private fun createNetworkCallback(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    val hasInternetCapability = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    //Timber.d("onAvailable: ${network}, $hasInternetCapability")
                    if (hasInternetCapability == true) {
                        waitForInternetStateChange()
                    }
                }

                override fun onLost(network: Network) {
                    postValue(false)
                }
            }
            return networkCallback
        } else {
            throw IllegalAccessError("Should not happened")
        }
    }

    private fun waitForInternetStateChange() {
        compositeDisposable.add(Single.fromCallable { Utils.isInternetAvailable() }
                .subscribeOn(Schedulers.io())
                .repeat()
                .skipWhile { isInternetAvailable -> isInternetAvailable == value }
                .subscribe { isInternetAvailable ->
                    postValue(isInternetAvailable)
                    compositeDisposable.clear()
                }
        )
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            waitForInternetStateChange()
        }
    }
}