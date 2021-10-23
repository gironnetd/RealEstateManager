package com.openclassrooms.realestatemanager.util

import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.util.Constants.CONVERSION_RATE_EUROS_DOLLARS
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
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

    @Test
    fun test_convert_string_to_date() {
        val stringDate = "21/07/2021"
        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        assertThat(Utils.fromStringToDate(stringDate)).isEqualTo(dateFormat.parse(stringDate))
    }

    @Test
    fun test_convert_date_to_string() {
        val date = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        assertThat(Utils.formatDate(date)).isEqualTo(dateFormat.format(date))
    }
}