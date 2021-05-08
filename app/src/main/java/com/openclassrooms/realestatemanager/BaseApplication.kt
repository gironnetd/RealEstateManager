package com.openclassrooms.realestatemanager

import androidx.multidex.MultiDexApplication
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.realestatemanager.di.AppComponent
import com.openclassrooms.realestatemanager.di.DaggerAppComponent
import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

open class BaseApplication: MultiDexApplication() {

    lateinit var appComponent: AppComponent

    private var browseComponent: BrowseComponent? = null

    override fun onCreate() {
        super.onCreate()
        initAppComponent()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            FirebaseFirestore.setLoggingEnabled(true)
        }

        MapsInitializer.initialize(this)


        // Call to Utils function to populate the firestore database
//        Firebase.firestore.collection(PROPERTIES_COLLECTION).get()
//                .addOnSuccessListener { querySnapshot ->
//                    if (querySnapshot.isEmpty) {
//                        populateFirestore()
//                    }
//                }
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