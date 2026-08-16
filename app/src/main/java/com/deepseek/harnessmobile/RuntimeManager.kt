package com.deepseek.harnessmobile

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

enum class RuntimeState {
    UNINITIALIZED,
    INITIALIZING,
    UBUNTU_READY,
    STARTING_HARNESS,
    RUNNING,
    ERROR
}

class RuntimeManager(private val context: Context) {

    companion object {
        private const val TAG = "RuntimeManager"
        private const val UBUNTU_DIR = "ubuntu"
        private const val HARNESS_DIR = "harness"
        private const val RUNTIME_DIR = "runtime"
        private const val PROJECTS_DIR = "projects"
    }

    var state: RuntimeState = RuntimeState.UNINITIALIZED
        private set

    private val appDir = File(context.filesDir, "linux")
    private val ubuntuRootfs = File(appDir, UBUNTU_DIR)
    private val harnessDir = File(appDir, HARNESS_DIR)
    private val runtimeDir = File(appDir, RUNTIME_DIR)
    private val projectsDir = File(context.getExternalFilesDir(null), PROJECTS_DIR)

    var processRunner: ProcessRunner? = null

    suspend fun start() = withContext(Dispatchers.IO) {
        try {
            state = RuntimeState.INITIALIZING
            Log.d(TAG, "Starting initialization...")
            
            // Create directories
            ubuntuRootfs.mkdirs()
            harnessDir.mkdirs()
            runtimeDir.mkdirs()
            projectsDir.mkdirs()
            
            // Check if Ubuntu is already set up
            if (!isUbuntuReady()) {
                Log.d(TAG, "Ubuntu not ready, would download rootfs here")
                // In v0.1, we'll skip actual download and just mark as ready
                // for testing the structure
                markUbuntuReady()
            }
            
            state = RuntimeState.UBUNTU_READY
            Log.d(TAG, "Ubuntu ready")
            
            // Start PRoot process
            startPrroot()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start runtime", e)
            state = RuntimeState.ERROR
        }
    }

    fun stop() {
        processRunner?.destroy()
        processRunner = null
        state = RuntimeState.UNINITIALIZED
        Log.d(TAG, "Runtime stopped")
    }

    private fun isUbuntuReady(): Boolean {
        return File(ubuntuRootfs, "bin").exists() &&
               File(ubuntuRootfs, "usr").exists()
    }

    private fun markUbuntuReady() {
        // Create minimal directory structure for testing
        File(ubuntuRootfs, "bin").mkdirs()
        File(ubuntuRootfs, "usr").mkdirs()
        File(ubuntuRootfs, "etc").mkdirs()
        File(ubuntuRootfs, "home").mkdirs()
        File(ubuntuRootfs, "tmp").mkdirs()
    }

    private fun startPrroot() {
        // In v0.1, we just create a placeholder
        // Actual PRoot integration requires the binary
        Log.d(TAG, "PRoot would start here")
    }
}
