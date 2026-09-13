import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

old_buttons = """
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
"""

new_buttons = """
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.addPerfumeFromUI(perfumeName, perfumeBrand, "Já possuo", index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Já possuo (ou A Caminho)", fontWeight = FontWeight.Bold) }
                                    
                                    Button(
                                        onClick = { viewModel.addPerfumeFromUI(perfumeName, perfumeBrand, "Quero ter", index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate700, contentColor = Slate100),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Quero ter (Próximos)", fontWeight = FontWeight.Bold) }
                                    
                                    Button(
                                        onClick = { viewModel.addPerfumeFromUI(perfumeName, perfumeBrand, "Sonhos Distantes", index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate700, contentColor = Slate100),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Sonho Distante", fontWeight = FontWeight.Bold) }
                                }
"""

content = content.replace(old_buttons, new_buttons)

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
