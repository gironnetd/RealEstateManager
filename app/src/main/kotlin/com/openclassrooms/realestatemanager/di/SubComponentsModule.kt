package com.openclassrooms.realestatemanager.di

import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.di.property.search.SearchComponent
import dagger.Module

@Module(subcomponents = [
    BrowseComponent::class,
    SearchComponent::class
])
class SubComponentsModule