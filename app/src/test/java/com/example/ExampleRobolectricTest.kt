package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.ResearchParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Research Assistant", appName)
    }

    @Test
    fun testResearchParserMarkdownDecomposition() {
        val testMarkdown = """
# Core Answer / Executive Summary
Solid-state batteries replace liquid electrolytes with solid ceramics, increasing energy density up to 500 Wh/kg.

# Key Details & Deep-Dive
### Chemical Mechanism
* Pure lithium metal anodes provide double the theoretical capacity.
* Eliminates flammable carbonate solvents.

# Comparison & Breakdown
| Feature | Solid-State | Li-Ion |
| --- | --- | --- |
| Density | 500 Wh/kg | 260 Wh/kg |

# Key Takeaways & Fact Checks
* Step-function improvement in EV safety.
* Uncertainty: High-rate fast charging under freezing temps is unverified.
        """.trimIndent()

        val parsed = ResearchParser.parse(testMarkdown)
        assertNotNull(parsed)
        assertEquals(true, parsed.executiveSummary.contains("Solid-state batteries"))
        assertEquals(1, parsed.keyDetails.size)
        assertEquals("Chemical Mechanism", parsed.keyDetails[0].title)
        assertNotNull(parsed.comparisonTable)
        assertEquals(2, parsed.comparisonTable?.headers?.size)
        assertEquals(1, parsed.keyTakeaways.size)
        assertEquals(1, parsed.uncertainties.size)
    }
}
