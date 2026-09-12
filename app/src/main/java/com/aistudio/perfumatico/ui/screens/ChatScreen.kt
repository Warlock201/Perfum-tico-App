package com.aistudio.perfumatico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier.imePadding().imePadding()
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.imePadding()
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatHistory.isEmpty()) {
                item {
                    Box(modifier = Modifier.imePadding().fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Olá! Sou seu Sommelier de Perfumes. Como posso ajudar a encontrar a fragrância perfeita hoje?",
                            color = Slate400,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.imePadding().padding(32.dp)
                        )
                    }
                }
            }

            items(chatHistory) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.imePadding().fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier.imePadding()
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ))
                            .background(if (isUser) Amber400 else Slate800)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = msg.parts.firstOrNull()?.text ?: "",
                            color = if (isUser) Slate950 else Slate100,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.imePadding().fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier.imePadding()
                                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                .background(Slate800)
                                .padding(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.imePadding().size(20.dp),
                                color = Amber400,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.imePadding().height(12.dp))

        Row(
            modifier = Modifier.imePadding().fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.imePadding().weight(1f),
                placeholder = { Text("Descreva o que procura...", color = Slate500, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber400,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate100,
                    focusedContainerColor = Slate950,
                    unfocusedContainerColor = Slate950,
                    cursorColor = Amber400
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            Spacer(modifier = Modifier.imePadding().width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                containerColor = Amber400,
                contentColor = Slate950,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.imePadding().size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.imePadding().size(20.dp)
                )
            }
        }
    }
}
