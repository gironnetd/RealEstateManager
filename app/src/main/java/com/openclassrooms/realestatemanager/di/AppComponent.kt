package com.openclassrooms.realestatemanager.di

import android.app.Application
import com.openclassrooms.realestatemanager.di.realestate.RealEstateComponent
import com.openclassrooms.realestatemanager.fragments.MainNavHostFragment
import com.openclassrooms.realestatemanager.view.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AppModule::class,
            FragmentModule::class,
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

    fun realEstateComponent(): RealEstateComponent.Factory
}