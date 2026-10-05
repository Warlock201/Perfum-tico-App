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

    fun downloadAndInstallUpdate(apkUrl: String, fileName: String = "perfumatico-v4.6.1.apk") {
        val publicDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetFile = File(publicDownloadDir, fileName)

        try {
            // Remove qualquer versão antiga para evitar conflitos de cache ou arquivo incompleto
            if (targetFile.exists()) {
                targetFile.delete()
            }
        } catch (e: Exception) {
            Log.w("UpdateManager", "Não foi possível remover arquivo anterior: ${e.message}")
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Perfumático - Atualização")
            .setDescription("Baixando atualização para a pasta Downloads...")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.widget.Toast.makeText(
                context,
                "Baixando atualização para a sua pasta Downloads...",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val localUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                            cursor.close()
                            installApkFromUri(c, localUriStr, targetFile)
                        } else {
                            val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            cursor.close()
                            Log.e("UpdateManager", "Download falhou com código: $reason")
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                android.widget.Toast.makeText(
                                    c,
                                    "Falha no download da atualização. Verifique a internet e tente novamente.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        cursor?.close()
                    }

                    try {
                        c.unregisterReceiver(this)
                    } catch (e: Exception) {
                        // Receiver já desregistrado
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApkFromUri(context: Context, localUriStr: String?, fallbackFile: File) {
        val fileToInstall: File = if (!localUriStr.isNullOrEmpty()) {
            val parsedUri = Uri.parse(localUriStr)
            if (parsedUri.scheme == "file") {
                File(parsedUri.path ?: "")
            } else {
                fallbackFile
            }
        } else {
            fallbackFile
        }

        if (!fileToInstall.exists() || fileToInstall.length() < 500_000) {
            Log.e("UpdateManager", "Arquivo APK inválido ou incompleto: ${fileToInstall.absolutePath}, bytes: ${fileToInstall.length()}")
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context,
                    "O download ficou incompleto. Tente novamente.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.widget.Toast.makeText(
                context,
                "Download concluído! Arquivo salvo na pasta Downloads.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // No Android 8.0+ (Oreo), verifica permissão para instalar fontes desconhecidas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        context,
                        "Por favor, autorize o Perfumático a instalar atualizações.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
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
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            }

            val resolveInfoList = context.packageManager.queryIntentActivities(
                installIntent,
                android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resolveInfoList) {
                context.grantUriPermission(
                    resolveInfo.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Erro ao abrir instalador: ${e.message}")
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context,
                    "Atualização salva em Downloads! Abra o arquivo manualmente para instalar.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
