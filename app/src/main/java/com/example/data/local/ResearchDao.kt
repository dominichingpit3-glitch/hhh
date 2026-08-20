package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResearchDao {
    @Query("SELECT * FROM research_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ResearchEntity>>

    @Query("SELECT * FROM research_reports WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedReports(): Flow<List<ResearchEntity>>

    @Query("SELECT * FROM research_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): ResearchEntity?

    @Query("SELECT * FROM research_reports WHERE query LIKE '%' || :search || '%' OR executiveSummary LIKE '%' || :search || '%' ORDER BY timestamp DESC")
    fun searchReports(search: String): Flow<List<ResearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ResearchEntity): Long

    @Update
    suspend fun updateReport(report: ResearchEntity)

    @Query("UPDATE research_reports SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Long, isBookmarked: Boolean)

    @Delete
    suspend fun deleteReport(report: ResearchEntity)

    @Query("DELETE FROM research_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM research_reports")
    suspend fun clearAll()
}
