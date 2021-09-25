package com.openclassrooms.realestatemanager.util

import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.util.Constants.CONVERSION_RATE_EUROS_DOLLARS
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.math.roundToInt

@RunWith(JUnit4::class)
class UtilsTest {

    @Test
    fun test_convert_dollar_to_euro() {
        val dollars = 50
        assertThat(Utils.convertDollarToEuro(dollars)).isEqualTo((dollars / CONVERSION_RATE_EUROS_DOLLARS).roundToInt())
    }

    @Test
    fun test_convert_euro_to_dollar() {
        val euros = 50
        assertThat(Utils.convertEuroToDollar(euros)).isEqualTo((euros * CONVERSION_RATE_EUROS_DOLLARS).roundToInt())
    }
}