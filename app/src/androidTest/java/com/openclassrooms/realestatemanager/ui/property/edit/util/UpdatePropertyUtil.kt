package com.openclassrooms.realestatemanager.ui.property.edit.util

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.models.InterestPoint
import com.openclassrooms.realestatemanager.models.PropertyStatus
import com.openclassrooms.realestatemanager.models.PropertyType
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.StringType
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import java.util.*

object UpdatePropertyUtil {

    fun update_property(testApplication: TestBaseApplication) {

        val mockNeat = MockNeat.threadLocal()

        onView(allOf(withId(R.id.description),
            isDisplayed()))
            .perform(replaceText(mockNeat.strings().size(40).type(StringType.LETTERS)
                .get()))

        onView(allOf(withId(R.id.entry_date),
            isDisplayed())).perform(ViewActions.click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        onView(ViewMatchers.withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(ViewActions.click())

        onView(allOf(withId(R.id.status),
            isDisplayed())).perform(ViewActions.click())

        onView(ViewMatchers.withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(ViewActions.click())

        onView(ViewMatchers.withText(R.string.change_property_status))
            .perform(ViewActions.click())

        onView(allOf(withId(R.id.sold_date),
            isDisplayed())).perform(ViewActions.click())

        onView(ViewMatchers.withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(ViewActions.click())

        InterestPoint.values().forEach { interestPoint ->
            onView(allOf(ViewMatchers.withText(testApplication.resources.getString(
                interestPoint.place)),
                isDisplayed())
            ).perform(ViewActions.click())
        }

        onView(withId(R.id.linearLayout_price_and_type))
            .perform(ViewActions.scrollTo(), ViewActions.click())
        onView(allOf(withId(R.id.price), isDisplayed()))
            .perform(replaceText(mockNeat.strings().size(6).type(StringType.NUMBERS)
                .get()))

        onView(allOf(withId(R.id.type), isDisplayed()))
            .perform(ViewActions.click())

        onView(ViewMatchers.withText(testApplication.resources.getString(PropertyType.FLAT.type)))
            .perform(ViewActions.click())

        onView(ViewMatchers.withText(R.string.change_property_type))
            .perform(ViewActions.click())

        onView(withId(R.id.linearLayout_surface_and_rooms))
            .perform(ViewActions.scrollTo(), ViewActions.click())

        onView(allOf(withId(R.id.surface),
            isDisplayed())).perform(
            replaceText(mockNeat.strings().size(3).type(StringType.NUMBERS).get()))
        onView(allOf(withId(R.id.rooms), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()))

        onView(withId(R.id.linearLayout_bathrooms_and_bedrooms))
            .perform(ViewActions.scrollTo(), ViewActions.click())

        onView(allOf(withId(R.id.bathrooms),
            isDisplayed())).perform(
            replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()))
        onView(allOf(withId(R.id.bedrooms),
            isDisplayed())).perform(
            replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()))

        onView(withId(R.id.linearLayout_location))
            .perform(ViewActions.scrollTo())

        onView(allOf(withId(R.id.street),
            isDisplayed()))
            .perform(replaceText(mockNeat.strings().size(12).type(StringType.LETTERS)
                .get()))
        onView(allOf(withId(R.id.city), isDisplayed()))
            .perform(replaceText(mockNeat.cities().capitalsEurope().get()))
        onView(allOf(withId(R.id.postal_code),
            isDisplayed()))
            .perform(replaceText(mockNeat.strings().size(5).type(StringType.NUMBERS)
                .get()))
        onView(allOf(withId(R.id.country),
            isDisplayed()))
            .perform(replaceText(mockNeat.countries().names().get()))
        onView(allOf(withId(R.id.state), isDisplayed()))
            .perform(replaceText(mockNeat.usStates().get()))
    }
}