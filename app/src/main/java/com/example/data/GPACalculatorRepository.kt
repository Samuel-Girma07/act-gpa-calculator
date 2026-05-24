package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GPACalculatorRepository(private val db: AppDatabase) {
    val semestersFlow: Flow<List<SemesterEntity>> = db.semesterDao().getAllSemestersFlow()
    val coursesFlow: Flow<List<CourseEntity>> = db.courseDao().getAllCoursesFlow()

    suspend fun checkAndPrepopulate() {
        val existingSemesters = db.semesterDao().getAllSemesters()
        if (existingSemesters.isEmpty()) {
            db.semesterDao().insertSemesters(ACTCurriculum.semesters)
            for (semester in ACTCurriculum.semesters) {
                val courses = ACTCurriculum.getDefaultCourses(semester.id)
                db.courseDao().insertCourses(courses)
            }
        }
    }

    suspend fun insertCourse(course: CourseEntity) {
        db.courseDao().insertCourse(course)
    }

    suspend fun updateCourse(course: CourseEntity) {
        db.courseDao().updateCourse(course)
    }

    suspend fun deleteCourse(courseId: String) {
        db.courseDao().deleteCourseById(courseId)
    }

    suspend fun updateSemester(semester: SemesterEntity) {
        db.semesterDao().updateSemester(semester)
    }

    suspend fun resetSemester(semesterId: String) {
        db.courseDao().deleteCoursesBySemester(semesterId)
        val defaultCourses = ACTCurriculum.getDefaultCourses(semesterId)
        db.courseDao().insertCourses(defaultCourses)
        
        val originalSemester = ACTCurriculum.semesters.firstOrNull { it.id == semesterId }
        if (originalSemester != null) {
            db.semesterDao().updateSemester(originalSemester)
        }
    }

    suspend fun resetAll() {
        db.courseDao().deleteAllCourses()
        db.semesterDao().deleteAllSemesters()
        db.semesterDao().insertSemesters(ACTCurriculum.semesters)
        for (semester in ACTCurriculum.semesters) {
            val courses = ACTCurriculum.getDefaultCourses(semester.id)
            db.courseDao().insertCourses(courses)
        }
    }
}
