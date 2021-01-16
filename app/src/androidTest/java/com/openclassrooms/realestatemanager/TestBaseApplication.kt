package com.openclassrooms.realestatemanager

import com.openclassrooms.realestatemanager.di.DaggerTestAppComponent

class TestBaseApplication : BaseApplication() {

    override fun initAppComponent() {
        appComponent = DaggerTestAppComponent.builder()
                .application(this)
                .build()
    }
}