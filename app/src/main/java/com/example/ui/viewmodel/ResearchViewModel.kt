package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ResearchEntity
import com.example.data.model.ParsedResearch
import com.example.data.model.QuickSampleQuery
import com.example.data.model.ResearchMode
import com.example.data.repository.ResearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ResearchViewModel(
    application: Application,
    private val repository: ResearchRepository = ResearchRepository(
        AppDatabase.getDatabase(application).researchDao()
    )
) : AndroidViewModel(application) {

    private val _queryText = MutableStateFlow("")
    val queryText: StateFlow<String> = _queryText.asStateFlow()

    private val _contextText = MutableStateFlow("")
    val contextText: StateFlow<String> = _contextText.asStateFlow()

    private val _selectedMode = MutableStateFlow(ResearchMode.DEEP_ANALYSIS)
    val selectedMode: StateFlow<ResearchMode> = _selectedMode.asStateFlow()

    private val _activeReport = MutableStateFlow<ParsedResearch?>(null)
    val activeReport: StateFlow<ParsedResearch?> = _activeReport.asStateFlow()

    private val _activeReportId = MutableStateFlow(0L)
    val activeReportId: StateFlow<Long> = _activeReportId.asStateFlow()

    private val _activeQuery = MutableStateFlow("")
    val activeQuery: StateFlow<String> = _activeQuery.asStateFlow()

    private val _activeContext = MutableStateFlow("")
    val activeContext: StateFlow<String> = _activeContext.asStateFlow()

    private val _activeIsBookmarked = MutableStateFlow(false)
    val activeIsBookmarked: StateFlow<Boolean> = _activeIsBookmarked.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _followUpText = MutableStateFlow("")
    val followUpText: StateFlow<String> = _followUpText.asStateFlow()

    val allReports: StateFlow<List<ResearchEntity>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Auto-load most recent research if available
        viewModelScope.launch {
            allReports.collect { list ->
                if (_activeReport.value == null && list.isNotEmpty()) {
                    val first = list.first()
                    loadEntity(first)
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _queryText.value = newQuery
    }

    fun onContextChange(newContext: String) {
        _contextText.value = newContext
    }

    fun onModeSelect(mode: ResearchMode) {
        _selectedMode.value = mode
    }

    fun onHistorySearchChange(query: String) {
        _historySearchQuery.value = query
    }

    fun onFollowUpChange(text: String) {
        _followUpText.value = text
    }

    fun selectSampleQuery(sample: QuickSampleQuery) {
        _queryText.value = sample.query
        _contextText.value = sample.context
        _selectedMode.value = sample.mode
        conductResearch(sample.query, sample.context, sample.mode)
    }

    fun conductResearch(
        overrideQuery: String? = null,
        overrideContext: String? = null,
        overrideMode: ResearchMode? = null
    ) {
        val query = (overrideQuery ?: _queryText.value).trim()
        val context = (overrideContext ?: _contextText.value).trim()
        val mode = overrideMode ?: _selectedMode.value

        if (query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _activeQuery.value = query
            _activeContext.value = context

            try {
                val (id, parsed) = repository.executeResearch(
                    query = query,
                    backgroundContext = context,
                    mode = mode
                )
                _activeReport.value = parsed
                _activeReportId.value = id
                _activeIsBookmarked.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to complete research synthesis"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitFollowUp() {
        val followUp = _followUpText.value.trim()
        if (followUp.isBlank()) return

        val originalQuery = _activeQuery.value
        val originalSummary = _activeReport.value?.executiveSummary ?: ""
        val combinedContext = "Original Research Topic: $originalQuery\nOriginal Executive Summary: $originalSummary\nPrior Context: ${_activeContext.value}"

        _followUpText.value = ""
        _queryText.value = "$originalQuery [Follow-up: $followUp]"
        _contextText.value = combinedContext

        conductResearch(
            overrideQuery = "$originalQuery - Refinement: $followUp",
            overrideContext = combinedContext,
            overrideMode = _selectedMode.value
        )
    }

    fun loadEntity(entity: ResearchEntity) {
        _activeReportId.value = entity.id
        _activeQuery.value = entity.query
        _activeContext.value = entity.backgroundContext
        _activeIsBookmarked.value = entity.isBookmarked
        _activeReport.value = repository.parseEntityToResearch(entity)
        _selectedMode.value = try {
            ResearchMode.valueOf(entity.mode)
        } catch (e: Exception) {
            ResearchMode.DEEP_ANALYSIS
        }
    }

    fun toggleActiveBookmark() {
        val id = _activeReportId.value
        val current = _activeIsBookmarked.value
        _activeIsBookmarked.value = !current
        if (id > 0) {
            viewModelScope.launch {
                repository.toggleBookmark(id, current)
            }
        }
    }

    fun toggleHistoryBookmark(entity: ResearchEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(entity.id, entity.isBookmarked)
            if (_activeReportId.value == entity.id) {
                _activeIsBookmarked.value = !entity.isBookmarked
            }
        }
    }

    fun deleteHistoryReport(entity: ResearchEntity) {
        viewModelScope.launch {
            repository.deleteReport(entity.id)
            if (_activeReportId.value == entity.id) {
                _activeReport.value = null
                _activeReportId.value = 0L
            }
        }
    }

    fun resetToNewQuery() {
        _queryText.value = ""
        _contextText.value = ""
        _activeReport.value = null
        _activeReportId.value = 0L
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResearchViewModel::class.java)) {
                return ResearchViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
