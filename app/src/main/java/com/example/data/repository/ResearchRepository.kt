package com.example.data.repository

import com.example.data.local.JsonHelper
import com.example.data.local.ResearchDao
import com.example.data.local.ResearchEntity
import com.example.data.model.ComparisonTable
import com.example.data.model.ParsedResearch
import com.example.data.model.ResearchMode
import com.example.data.model.ResearchSection
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ResearchRepository(
    private val researchDao: ResearchDao,
    private val geminiService: GeminiService = GeminiService()
) {
    val allReports: Flow<List<ResearchEntity>> = researchDao.getAllReports()
    val bookmarkedReports: Flow<List<ResearchEntity>> = researchDao.getBookmarkedReports()

    fun searchReports(query: String): Flow<List<ResearchEntity>> =
        researchDao.searchReports(query)

    suspend fun getReportById(id: Long): ResearchEntity? =
        researchDao.getReportById(id)

    suspend fun executeResearch(
        query: String,
        backgroundContext: String = "",
        mode: ResearchMode = ResearchMode.DEEP_ANALYSIS,
        saveToDatabase: Boolean = true
    ): Pair<Long, ParsedResearch> {
        val parsed = geminiService.conductResearch(
            query = query,
            backgroundContext = backgroundContext,
            mode = mode
        )

        val entity = ResearchEntity(
            query = query,
            backgroundContext = backgroundContext,
            mode = mode.name,
            executiveSummary = parsed.executiveSummary,
            keyDetailsJson = JsonHelper.sectionListAdapter.toJson(parsed.keyDetails),
            comparisonTableJson = parsed.comparisonTable?.let { JsonHelper.comparisonTableAdapter.toJson(it) } ?: "",
            keyTakeawaysJson = JsonHelper.stringListAdapter.toJson(parsed.keyTakeaways),
            uncertaintiesJson = JsonHelper.stringListAdapter.toJson(parsed.uncertainties),
            rawMarkdown = parsed.rawMarkdown,
            timestamp = System.currentTimeMillis(),
            isBookmarked = false,
            tags = mode.displayName
        )

        val id = if (saveToDatabase) {
            researchDao.insertReport(entity)
        } else {
            0L
        }

        return Pair(id, parsed)
    }

    suspend fun toggleBookmark(id: Long, currentStatus: Boolean) {
        researchDao.updateBookmarkStatus(id, !currentStatus)
    }

    suspend fun deleteReport(id: Long) {
        researchDao.deleteReportById(id)
    }

    suspend fun clearHistory() {
        researchDao.clearAll()
    }

    fun parseEntityToResearch(entity: ResearchEntity): ParsedResearch {
        val details: List<ResearchSection> = try {
            if (entity.keyDetailsJson.isNotEmpty()) {
                JsonHelper.sectionListAdapter.fromJson(entity.keyDetailsJson) ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val table: ComparisonTable? = try {
            if (entity.comparisonTableJson.isNotEmpty()) {
                JsonHelper.comparisonTableAdapter.fromJson(entity.comparisonTableJson)
            } else null
        } catch (e: Exception) {
            null
        }

        val takeaways: List<String> = try {
            if (entity.keyTakeawaysJson.isNotEmpty()) {
                JsonHelper.stringListAdapter.fromJson(entity.keyTakeawaysJson) ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val uncertainties: List<String> = try {
            if (entity.uncertaintiesJson.isNotEmpty()) {
                JsonHelper.stringListAdapter.fromJson(entity.uncertaintiesJson) ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return ParsedResearch(
            executiveSummary = entity.executiveSummary,
            keyDetails = details,
            comparisonTable = table,
            keyTakeaways = takeaways,
            uncertainties = uncertainties,
            rawMarkdown = entity.rawMarkdown
        )
    }
}
