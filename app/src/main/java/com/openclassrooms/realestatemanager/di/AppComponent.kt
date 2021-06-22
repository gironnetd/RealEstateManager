package com.openclassrooms.realestatemanager.di

import android.app.Application
import com.openclassrooms.realestatemanager.data.cache.provider.AppContentProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.fragments.MainNavHostFragment
import com.openclassrooms.realestatemanager.ui.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AppModule::class,
            AppFragmentModule::class,
            SubComponentsModule::class
        ])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): AppComponent
    }

    fun inject(mainActivity: MainActivity)

    fun inject(mainNavHostFragment: MainNavHostFragment)

    fun inject(contentProvider: AppContentProvider)

    fun browseComponent(): BrowseComponent.Factory
}