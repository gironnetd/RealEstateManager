package com.openclassrooms.realestatemanager.di

import com.openclassrooms.realestatemanager.di.property.TestBrowseComponent
import dagger.Module

@Module(subcomponents = [TestBrowseComponent::class])
class TestSubComponentsModule