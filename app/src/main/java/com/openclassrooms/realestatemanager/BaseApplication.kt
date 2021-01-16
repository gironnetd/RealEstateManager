package com.openclassrooms.realestatemanager

import android.app.Application
import com.openclassrooms.realestatemanager.di.AppComponent
import com.openclassrooms.realestatemanager.di.DaggerAppComponent
import com.openclassrooms.realestatemanager.di.realestate.RealEstateComponent

open class BaseApplication: Application() {

    private val TAG: String = "AppDebug"

    lateinit var appComponent: AppComponent

    private var realEstateComponent: RealEstateComponent? = null

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build()
    }

    fun releaseRealEstateComponent() {
        realEstateComponent = null
    }

    fun realEstateComponent(): RealEstateComponent {
        if (realEstateComponent == null) {
            realEstateComponent = appComponent.realEstateComponent().create()
        }
        return realEstateComponent as RealEstateComponent
    }
}