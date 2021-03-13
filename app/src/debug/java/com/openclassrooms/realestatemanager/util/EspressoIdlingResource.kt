package com.openclassrooms.realestatemanager.util

import androidx.test.espresso.idling.CountingIdlingResource
import timber.log.Timber

object EspressoIdlingResource {

    private val CLASS_NAME = "EspressoIdlingResource"

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        Timber.d("INCREMENTING.")
        countingIdlingResource.increment()
    }

    private fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            Timber.d("DECREMENTING.")
            countingIdlingResource.decrement()
        }
    }

    fun clear(): Boolean {
        return if (!countingIdlingResource.isIdleNow) {
            decrement()
            false
        } else {
            true
        }
    }
}
