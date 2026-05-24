package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY year ASC, semesterNumber ASC")
    fun getAllSemestersFlow(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM semesters ORDER BY year ASC, semesterNumber ASC")
    suspend fun getAllSemesters(): List<SemesterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemesters(semesters: List<SemesterEntity>)

    @Update
    suspend fun updateSemester(semester: SemesterEntity)

    @Query("DELETE FROM semesters")
    suspend fun deleteAllSemesters()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE semesterId = :semesterId")
    fun getCoursesForSemesterFlow(semesterId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE semesterId = :semesterId")
    suspend fun getCoursesForSemester(semesterId: String): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: String)

    @Query("DELETE FROM courses WHERE semesterId = :semesterId")
    suspend fun deleteCoursesBySemester(semesterId: String)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()
}

@Database(entities = [SemesterEntity::class, CourseEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao
    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "act_gpa_calculator_db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
