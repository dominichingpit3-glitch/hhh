package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ComparisonTable
import com.example.data.model.ResearchSection
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonHelper {
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val sectionListType = Types.newParameterizedType(List::class.java, ResearchSection::class.java)
    val sectionListAdapter = moshi.adapter<List<ResearchSection>>(sectionListType)

    val comparisonTableAdapter = moshi.adapter(ComparisonTable::class.java)
}

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.let { JsonHelper.stringListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            JsonHelper.stringListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
