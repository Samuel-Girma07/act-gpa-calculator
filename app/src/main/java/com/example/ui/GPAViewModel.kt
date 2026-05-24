package com.example.ui

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID
import kotlin.math.round

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppTab {
    GPA, CGPA, TARGET
}

class GPAViewModel(private val repository: GPACalculatorRepository) : ViewModel() {
    
    val themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    
    // Initialize database populate
    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }
    }

    // Navigation and Selections
    val activeTab = MutableStateFlow(AppTab.GPA)
    val selectedSemesterId = MutableStateFlow("Y2S1") // Default to Y2S1 or Y1S1

    // Flows from Repository
    val semesters = repository.semestersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val courses = repository.coursesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // User Feedback / Validation Errors
    val uiErrorEvent = MutableStateFlow<String?>(null)

    // Target Prediction Form Input States
    val targetCurrentCgpa = MutableStateFlow("3.25")
    val targetDoneCredits = MutableStateFlow("78")
    val targetCgpaInput = MutableStateFlow("3.75")
    val targetRemainingCredits = MutableStateFlow("42")

    // Flag to see if the user has manually changed fields or if it's auto-syncing
    private var hasManuallyEditedTarget = false

    // Real-time calculated properties for Selected Semester
    val selectedSemesterDetails = combine(
        selectedSemesterId,
        courses
    ) { semId, allCourses ->
        val semCourses = allCourses.filter { it.semesterId == semId }
        val validCourses = semCourses.filter { it.isIncluded && it.mark != null && !it.isRetaken }
        val majorCourses = semCourses.filter { it.isIncluded && it.mark != null && !it.isRetaken && it.isMajor }

        var totalGradePoints = 0.0
        var totalCreditHours = 0
        var totalMajorGP = 0.0
        var totalMajorCredits = 0

        val defaultTotalCredits = semCourses.filter { !it.isRetaken }.sumOf { it.creditHours }

        for (course in validCourses) {
            val mark = course.mark ?: continue
            val (_, gradePoint) = convertMarkToGrade(mark)
            totalGradePoints += course.creditHours * gradePoint
            totalCreditHours += course.creditHours
            
            if (course.isMajor) {
                totalMajorGP += course.creditHours * gradePoint
                totalMajorCredits += course.creditHours
            }
        }

        val gpa = if (totalCreditHours > 0) {
            round((totalGradePoints / totalCreditHours) * 100.0) / 100.0
        } else {
            0.0
        }
        
        val majorGpa = if (totalMajorCredits > 0) {
            round((totalMajorGP / totalMajorCredits) * 100.0) / 100.0
        } else {
            0.0
        }

        SemesterCalculationDetail(
            gpa = gpa,
            majorGpa = majorGpa,
            earnedCredits = totalCreditHours,
            totalCredits = defaultTotalCredits,
            courses = semCourses,
            validCourses = validCourses
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SemesterCalculationDetail(0.0, 0.0, 0, 0, emptyList(), emptyList())
    )

    // Real-time overall CGPA calculation
    val overallCgpaDetails = combine(
        semesters,
        courses
    ) { allSemesters, allCourses ->
        // Major CGPA Calculation across ALL valid courses
        val allValidCourses = allCourses.filter { it.isIncluded && it.mark != null && !it.isRetaken }
        var totalMajorGP = 0.0
        var totalMajorCredits = 0
        allValidCourses.filter { it.isMajor }.forEach { course ->
            val (_, gp) = convertMarkToGrade(course.mark ?: 0.0)
            totalMajorGP += course.creditHours * gp
            totalMajorCredits += course.creditHours
        }
        val majorCgpa = if (totalMajorCredits > 0) round((totalMajorGP / totalMajorCredits) * 100.0) / 100.0 else 0.0

        // Calculate GPA dynamically for each semester to show or sync
        val semesterDataList = allSemesters.map { semester ->
            val semCourses = allCourses.filter { it.semesterId == semester.id }
            val validCourses = semCourses.filter { it.isIncluded && it.mark != null && !it.isRetaken }

            var totalGradePoints = 0.0
            var totalCreditHours = 0

            for (course in validCourses) {
                val mark = course.mark ?: continue
                val (_, gp) = convertMarkToGrade(mark)
                totalGradePoints += course.creditHours * gp
                totalCreditHours += course.creditHours
            }

            // Real dynamic computed values
            val computedGpa = if (totalCreditHours > 0) {
                round((totalGradePoints / totalCreditHours) * 100.0) / 100.0
            } else {
                0.0
            }

            val finalGpa = semester.manualGpa ?: computedGpa
            val finalCredits = semester.manualCredits ?: if (totalCreditHours > 0) totalCreditHours else semCourses.filter { !it.isRetaken }.sumOf { it.creditHours }

            semester.id to SemesterSummaryData(
                semesterId = semester.id,
                name = semester.name,
                isIncluded = semester.isIncluded,
                gpa = finalGpa,
                credits = finalCredits,
                isManual = semester.manualGpa != null
            )
        }.toMap()

        // Filter semesters that are included
        val validSemesterItems = semesterDataList.values.filter { it.isIncluded && it.gpa > 0.0 }

        var overallGP = 0.0
        var overallCredits = 0

        for (sem in validSemesterItems) {
            overallGP += sem.credits * sem.gpa
            overallCredits += sem.credits
        }

        val cgpa = if (overallCredits > 0) {
            round((overallGP / overallCredits) * 100.0) / 100.0
        } else {
            0.0
        }
        
        // Determine Academic Standing
        val standing = when {
            cgpa >= 3.8 -> "President's List"
            cgpa >= 3.5 -> "Dean's List"
            cgpa >= 2.0 -> "Good Standing"
            cgpa > 0.0 -> "Academic Probation"
            else -> "Not Established"
        }

        CGPACalculationDetail(
            cgpa = cgpa,
            majorCgpa = majorCgpa,
            totalCredits = overallCredits,
            academicStanding = standing,
            semestersData = semesterDataList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CGPACalculationDetail(0.0, 0.0, 0, "Not Established", emptyMap())
    )

    // Loading state for OCR
    val isScanningMode = MutableStateFlow(false)
    val scanError = MutableStateFlow<String?>(null)

    // Trigger auto prediction parameters pre-fill (only if user hasn't explicitly customized yet or based on dynamic sync)
    init {
        viewModelScope.launch {
            overallCgpaDetails.collect { detail ->
                if (!hasManuallyEditedTarget) {
                    if (detail.cgpa > 0.0) {
                        targetCurrentCgpa.value = detail.cgpa.toString()
                    }
                    if (detail.totalCredits > 0) {
                        targetDoneCredits.value = detail.totalCredits.toString()
                    }
                }
            }
        }
    }

    // UI actions
    fun changeTab(tab: AppTab) {
        activeTab.value = tab
    }

    fun selectSemester(semesterId: String) {
        selectedSemesterId.value = semesterId
    }

    fun updateCourseMark(courseId: String, markString: String) {
        viewModelScope.launch {
            val course = courses.value.firstOrNull { it.id == courseId } ?: return@launch
            val trimmed = markString.trim().uppercase()
            if (trimmed.isEmpty()) {
                repository.updateCourse(course.copy(mark = null, letterGrade = null, gradePoint = null))
                return@launch
            }
            
            var parsedMark = trimmed.toDoubleOrNull()
            
            if (parsedMark == null) {
                // If it's not a number, check if it's a valid letter grade
                parsedMark = when (trimmed) {
                    "A+", "A +" -> 95.0
                    "A" -> 87.5
                    "A-", "A -" -> 82.5
                    "B+", "B +" -> 77.5
                    "B" -> 72.5
                    "B-", "B -" -> 67.5
                    "C+", "C +" -> 62.5
                    "C" -> 57.5
                    "C-", "C -" -> 52.5
                    "D+", "D", "D-", "D +" -> 45.0
                    "F" -> 0.0
                    else -> null
                }
            }
            
            if (parsedMark == null || parsedMark < 0.0 || parsedMark > 100.0) {
                // Ignore silent invalid typings to not spam too heavily, but if it's explicitly two letters or completely weird, show error.
                // However, since UI triggers on every character, typing "A" then "+" will temporarily be "A", which is valid.
                // Typing "P" will be invalid. Let's show a subtle error if it gets too long.
                if (trimmed.length > 2) {
                    uiErrorEvent.value = "Invalid input. Please enter a mark (0-100) or valid letter grade."
                }
                return@launch
            }
            // Real conversion
            val (letter, gp) = convertMarkToGrade(parsedMark)
            repository.updateCourse(course.copy(
                mark = parsedMark,
                letterGrade = letter,
                gradePoint = gp
            ))
        }
    }

    fun updateCourseCredits(courseId: String, credits: Int) {
        viewModelScope.launch {
            val course = courses.value.firstOrNull { it.id == courseId } ?: return@launch
            val otherCourses = courses.value.filter { it.semesterId == course.semesterId && it.id != courseId }
            val otherCredits = otherCourses.sumOf { it.creditHours }
            
            if (otherCredits + credits > 21) {
                uiErrorEvent.value = "Credit hour limit exceeded (max 21)"
                return@launch
            }
            repository.updateCourse(course.copy(creditHours = credits))
        }
    }

    fun toggleCourseInclusion(courseId: String, isIncluded: Boolean) {
        viewModelScope.launch {
            val course = courses.value.firstOrNull { it.id == courseId } ?: return@launch
            repository.updateCourse(course.copy(isIncluded = isIncluded))
        }
    }

    fun toggleCourseMajor(courseId: String, isMajor: Boolean) {
        viewModelScope.launch {
            val course = courses.value.firstOrNull { it.id == courseId } ?: return@launch
            repository.updateCourse(course.copy(isMajor = isMajor))
        }
    }

    fun toggleCourseRetaken(courseId: String, isRetaken: Boolean) {
        viewModelScope.launch {
            val course = courses.value.firstOrNull { it.id == courseId } ?: return@launch
            repository.updateCourse(course.copy(isRetaken = isRetaken))
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            repository.deleteCourse(courseId)
        }
    }

    fun addCourseToSemester(courseName: String, creditHours: Int) {
        val semId = selectedSemesterId.value
        val nameTrimmed = courseName.trim()
        if (nameTrimmed.isEmpty()) {
            uiErrorEvent.value = "Please enter a valid course name"
            return
        }

        val semCourses = courses.value.filter { it.semesterId == semId }
        val currentCredits = semCourses.sumOf { it.creditHours }

        if (currentCredits + creditHours > 21) {
            uiErrorEvent.value = "Credit hour limit exceeded (max 21)"
            return
        }

        val isDuplicate = semCourses.any { it.name.lowercase() == nameTrimmed.lowercase() }
        if (isDuplicate) {
            uiErrorEvent.value = "Course already added!"
            return
        }

        viewModelScope.launch {
            repository.insertCourse(
                CourseEntity(
                    semesterId = semId,
                    name = nameTrimmed,
                    creditHours = creditHours,
                    isIncluded = true
                )
            )
        }
    }

    fun resetSemester() {
        viewModelScope.launch {
            repository.resetSemester(selectedSemesterId.value)
        }
    }

    // CGPA Tab actions
    fun toggleSemesterInclusion(semesterId: String, isIncluded: Boolean) {
        viewModelScope.launch {
            val semester = semesters.value.firstOrNull { it.id == semesterId } ?: return@launch
            repository.updateSemester(semester.copy(isIncluded = isIncluded))
        }
    }

    fun updateSemesterManualGpa(semesterId: String, gpaString: String) {
        viewModelScope.launch {
            val semester = semesters.value.firstOrNull { it.id == semesterId } ?: return@launch
            if (gpaString.trim().isEmpty()) {
                repository.updateSemester(semester.copy(manualGpa = null))
                return@launch
            }
            val parsed = gpaString.toDoubleOrNull()
            if (parsed == null || parsed < 0.0 || parsed > 4.0) {
                return@launch
            }
            repository.updateSemester(semester.copy(manualGpa = parsed))
        }
    }

    fun updateSemesterManualCredits(semesterId: String, creditsString: String) {
        viewModelScope.launch {
            val semester = semesters.value.firstOrNull { it.id == semesterId } ?: return@launch
            if (creditsString.trim().isEmpty()) {
                repository.updateSemester(semester.copy(manualCredits = null))
                return@launch
            }
            val parsed = creditsString.toIntOrNull()
            if (parsed == null || parsed <= 0) {
                return@launch
            }
            repository.updateSemester(semester.copy(manualCredits = parsed))
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            repository.resetAll()
            hasManuallyEditedTarget = false
        }
    }

    // Prediction Form Updates
    fun updateTargetCurrentCgpa(value: String) {
        hasManuallyEditedTarget = true
        targetCurrentCgpa.value = value
    }

    fun updateTargetDoneCredits(value: String) {
        hasManuallyEditedTarget = true
        targetDoneCredits.value = value
    }

    fun updateTargetCgpaInput(value: String) {
        hasManuallyEditedTarget = true
        targetCgpaInput.value = value
    }

    fun updateTargetRemainingCredits(value: String) {
        hasManuallyEditedTarget = true
        targetRemainingCredits.value = value
    }

    fun clearTargetPredictor() {
        targetCurrentCgpa.value = ""
        targetDoneCredits.value = ""
        targetCgpaInput.value = ""
        targetRemainingCredits.value = ""
        hasManuallyEditedTarget = true
    }

    // Helper conversions
    fun convertMarkToGrade(mark: Double): Pair<String, Double> {
        return when {
            mark >= 90.0 && mark <= 100.0 -> "A+" to 4.0
            mark >= 85.0 && mark < 90.0 -> "A" to 4.0
            mark >= 80.0 && mark < 85.0 -> "A-" to 3.75
            mark >= 75.0 && mark < 80.0 -> "B+" to 3.5
            mark >= 70.0 && mark < 75.0 -> "B" to 3.0
            mark >= 65.0 && mark < 70.0 -> "B-" to 2.75
            mark >= 60.0 && mark < 65.0 -> "C+" to 2.5
            mark >= 55.0 && mark < 60.0 -> "C" to 2.0
            mark >= 50.0 && mark < 55.0 -> "C-" to 1.75
            mark >= 40.0 && mark < 50.0 -> "D" to 1.0
            else -> "F" to 0.0
        }
    }

    fun scanDocumentFromUri(context: android.content.Context, uri: android.net.Uri, semesterId: String) {
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            isScanningMode.value = true
            scanError.value = null
            try {
                // Determine mime type
                val mimeType = appContext.contentResolver.getType(uri) ?: "application/pdf"
                val inputStream = appContext.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    scanError.value = "Failed to load document for scanning."
                    uiErrorEvent.value = "Failed to load document for scanning."
                    isScanningMode.value = false
                    return@launch
                }

                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY") {
                    scanError.value = "Gemini API key is missing. Please add it to your Config."
                    isScanningMode.value = false
                    return@launch
                }
                
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )

                val prompt = """
                    Analyze the image or PDF of this academic transcript or syllabus.
                    Extract the courses with their corresponding credit hours and obtained grades or marks.
                    Return ONLY a JSON array of objects, where each object has exactly these fields:
                    - "name": string (the course name, short but descriptive)
                    - "creditHours": int (usually between 1-5, default to 3 if unknown)
                    - "mark": double (If a percentage mark is visible, use it. If only a letter grade like A, B+, C is visible, convert it to a standard percentage estimate, e.g. A -> 90, A- -> 82, B+ -> 78, B -> 72, C -> 58, etc. using standard US college scales approx if specific one isnt given. Must be a double.)
                    Do not wrap the output in markdown block like ```json. Return just the raw JSON array string starting with [ and ending with ].
                """.trimIndent()
                
                val inputContent = content {
                    blob(mimeType, bytes)
                    text(prompt)
                }
                
                val response = generativeModel.generateContent(inputContent)
                val responseText = response.text?.trim()?.removePrefix("```json")?.removeSuffix("```")?.trim() ?: "[]"
                
                val jsonArray = JSONArray(responseText)
                var addedCount = 0
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val name = obj.optString("name", "Unknown Course")
                    if (name.isBlank() || name == "Unknown Course") continue
                    
                    val credits = obj.optInt("creditHours", 3)
                    val mark = obj.optDouble("mark", 0.0)
                    
                    val course = CourseEntity(
                        id = UUID.randomUUID().toString(),
                        semesterId = semesterId,
                        name = name,
                        creditHours = if (credits > 0) credits else 3,
                        mark = if (mark > 0.0) mark else null,
                        isIncluded = true
                    )
                    repository.insertCourse(course)
                    addedCount++
                }
                
                uiErrorEvent.value = "Successfully scanned $addedCount courses!"
            } catch (e: Throwable) {
                e.printStackTrace()
                scanError.value = "Scan Failed: ${e.localizedMessage}"
                uiErrorEvent.value = "Scan Failed: ${e.localizedMessage}"
            } finally {
                isScanningMode.value = false
            }
        }
    }

    fun scanTranscript(bitmap: Bitmap, semesterId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isScanningMode.value = true
            scanError.value = null
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY") {
                    scanError.value = "Gemini API key is missing. Please add it to your Config."
                    isScanningMode.value = false
                    return@launch
                }
                
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
                
                val prompt = """
                    Analyze the image of this academic transcript or syllabus.
                    Extract the courses with their corresponding credit hours and obtained grades or marks.
                    Return ONLY a JSON array of objects, where each object has exactly these fields:
                    - "name": string (the course name, short but descriptive)
                    - "creditHours": int (usually between 1-5, default to 3 if unknown)
                    - "mark": double (If a percentage mark is visible, use it. If only a letter grade like A, B+, C is visible, convert it to a standard percentage estimate, e.g. A -> 90, A- -> 82, B+ -> 78, B -> 72, C -> 58, etc. using standard US college scales approx if specific one isnt given. Must be a double.)
                    Do not wrap the output in markdown block like ```json. Return just the raw JSON array string starting with [ and ending with ].
                """.trimIndent()
                
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }
                
                val response = generativeModel.generateContent(inputContent)
                val responseText = response.text?.trim()?.removePrefix("```json")?.removeSuffix("```")?.trim() ?: "[]"
                
                val jsonArray = JSONArray(responseText)
                var addedCount = 0
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val name = obj.optString("name", "Unknown Course")
                    if (name.isBlank() || name == "Unknown Course") continue
                    
                    val credits = obj.optInt("creditHours", 3)
                    val mark = obj.optDouble("mark", 0.0)
                    
                    val course = CourseEntity(
                        id = UUID.randomUUID().toString(),
                        semesterId = semesterId,
                        name = name,
                        creditHours = if (credits > 0) credits else 3,
                        mark = if (mark > 0.0) mark else null,
                        isIncluded = true
                    )
                    repository.insertCourse(course)
                    addedCount++
                }
                
                uiErrorEvent.value = "Successfully scanned $addedCount courses!"
            } catch (e: Throwable) {
                scanError.value = "Scan Failed: ${e.localizedMessage}"
                uiErrorEvent.value = "Scan Failed: ${e.localizedMessage}"
            } finally {
                isScanningMode.value = false
            }
        }
    }
}

// Data structures
data class SemesterCalculationDetail(
    val gpa: Double,
    val majorGpa: Double,
    val earnedCredits: Int,
    val totalCredits: Int,
    val courses: List<CourseEntity>,
    val validCourses: List<CourseEntity>
)

data class SemesterSummaryData(
    val semesterId: String,
    val name: String,
    val isIncluded: Boolean,
    val gpa: Double,
    val credits: Int,
    val isManual: Boolean
)

data class CGPACalculationDetail(
    val cgpa: Double,
    val majorCgpa: Double,
    val totalCredits: Int,
    val academicStanding: String,
    val semestersData: Map<String, SemesterSummaryData>
)

class GPAViewModelFactory(private val repository: GPACalculatorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GPAViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GPAViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
