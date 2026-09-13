import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "r") as f:
    content = f.read()

# Add the Switch UI before the Save Row
old_action_buttons = """
                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Usar Hoje & Salvar
"""

new_action_buttons = """
                Spacer(modifier = Modifier.height(14.dp))

                val isCurrentlyNew = perfume.markedNewAt > 0L && (System.currentTimeMillis() - perfume.markedNewAt) < 14L * 24 * 60 * 60 * 1000
                var markedNewToggle by remember { mutableStateOf(isCurrentlyNew) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Exibir etiqueta NOVO (14 dias)", color = Slate300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = markedNewToggle,
                        onCheckedChange = { markedNewToggle = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald400, checkedTrackColor = Emerald400.copy(alpha=0.3f), uncheckedThumbColor = Slate400, uncheckedTrackColor = Slate700)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Usar Hoje & Salvar
"""
content = content.replace(old_action_buttons.strip(), new_action_buttons.strip())

# Add logic for saving markedNewAt
old_save = """
                            bottlesJson = "[{\\"size\\":\\"${bottleSize.trim()}\\", \\"level\\":${bottleLevel.toInt()}}]"
                        )
"""

new_save = """
                            bottlesJson = "[{\\"size\\":\\"${bottleSize.trim()}\\", \\"level\\":${bottleLevel.toInt()}}]",
                            markedNewAt = if (markedNewToggle && !isCurrentlyNew) System.currentTimeMillis() else if (markedNewToggle) perfume.markedNewAt else 0L
                        )
"""
content = content.replace(old_save.strip(), new_save.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt", "w") as f:
    f.write(content)
