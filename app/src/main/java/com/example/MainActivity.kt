package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.repository.WorkforceRepository
import com.example.location.LocationHelper
import com.example.model.UserRole
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.SupervisorDashboardScreen
import com.example.ui.screens.WorkerDashboardScreen
import com.example.ui.theme.ArtifyTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.SupervisorViewModel
import com.example.ui.viewmodel.WorkerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtifyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ArtifyAppRoot()
                }
            }
        }
    }
}

@Composable
fun ArtifyAppRoot() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repository = remember { WorkforceRepository(db) }
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    val authViewModel = remember { AuthViewModel(repository) }
    val authState by authViewModel.uiState.collectAsState()

    var projects by remember { mutableStateOf<List<com.example.data.entity.ProjectEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.seedInitialDataIfEmpty()
        repository.getAllProjects().collect {
            projects = it
        }
    }

    // Dynamic Permission Launcher for Location & Camera
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    val currentUser = authState.currentUser

    Crossfade(targetState = currentUser, label = "auth_screen_crossfade") { user ->
        if (user == null) {
            AuthScreen(
                authViewModel = authViewModel,
                projects = projects
            )
        } else {
            when (user.role) {
                UserRole.SUPERVISOR.name -> {
                    val supervisorViewModel = remember(user.userId) {
                        SupervisorViewModel(repository, user)
                    }
                    SupervisorDashboardScreen(
                        supervisorViewModel = supervisorViewModel,
                        onLogoutClick = { authViewModel.logout() }
                    )
                }
                else -> {
                    // Worker or Staff
                    val workerViewModel = remember(user.userId) {
                        WorkerViewModel(repository, user)
                    }

                    // Feed device location if available
                    LaunchedEffect(hasLocationPermission) {
                        if (hasLocationPermission) {
                            coroutineScope.launch {
                                val loc = locationHelper.getCurrentLocation()
                                if (loc != null) {
                                    workerViewModel.onDeviceLocationReceived(loc)
                                }
                            }
                        }
                    }

                    WorkerDashboardScreen(
                        workerViewModel = workerViewModel,
                        repository = repository,
                        onLogoutClick = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}
