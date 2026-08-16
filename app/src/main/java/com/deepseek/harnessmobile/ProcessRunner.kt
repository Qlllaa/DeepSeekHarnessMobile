package com.deepseek.harnessmobile

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

class ProcessRunner(private val command: List<String>, private val env: Map<String, String> = emptyMap()) {
    
    companion object {
        private const val TAG = "ProcessRunner"
    }
    
    private var process: Process? = null
    private val outputLines = mutableListOf<String>()
    
    fun start() {
        try {
            val builder = ProcessBuilder(command)
            builder.redirectErrorStream(true)
            
            if (env.isNotEmpty()) {
                builder.environment().putAll(env)
            }
            
            process = builder.start()
            Log.d(TAG, "Process started: ${command.joinToString(" ")}")
            
            // Read output in background
            Thread {
                try {
                    val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        outputLines.add(line!!)
                        Log.d(TAG, "OUT: $line")
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading process output", e)
                }
            }.start()
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start process", e)
        }
    }
    
    fun destroy() {
        process?.destroy()
        Log.d(TAG, "Process destroyed")
    }
    
    fun isRunning(): Boolean = process?.isAlive ?: false
    
    fun getOutput(): List<String> = outputLines.toList()
}
