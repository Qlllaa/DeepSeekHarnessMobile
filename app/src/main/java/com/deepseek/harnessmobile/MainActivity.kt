package com.deepseek.harnessmobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        Tab("Agent", Icons.Default.Assistant),
        Tab("项目", Icons.Default.Folder),
        Tab("终端", Icons.Default.Terminal),
        Tab("设置", Icons.Default.Settings)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DeepSeek Harness") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Runtime status bar
            RuntimeStatusBar()
            
            when (selectedTab) {
                0 -> AgentTab()
                1 -> ProjectsTab()
                2 -> TerminalTab()
                3 -> SettingsTab()
            }
        }
    }
}

@Composable
fun RuntimeStatusBar() {
    val state = LinuxRuntimeService.runtimeState
    val stateColor = when (state) {
        RuntimeState.IDLE -> MaterialTheme.colorScheme.error
        RuntimeState.INITIALIZING -> MaterialTheme.colorScheme.tertiary
        RuntimeState.UBUNTU_READY, RuntimeState.HARNESS_RUNNING -> MaterialTheme.colorScheme.primary
        RuntimeState.ERROR -> MaterialTheme.colorScheme.error
    }
    val stateText = when (state) {
        RuntimeState.IDLE -> "未运行"
        RuntimeState.INITIALIZING -> "初始化中..."
        RuntimeState.UBUNTU_READY -> "Ubuntu 就绪"
        RuntimeState.HARNESS_RUNNING -> "运行中"
        RuntimeState.ERROR -> "错误"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.width(4.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(stateColor, circleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stateText,
            fontSize = 14.sp,
            color = stateColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        if (state == RuntimeState.IDLE || state == RuntimeState.ERROR) {
            Button(
                size = ButtonSizes.Compact,
                onClick = { startRuntime() }
            ) {
                Text("启动")
            }
        } else if (state == RuntimeState.HARNESS_RUNNING) {
            Button(
                size = ButtonSizes.Compact,
                onClick = { stopRuntime() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("停止")
            }
        }
    }
}

fun startRuntime() {
    val intent = Intent(android.content.Context.START_SERVICE)
    intent.setClassName("com.deepseek.harnessmobile", "com.deepseek.harnessmobile.LinuxRuntimeService")
    intent.action = LinuxRuntimeService.ACTION_START
    // Note: In real app, use Context.startForegroundService()
}

fun stopRuntime() {
    val intent = Intent(android.content.Context.START_SERVICE)
    intent.setClassName("com.deepseek.harnessmobile", "com.deepseek.harnessmobile.LinuxRuntimeService")
    intent.action = LinuxRuntimeService.ACTION_STOP
    // Note: In real app, use Context.startForegroundService()
}

data class Tab(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
private object ButtonSizes {
    val Compact = ButtonSize.MinimalHeight
}

@Composable
fun AgentTab() {
    var messages by remember { mutableStateOf(listOf<Message>(Message("DeepSeek Harness", "你好！我是你的 AI 编程助手。请输入任务。")) ) }
    var input by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { msg ->
            MessageBubble(msg)
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("输入任务...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        messages = messages + Message("你", input)
                        messages = messages + Message("DeepSeek Harness", "处理中...")
                        input = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("发送")
            }
        }
    }
}

@Composable
fun MessageBubble(msg: Message) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(if (msg.sender == "你") Alignment.End else Alignment.Start),
        colors = CardDefaults.cardColors(
            containerColor = if (msg.sender == "你") 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = msg.sender,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = msg.text,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

data class Message(val sender: String, val text: String)

@Composable
fun ProjectsTab() {
    val projects = remember { mutableStateOf(listOf<String>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("项目目录", style = MaterialTheme.typography.headlineSmall)
        
        if (projects.value.isEmpty()) {
            Text("暂无项目，请添加项目", modifier = Modifier.padding(vertical = 32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects.value) { project ->
                    Card(onClick = { /* Open project */ }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(project, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "打开",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = { /* Add project */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("添加项目")
        }
    }
}

@Composable
fun TerminalTab() {
    var output by remember { mutableStateOf(listOf("root@android:~# ")) }
    var input by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(output) { line ->
                    Text(
                        text = line,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("root@android:~# ", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = androidx.compose.ui.text.style.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            )
            IconButton(onClick = {
                if (input.isNotBlank()) {
                    output = output + listOf("root@android:~# $input", "", "命令输出...", "")
                    input = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "执行")
            }
        }
    }
}

@Composable
fun SettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall)
        
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Linux 环境", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                SettingRow("版本", "Ubuntu 24.04 ARM64")
                SettingRow("Node.js", "v24.x")
                SettingRow("pnpm", "v10.x")
                SettingRow("存储", "3.2 GB / 12 GB")
            }
        }
        
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Harness", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                SettingRow("API Key", "已配置")
                SettingRow("模型", "deepseek-coder")
                SettingRow("端口", "3080")
            }
        }
        
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("操作", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Restart */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重启 Ubuntu")
                }
                Button(
                    onClick = { /* Clear cache */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text("清理缓存")
                }
            }
        }
    }
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
