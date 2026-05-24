package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey val id: String, // e.g., "Y1S1", "Y1S2"
    val year: Int,
    val semesterNumber: Int,
    val name: String,
    val isIncluded: Boolean = true,
    val manualGpa: Double? = null,
    val manualCredits: Int? = null
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val semesterId: String,
    val name: String,
    val creditHours: Int,
    val mark: Double? = null,
    val letterGrade: String? = null,
    val gradePoint: Double? = null,
    val isIncluded: Boolean = true,
    val isMajor: Boolean = false,
    val isRetaken: Boolean = false
)
