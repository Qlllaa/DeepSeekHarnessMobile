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
    
    fun startRuntime() {
        val intent = Intent(this, LinuxRuntimeService::class.java)
        intent.action = LinuxRuntimeService.ACTION_START
        startForegroundService(intent)
    }
    
    fun stopRuntime() {
        val intent = Intent(this, LinuxRuntimeService::class.java)
        intent.action = LinuxRuntimeService.ACTION_STOP
        stopService(intent)
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Agent" to Icons.Default.Assistant, "项目" to Icons.Default.Folder, "终端" to Icons.Default.Terminal, "设置" to Icons.Default.Settings)
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("DeepSeek Harness") }) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, (title, icon) ->
                    NavigationBarItem(icon = { Icon(icon, title) }, label = { Text(title) }, selected = selectedTab == index, onClick = { selectedTab = index })
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            RuntimeStatus()
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
fun RuntimeStatus() {
    val state = LinuxRuntimeService.runtimeState
    val text = when (state) {
        RuntimeState.IDLE -> "未运行"
        RuntimeState.INITIALIZING -> "初始化中..."
        RuntimeState.UBUNTU_READY -> "Ubuntu 就绪"
        RuntimeState.HARNESS_RUNNING -> "运行中"
        RuntimeState.ERROR -> "错误"
    }
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(if (state == RuntimeState.HARNESS_RUNNING) Color.Green else Color.Red, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        if (state == RuntimeState.IDLE || state == RuntimeState.ERROR) {
            Button(onClick = { /* Call activity method */ }) { Text("启动") }
        } else if (state == RuntimeState.HARNESS_RUNNING) {
            Button(onClick = { /* Call activity method */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("停止") }
        }
    }
}

@Composable
fun AgentTab() {
    var messages by remember { mutableStateOf(listOf("系统" to "你好！")) }
    var input by remember { mutableStateOf("") }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(messages) { (sender, text) ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("$sender: $text", modifier = Modifier.padding(12.dp))
            }
        }
        item {
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (input.isNotBlank()) { messages = messages + listOf("你" to input, "AI" to "处理中..."); input = "" } }, modifier = Modifier.fillMaxWidth()) { Text("发送") }
        }
    }
}

@Composable
fun ProjectsTab() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("项目目录", style = MaterialTheme.typography.headlineSmall)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("暂无项目") }
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("添加项目") }
    }
}

@Composable
fun TerminalTab() {
    var output by remember { mutableStateOf(listOf("root@android:~# ")) }
    var input by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Surface(modifier = Modifier.weight(1f).fillMaxWidth(), color = Color(0xFF1E1E1E)) {
            LazyColumn(modifier = Modifier.padding(8.dp)) { items(output) { Text(it, fontFamily = FontFamily.Monospace, color = Color.Green) } }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("root@android:~# ", fontFamily = FontFamily.Monospace)
            TextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f))
            IconButton(onClick = { if (input.isNotBlank()) { output = output + listOf(input, "输出...", ""); input = "" } }) { Icon(Icons.Default.Send, "发送", tint = Color.White) }
        }
    }
}

@Composable
fun SettingsTab() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("设置", style = MaterialTheme.typography.headlineSmall)
        Card { Column(modifier = Modifier.padding(16.dp)) { Text("版本", style = MaterialTheme.typography.titleMedium); Text("Ubuntu 24.04 ARM64") } }
        Card { Column(modifier = Modifier.padding(16.dp)) { Text("操作", style = MaterialTheme.typography.titleMedium); Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("重启") } } }
    }
}
