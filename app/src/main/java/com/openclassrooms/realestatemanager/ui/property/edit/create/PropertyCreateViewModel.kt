package com.openclassrooms.realestatemanager.ui.property.edit.create

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewModel
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import io.reactivex.Observable
import javax.inject.Inject

class PropertyCreateViewModel
@Inject internal constructor(private val propertyCreateActionProcessor: PropertyCreateActionProcessor)
    : ViewModel(), MviViewModel<PropertyEditIntent.PropertyCreateIntent, PropertyEditViewState> {

    override fun processIntents(intents: Observable<PropertyEditIntent.PropertyCreateIntent>) {
        TODO("Not yet implemented")
    }

    override fun states(): Observable<PropertyEditViewState> {
        TODO("Not yet implemented")
    }


}