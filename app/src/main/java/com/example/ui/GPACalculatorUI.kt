package com.example.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import kotlinx.coroutines.launch
import kotlin.math.round

val LocalThemeMode = compositionLocalOf { true }

@Composable
fun isSystemInDarkTheme(): Boolean {
    return LocalThemeMode.current
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GPACalculatorApp(viewModel: GPAViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val errorEvent by viewModel.uiErrorEvent.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Error event listener to display elegant Snackbar
    LaunchedEffect(errorEvent) {
        errorEvent?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.uiErrorEvent.value = null
            }
        }
    }

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemDark
    }

    CompositionLocalProvider(LocalThemeMode provides isDark) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBarCompact(viewModel)
            },
            bottomBar = {
                BottomNavBar(activeTab = activeTab, onTabSelected = { viewModel.changeTab(it) })
            },
            containerColor = if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB) // Professional Polish background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    AppTab.GPA -> GPAScreen(viewModel = viewModel)
                    AppTab.CGPA -> CGPAScreen(viewModel = viewModel)
                    AppTab.TARGET -> TargetScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TopAppBarCompact(viewModel: GPAViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()

    Surface(
        color = if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 18.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "ACT Logo",
                    tint = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ACT GPA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33),
                    letterSpacing = (-0.02).sp
                )
                Text(
                    text = "COMPUTER SCIENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77).copy(alpha = 0.8f),
                    letterSpacing = 1.5.sp
                )
            }
            
            IconButton(onClick = {
                val nextMode = when (themeMode) {
                    AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                    AppThemeMode.LIGHT -> AppThemeMode.DARK
                    AppThemeMode.DARK -> AppThemeMode.SYSTEM
                }
                viewModel.themeMode.value = nextMode
            }) {
                val icon = when (themeMode) {
                    AppThemeMode.LIGHT -> Icons.Filled.LightMode
                    AppThemeMode.DARK -> Icons.Filled.DarkMode
                    AppThemeMode.SYSTEM -> Icons.Filled.SettingsSystemDaydream
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle Theme",
                    tint = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33)
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(activeTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9)),
        color = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Filled.Calculate,
                label = "GPA",
                isActive = activeTab == AppTab.GPA,
                onClick = { onTabSelected(AppTab.GPA) }
            )
            NavBarItem(
                icon = Icons.Filled.Analytics,
                label = "CGPA",
                isActive = activeTab == AppTab.CGPA,
                onClick = { onTabSelected(AppTab.CGPA) }
            )
            NavBarItem(
                icon = Icons.Filled.TrackChanges,
                label = "Target",
                isActive = activeTab == AppTab.TARGET,
                onClick = { onTabSelected(AppTab.TARGET) }
            )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) if (isSystemInDarkTheme()) Color(0xFF1C2B3E) else Color(0xFFD3E3FD) else Color.Transparent
    val contentColor = if (isActive) if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77) else if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(containerColor, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun GPAScreen(viewModel: GPAViewModel) {
    val selectedSemId by viewModel.selectedSemesterId.collectAsState()
    val semestersList by viewModel.semesters.collectAsState()
    val selectedCalcDetail by viewModel.selectedSemesterDetails.collectAsState()
    val isScanningMode by viewModel.isScanningMode.collectAsState()

    var showAddCourseDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.scanDocumentFromUri(context, it, selectedSemId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown Selector & Credits Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Menu Selector
                Box {
                    val currentSemName = semestersList.firstOrNull { it.id == selectedSemId }?.name ?: selectedSemId
                    Row(
                        modifier = Modifier
                            .background(if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .border(1.dp, if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6), RoundedCornerShape(10.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentSemName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Expand menu",
                            tint = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        scrollState = rememberScrollState(),
                        modifier = Modifier.background(if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White)
                    ) {
                        semestersList.forEach { sem ->
                            DropdownMenuItem(
                                text = { Text(sem.name) },
                                onClick = {
                                    viewModel.selectSemester(sem.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Header Badge
                val semTotalCreditsLimitStatusColor = if (selectedCalcDetail.totalCredits > 21) if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37) else if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132)
                val semTotalCreditsLimitStatusBg = if (selectedCalcDetail.totalCredits > 21) if (isSystemInDarkTheme()) Color(0xFF93000A) else Color(0xFFF8D7DA) else if (isSystemInDarkTheme()) Color(0xFF00391C) else Color(0xFFD1E7DD)

                Row(
                    modifier = Modifier
                        .background(semTotalCreditsLimitStatusBg, RoundedCornerShape(9999.dp))
                        .border(1.dp, semTotalCreditsLimitStatusColor.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedCalcDetail.totalCredits > 21) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = "Indicator",
                        tint = semTotalCreditsLimitStatusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${selectedCalcDetail.totalCredits}/21 cr",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = semTotalCreditsLimitStatusColor
                    )
                }
            }
        }

        // Result Container Card - Styled using "Professional Polish" guidelines
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1C2B3E) else Color(0xFFD3E3FD)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Semester GPA",
                                fontSize = 14.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format("%.2f", selectedCalcDetail.gpa),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49),
                                fontFamily = FontFamily.Monospace
                            )
                            if (selectedCalcDetail.majorGpa > 0.0) {
                                Text(
                                    text = "Major GPA: ${String.format("%.2f", selectedCalcDetail.majorGpa)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Credits Earned",
                                fontSize = 12.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${selectedCalcDetail.earnedCredits}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar Gradient Indicator
                    val boundsPercent = (selectedCalcDetail.gpa / 4.0).coerceIn(0.0, 1.0).toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(boundsPercent)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37), Color(0xFFF7A27B), if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.0", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.6f))
                        Text("2.0", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.6f))
                        Text("4.0", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.6f))
                    }

                    // View Calculation Details Accordion
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var isExpanded by remember { mutableStateOf(false) }
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "View Calculation Details",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "Expand details",
                                    tint = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (selectedCalcDetail.validCourses.isEmpty()) {
                                        Text(
                                            text = "No courses with valid marks to show.",
                                            fontSize = 12.sp,
                                            color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.7f)
                                        )
                                    } else {
                                        selectedCalcDetail.validCourses.forEach { course ->
                                            val (_, gp) = viewModel.convertMarkToGrade(course.mark ?: 0.0)
                                            val contribution = course.creditHours * gp
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = course.name,
                                                    fontSize = 12.sp,
                                                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49),
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${course.letterGrade ?: "F"} (${String.format("%.2f", gp)}) × ${course.creditHours}cr = ${String.format("%.1f", contribution)}",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6), modifier = Modifier.padding(vertical = 4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val totalGP = selectedCalcDetail.validCourses.sumOf {
                                                val (_, gp) = viewModel.convertMarkToGrade(it.mark ?: 0.0)
                                                it.creditHours * gp
                                            }
                                            Text(
                                                text = "Total Grade Points",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49)
                                            )
                                            Text(
                                                text = String.format("%.2f", totalGP),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Courses
        item {
            Text(
                text = "Courses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // List of Course Cards
        if (selectedCalcDetail.courses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White),
                    border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No courses. Reset or add custom ones!",
                            color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = selectedCalcDetail.courses,
                key = { it.id }
            ) { course ->
                CourseCard(
                    course = course,
                    onMarkChanged = { viewModel.updateCourseMark(course.id, it) },
                    onCreditsChanged = { viewModel.updateCourseCredits(course.id, it) },
                    onInclusionToggled = { viewModel.toggleCourseInclusion(course.id, it) },
                    onDelete = { viewModel.deleteCourse(course.id) },
                    onMajorToggled = { viewModel.toggleCourseMajor(course.id, it) },
                    onRetakenToggled = { viewModel.toggleCourseRetaken(course.id, it) }
                )
            }
        }

        // Sticky bottom row actions spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showAddCourseDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Icon")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Course", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        // Recalculates dynamically, let's show confirmation notification
                        viewModel.uiErrorEvent.value = "Calculated Selected Semester GPA: ${String.format("%.2f", selectedCalcDetail.gpa)}"
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                ) {
                    Icon(imageVector = Icons.Filled.Calculate, contentDescription = "Calc Icon")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calculate GPA", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { 
                    try {
                        documentPickerLauncher.launch(arrayOf("image/*", "application/pdf")) 
                    } catch (e: Exception) {
                        viewModel.uiErrorEvent.value = "File picker not available on this device"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFFB4A1FF) else Color(0xFF6200EE)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isSystemInDarkTheme()) Color(0xFFB4A1FF) else Color(0xFF6200EE))
            ) {
                if (isScanningMode) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = if (isSystemInDarkTheme()) Color(0xFFB4A1FF) else Color(0xFF6200EE), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Scanning with Gemini AI...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Filled.DocumentScanner, contentDescription = "Scan Icon")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Transcript (PDF/Image)", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset Semester",
                    color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.resetSemester() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismiss = { showAddCourseDialog = false },
            onAdd = { name, credits ->
                viewModel.addCourseToSemester(name, credits)
                showAddCourseDialog = false
            }
        )
    }
}

@Composable
fun CourseCard(
    course: CourseEntity,
    onMarkChanged: (String) -> Unit,
    onCreditsChanged: (Int) -> Unit,
    onInclusionToggled: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onMajorToggled: (Boolean) -> Unit,
    onRetakenToggled: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (course.isIncluded && !course.isRetaken) if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White else if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (course.isIncluded && !course.isRetaken) if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9) else if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Checkbox and Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = course.isIncluded,
                        onCheckedChange = onInclusionToggled,
                        colors = CheckboxDefaults.colors(checkedColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)),
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = course.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (course.isIncluded && !course.isRetaken) if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33) else if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E),
                            textDecoration = if (course.isRetaken) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                        )
                        Text(
                            text = "Default Credits: ${course.creditHours}",
                            fontSize = 12.sp,
                            color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)
                        )
                    }
                }

                // Grade / Delete action section
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (course.isRetaken) {
                            GradeTierBadge("RTK")
                        } else {
                            course.letterGrade?.let { letter ->
                                GradeTierBadge(letter)
                            }
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Course",
                                tint = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37).copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    course.gradePoint?.let { gp ->
                        Text(
                            text = String.format("%.2f GP", gp),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp),
                            textDecoration = if (course.isRetaken) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mark & Credits input layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mark (0-100) Input or Letter Grade
                val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                var markInputText by remember {
                    mutableStateOf(course.mark?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
                }
                
                LaunchedEffect(course.mark) {
                    val currentVal = markInputText.trim().uppercase()
                    val parsedCurrent = currentVal.toDoubleOrNull() ?: when (currentVal) {
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
                    if (course.mark != parsedCurrent) {
                        markInputText = course.mark?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
                    }
                }

                OutlinedTextField(
                    value = markInputText,
                    onValueChange = {
                        val trimmed = it.trim()
                        if (trimmed.length <= 5) {
                            markInputText = trimmed
                            onMarkChanged(trimmed)
                        }
                    },
                    label = { Text("Mark or Grade", fontSize = 10.sp) },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = {
                        markInputText = course.mark?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
                        focusManager.clearFocus()
                    })
                )

                // Credits Selector Dropdown
                var creditsExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${course.creditHours} cr",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Credits") },
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Expand dropdown",
                                modifier = Modifier.clickable { creditsExpanded = true }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = creditsExpanded,
                        onDismissRequest = { creditsExpanded = false },
                        modifier = Modifier.background(if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White)
                    ) {
                        listOf(1, 2, 3, 4).forEach { cr ->
                            DropdownMenuItem(
                                text = { Text("$cr credits") },
                                onClick = {
                                    onCreditsChanged(cr)
                                    creditsExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Flags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = course.isMajor,
                        onCheckedChange = onMajorToggled,
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(checkedColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Major Course", fontSize = 12.sp, color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = course.isRetaken,
                        onCheckedChange = onRetakenToggled,
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(checkedColor = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retaken (Exclude)", fontSize = 12.sp, color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E))
                }
            }
        }
    }
}

@Composable
fun GradeTierBadge(letterGrade: String) {
    val (bgColor, textColor) = when {
        letterGrade.startsWith("A") -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        letterGrade.startsWith("B") -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        letterGrade.startsWith("C") -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        letterGrade.startsWith("D") -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
        else -> Color(0xFFFEE2E2) to Color(0xFFBA1A1A)
    }

    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .border(1.dp, textColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = letterGrade,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCredits by remember { mutableStateOf(3) }
    var customCourseName by remember { mutableStateOf("") }

    // Filter Alphabetical catalog based on search selection
    val filteredCatalog = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            ACTCurriculum.catalog
        } else {
            ACTCurriculum.catalog.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Course",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Search standard catalog
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search catalog course") },
                    placeholder = { Text("e.g. Data Structures") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable results box or manual type
                Text("Catalog results (Tap to select):", fontSize = 12.sp, color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E), fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .border(1.dp, if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6), RoundedCornerShape(8.dp)),
                    color = if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCatalog) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        customCourseName = item.name
                                        selectedCredits = item.defaultCredits
                                    }
                                    .background(
                                        if (customCourseName == item.name) if (isSystemInDarkTheme()) Color(0xFF1C2B3E) else Color(0xFFD3E3FD) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.defaultCredits} cr",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Customized Option
                OutlinedTextField(
                    value = customCourseName,
                    onValueChange = { customCourseName = it },
                    label = { Text("Course Name (selected or custom)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Credits Needed:", fontSize = 12.sp, color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 4).forEach { cr ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedCredits == cr) if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77) else if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB)
                                )
                                .border(
                                    1.dp,
                                    if (selectedCredits == cr) if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77) else if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCredits = cr },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cr",
                                color = if (selectedCredits == cr) if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White else if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customCourseName.trim().isNotEmpty()) {
                                onAdd(customCourseName, selectedCredits)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun CgpaTrendChart(
    semestersData: List<SemesterSummaryData>
) {
    if (semestersData.size < 2) return

    val gpas = semestersData.filter { it.isIncluded }.map { it.gpa }
    if (gpas.size < 2) return

    val labels = semestersData.filter { it.isIncluded }.map { it.name.take(4) }
    val maxGpa = 4.0
    val minGpa = 0.0

    val textMeasurer = rememberTextMeasurer()
    val lineColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)
    val dotColor = if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF041E49)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9)),
        modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("GPA Analytics Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = dotColor)
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
                val width = size.width
                val height = size.height
                val xStep = width / (gpas.size - 1)

                val path = Path()
                val points = mutableListOf<Offset>()

                gpas.forEachIndexed { index, gpa ->
                    val x = index * xStep
                    // Map GPA (0 to 4.0) to height (height to 0)
                    val y = height - ((gpa / 4.0) * height).toFloat()
                    val point = Offset(x, y)
                    points.add(point)

                    if (index == 0) {
                        path.moveTo(point.x, point.y)
                    } else {
                        // Create a smooth cubic bezier curve
                        val previousPoint = points[index - 1]
                        val controlPointX = (previousPoint.x + point.x) / 2
                        path.cubicTo(
                            controlPointX, previousPoint.y,
                            controlPointX, point.y,
                            point.x, point.y
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 6f)
                )

                points.forEachIndexed { index, point ->
                    drawCircle(
                        color = dotColor,
                        radius = 12f,
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 8f,
                        center = point
                    )
                    
                    val textLayoutResult = textMeasurer.measure(
                        text = labels[index],
                        style = TextStyle(color = lineColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(point.x - textLayoutResult.size.width / 2, size.height + 8f)
                    )
                    
                    val gpaValResult = textMeasurer.measure(
                        text = String.format("%.2f", gpas[index]),
                        style = TextStyle(color = dotColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    drawText(
                        textLayoutResult = gpaValResult,
                        topLeft = Offset(point.x - gpaValResult.size.width / 2, point.y - 48f)
                    )
                }
            }
        }
    }
}

@Composable
fun CGPAScreen(viewModel: GPAViewModel) {
    val overallCgpaDetails by viewModel.overallCgpaDetails.collectAsState()
    val semestersList by viewModel.semesters.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CGPA Calculator",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33)
                )
                Text(
                    text = "Enter GPA for completed semesters to project your cumulative standing.",
                    fontSize = 14.sp,
                    color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)
                )
            }
        }

        // Result Bento Board Card with Progress Ring drawing - Brand Compliant
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1C2B3E) else Color(0xFFD3E3FD)),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Cumulative GPA",
                            fontSize = 12.sp,
                            color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format("%.2f", overallCgpaDetails.cgpa),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49),
                            fontFamily = FontFamily.Monospace
                        )
                        if (overallCgpaDetails.majorCgpa > 0.0) {
                            Text(
                                text = "Major CGPA: ${String.format("%.2f", overallCgpaDetails.majorCgpa)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        
                        // Academic Standing
                        Box(
                            modifier = Modifier
                                .background(if (isSystemInDarkTheme()) Color(0xFF00391C) else Color(0xFFD1E7DD), RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = overallCgpaDetails.academicStanding, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        ToXCreditsBadge(totalCredits = overallCgpaDetails.totalCredits)
                    }

                    // Progress Ring SVG replication
                    val graduationRequiredCreditsGoal = 120.0f
                    val creditsRatio = (overallCgpaDetails.totalCredits / graduationRequiredCreditsGoal).coerceIn(0.0f, 1.0f)
                    val percentageNum = (creditsRatio * 100).toInt()

                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                        val arcColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track circle
                            drawCircle(
                                color = trackColor,
                                radius = size.minDimension / 2.0f,
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Progress arc
                            drawArc(
                                color = arcColor,
                                startAngle = -90f,
                                sweepAngle = 360f * creditsRatio,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx())
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$percentageNum%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "done",
                                fontSize = 9.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Trend Chart
        item {
            val chartData = semestersList.mapNotNull { semester ->
                overallCgpaDetails.semestersData[semester.id]
            }.sortedBy { it.name } // basic sort or use inherent order
            
            CgpaTrendChart(semestersData = chartData)
        }

        // Semesters Title list
        item {
            Text(
                text = "Semester Entry History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Semesters item list
        items(
            items = semestersList,
            key = { it.id }
        ) { semester ->
            val summaryData = overallCgpaDetails.semestersData[semester.id] ?: SemesterSummaryData(
                semesterId = semester.id,
                name = semester.name,
                isIncluded = semester.isIncluded,
                gpa = semester.manualGpa ?: 0.0,
                credits = semester.manualCredits ?: 20,
                isManual = semester.manualGpa != null
            )

            SemesterRecordCard(
                semester = semester,
                summaryData = summaryData,
                onInclusionToggled = { viewModel.toggleSemesterInclusion(semester.id, it) },
                onGpaChanged = { viewModel.updateSemesterManualGpa(semester.id, it) },
                onCreditsChanged = { viewModel.updateSemesterManualCredits(semester.id, it) }
            )
        }

        // Sticky Bottom Reset buttons
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    viewModel.uiErrorEvent.value = "Calculated Overall CGPA: ${String.format("%.2f", overallCgpaDetails.cgpa)}"
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
            ) {
                Text("Calculate CGPA", fontWeight = FontWeight.Bold, minLines = 1)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset All System Records",
                    color = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.resetAll() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ToXCreditsBadge(totalCredits: Int) {
    Text(
        text = "Total Credits: $totalCredits",
        fontSize = 13.sp,
        color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF041E49),
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
fun SemesterRecordCard(
    semester: SemesterEntity,
    summaryData: SemesterSummaryData,
    onInclusionToggled: (Boolean) -> Unit,
    onGpaChanged: (String) -> Unit,
    onCreditsChanged: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (semester.isIncluded) if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White else if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (semester.isIncluded) if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9) else if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = semester.isIncluded,
                        onCheckedChange = onInclusionToggled,
                        colors = SwitchDefaults.colors(checkedThumbColor = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White, checkedTrackColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = semester.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (semester.isIncluded) if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33) else if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)
                    )
                }

                if (summaryData.isManual) {
                    Box(
                        modifier = Modifier
                            .background(if (isSystemInDarkTheme()) Color(0xFF1C2B3E) else Color(0xFFD3E3FD), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Manual", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(if (isSystemInDarkTheme()) Color(0xFF00391C) else Color(0xFFD1E7DD), RoundedCornerShape(6.dp))
                            .border(1.dp, if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Computed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Credits manual input
                var creditsText by remember(semester.manualCredits, summaryData.credits) {
                    mutableStateOf(semester.manualCredits?.toString() ?: summaryData.credits.toString())
                }
                OutlinedTextField(
                    value = creditsText,
                    onValueChange = {
                        val input = it.trim()
                        creditsText = input
                        onCreditsChanged(input)
                    },
                    label = { Text("Credits (Default: ${summaryData.credits})") },
                    modifier = Modifier.weight(1f),
                    enabled = semester.isIncluded,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )

                // GPA manual input
                var gpaText by remember(semester.manualGpa, summaryData.gpa) {
                    mutableStateOf(semester.manualGpa?.toString() ?: String.format("%.2f", summaryData.gpa))
                }
                OutlinedTextField(
                    value = gpaText,
                    onValueChange = {
                        val input = it.trim()
                        gpaText = input
                        onGpaChanged(input)
                    },
                    label = { Text("GPA Override") },
                    modifier = Modifier.weight(1f),
                    enabled = semester.isIncluded,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
            }
        }
    }
}

@Composable
fun TargetScreen(viewModel: GPAViewModel) {
    val currentCgpaText by viewModel.targetCurrentCgpa.collectAsState()
    val completedCreditsText by viewModel.targetDoneCredits.collectAsState()
    val targetCgpaText by viewModel.targetCgpaInput.collectAsState()
    val remainingCreditsText by viewModel.targetRemainingCredits.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    // Calculation states
    var displayResult by remember { mutableStateOf<TargetResult?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Target Calculator",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33)
                )
                Text(
                    text = "Find out what GPA you need in remaining semesters to achieve your cumulative graduation goal.",
                    fontSize = 14.sp,
                    color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)
                )
            }
        }

        // Grid Input Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Current CGPA
                        OutlinedTextField(
                            value = currentCgpaText,
                            onValueChange = { viewModel.updateTargetCurrentCgpa(it) },
                            label = { Text("Current CGPA") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = { Icon(imageVector = Icons.Filled.Analytics, contentDescription = "Icon") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                                unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                            ),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )

                        // Done Credits
                        OutlinedTextField(
                            value = completedCreditsText,
                            onValueChange = { viewModel.updateTargetDoneCredits(it) },
                            label = { Text("Done Credits") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = { Icon(imageVector = Icons.Filled.Book, contentDescription = "Icon") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                                unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                            ),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Target CGPA
                        OutlinedTextField(
                            value = targetCgpaText,
                            onValueChange = { viewModel.updateTargetCgpaInput(it) },
                            label = { Text("Target CGPA") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = { Icon(imageVector = Icons.Filled.Flag, contentDescription = "Icon") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                                unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                            ),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )

                        // Remaining Credits
                        OutlinedTextField(
                            value = remainingCreditsText,
                            onValueChange = { viewModel.updateTargetRemainingCredits(it) },
                            label = { Text("Left Credits") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = { Icon(imageVector = Icons.Filled.HourglassEmpty, contentDescription = "Icon") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                                unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF2E4057) else Color(0xFFC2D6F6)
                            ),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Alert",
                            tint = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Values sync with completed course histories organically.",
                            fontSize = 11.sp,
                            color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)
                        )
                    }
                }
            }
        }

        // Action calculate button
        item {
            Button(
                onClick = {
                    keyboardController?.hide()
                    // Equation logic
                    val currentCgpa = currentCgpaText.toDoubleOrNull() ?: 0.0
                    val completedCredits = completedCreditsText.toIntOrNull() ?: 0
                    val targetCgpa = targetCgpaText.toDoubleOrNull() ?: 0.0
                    val remainingCredits = remainingCreditsText.toIntOrNull() ?: 0

                    if (currentCgpa <= 0.0 || targetCgpa <= 0.0 || completedCredits < 0 || remainingCredits <= 0) {
                        displayResult = TargetResult.Error("Enter valid positive numbers.")
                        return@Button
                    }

                    if (currentCgpa > 4.0 || targetCgpa > 4.0) {
                        displayResult = TargetResult.Error("Enter valid CGPA (0-4)")
                        return@Button
                    }

                    val currentTotalPoints = completedCredits * currentCgpa
                    val totalTargetPoints = (completedCredits + remainingCredits) * targetCgpa
                    val requiredPoints = totalTargetPoints - currentTotalPoints
                    val requiredGpa = requiredPoints / remainingCredits

                    val roundedGpa = round(requiredGpa * 100) / 100.0

                    displayResult = if (roundedGpa > 4.0) {
                        TargetResult.Unachievable(roundedGpa, remainingCredits, targetCgpa)
                    } else {
                        TargetResult.Achievable(roundedGpa, remainingCredits, targetCgpa)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77))
            ) {
                Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = "Compute icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate Required GPA", fontWeight = FontWeight.Bold)
            }
        }

        // High Impact Result Section
        item {
            displayResult?.let { res ->
                TargetPredictionResultCard(result = res, onClear = {
                    viewModel.clearTargetPredictor()
                    displayResult = null
                })
            }
        }
    }
}

sealed class TargetResult {
    data class Achievable(val gpaNeeded: Double, val remainingCredits: Int, val target: Double) : TargetResult()
    data class Unachievable(val gpaNeeded: Double, val remainingCredits: Int, val target: Double) : TargetResult()
    data class Error(val errorMessage: String) : TargetResult()
}

@Composable
fun TargetPredictionResultCard(
    result: TargetResult,
    onClear: () -> Unit
) {
    val borderColor = when (result) {
        is TargetResult.Achievable -> if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132)
        is TargetResult.Unachievable -> if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37)
        is TargetResult.Error -> Color(0xFFFEF3C7)
    }

    val bannerText = when (result) {
        is TargetResult.Achievable -> "ACHIEVABLE GOAL"
        is TargetResult.Unachievable -> "GOAL EXCEEDS LIMITS"
        is TargetResult.Error -> "VALIDATION CHECK"
    }

    val primaryTextColor = when (result) {
        is TargetResult.Achievable -> if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132)
        is TargetResult.Unachievable -> if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37)
        is TargetResult.Error -> Color(0xFF92400E)
    }

    val bodyContent = when (result) {
        is TargetResult.Achievable -> {
            "You need ${String.format("%.2f", result.gpaNeeded)} GPA over ${result.remainingCredits} remaining credits to reach target CGPA of ${String.format("%.2f", result.target)}."
        }
        is TargetResult.Unachievable -> {
            "Required GPA is ${String.format("%.2f", result.gpaNeeded)} over ${result.remainingCredits} credits to reach target of ${String.format("%.2f", result.target)}. This exceeds the standard 4.0 maximum GPA range!"
        }
        is TargetResult.Error -> result.errorMessage
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E2329) else Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(borderColor.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = bannerText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    letterSpacing = 0.1.sp
                )
            }

            when (result) {
                is TargetResult.Achievable -> {
                    Text(
                        text = String.format("%.2f", result.gpaNeeded),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color(0xFF82CFFF) else Color(0xFF004A77),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 48.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mood,
                            contentDescription = "Achieved icon",
                            tint = if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "You've got this!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFF81C995) else Color(0xFF0F5132)
                        )
                    }
                }
                is TargetResult.Unachievable -> {
                    Text(
                        text = String.format("%.2f", result.gpaNeeded),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 48.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SentimentVeryDissatisfied,
                            contentDescription = "Unachievable icon",
                            tint = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Not Achievable",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFB02A37)
                        )
                    }
                }
                is TargetResult.Error -> {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Validation alert",
                        tint = Color(0xFF92400E),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isSystemInDarkTheme()) Color(0xFF111418) else Color(0xFFF7F9FB),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFE1E2E9))
            ) {
                Text(
                    text = bodyContent,
                    fontSize = 13.sp,
                    color = if (isSystemInDarkTheme()) Color(0xFFE1EBF5) else Color(0xFF001D33),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClear() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Clear All",
                        tint = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clear Predictor Inputs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSystemInDarkTheme()) Color(0xFFBEC6DC) else Color(0xFF44474E)
                    )
                }
            }
        }
    }
}
