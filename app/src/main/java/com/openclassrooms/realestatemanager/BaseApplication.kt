package com.openclassrooms.realestatemanager

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.openclassrooms.realestatemanager.di.AppComponent
import com.openclassrooms.realestatemanager.di.DaggerAppComponent
import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import com.openclassrooms.realestatemanager.util.FirestoreUtils.Companion.populateFirestore
import timber.log.Timber
import timber.log.Timber.DebugTree

open class BaseApplication: Application() {

    lateinit var appComponent: AppComponent

    private var browseComponent: BrowseComponent? = null

    override fun onCreate() {
        super.onCreate()
        initAppComponent()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        // Call to Utils function to populate the firestore database
        Firebase.firestore.collection(PROPERTIES_COLLECTION).get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        populateFirestore()
                    }
                }
    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build()
    }

    open fun releaseBrowseComponent() {
        browseComponent = null
    }

    open fun browseComponent(): BrowseComponent {
        if (browseComponent == null) {
            browseComponent = appComponent.browseComponent().create()
        }
        return browseComponent as BrowseComponent
    }
}