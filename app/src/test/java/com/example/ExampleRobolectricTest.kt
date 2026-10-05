package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AgeRubricHelper
import com.example.data.model.StandardCommittees
import com.example.data.model.StandardCountries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DiplomatAI", appName)
  }

  @Test
  fun `verify age bracket calibration`() {
    val highSchoolBracket = AgeRubricHelper.getBracket(16)
    assertTrue(highSchoolBracket.contains("High School"))

    val universityBracket = AgeRubricHelper.getBracket(20)
    assertTrue(universityBracket.contains("University"))

    val juniorBracket = AgeRubricHelper.getBracket(13)
    assertTrue(juniorBracket.contains("Junior"))
  }

  @Test
  fun `verify standard committees and countries available`() {
    assertTrue(StandardCommittees.ALL.isNotEmpty())
    val unsc = StandardCommittees.ALL.firstOrNull { it.acronym == "UNSC" }
    assertNotNull(unsc)

    val usa = StandardCountries.find("USA")
    assertEquals("United States of America", usa.name)
    assertTrue(usa.isP5)
  }
}
