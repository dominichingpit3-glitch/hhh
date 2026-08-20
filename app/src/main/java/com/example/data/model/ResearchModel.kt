package com.example.data.model

enum class ResearchMode(
    val displayName: String,
    val description: String,
    val iconName: String
) {
    DEEP_ANALYSIS("Deep Analysis", "Comprehensive objective breakdown with key details", "Search"),
    COMPARATIVE("Comparative Benchmark", "Side-by-side breakdown with comparison table", "Compare"),
    EXECUTIVE_BRIEF("Executive Brief", "Fast 2-3 sentence core answer with high-level takeaways", "Bolt"),
    FACT_CHECK("Fact-Check & Verify", "Claim scrutiny, verifiable facts, and uncertainty check", "Verified")
}

data class ResearchSection(
    val title: String,
    val bullets: List<String> = emptyList(),
    val content: String = ""
)

data class ComparisonTable(
    val headers: List<String>,
    val rows: List<List<String>>
)

data class ParsedResearch(
    val executiveSummary: String,
    val keyDetails: List<ResearchSection> = emptyList(),
    val comparisonTable: ComparisonTable? = null,
    val keyTakeaways: List<String> = emptyList(),
    val uncertainties: List<String> = emptyList(),
    val rawMarkdown: String = ""
)

data class QuickSampleQuery(
    val title: String,
    val subtitle: String,
    val query: String,
    val context: String = "",
    val mode: ResearchMode = ResearchMode.DEEP_ANALYSIS
)
