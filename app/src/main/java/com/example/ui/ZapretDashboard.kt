package com.example.ui

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ZapretDashboard(viewModel: ZapretViewModel) {
    val isVpnRunning by viewModel.isVpnRunning.collectAsState()
    val processedPackets by viewModel.processedPackets.collectAsState()
    val activeLogs by viewModel.vpnLogs.collectAsState()
    
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Console, 1: Diagnostic, 2: AI Doctor
    
    // Create Vpn prepare intent launcher
    val prepareVpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleVpn()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberObsidian),
        containerColor = CyberObsidian,
        bottomBar = {
            NavigationBar(
                containerColor = CyberDarkSlate,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.SettingsInputComponent, "Console") },
                    label = { Text("Запрет Консоль", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NeonGreen,
                        unselectedIconColor = TextGrayMuted,
                        unselectedTextColor = TextGrayMuted,
                        indicatorColor = NeonGreen
                    ),
                    modifier = Modifier.testTag("nav_console_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Speed, "Diagnostic") },
                    label = { Text("Диагностика пинга", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NeonCyan,
                        unselectedIconColor = TextGrayMuted,
                        unselectedTextColor = TextGrayMuted,
                        indicatorColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("nav_diagnostic_tab")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.SmartToy, "AI Doctor") },
                    label = { Text("ИИ NetDoctor", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NeonGold,
                        unselectedIconColor = TextGrayMuted,
                        unselectedTextColor = TextGrayMuted,
                        indicatorColor = NeonGold
                    ),
                    modifier = Modifier.testTag("nav_ai_tab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CyberObsidian,
                            CyberDarkSlate
                        )
                    )
                )
        ) {
            // Glowing Network Status Header
            HeaderBlock(isVpnRunning, processedPackets)

            // Dynamic view selector based on Tab
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { tab ->
                when (tab) {
                    0 -> ConsoleTabScreen(
                        viewModel = viewModel,
                        isVpnRunning = isVpnRunning,
                        activeLogs = activeLogs,
                        onToggleClick = {
                            val intent = VpnService.prepare(context)
                            if (intent != null) {
                                prepareVpnLauncher.launch(intent)
                            } else {
                                viewModel.toggleVpn()
                            }
                        }
                    )
                    1 -> DiagnosticTabScreen(viewModel = viewModel)
                    2 -> AiDoctorTabScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HeaderBlock(isVpnRunning: Boolean, processedPackets: Int) {
    var tickCount by remember { mutableStateOf(0) }
    
    // Quick ticking timer for cyber dashboard feel
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            tickCount++
        }
    }

    val glowColor = if (isVpnRunning) NeonGreen else CyberCrimson
    val statusText = if (isVpnRunning) "ZAPRET: СТАТУС АКТИВЕН" else "ZAPRET: СЕРВИС ВЫКЛЮЧЕН"
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
            .drawBehind {
                val strokeWidth = 2f
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = glowColor.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "ZAPRET NETWORK TUNER",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "SYS_VER_1.4.2 // DEV_MODE_ACTIVE",
                    color = TextGrayMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Blinking matrix-like system timer
            Box(
                modifier = Modifier
                    .background(CyberDarkSlate, RoundedCornerShape(4.dp))
                    .border(1.dp, glowColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SEC: $tickCount",
                    color = glowColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(glowColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                color = glowColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            
            if (isVpnRunning) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "ПАКЕТОВ: $processedPackets",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConsoleTabScreen(
    viewModel: ZapretViewModel,
    isVpnRunning: Boolean,
    activeLogs: List<String>,
    onToggleClick: () -> Unit
) {
    val selectedDns by viewModel.selectedDns.collectAsState()
    val selectedMtu by viewModel.selectedMtu.collectAsState()
    val selectedStrategy by viewModel.selectedStrategy.collectAsState()
    val customSni by viewModel.customSni.collectAsState()

    var showStratMenu by remember { mutableStateOf(false) }
    var showDnsMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Control Toggle Panel (Vibe: Retro Cyber Switch Button)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkSlate),
                border = BorderStroke(1.dp, if (isVpnRunning) NeonGreen.copy(alpha = 0.8f) else TextGrayMuted.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isVpnRunning) "Байпас Сессии Запущен" else "Обход Блокировок Выключен",
                        color = if (isVpnRunning) NeonGreen else TextSlateWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isVpnRunning) "Ваш трафик маршрутизируется со снижением MTU и DoH" else "Все запросы заблокированных сайтов напрямую могут резаться вашим провайдером",
                        color = TextGrayMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // HUGE GLOWING LAUNCH BUTTON
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .testTag("vpn_toggle_button")
                            .size(110.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                color = if (isVpnRunning) NeonGreen.copy(alpha = 0.15f) else CyberCrimson.copy(
                                    alpha = 0.1f
                                )
                            )
                            .border(
                                2.dp,
                                if (isVpnRunning) NeonGreen else CyberCrimson.copy(alpha = 0.5f),
                                RoundedCornerShape(50)
                            )
                            .clickable(onClick = onToggleClick)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isVpnRunning) Icons.Default.PowerSettingsNew else Icons.Default.PowerSettingsNew,
                                contentDescription = "Toggle Zapret",
                                tint = if (isVpnRunning) NeonGreen else CyberCrimson,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isVpnRunning) "ВЫКЛ" else "СТАРТ",
                                color = if (isVpnRunning) NeonGreen else CyberCrimson,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // DNS, MTU & Custom Settings Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkSlate),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "[ПАРАМЕТРЫ ФРАГМЕНТАЦИИ]",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bypass Strategy Select dropdown
                    Text("Стратегия Обхода (Bypass Mode):", color = TextSlateWhite, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStratMenu = true }
                    ) {
                        OutlinedTextField(
                            value = selectedStrategy,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            textStyle = TextStyle(color = NeonGreen, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStratMenu) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = NeonGreen,
                                disabledBorderColor = CyberGrayCard,
                                disabledTrailingIconColor = TextGrayMuted
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("strategy_selector")
                        )
                        DropdownMenu(
                            expanded = showStratMenu,
                            onDismissRequest = { showStratMenu = false },
                            modifier = Modifier.background(CyberDarkSlate)
                        ) {
                            viewModel.bypassStrategies.forEach { (key, desc) ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(key, color = NeonGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(desc, color = TextGrayMuted, fontSize = 10.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectedStrategy.value = key
                                        showStratMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DNS Select dropdown
                    Text("Безопасный DNS (DNS Over HTTPS):", color = TextSlateWhite, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDnsMenu = true }
                    ) {
                        OutlinedTextField(
                            value = viewModel.dnsProviders.find { it.first == selectedDns }?.second ?: selectedDns,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            textStyle = TextStyle(color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDnsMenu) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = NeonCyan,
                                disabledBorderColor = CyberGrayCard,
                                disabledTrailingIconColor = TextGrayMuted
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dns_selector")
                        )
                        DropdownMenu(
                            expanded = showDnsMenu,
                            onDismissRequest = { showDnsMenu = false },
                            modifier = Modifier.background(CyberDarkSlate)
                        ) {
                            viewModel.dnsProviders.forEach { (ip, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(label, color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(ip, color = TextGrayMuted, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectedDns.value = ip
                                        showDnsMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // MTU Slider
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Тюнинг MTU (Размер пакета):", color = TextSlateWhite, fontSize = 12.sp)
                        Text("$selectedMtu байт", color = NeonCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = selectedMtu.toFloat(),
                        onValueChange = { viewModel.selectedMtu.value = it.toInt() },
                        valueRange = 500f..1500f,
                        steps = 20,
                        colors = SliderDefaults.colors(
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CyberGrayCard,
                            thumbColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("mtu_slider")
                    )
                    Text(
                        text = "Уменьшение MTU до 1250 или ниже заставляет DPI обрывать проверку TLS из-за фрагментации пакета (Zapret byeDPI стандарт).",
                        color = TextGrayMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SNI Override Input
                    Text("Кастомный SNI для поддельного пакета:", color = TextSlateWhite, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customSni,
                        onValueChange = { viewModel.customSni.value = it },
                        textStyle = TextStyle(color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        placeholder = { Text("discord.com", color = TextGrayMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberGrayCard
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sni_input")
                    )
                }
            }
        }

        // Live Log Window
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkSlate),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[ВЫВОД ХАУСА LOGS]",
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "LIVE STREAM",
                            color = NeonGold.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CyberObsidian)
                            .border(1.dp, CyberGrayCard, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(activeLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("Ошибка") || log.contains("Блокировка")) CyberCrimson else if (log.contains("успешно") || log.contains("Bypass")) NeonGreen else TextSlateWhite,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun DiagnosticTabScreen(viewModel: ZapretViewModel) {
    val serviceStatuses by viewModel.serviceStatuses.collectAsState()
    val isDiagnosing by viewModel.isDiagnosing.collectAsState()
    val isVpnRunning by viewModel.isVpnRunning.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Latency explanation and diagnostic block starter
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkSlate),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "[СИСТЕМНЫЙ СЕТЕВОЙ МОНИТОР]",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Встроенный сканер задержки посылает пакетные пинги и устанавливает статус доступности для ключевых платформ.",
                        color = TextSlateWhite,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.runGlobalDiagnostics() },
                        enabled = !isDiagnosing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            disabledContainerColor = CyberGrayCard,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_diagnostics_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isDiagnosing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Проверяем узлы связи...", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Bolt, "Bolt", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ЗАПУСТИТЬ ТЕСТ СВЯЗИ", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live grid items showing service connection state
        items(serviceStatuses) { status ->
            val indicatorColor = when {
                status.pingMs in 1..100 -> NeonGreen
                status.pingMs > 100 -> NeonGold
                else -> CyberCrimson
            }
            
            val statusDisplay = when {
                status.pingMs > 0 -> "${status.pingMs} ms"
                isDiagnosing -> "Проверка..."
                else -> "Таймаут (DPI Блок!)"
            }

            val icon = when (status.category) {
                "Video" -> Icons.Default.PlayCircle
                "Social" -> Icons.Default.ChatBubble
                "Game" -> Icons.Default.SportsEsports
                else -> Icons.Default.Memory
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkSlate),
                border = BorderStroke(1.dp, indicatorColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("service_card_${status.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(indicatorColor.copy(alpha = 0.1f))
                                .border(1.dp, indicatorColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(icon, null, tint = indicatorColor, modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = status.name,
                                color = TextSlateWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = status.message,
                                color = TextGrayMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = statusDisplay,
                            color = indicatorColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(indicatorColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (status.pingMs > 0) "ОК" else "FAIL",
                                color = indicatorColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun AiDoctorTabScreen(viewModel: ZapretViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    
    var chatInputValue by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Slide down to new user messages
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Chat Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CyberObsidian)
                .border(1.dp, NeonGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
                    val alignment = if (message.isUser) Alignment.End else Alignment.Start
                    val bubbleBg = if (message.isUser) CyberGrayCard else CyberDarkSlate
                    val border = if (message.isUser) NeonCyan.copy(alpha = 0.4f) else NeonGold.copy(alpha = 0.4f)
                    val textColor = if (message.isUser) NeonCyan else TextSlateWhite

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = alignment
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bubbleBg)
                                .border(1.dp, border, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (message.isUser) "root@user:~$" else "ai@netdoctor:~$",
                                    color = if (message.isUser) NeonCyan else NeonGold,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.text,
                                    color = textColor,
                                    fontSize = 12.sp,
                                    style = LocalTextStyle.current.copy(lineHeight = 16.sp)
                                )
                            }
                        }
                    }
                }
                
                if (isChatLoading) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberDarkSlate)
                                    .border(1.dp, NeonGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = NeonGold, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Анализ подключения...", color = NeonGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions: Clean Chat Terminal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { viewModel.clearChat() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberDarkSlate,
                    contentColor = TextSlateWhite
                ),
                border = BorderStroke(1.dp, CyberGrayCard),
                modifier = Modifier
                    .wrapContentSize()
                    .testTag("clear_chat_button")
            ) {
                Icon(Icons.Default.DeleteSweep, "Clear Logs", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ОЧИСТИТЬ ЭКРАН", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Terminal Command Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInputValue,
                onValueChange = { chatInputValue = it },
                textStyle = TextStyle(color = TextSlateWhite, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                placeholder = { Text("Спроси про YouTube, Discord, Roblox...", color = TextGrayMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (chatInputValue.isNotBlank()) {
                            viewModel.sendAiMessage(chatInputValue)
                            chatInputValue = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGold,
                    unfocusedBorderColor = CyberGrayCard
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input")
            )
            
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (chatInputValue.isNotBlank()) {
                        viewModel.sendAiMessage(chatInputValue)
                        chatInputValue = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGold,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .aspectRatio(1f)
                    .testTag("ai_send_button"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.ArrowOutward, "Send Command", modifier = Modifier.size(24.dp))
            }
        }
    }
}
