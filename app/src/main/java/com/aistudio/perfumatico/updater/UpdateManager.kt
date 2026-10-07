package com.aistudio.perfumatico.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.aistudio.perfumatico.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    data class Completed(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val releaseNotes: String,
    val apkUrl: String
)

class UpdateManager(private val context: Context) {

    private val updateCheckUrl = "https://raw.githubusercontent.com/Warlock201/Perfum-tico-App/main/releases/update.json"

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val bustUrl = if (updateCheckUrl.contains("?")) "$updateCheckUrl&_t=${System.currentTimeMillis()}" else "$updateCheckUrl?_t=${System.currentTimeMillis()}"
            val url = URL(bustUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Cache-Control", "no-cache")
            connection.setRequestProperty("Pragma", "no-cache")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val serverVersionCode = json.optInt("versionCode", -1)
                val serverVersionName = json.optString("versionName", "")
                val apkUrl = json.optString("apkUrl", "")
                val releaseNotes = json.optString("releaseNotes", "Nova versão disponível!")

                val currentVersionCode = BuildConfig.VERSION_CODE
                val hasUpdate = serverVersionCode > currentVersionCode
                
                return@withContext UpdateInfo(
                    hasUpdate = hasUpdate,
                    latestVersionCode = serverVersionCode,
                    latestVersionName = serverVersionName,
                    apkUrl = apkUrl,
                    releaseNotes = releaseNotes
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Erro ao verificar atualizações: ${e.message}")
        }
        return@withContext null
    }

    fun downloadAndInstallUpdate(apkUrl: String, fileName: String = "perfumatico-update.apk") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _downloadState.value = DownloadState.Downloading(0, 0, -1)

                var currentUrl = apkUrl
                var connection: HttpURLConnection? = null
                var redirects = 0
                val maxRedirects = 5

                while (redirects < maxRedirects) {
                    val url = URL(currentUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = true
                        connectTimeout = 20000
                        readTimeout = 20000
                        setRequestProperty("User-Agent", "Perfumatico-App")
                    }
                    val code = connection.responseCode
                    if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                        code == HttpURLConnection.HTTP_MOVED_TEMP ||
                        code == HttpURLConnection.HTTP_SEE_OTHER ||
                        code == 307 || code == 308
                    ) {
                        val newLocation = connection.getHeaderField("Location")
                        if (!newLocation.isNullOrBlank()) {
                            currentUrl = newLocation
                            redirects++
                            continue
                        }
                    }
                    break
                }

                val finalConn = connection
                if (finalConn != null && finalConn.responseCode == HttpURLConnection.HTTP_OK) {
                    val totalBytes = finalConn.contentLengthLong
                    val cacheFile = File(context.cacheDir, "perfumatico_update.apk")
                    if (cacheFile.exists()) {
                        cacheFile.delete()
                    }

                    var totalRead = 0L
                    val buffer = ByteArray(16384)
                    finalConn.inputStream.use { input ->
                        cacheFile.outputStream().use { output ->
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                val percent = if (totalBytes > 0) ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100) else -1
                                _downloadState.value = DownloadState.Downloading(percent, totalRead, totalBytes)
                            }
                        }
                    }

                    _downloadState.value = DownloadState.Completed(cacheFile)

                    withContext(Dispatchers.Main) {
                        installApkDirect(context, cacheFile)
                    }
                } else {
                    val code = finalConn?.responseCode ?: -1
                    val msg = "Erro no servidor (Código: $code). Verifique sua conexão."
                    _downloadState.value = DownloadState.Error(msg)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateManager", "Erro no download direto: ${e.message}")
                val err = e.localizedMessage ?: "Erro de rede"
                _downloadState.value = DownloadState.Error(err)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falha ao baixar: $err", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun installCurrentApk() {
        val cacheFile = File(context.cacheDir, "perfumatico_update.apk")
        if (cacheFile.exists()) {
            installApkDirect(context, cacheFile)
        } else {
            Toast.makeText(context, "Arquivo APK não encontrado. Baixe novamente.", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    private fun installApkDirect(context: Context, fileToInstall: File) {
        if (!fileToInstall.exists() || fileToInstall.length() < 500_000) {
            Log.e("UpdateManager", "Arquivo APK inválido ou incompleto: ${fileToInstall.length()} bytes")
            Toast.makeText(
                context,
                "O arquivo baixado está incompleto. Tente novamente.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    context,
                    "Por favor, permita a instalação de atualizações.",
                    Toast.LENGTH_LONG
                ).show()
                val permissionIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(permissionIntent)
                return
            }
        }

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                fileToInstall
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Erro ao abrir instalador: ${e.message}")
            Toast.makeText(
                context,
                "Erro ao abrir instalador: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
