package com.openclassrooms.realestatemanager.ui.property.edit.util

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
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

object EnterPropertyUtil {

    fun update_property(testApplication: TestBaseApplication) {

        val mockNeat = MockNeat.threadLocal()

        onView(allOf(withId(R.id.description),
            isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(120).type(StringType.LETTERS).get()),
                //pressBack()
            )

        onView(allOf(withId(R.id.entry_date),
            isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        onView(ViewMatchers.withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(click())

        onView(allOf(withId(R.id.status),
            isDisplayed())).perform(click())

        onView(ViewMatchers.withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(ViewMatchers.withText(R.string.change_property_status))
            .perform(click())

        onView(allOf(withId(R.id.sold_date),
            isDisplayed())).perform(click())

        onView(ViewMatchers.withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(click())

        InterestPoint.values().forEach { interestPoint ->
            if(interestPoint != InterestPoint.NONE) {
                onView(allOf(ViewMatchers.withText(testApplication.resources.getString(
                    interestPoint.place)),
                    isDisplayed())
                ).perform(click())
            }
        }

        onView(withId(R.id.linearLayout_price_and_type))
            .perform(scrollTo(), click())
        onView(allOf(withId(R.id.price), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(6).type(StringType.NUMBERS).get()),
                //pressImeActionButton()
            )

        onView(allOf(withId(R.id.type), isDisplayed()))
            .perform(click())

        onView(ViewMatchers.withText(testApplication.resources.getString(PropertyType.FLAT.type)))
            .perform(click())

        onView(ViewMatchers.withText(R.string.change_property_type))
            .perform(click())

        onView(withId(R.id.linearLayout_surface_and_rooms))
            .perform(scrollTo(), click())

        onView(allOf(withId(R.id.surface),
            isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(3).type(StringType.NUMBERS).get()),
                //pressImeActionButton()
            )
        onView(allOf(withId(R.id.rooms), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()),
                //pressImeActionButton()
            )

        onView(withId(R.id.linearLayout_bathrooms_and_bedrooms))
            .perform(scrollTo(), click())

        onView(allOf(withId(R.id.bathrooms), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()),
                //pressImeActionButton()
            )

        onView(allOf(withId(R.id.bedrooms), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()),
                //pressImeActionButton()
            )

        onView(withId(R.id.linearLayout_location))
            .perform(scrollTo())

        onView(allOf(withId(R.id.street), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(12).type(StringType.LETTERS).get()),
                //pressBack()
            )

        onView(allOf(withId(R.id.city), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.cities().capitalsEurope().get()),
                //pressBack()
            )
        onView(allOf(withId(R.id.postal_code), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.strings().size(5).type(StringType.NUMBERS).get()),
                //pressBack()
            )
        onView(allOf(withId(R.id.country), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.countries().names().get()),
                //pressBack()
            )
        onView(allOf(withId(R.id.state), isDisplayed()))
            .perform(
                //click(),
                replaceText(mockNeat.usStates().get()),
                //pressBack()
            )
    }
}