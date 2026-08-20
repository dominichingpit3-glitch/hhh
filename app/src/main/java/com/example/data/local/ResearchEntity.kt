package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ResearchMode

@Entity(tableName = "research_reports")
data class ResearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val backgroundContext: String = "",
    val mode: String = ResearchMode.DEEP_ANALYSIS.name,
    val executiveSummary: String,
    val keyDetailsJson: String = "",
    val comparisonTableJson: String = "",
    val keyTakeawaysJson: String = "",
    val uncertaintiesJson: String = "",
    val rawMarkdown: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val tags: String = ""
)
