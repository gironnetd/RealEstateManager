package com.openclassrooms.realestatemanager.di

import android.app.Application
import com.openclassrooms.realestatemanager.data.cache.provider.AppContentProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.di.property.search.SearchComponent
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.fragments.MainNavHostFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AppModule::class,
            AppFragmentModule::class,
            SubComponentsModule::class,
            RepositoryModule::class,
            ViewModelModule::class
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
    fun searchComponent(): SearchComponent.Factory
}