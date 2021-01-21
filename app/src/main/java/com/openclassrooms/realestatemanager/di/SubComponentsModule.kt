package com.openclassrooms.realestatemanager.di

import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import dagger.Module

@Module(subcomponents = [
    BrowseComponent::class,
])
class SubComponentsModule