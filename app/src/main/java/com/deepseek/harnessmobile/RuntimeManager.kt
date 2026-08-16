package com.deepseek.harnessmobile

import android.content.Context
import android.util.Log
import java.io.File

class RuntimeManager(private val context: Context) {

    companion object {
        private const val TAG = "RuntimeManager"
        @JvmStatic
        var instance: RuntimeManager? = null
    }

    private val appDir = File(context.filesDir, "linux")
    private val ubuntuRootfs = File(appDir, "ubuntu")
    private val harnessDir = File(appDir, "harness")
    private val projectsDir = File(context.getExternalFilesDir(null) ?: File(context.filesDir, "projects"), "projects")

    var processRunner: ProcessRunner? = null
        private set

    fun initializeEnvironment() {
        Log.d(TAG, "Initializing environment...")
        ubuntuRootfs.mkdirs()
        harnessDir.mkdirs()
        projectsDir.mkdirs()
        
        if (!isUbuntuReady()) {
            Log.d(TAG, "Creating placeholder Ubuntu structure")
            createPlaceholderUbuntu()
        }
        Log.d(TAG, "Environment initialized")
    }

    fun startPrroot() {
        Log.d(TAG, "Starting PRoot...")
        LinuxRuntimeService.runtimeState = RuntimeState.HARNESS_RUNNING
    }

    fun stop() {
        processRunner?.destroy()
        processRunner = null
        Log.d(TAG, "Runtime stopped")
    }

    private fun isUbuntuReady(): Boolean {
        return File(ubuntuRootfs, "bin").exists() && File(ubuntuRootfs, "usr").exists()
    }

    private fun createPlaceholderUbuntu() {
        File(ubuntuRootfs, "bin").mkdirs()
        File(ubuntuRootfs, "usr").mkdirs()
        File(ubuntuRootfs, "etc").mkdirs()
        File(ubuntuRootfs, "home").mkdirs()
        File(ubuntuRootfs, "tmp").mkdirs()
        File(ubuntuRootfs, "root").mkdirs()
    }

    fun getProjectPath(projectName: String): String {
        return File(projectsDir, projectName).absolutePath
    }

    fun listProjects(): List<String> {
        return projectsDir.list()?.toList() ?: emptyList()
    }
}
