import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

# Replace items with itemsIndexed
content = content.replace("items(chatHistory) { msg ->", "itemsIndexed(chatHistory) { index, msg ->")

old_bubble = """
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
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
"""

new_bubble = """
                val rawText = msg.parts.firstOrNull()?.text ?: ""
                val askRegex = Regex("\\[ASK_COLLECTION:\\s*(.+?)\\s*\\|\\s*(.+?)\\s*\\]")
                val match = askRegex.find(rawText)
                val cleanText = rawText.replace(askRegex, "").trim()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
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
                        Column {
                            Text(
                                text = cleanText,
                                color = if (isUser) Slate950 else Slate100,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            
                            if (match != null && !isUser) {
                                val perfumeName = match.groupValues[1].trim()
                                val perfumeBrand = match.groupValues[2].trim()
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Onde deseja guardar '$perfumeName'?", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.addPerfumeFromUI(perfumeName, perfumeBrand, "Quero ter", index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Quero ter", fontWeight = FontWeight.Bold) }
                                    
                                    Button(
                                        onClick = { viewModel.addPerfumeFromUI(perfumeName, perfumeBrand, "Já possuo", index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate700, contentColor = Slate100),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Já possuo", fontWeight = FontWeight.Bold) }
                                    
                                    Button(
                                        onClick = { viewModel.addPerfumeFromUI(perfumeName, perfumeBrand, "Pipeline", index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate700, contentColor = Slate100),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Pipeline (A caminho)", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
"""

content = content.replace(old_bubble, new_bubble)

# Also add import for itemsIndexed if missing
if "import androidx.compose.foundation.lazy.itemsIndexed" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.items", "import androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.lazy.itemsIndexed")

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
