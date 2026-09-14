@file:Suppress("DEPRECATION")
package com.aistudio.perfumatico.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.util.Log
import com.aistudio.perfumatico.R
import com.aistudio.perfumatico.ui.components.ApiKeyConfigDialog
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSotdDialog(
    viewModel: PerfumeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val sotdList by viewModel.sotdHistory.collectAsState()
    
    val currentUser by viewModel.currentUser.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val isAdmin = viewModel.isAdmin

    var displayName by remember(profile) { mutableStateOf(profile?.displayName ?: "Colecionador") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "Apaixonado por alta perfumaria") }
    var signature by remember(profile) { mutableStateOf(profile?.signaturePerfumeName ?: "") }

    var activeTab by remember { mutableStateOf(0) } // 0: Perfil, 1: SOTD, 2: Nuvem
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val sha1Fingerprint = "39:24:B3:A6:91:F4:A5:87:39:01:99:60:92:F1:C8:F5:55:E0:60:4A"
    var showFirebaseHelp by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    isLoading = true
                    viewModel.signInWithGoogleIdToken(idToken) { success, error ->
                        isLoading = false
                        if (!success) {
                            errorMessage = error ?: "Falha ao autenticar com Google no Firebase."
                        }
                    }
                } else {
                    errorMessage = "Não foi possível obter a credencial do Google (idToken nulo)."
                }
            } catch (e: ApiException) {
                isLoading = false
                val code = e.statusCode
                Log.e("GoogleSignIn", "Falha de autenticação Google. Código: $code", e)
                when (code) {
                    10 -> {
                        showFirebaseHelp = true
                        errorMessage = "Erro 10 (DEVELOPER_ERROR): A impressão digital SHA-1 deste APK ou o pacote '${context.packageName}' precisa ser cadastrado no Firebase Console."
                    }
                    12500 -> {
                        errorMessage = "Erro 12500 (SIGN_IN_FAILED): Verifique se o provedor Google está ativado em 'Firebase Console > Authentication > Sign-in method'."
                    }
                    12501 -> {
                        // Usuário fechou ou cancelou o seletor de contas
                    }
                    7 -> {
                        errorMessage = "Erro 7 (NETWORK_ERROR): Sem conexão com a internet. Verifique sua rede."
                    }
                    else -> {
                        errorMessage = "Google Sign-In retornou o código $code. Verifique o Google Play Services e a conta selecionada."
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Erro no Google Sign-In: ${e.localizedMessage}"
            }
        } else {
            isLoading = false
            if (result.resultCode != Activity.RESULT_OK) {
                errorMessage = "Seleção de conta Google cancelada ou indisponível."
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("profile_sotd_dialog"),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERFIL & CONFIGURAÇÕES",
                        color = Amber400,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = Slate300, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Perfil / SOTD Histórico / Conta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { activeTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 0) Amber400 else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (activeTab == 0) Slate950 else Slate400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("PERFIL", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { activeTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 1) Emerald400 else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (activeTab == 1) Slate950 else Slate400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("SOTD", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    
                    Button(
                        onClick = { activeTab = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 2) Rose500 else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (activeTab == 2) Slate950 else Slate400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("CONTA", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeTab == 0) {
                    // Profile Form
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Nome de Exibição") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber400,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100,
                                focusedContainerColor = Slate950,
                                unfocusedContainerColor = Slate950
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio / Sobre Você") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber400,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100,
                                focusedContainerColor = Slate950,
                                unfocusedContainerColor = Slate950
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = signature,
                            onValueChange = { signature = it },
                            label = { Text("Perfume Assinatura") },
                            placeholder = { Text("Ex: Bleu de Chanel, Aventus...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber400,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100,
                                focusedContainerColor = Slate950,
                                unfocusedContainerColor = Slate950
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedButton(
                            onClick = { showApiKeyDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Slate950, contentColor = Amber400),
                            border = BorderStroke(1.dp, Slate800),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp), tint = Amber400)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONFIGURAR CHAVE GEMINI IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                viewModel.saveUserProfile(displayName.trim(), bio.trim(), signature.trim())
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("SALVAR PERFIL", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                } else if (activeTab == 1) {
                    // SOTD History
                    if (sotdList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum perfume do dia registrado ainda.\nClique em 'Usar Hoje' no card de um perfume!", color = Slate500, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(sotdList, key = { it.id }) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Slate950),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.perfumeBrand.uppercase(), color = Amber400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                            Text(item.perfumeName, color = Slate100, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("Usado em: ${item.date}", color = Emerald400, fontSize = 11.sp)
                                            if (item.comment.isNotBlank()) {
                                                Text(item.comment, color = Slate400, fontSize = 11.sp)
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteSotd(item.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remover", tint = Rose500, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Cloud Account & Sync
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (currentUser != null) {
                            // Logged In state
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Amber400.copy(alpha = 0.2f))
                                    .border(2.dp, Amber400, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Amber400,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
        
                            Spacer(modifier = Modifier.height(10.dp))
        
                            Text(
                                text = currentUser?.displayName ?: "Usuário Conectado",
                                color = Slate100,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
        
                            Text(
                                text = currentUser?.email ?: "",
                                color = Slate400,
                                fontSize = 12.sp
                            )
        
                            Spacer(modifier = Modifier.height(8.dp))
        
                            if (isAdmin) {
                                Surface(
                                    color = Amber500.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber400)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Amber400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "ADMINISTRADOR (ACESSO TOTAL)",
                                            color = Amber400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
        
                            Surface(
                                color = Slate950,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Emerald400)
                                    )
                                    Text(
                                        text = "Status: $syncStatus",
                                        color = Emerald400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
        
                            Spacer(modifier = Modifier.height(18.dp))
        
                            OutlinedButton(
                                onClick = { viewModel.signOut() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DESCONECTAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            // Not Logged In State
                            Text(
                                text = "Conecte sua conta para sincronizar seu acervo com o banco de dados 'gestor-de-perfumes' do Firebase em tempo real.",
                                color = Slate400,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
        
                            Spacer(modifier = Modifier.height(16.dp))
        
                            // Google Sign-In Button
                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    val webClientId = try {
                                        context.getString(R.string.default_web_client_id)
                                    } catch (e: Exception) {
                                        // Fallback if not injected by google-services properly
                                        "784506326280-i4dfu7es4v9575jdt13fa6upcubh5spe.apps.googleusercontent.com"
                                    }
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(webClientId)
                                        .requestEmail()
                                        .build()
                                    val client = GoogleSignIn.getClient(context, gso)
                                    // Limpa qualquer sessão travada anterior antes de abrir
                                    client.signOut().addOnCompleteListener {
                                        isLoading = false
                                        try {
                                            googleSignInLauncher.launch(client.signInIntent)
                                        } catch (e: Exception) {
                                            errorMessage = "Não foi possível abrir o Google Sign-In: ${e.localizedMessage}"
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Slate100,
                                    contentColor = Slate950
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("google_signin_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Slate950, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Slate950,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ENTRAR COM O GOOGLE",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Slate800)
                                Text("OU POR E-MAIL", color = Slate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Slate800)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-mail") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Amber400,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = Slate100,
                                    unfocusedTextColor = Slate100,
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Senha") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Amber400,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = Slate100,
                                    unfocusedTextColor = Slate100,
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Rose500.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = errorMessage!!,
                                        color = Rose500,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Helper button to show SHA-1 & Firebase config info
                            TextButton(
                                onClick = { showFirebaseHelp = !showFirebaseHelp },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(
                                    imageVector = if (showFirebaseHelp) Icons.Default.KeyboardArrowUp else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Amber400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showFirebaseHelp) "Ocultar dados do Firebase" else "Configuração Firebase (SHA-1 / Pacote)",
                                    color = Amber400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (showFirebaseHelp) {
                                Surface(
                                    color = Slate950,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Para o Google Sign-In funcionar, o Firebase exige:",
                                            color = Slate300,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Pacote: ${context.packageName}",
                                            color = Slate400,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "SHA-1: $sha1Fingerprint",
                                            color = Amber400,
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(sha1Fingerprint))
                                                errorMessage = "SHA-1 copiado para a área de transferência!"
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Amber400,
                                                contentColor = Slate950
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Slate950,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "COPIAR SHA-1 DO APP",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
        
                            Spacer(modifier = Modifier.height(14.dp))
        
                            Button(
                                onClick = {
                                    if (email.isBlank() || password.isBlank()) {
                                        errorMessage = "Preencha o e-mail e a senha."
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.signInWithEmail(email.trim(), password.trim()) { success, err ->
                                        isLoading = false
                                        if (!success) {
                                            errorMessage = err ?: "Erro ao autenticar."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("auth_login_button"),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Slate950, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("ENTRAR / CADASTRAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                val versionName = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    "Desconhecida"
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Versão: $versionName",
                        color = Slate500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        onClick = { viewModel.checkUpdatesManually() },
                        enabled = !isCheckingUpdate
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(color = Amber400, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando...", color = Slate400, fontSize = 11.sp)
                        } else {
                            Text("Buscar Atualizações", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeyConfigDialog(onDismiss = { showApiKeyDialog = false })
    }
}
