package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.ComparisonTable
import com.example.ui.components.ComparisonTableView
import com.example.ui.components.ExecutiveSummaryCard
import com.example.ui.theme.ResearchAssistantTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      ResearchAssistantTheme {
        Column(modifier = Modifier.padding(16.dp)) {
          ExecutiveSummaryCard(
            summary = "Quantum computing leverages superposition and entanglement to execute simulations exponentially faster."
          )
          ComparisonTableView(
            table = ComparisonTable(
              headers = listOf("Metric", "Quantum", "Classical"),
              rows = listOf(listOf("Unit", "Qubit", "Bit"))
            )
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
