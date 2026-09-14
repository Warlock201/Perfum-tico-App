package com.aistudio.perfumatico.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.perfumatico.BuildConfig
import com.aistudio.perfumatico.data.local.ApiKeyManager
import com.aistudio.perfumatico.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ApiKeyConfigDialog(
    onDismiss: () -> Unit,
    onKeySaved: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val currentCustomKey = remember { ApiKeyManager.getCustomGeminiApiKey(context) ?: "" }
    var inputKey by remember { mutableStateOf(currentCustomKey) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val systemKey = remember { BuildConfig.GEMINI_API_KEY_NEW.ifBlank { BuildConfig.GEMINI_API_KEY }.trim() }
    val isSystemKeyInvalid = remember { systemKey.isNotBlank() && !ApiKeyManager.isKeyFormatLikelyValid(systemKey) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Slate800, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Amber400.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Chave Gemini",
                                tint = Amber400,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Chave Gemini AI",
                                color = Slate100,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sommelier & Oráculo Inteligente",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status info box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.dp, if (currentCustomKey.isNotBlank()) Emerald500.copy(alpha = 0.4f) else if (isSystemKeyInvalid) Rose500.copy(alpha = 0.4f) else Slate800, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (currentCustomKey.isNotBlank()) "Chave Personalizada Ativa" else "Chave do Sistema (Padrão)",
                            color = if (currentCustomKey.isNotBlank()) Emerald400 else if (isSystemKeyInvalid) Rose500 else Amber400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentCustomKey.isNotBlank()) {
                            Text(
                                text = "Utilizando sua chave personalizada salva neste aparelho.",
                                color = Slate300,
                                fontSize = 12.sp
                            )
                        } else if (isSystemKeyInvalid) {
                            Text(
                                text = "A chave padrão do sistema (${systemKey.take(8)}...) parece inválida ou ausente. Insira abaixo uma chave válida para ativar a IA.",
                                color = Slate300,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "Usando chave configurada nas variáveis de ambiente.",
                                color = Slate300,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Field
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        testResult = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chave de API Gemini") },
                    placeholder = { Text("Ex: AQ.Ab8... ou AIzaSy...", color = Slate600) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100,
                        focusedContainerColor = Slate950,
                        unfocusedContainerColor = Slate950,
                        cursorColor = Amber400
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (inputKey.isNotBlank()) {
                            IconButton(onClick = { inputKey = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar", tint = Slate400)
                            }
                        } else {
                            IconButton(onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    inputKey = text.trim()
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Colar", tint = Amber400)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Link helper to Google AI Studio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precisa de uma chave gratuita?",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // ignore
                            }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Obter no AI Studio ↗",
                            color = Amber400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Test result alert if present
                if (testResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isError) Rose500.copy(alpha = 0.15f) else Emerald500.copy(alpha = 0.15f))
                            .border(1.dp, if (isError) Rose500 else Emerald500, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isError) Rose500 else Emerald400,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = testResult ?: "",
                                color = if (isError) Slate100 else Emerald400,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test button
                    OutlinedButton(
                        onClick = {
                            val keyToTest = inputKey.ifBlank { systemKey }
                            if (keyToTest.isBlank()) {
                                testResult = "Informe uma chave para testar."
                                isError = true
                                return@OutlinedButton
                            }
                            isTesting = true
                            testResult = null
                            scope.launch {
                                val result = ApiKeyManager.testApiKey(keyToTest)
                                isTesting = false
                                result.fold(
                                    onSuccess = {
                                        testResult = it
                                        isError = false
                                    },
                                    onFailure = {
                                        testResult = it.message ?: "Erro desconhecido ao testar."
                                        isError = true
                                    }
                                )
                            }
                        },
                        enabled = !isTesting,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Slate950,
                            contentColor = Slate200
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Amber400,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Testar", fontSize = 13.sp)
                        }
                    }

                    // Save button
                    Button(
                        onClick = {
                            val trimmed = inputKey.trim()
                            ApiKeyManager.setCustomGeminiApiKey(context, if (trimmed.isBlank()) null else trimmed)
                            onKeySaved(trimmed)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber400,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Restore default button if custom key is currently set
                if (currentCustomKey.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = {
                            ApiKeyManager.setCustomGeminiApiKey(context, null)
                            inputKey = ""
                            testResult = null
                            onKeySaved("")
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Restaurar chave padrão",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
