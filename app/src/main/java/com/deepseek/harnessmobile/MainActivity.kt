package com.deepseek.harnessmobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        TabItem("Agent", Icons.Default.Assistant),
        TabItem("项目", Icons.Default.Folder),
        TabItem("终端", Icons.Default.Terminal),
        TabItem("设置", Icons.Default.Settings)
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
    val (stateColor, stateText, buttonText, onButtonClick) = when (state) {
        RuntimeState.IDLE -> MaterialTheme.colorScheme.error to "未运行" to "启动" to { startRuntime() }
        RuntimeState.INITIALIZING -> Color.Yellow to "初始化中..." to "" to {}
        RuntimeState.UBUNTU_READY -> MaterialTheme.colorScheme.primary to "Ubuntu 就绪" to "启动 Harness" to { startRuntime() }
        RuntimeState.HARNESS_RUNNING -> Color.Green to "运行中" to "停止" to { stopRuntime() }
        RuntimeState.ERROR -> MaterialTheme.colorScheme.error to "错误" to "重启" to { startRuntime() }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(stateColor, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stateText,
            fontSize = 14.sp,
            color = stateColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        if (buttonText.isNotEmpty()) {
            Button(
                onClick = onButtonClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}

fun startRuntime() {
    val intent = Intent("com.deepseek.harnessmobile.ACTION_START")
    intent.setClassName("com.deepseek.harnessmobile", "com.deepseek.harnessmobile.LinuxRuntimeService")
    try {
        android.content.ContextCompat.startForegroundService(
            androidx.core.content.ContextCompat.getSystemService(
                MainActivity()!!, 
                android.content.Context.NOTIFICATION_SERVICE
            )!!,
            intent
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun stopRuntime() {
    val intent = Intent("com.deepseek.harnessmobile.ACTION_STOP")
    intent.setClassName("com.deepseek.harnessmobile", "com.deepseek.harnessmobile.LinuxRuntimeService")
    try {
        stopService(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

data class TabItem(val title: String, val icon: ImageVector)

@Composable
fun AgentTab() {
    var messages by remember { mutableStateOf(listOf(Message("系统", "你好！我是 DeepSeek Harness AI。请输入任务。")) ) }
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
                        messages = messages + Message("AI", "处理中...")
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
    val isUser = msg.sender == "你"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(if (isUser) Alignment.End else Alignment.Start),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无项目，点击添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects.value) { project ->
                    Card(onClick = { /* Open project */ }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(project, style = MaterialTheme.typography.titleMedium)
                            Text("打开", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
            color = Color(0xFF1E1E1E),
            contentColor = Color(0xFF00FF00)
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(output) { line ->
                    Text(
                        text = line,
                        fontFamily = FontFamily.Monospace,
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
            Text("root@android:~# ", fontFamily = FontFamily.Monospace)
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                singleLine = true
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
                Spacer(modifier = Modifier.height(8.dp))
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
