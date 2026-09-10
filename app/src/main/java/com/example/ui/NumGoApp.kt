package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AutoCreateScreen
import com.example.ui.screens.GetNumberScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OtpMonitorScreen
import com.example.ui.screens.ProxySettingsScreen
import com.example.ui.screens.SetPasswordScreen
import com.example.ui.screens.TwoFactorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumGoApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userPassword by viewModel.userPassword.collectAsState()
    val masterProxy by viewModel.masterProxy.collectAsState()
    val liveServices by viewModel.liveServices.collectAsState()
    val selectedService by viewModel.selectedService.collectAsState()
    val activeNumbers by viewModel.activeNumbers.collectAsState()
    val otpList by viewModel.otpList.collectAsState()
    val createdAccounts by viewModel.createdAccounts.collectAsState()
    val isAutoCreating by viewModel.isAutoCreating.collectAsState()
    val connectingInfo by viewModel.connectingInfo.collectAsState()
    val autoCreateLogs by viewModel.autoCreateLogs.collectAsState()
    val isFetchingNumber by viewModel.isFetchingNumber.collectAsState()
    val latestFetchedNumber by viewModel.latestFetchedNumber.collectAsState()
    val twoFactorSecret by viewModel.twoFactorSecret.collectAsState()
    val twoFactorCode by viewModel.twoFactorCode.collectAsState()
    val twoFactorSecondsLeft by viewModel.twoFactorSecondsLeft.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    BackHandler(enabled = currentScreen != AppNavScreen.HOME) {
        viewModel.navigateTo(AppNavScreen.HOME)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            AppNavScreen.HOME -> "NumGo Live"
                            AppNavScreen.GET_NUMBER -> "🎲 Get Number"
                            AppNavScreen.AUTO_CREATE -> "🚀 Auto Create"
                            AppNavScreen.SET_PASSWORD -> "🔑 Set Password"
                            AppNavScreen.TWO_FACTOR -> "🔐 2FA Code"
                            AppNavScreen.OTP_MONITOR -> "🎯 OTP Monitor"
                            AppNavScreen.PROXY_SETTINGS -> "🌐 Proxy Settings"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    if (currentScreen != AppNavScreen.HOME) {
                        IconButton(onClick = { viewModel.navigateTo(AppNavScreen.HOME) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "পিছনে যান"
                            )
                        }
                    }
                },
                actions = {
                    if (currentScreen != AppNavScreen.PROXY_SETTINGS) {
                        IconButton(onClick = { viewModel.navigateTo(AppNavScreen.PROXY_SETTINGS) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "প্রক্সি সেটিংস"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppNavScreen.HOME,
                    onClick = { viewModel.navigateTo(AppNavScreen.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "হোম") },
                    label = { Text("হোম") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppNavScreen.GET_NUMBER,
                    onClick = { viewModel.navigateTo(AppNavScreen.GET_NUMBER) },
                    icon = {
                        if (activeNumbers.isNotEmpty()) {
                            BadgedBox(badge = { Badge { Text("${activeNumbers.size}") } }) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = "নাম্বার")
                            }
                        } else {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = "নাম্বার")
                        }
                    },
                    label = { Text("নাম্বার") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppNavScreen.AUTO_CREATE,
                    onClick = { viewModel.navigateTo(AppNavScreen.AUTO_CREATE) },
                    icon = { Icon(Icons.Default.RocketLaunch, contentDescription = "অটো ক্রিয়েট") },
                    label = { Text("অটো ক্রিয়েট") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppNavScreen.TWO_FACTOR,
                    onClick = { viewModel.navigateTo(AppNavScreen.TWO_FACTOR) },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "২এফএ") },
                    label = { Text("২এফএ") }
                )
                NavigationBarItem(
                    selected = currentScreen == AppNavScreen.OTP_MONITOR,
                    onClick = { viewModel.navigateTo(AppNavScreen.OTP_MONITOR) },
                    icon = {
                        if (otpList.isNotEmpty()) {
                            BadgedBox(badge = { Badge { Text("${otpList.size}") } }) {
                                Icon(Icons.Default.MarkEmailRead, contentDescription = "ওটিপি")
                            }
                        } else {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = "ওটিপি")
                        }
                    },
                    label = { Text("ওটিপি") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppNavScreen.HOME -> {
                    HomeScreen(
                        userPassword = userPassword,
                        masterProxy = masterProxy,
                        activeNumbersCount = activeNumbers.size,
                        otpCount = otpList.size,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                AppNavScreen.GET_NUMBER -> {
                    GetNumberScreen(
                        services = liveServices,
                        selectedService = selectedService,
                        activeNumbers = activeNumbers,
                        latestFetched = latestFetchedNumber,
                        isFetching = isFetchingNumber,
                        onSelectService = { viewModel.selectService(it) },
                        onRequestNumber = { range, svc -> viewModel.requestNumber(range, svc) },
                        onRemoveActiveNumber = { viewModel.removeActiveNumber(it) },
                        onRefreshServices = { viewModel.loadLiveServices() }
                    )
                }
                AppNavScreen.AUTO_CREATE -> {
                    AutoCreateScreen(
                        userPassword = userPassword,
                        services = liveServices,
                        createdAccounts = createdAccounts,
                        isCreating = isAutoCreating,
                        connectingInfo = connectingInfo,
                        logs = autoCreateLogs,
                        onStartAutoCreate = { range, count -> viewModel.startAutoCreate(range, count) },
                        onNavigateSetPassword = { viewModel.navigateTo(AppNavScreen.SET_PASSWORD) }
                    )
                }
                AppNavScreen.SET_PASSWORD -> {
                    SetPasswordScreen(
                        currentPassword = userPassword,
                        onSavePassword = { viewModel.setPassword(it) }
                    )
                }
                AppNavScreen.TWO_FACTOR -> {
                    TwoFactorScreen(
                        secret = twoFactorSecret,
                        code = twoFactorCode,
                        secondsLeft = twoFactorSecondsLeft,
                        onSecretChanged = { viewModel.setTwoFactorSecret(it) }
                    )
                }
                AppNavScreen.OTP_MONITOR -> {
                    OtpMonitorScreen(
                        otpList = otpList,
                        activeNumbers = activeNumbers
                    )
                }
                AppNavScreen.PROXY_SETTINGS -> {
                    ProxySettingsScreen(
                        currentProxy = masterProxy,
                        onSaveProxy = { viewModel.setMasterProxy(it) }
                    )
                }
            }
        }
    }
}
