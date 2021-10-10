package com.openclassrooms.realestatemanager

import com.openclassrooms.realestatemanager.di.DaggerTestAppComponent
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.di.property.TestBrowseComponent

class TestBaseApplication : BaseApplication() {

    private var browseComponent: TestBrowseComponent? = null

    override fun initAppComponent() {
        appComponent = DaggerTestAppComponent.builder()
                .application(this)
                .build()
    }

    override fun releaseBrowseComponent() {
        browseComponent = null
    }

    override fun browseComponent(): TestBrowseComponent {
        if (browseComponent == null) {
            browseComponent = (appComponent as TestAppComponent).testBrowseComponent().create()
        }
        return browseComponent as TestBrowseComponent
    }
}