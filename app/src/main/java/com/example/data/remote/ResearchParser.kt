package com.example.data.remote

import com.example.data.model.ComparisonTable
import com.example.data.model.ParsedResearch
import com.example.data.model.ResearchSection

object ResearchParser {

    fun parse(markdown: String): ParsedResearch {
        val lines = markdown.lines()
        var currentSection = ""
        val summaryLines = mutableListOf<String>()
        val currentSubsections = mutableListOf<ResearchSection>()
        var subTitle = ""
        var subBullets = mutableListOf<String>()
        var subContent = mutableListOf<String>()
        val tableLines = mutableListOf<String>()
        val takeawayLines = mutableListOf<String>()
        val uncertaintyLines = mutableListOf<String>()

        fun flushSubSection() {
            if (subTitle.isNotEmpty() || subBullets.isNotEmpty() || subContent.isNotEmpty()) {
                currentSubsections.add(
                    ResearchSection(
                        title = subTitle.ifEmpty { "Detailed Analysis" },
                        bullets = subBullets.toList(),
                        content = subContent.joinToString("\n").trim()
                    )
                )
                subTitle = ""
                subBullets = mutableListOf()
                subContent = mutableListOf()
            }
        }

        for (line in lines) {
            val trimmed = line.trim()
            val lower = trimmed.lowercase()

            if (trimmed.startsWith("# ") || trimmed.startsWith("## ")) {
                val headerText = trimmed.replace(Regex("^#+\\s*"), "").trim()
                val headerLower = headerText.lowercase()

                flushSubSection()

                currentSection = when {
                    headerLower.contains("executive summary") || headerLower.contains("core answer") -> "SUMMARY"
                    headerLower.contains("deep-dive") || headerLower.contains("key details") || headerLower.contains("analysis") -> "DETAILS"
                    headerLower.contains("comparison") || headerLower.contains("breakdown") || headerLower.contains("matrix") -> "TABLE"
                    headerLower.contains("takeaway") || headerLower.contains("key findings") -> "TAKEAWAYS"
                    headerLower.contains("uncertaint") || headerLower.contains("limitation") || headerLower.contains("fact check") -> "UNCERTAINTIES"
                    else -> currentSection.ifEmpty { "DETAILS" }
                }
                continue
            }

            if (trimmed.startsWith("### ") || trimmed.startsWith("#### ")) {
                if (currentSection == "DETAILS" || currentSection.isEmpty()) {
                    flushSubSection()
                    subTitle = trimmed.replace(Regex("^#+\\s*"), "").trim()
                    continue
                }
            }

            // Check for Markdown table lines
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                tableLines.add(trimmed)
                continue
            }

            when (currentSection) {
                "SUMMARY" -> {
                    if (trimmed.isNotEmpty()) {
                        summaryLines.add(trimmed)
                    }
                }
                "DETAILS" -> {
                    if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
                        subBullets.add(trimmed.substring(2).trim())
                    } else if (trimmed.isNotEmpty()) {
                        subContent.add(trimmed)
                    }
                }
                "TABLE" -> {
                    // Non-table lines in table section
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("|")) {
                        subContent.add(trimmed)
                    }
                }
                "TAKEAWAYS" -> {
                    if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
                        val bullet = trimmed.substring(2).trim()
                        if (bullet.lowercase().startsWith("uncertaint") || bullet.lowercase().startsWith("limitation")) {
                            uncertaintyLines.add(bullet)
                        } else {
                            takeawayLines.add(bullet)
                        }
                    } else if (trimmed.isNotEmpty()) {
                        takeawayLines.add(trimmed)
                    }
                }
                "UNCERTAINTIES" -> {
                    if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
                        uncertaintyLines.add(trimmed.substring(2).trim())
                    } else if (trimmed.isNotEmpty()) {
                        uncertaintyLines.add(trimmed)
                    }
                }
                else -> {
                    // Fallback to summary if beginning of response
                    if (summaryLines.isEmpty() && trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        summaryLines.add(trimmed)
                    } else if (trimmed.startsWith("* ") || trimmed.startsWith("- ")) {
                        subBullets.add(trimmed.substring(2).trim())
                    }
                }
            }
        }
        flushSubSection()

        val parsedTable = parseMarkdownTable(tableLines)
        val finalSummary = summaryLines.joinToString("\n\n").trim().ifEmpty {
            "Research query analyzed objectively. Key findings, technical trade-offs, and critical takeaways are detailed below."
        }

        return ParsedResearch(
            executiveSummary = finalSummary,
            keyDetails = currentSubsections,
            comparisonTable = parsedTable,
            keyTakeaways = takeawayLines,
            uncertainties = uncertaintyLines,
            rawMarkdown = markdown
        )
    }

    private fun parseMarkdownTable(lines: List<String>): ComparisonTable? {
        if (lines.size < 2) return null
        val cleanRows = lines.map { line ->
            line.trim()
                .removePrefix("|")
                .removeSuffix("|")
                .split("|")
                .map { it.trim() }
        }.filter { it.isNotEmpty() }

        if (cleanRows.size < 2) return null

        val headerRow = cleanRows[0]
        // Check if second row is separator like --- | ---
        val dataRows = if (cleanRows.size >= 2 && cleanRows[1].all { it.matches(Regex("[-:]+")) }) {
            cleanRows.drop(2)
        } else {
            cleanRows.drop(1)
        }

        if (headerRow.isEmpty() || dataRows.isEmpty()) return null

        return ComparisonTable(
            headers = headerRow,
            rows = dataRows
        )
    }
}
