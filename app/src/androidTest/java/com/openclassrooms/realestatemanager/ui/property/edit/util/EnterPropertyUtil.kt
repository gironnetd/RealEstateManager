package com.openclassrooms.realestatemanager.ui.property.edit.util

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
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

object EnterPropertyUtil {

    fun update_property(testApplication: TestBaseApplication) {

        val mockNeat = MockNeat.threadLocal()

        onView(allOf(withId(R.id.description),
            isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(120).type(StringType.LETTERS).get()),
            )

        onView(allOf(withId(R.id.entry_date), withEffectiveVisibility(VISIBLE))).perform(scrollTo(), click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH] + 1,
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(click())

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status))
            .perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).perform(click())

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH] + 1,
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(click())

        InterestPoint.values().forEach { interestPoint ->
            if(interestPoint != InterestPoint.NONE) {
                onView(allOf(withText(testApplication.resources.getString(
                    interestPoint.place)),
                    isDisplayed())
                ).perform(click())
            }
        }

        onView(allOf(withId(R.id.price), withEffectiveVisibility(VISIBLE))).perform(scrollTo())
        onView(allOf(withId(R.id.price), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(6).type(StringType.NUMBERS).get()),
            )

        onView(allOf(withId(R.id.type), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyType.FLAT.type))).perform(click())

        onView(withText(R.string.change_property_type)).perform(click())

        onView(allOf(withId(R.id.surface), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.surface), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(3).type(StringType.NUMBERS).get()),
            )
        onView(allOf(withId(R.id.rooms), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()),
            )

        onView(allOf(withId(R.id.bathrooms), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.bathrooms), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()),
            )

        onView(allOf(withId(R.id.bedrooms), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()),
            )

        onView(allOf(withId(R.id.location_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.street), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(12).type(StringType.LETTERS).get()),
            )

        onView(allOf(withId(R.id.city), isDisplayed()))
            .perform(
                replaceText(mockNeat.cities().capitalsEurope().get()),
            )
        onView(allOf(withId(R.id.postal_code), isDisplayed()))
            .perform(
                replaceText(mockNeat.strings().size(5).type(StringType.NUMBERS).get()),
            )
        onView(allOf(withId(R.id.country), isDisplayed()))
            .perform(
                replaceText(mockNeat.countries().names().get()),
            )
        onView(allOf(withId(R.id.state), isDisplayed()))
            .perform(
                replaceText(mockNeat.usStates().get()),
            )
    }
}