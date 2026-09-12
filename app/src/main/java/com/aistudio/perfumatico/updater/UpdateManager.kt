package com.aistudio.perfumatico.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.aistudio.perfumatico.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val releaseNotes: String,
    val apkUrl: String
)

class UpdateManager(private val context: Context) {

    // IMPORTANTE: Aqui vai a URL do seu JSON na nuvem!
    // Você pode usar o GitHub (raw user content), Firebase Storage, Google Drive, ou seu próprio site.
    private val updateCheckUrl = "https://raw.githubusercontent.com/Warlock201/Perfum-tico-App/main/releases/update.json"

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(updateCheckUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
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
        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Atualizando Perfumático")
            .setDescription("Baixando a nova versão...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(context, fileName)
                    context.unregisterReceiver(this)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(context: Context, fileName: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        }
    }
}
