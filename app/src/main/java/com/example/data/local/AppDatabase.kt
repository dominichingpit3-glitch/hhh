package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ComparisonTable
import com.example.data.model.ResearchMode
import com.example.data.model.ResearchSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ResearchEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun researchDao(): ResearchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "research_assistant_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialSeed(database.researchDao())
                    }
                }
            }
        }

        private suspend fun populateInitialSeed(dao: ResearchDao) {
            val sampleTable = ComparisonTable(
                headers = listOf("Metric / Feature", "Quantum Computing", "Classical Computing"),
                rows = listOf(
                    listOf("Basic Unit", "Qubit (superposition & entanglement)", "Bit (binary 0 or 1)"),
                    listOf("Processing Paradigm", "Simultaneous state space evaluation", "Deterministic sequential / multi-threaded execution"),
                    listOf("Optimal Use-Cases", "Molecular simulation, cryptography, combinatorial optimization", "General computing, database indexing, daily web applications"),
                    listOf("Error Rates & Decoherence", "High sensitivity to thermal noise; requires active error correction", "Negligible hardware bit-flip rate under standard conditions"),
                    listOf("Physical Operating Temp", "Near absolute zero (~15 mK) for superconducting systems", "Standard room temperature / active fan cooling (~300 K)")
                )
            )

            val sampleSections = listOf(
                ResearchSection(
                    title = "Architectural Foundations & Physics",
                    bullets = listOf(
                        "Quantum systems exploit superposition (alpha|0> + beta|1>) and quantum entanglement to maintain exponential state spaces (2^n).",
                        "Classical computing relies on silicon semiconductor CMOS transistors switching between high and low voltage states deterministically.",
                        "Quantum decoherence remains the primary engineering hurdle, requiring topological protection or surface code error mitigation."
                    ),
                    content = "Quantum computing does not simply represent a faster classical processor; it executes fundamentally distinct complexity classes, specifically BQP (Bounded-error Quantum Polynomial time)."
                ),
                ResearchSection(
                    title = "Practical Application Frontiers",
                    bullets = listOf(
                        "Materials Science: Simulating FeMoco nitrogenase enzymes for low-energy fertilizer synthesis.",
                        "Cybersecurity: Shor's algorithm poses a theoretical break to RSA-2048, driving NIST post-quantum cryptography (PQC) standards.",
                        "Financial Portfolio Optimization: Quadratic unconstrained binary optimization (QUBO) algorithms."
                    ),
                    content = "For linear data manipulation, word processing, and typical web latency pipelines, classical architectures remain fundamentally superior in throughput and cost efficiency."
                )
            )

            val sampleTakeaways = listOf(
                "Quantum computing provides polynomial to exponential speedups for niche problem spaces (BQP complexity class), not general computing tasks.",
                "Hybrid architectures (QPU + GPU/CPU co-processing) will define near-term commercial viability rather than standalone quantum mainframes.",
                "NIST Post-Quantum Cryptography migration is mandatory before cryptanalytically relevant quantum computers (CRQCs) mature."
            )

            val sampleUncertainties = listOf(
                "Exact timeline for fault-tolerant logical qubits (>10,000 physical qubits per logical qubit) is debated (projected between 2029–2036).",
                "Superconducting vs Trapped-Ion vs Photonic modalities each present unresolved trade-offs in gate fidelity vs scaling interconnects."
            )

            val seedEntity = ResearchEntity(
                query = "Quantum Computing vs Classical Computing: Architectural Differences, Complexity Classes, and Real-World Viability",
                backgroundContext = "Analyze the fundamental physical limits, practical use cases, and near-term deployment horizons.",
                mode = ResearchMode.COMPARATIVE.name,
                executiveSummary = "Quantum computing harnesses quantum mechanical superposition and entanglement to execute specific mathematical and physical simulations exponentially faster than classical computers, but does not replace general-purpose classical computing. Commercial viability currently hinges on achieving fault-tolerant quantum error correction within hybrid HPC-quantum infrastructure.",
                keyDetailsJson = JsonHelper.sectionListAdapter.toJson(sampleSections),
                comparisonTableJson = JsonHelper.comparisonTableAdapter.toJson(sampleTable),
                keyTakeawaysJson = JsonHelper.stringListAdapter.toJson(sampleTakeaways),
                uncertaintiesJson = JsonHelper.stringListAdapter.toJson(sampleUncertainties),
                rawMarkdown = """
# Core Answer / Executive Summary
Quantum computing harnesses quantum mechanical superposition and entanglement to execute specific mathematical and physical simulations exponentially faster than classical computers, but does not replace general-purpose classical computing. Commercial viability currently hinges on achieving fault-tolerant quantum error correction within hybrid HPC-quantum infrastructure.

# Key Details & Deep-Dive
### Architectural Foundations & Physics
* Quantum systems exploit superposition and quantum entanglement to maintain exponential state spaces (2^n).
* Classical computing relies on silicon semiconductor CMOS transistors switching deterministically.
* Quantum decoherence remains the primary engineering hurdle.

### Practical Application Frontiers
* Materials Science: Simulating complex molecular catalysts.
* Cybersecurity: Driving NIST post-quantum cryptography (PQC) standards.
* Optimization: Quadratic unconstrained binary optimization algorithms.

# Comparison & Breakdown
| Metric / Feature | Quantum Computing | Classical Computing |
| --- | --- | --- |
| Basic Unit | Qubit (superposition & entanglement) | Bit (binary 0 or 1) |
| Processing Paradigm | Simultaneous state space evaluation | Deterministic sequential execution |
| Optimal Use-Cases | Molecular simulation, cryptography, optimization | General computing, daily web applications |
| Error Rates | High sensitivity to thermal noise | Negligible hardware bit-flip rate |
| Operating Temp | Near absolute zero (~15 mK) | Standard room temperature (~300 K) |

# Key Takeaways & Fact Checks
* Quantum computing provides polynomial to exponential speedups for niche problem spaces (BQP complexity class), not general computing tasks.
* Hybrid architectures (QPU + GPU/CPU co-processing) will define near-term commercial viability.
* Uncertainties: Timeline for fault-tolerant logical qubits is estimated between 2029–2036 across superconducting and trapped-ion roadmaps.
                """.trimIndent(),
                timestamp = System.currentTimeMillis(),
                isBookmarked = true,
                tags = "Physics, Computing, Benchmark"
            )

            dao.insertReport(seedEntity)
        }
    }
}
