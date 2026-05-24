package com.example.data

data class CatalogCourse(
    val name: String,
    val defaultCredits: Int
)

object ACTCurriculum {
    val semesters = listOf(
        SemesterEntity("Y1S1", 1, 1, "Year 1 — Semester 1"),
        SemesterEntity("Y1S2", 1, 2, "Year 1 — Semester 2"),
        SemesterEntity("Y2S1", 2, 1, "Year 2 — Semester 1"),
        SemesterEntity("Y2S2", 2, 2, "Year 2 — Semester 2"),
        SemesterEntity("Y3S1", 3, 1, "Year 3 — Semester 1"),
        SemesterEntity("Y3S2", 3, 2, "Year 3 — Semester 2"),
        SemesterEntity("Y4S1", 4, 1, "Year 4 — Semester 1"),
        SemesterEntity("Y4S2", 4, 2, "Year 4 — Semester 2")
    )

    val catalog = listOf(
        CatalogCourse("Advanced Database Systems", 3),
        CatalogCourse("Applied Mathematics I", 3),
        CatalogCourse("Automata and Complexity Theory", 3),
        CatalogCourse("Communicative English I", 3),
        CatalogCourse("Communicative English II", 3),
        CatalogCourse("Compiler Design", 3),
        CatalogCourse("Computer Graphics", 3),
        CatalogCourse("Computer Organization and Architecture", 3),
        CatalogCourse("Computer Programming", 4),
        CatalogCourse("Computer Security", 3),
        CatalogCourse("Computer Vision and Image Processing", 3),
        CatalogCourse("Critical Thinking", 3),
        CatalogCourse("Data Communication and Computer Networks", 3),
        CatalogCourse("Data Structures and Algorithms", 3),
        CatalogCourse("Design and Analysis of Algorithms", 3),
        CatalogCourse("Digital Logic Design", 3),
        CatalogCourse("Discrete Mathematics and Combinatorics", 3),
        CatalogCourse("Distributed Systems", 3),
        CatalogCourse("Economics", 3),
        CatalogCourse("Elective", 3),
        CatalogCourse("Emerging Technologies", 3),
        CatalogCourse("Entrepreneurship and Business Development", 3),
        CatalogCourse("Final Year Project I", 3),
        CatalogCourse("Final Year Project II", 3),
        CatalogCourse("Fundamentals of Database Systems", 3),
        CatalogCourse("General Physics", 3),
        CatalogCourse("General Psychology", 3),
        CatalogCourse("Geography of Ethiopia and the Horn", 3),
        CatalogCourse("Global Trends", 2),
        CatalogCourse("History of Ethiopia and the Horn", 3),
        CatalogCourse("Industrial Practice", 2),
        CatalogCourse("Inclusiveness", 2),
        CatalogCourse("Introduction to Artificial Intelligence", 3),
        CatalogCourse("Java Programming", 3),
        CatalogCourse("Linear Algebra", 3),
        CatalogCourse("Mathematics for Natural Sciences", 3),
        CatalogCourse("Microprocessor and Assembly Language", 3),
        CatalogCourse("Moral and Civic Education", 2),
        CatalogCourse("Network and System Administration", 3),
        CatalogCourse("Numerical Analysis", 3),
        CatalogCourse("Object Oriented Programming", 3),
        CatalogCourse("Operating Systems", 3),
        CatalogCourse("Physical Fitness", 2),
        CatalogCourse("Probability and Statistics", 3),
        CatalogCourse("Real-Time and Embedded Systems", 3),
        CatalogCourse("Research Methods", 2),
        CatalogCourse("Selected Topics in Computer Science", 3),
        CatalogCourse("Social Anthropology", 2),
        CatalogCourse("Software Engineering", 3),
        CatalogCourse("Web Programming I", 3),
        CatalogCourse("Web Programming II", 3),
        CatalogCourse("Web Technologies", 3),
        CatalogCourse("Wireless and Mobile Computing", 3)
    )

    fun getDefaultCourses(semesterId: String): List<CourseEntity> {
        val courseNamesAndCredits = when (semesterId) {
            "Y1S1" -> listOf(
                "Communicative English I" to 3,
                "Geography of Ethiopia and the Horn" to 3,
                "Critical Thinking" to 3,
                "Mathematics for Natural Sciences" to 3,
                "General Physics" to 3,
                "General Psychology" to 3,
                "Physical Fitness" to 2
            )
            "Y1S2" -> listOf(
                "Communicative English II" to 3,
                "Social Anthropology" to 2,
                "Economics" to 3,
                "Applied Mathematics I" to 3,
                "History of Ethiopia and the Horn" to 3,
                "Emerging Technologies" to 3,
                "Moral and Civic Education" to 2,
                "Global Trends" to 2
            )
            "Y2S1" -> listOf(
                "Computer Programming" to 4,
                "Fundamentals of Database Systems" to 3,
                "Digital Logic Design" to 3,
                "Linear Algebra" to 3,
                "Probability and Statistics" to 3,
                "Inclusiveness" to 2
            )
            "Y2S2" -> listOf(
                "Computer Organization and Architecture" to 3,
                "Data Communication and Computer Networks" to 3,
                "Advanced Database Systems" to 3,
                "Java Programming" to 3,
                "Discrete Mathematics and Combinatorics" to 3,
                "Numerical Analysis" to 3
            )
            "Y3S1" -> listOf(
                "Object Oriented Programming" to 3,
                "Data Structures and Algorithms" to 3,
                "Operating Systems" to 3,
                "Microprocessor and Assembly Language" to 3,
                "Software Engineering" to 3,
                "Web Programming I" to 3,
                "Automata and Complexity Theory" to 3
            )
            "Y3S2" -> listOf(
                "Real-Time and Embedded Systems" to 3,
                "Wireless and Mobile Computing" to 3,
                "Computer Graphics" to 3,
                "Web Programming II" to 3,
                "Design and Analysis of Algorithms" to 3,
                "Industrial Practice" to 2,
                "Entrepreneurship and Business Development" to 3
            )
            "Y4S1" -> listOf(
                "Introduction to Artificial Intelligence" to 3,
                "Computer Security" to 3,
                "Compiler Design" to 3,
                "Computer Vision and Image Processing" to 3,
                "Research Methods" to 2,
                "Final Year Project I" to 3,
                "Web Technologies" to 3
            )
            "Y4S2" -> listOf(
                "Network and System Administration" to 3,
                "Distributed Systems" to 3,
                "Final Year Project II" to 3,
                "Selected Topics in Computer Science" to 3,
                "Elective" to 3
            )
            else -> emptyList()
        }

        return courseNamesAndCredits.map { (name, credits) ->
            CourseEntity(
                semesterId = semesterId,
                name = name,
                creditHours = credits
            )
        }
    }
}
