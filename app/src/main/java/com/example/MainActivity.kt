package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AcademicRepository
import com.example.data.local.MasarDatabase
import com.example.data.local.StudentPersonalStorage
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiyasAndExamsScreen
import com.example.ui.screens.SourcesAndSyncScreen
import com.example.ui.screens.StudyAndWeightedScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AcademicViewModel
import com.example.widget.MasarAppWidgetProvider

enum class MasarTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    QIYAS("qiyas", "قياس والاختبارات", Icons.Filled.School, Icons.Outlined.School),
    STUDY("study", "الموزونة والقبول", Icons.Filled.Calculate, Icons.Outlined.Calculate),
    SOURCES("sources", "التحديث والمصادر", Icons.Filled.CloudSync, Icons.Outlined.CloudSync)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MasarDatabase.getInstance(applicationContext)
        val repository = AcademicRepository(database.masarDao())
        val personalStorage = StudentPersonalStorage.fromContext(applicationContext)

        // Update Home Screen App Widget on launch
        MasarAppWidgetProvider.refreshAllWidgets(applicationContext)

        setContent {
            MyApplicationTheme {
                val academicViewModel: AcademicViewModel = viewModel(
                    factory = AcademicViewModel.provideFactory(repository, personalStorage)
                )
                MasarAppRoot(viewModel = academicViewModel)
            }
        }
    }
}

@Composable
fun MasarAppRoot(viewModel: AcademicViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(MasarTab.HOME) }

    if (currentTab != MasarTab.HOME) {
        BackHandler {
            currentTab = MasarTab.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MasarTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            MasarTab.HOME -> HomeScreen(
                uiState = uiState,
                onSelectGrade = { grade ->
                    viewModel.selectGrade(grade)
                    MasarAppWidgetProvider.refreshAllWidgets(context)
                },
                onSelectRegion = { region ->
                    viewModel.selectRegion(region)
                    MasarAppWidgetProvider.refreshAllWidgets(context)
                },
                onSelectFilter = viewModel::setHomeCategoryFilter,
                onRefreshSync = { viewModel.triggerAutoSync(silent = false) },
                onNavigateToExams = { currentTab = MasarTab.QIYAS },
                onNavigateToWeighted = { currentTab = MasarTab.STUDY },
                modifier = Modifier.padding(innerPadding)
            )

            MasarTab.QIYAS -> QiyasAndExamsScreen(
                uiState = uiState,
                minuteVibrationEvents = viewModel.minuteVibrationEvents,
                onSaveComputerizedBooking = viewModel::saveComputerizedBooking,
                onClearComputerizedBooking = viewModel::clearComputerizedBooking,
                onAddShortSchoolQuiz = viewModel::addShortSchoolQuiz,
                onDeleteShortSchoolQuiz = viewModel::deleteShortSchoolQuiz,
                onUpdateTahsiliPages = viewModel::updateTahsiliRemainingPages,
                onUpdateSpeedTimerQuestions = viewModel::updateSpeedTimerQuestionCount,
                onToggleSpeedTimer = viewModel::toggleSpeedTimerRunning,
                onResetSpeedTimer = viewModel::resetSpeedTimer,
                onAddCustomExam = viewModel::addCustomExam,
                onDeleteCustomExam = viewModel::deleteCustomExam,
                modifier = Modifier.padding(innerPadding)
            )

            MasarTab.STUDY -> StudyAndWeightedScreen(
                uiState = uiState,
                onUpdateMasaratGrades = viewModel::updateMasaratGrades,
                onAdoptMasaratGpa = viewModel::adoptMasaratGpaIntoWeighted,
                onUpdateWeightedInputs = viewModel::updateWeightedInputs,
                onApplyPreset = viewModel::applyWeightedPreset,
                onSaveScenario = viewModel::saveCurrentWeightedScenario,
                onDeleteScenario = viewModel::deleteWeightedScenario,
                onToggleEligibleMajorsFilter = viewModel::setFilterOnlyEligibleMajors,
                onSelectMajorField = viewModel::setMajorFieldFilter,
                onSelectUniversityRegion = viewModel::setUniversityRegionFilter,
                onAddStudyTask = viewModel::addStudyTask,
                onToggleStudyTask = viewModel::toggleStudyTask,
                onDeleteStudyTask = viewModel::deleteStudyTask,
                modifier = Modifier.padding(innerPadding)
            )

            MasarTab.SOURCES -> SourcesAndSyncScreen(
                uiState = uiState,
                onTriggerSync = { viewModel.triggerAutoSync(silent = false) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
