package com.openclassrooms.realestatemanager.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ConnectivityUtil {
    companion object {
        lateinit var context: Context
        private var compositeDisposable: CompositeDisposable = CompositeDisposable()

        fun switchAllNetworks(enabled: Boolean) : Completable {
            return Completable.create { emitter ->
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        allNetworksEnableLollipopMinSdkVersion(enabled = enabled)
                    }
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP -> {
                        allNetworksEnableKitKatMaxSdkVersion(enabled = enabled)
                    }
                }
                emitter.onComplete()
            }
        }

        fun waitInternetStateChange(isInternetAvailable: Boolean) : Completable {

            return Completable.create { emitter ->
                compositeDisposable.add(Single.fromCallable { Utils.isInternetAvailable() }
                        .subscribeOn(Schedulers.io())
                        .repeat()
                        .skipWhile { it != isInternetAvailable }
                        .take(1)
                        .delay(Constants.TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                        .subscribe {
                            emitter.onComplete()
                            compositeDisposable.clear()
                        })
            }
        }

        @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
        private fun allNetworksEnableLollipopMinSdkVersion(enabled: Boolean) {
            when(enabled) {
                true -> {
                    switchWifiLollipopMinSdkVersion(true)
                    switchMobileDataLollipopMinSdkVersion(true)
                }

                false -> {
                    switchWifiLollipopMinSdkVersion(false)
                    switchMobileDataLollipopMinSdkVersion(false)
                }
            }
        }

        @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
        fun switchWifiLollipopMinSdkVersion(enabled: Boolean) {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            when(enabled) {
                true -> {
                    uiAutomation.executeShellCommand("svc wifi enable")
                }

                false -> {
                    uiAutomation.executeShellCommand("svc wifi disable")
                }
            }
        }

        @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
        fun switchMobileDataLollipopMinSdkVersion(enabled: Boolean) {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            when(enabled) {
                true -> {
                    uiAutomation.executeShellCommand("svc data enable")
                }

                false -> {
                    uiAutomation.executeShellCommand("svc data disable")
                }
            }
        }

        @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.KITKAT_WATCH)
        private fun allNetworksEnableKitKatMaxSdkVersion(enabled: Boolean) {
            when(enabled) {
                true -> {
                    switchAllNetworksDataKitKatMaxSdkVersion(true)
                }

                false -> {
                    switchAllNetworksDataKitKatMaxSdkVersion(false)
                }
            }
        }

        @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.KITKAT_WATCH)
        fun switchAllNetworksDataKitKatMaxSdkVersion(enabled: Boolean) {
            try {
                val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val connectivityManagerClass = Class.forName(connectivityManager.javaClass.name)
                val iConnectivityManagerField = connectivityManagerClass.getDeclaredField("mService")
                iConnectivityManagerField.isAccessible = true
                val iConnectivityManager = iConnectivityManagerField[connectivityManager]
                val iConnectivityManagerClass = Class.forName(iConnectivityManager.javaClass.name)
                val setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", java.lang.Boolean.TYPE)
                setMobileDataEnabledMethod.isAccessible = true
                setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}