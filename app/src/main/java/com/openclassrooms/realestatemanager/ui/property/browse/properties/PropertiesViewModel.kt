package com.openclassrooms.realestatemanager.ui.property.browse.properties

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PropertiesViewModel @Inject internal constructor(
        private val propertiesProcessor: PropertiesActionProcessor,
) : ViewModel(), BaseViewModel<PropertiesIntent, PropertiesUiModel> {

    private var intentsSubject: PublishSubject<PropertiesIntent> = PublishSubject.create()

    private val statesSubject: Observable<PropertiesUiModel> = Observable.empty()

    override fun processIntents(intents: Observable<PropertiesIntent>) {
        intents.subscribe(intentsSubject)
    }

    override fun states(): Observable<PropertiesUiModel> {
        return statesSubject
    }
}

