package com.openclassrooms.realestatemanager.di

import com.openclassrooms.realestatemanager.di.realestate.RealEstateComponent
import dagger.Module

@Module(subcomponents = [
    RealEstateComponent::class
])
class SubComponentsModule