import re

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'r') as f:
    code = f.read()

imports = '''import androidx.compose.material.icons.filled.AutoAwesome
import org.json.JSONObject
import kotlinx.coroutines.launch'''

code = code.replace('import java.util.UUID', 'import java.util.UUID\n' + imports)

# Add states
state_old = 'var status by remember { mutableStateOf(perfumeToEdit?.status ?: "Já possuo") }'
state_new = 'var status by remember { mutableStateOf(perfumeToEdit?.status ?: "Já possuo") }\n    var isAutoFilling by remember { mutableStateOf(false) }\n    val coroutineScope = rememberCoroutineScope()'
code = code.replace(state_old, state_new)

# Replace the OutlinedTextField for Name
name_field_old = '''                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Perfume *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_perfume_name"),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )'''

name_field_new = '''                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome do Perfume *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_perfume_name"),
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        IconButton(
                            onClick = {
                                if (name.isNotBlank()) {
                                    isAutoFilling = true
                                    viewModel.autoFillPerfume(name) { jsonResult ->
                                        isAutoFilling = false
                                        try {
                                            val json = JSONObject(jsonResult)
                                            if (json.has("brand")) brand = json.getString("brand")
                                            if (json.has("family")) family = json.getString("family")
                                            if (json.has("topNotes")) topNotes = json.getString("topNotes")
                                            if (json.has("heartNotes")) heartNotes = json.getString("heartNotes")
                                            if (json.has("baseNotes")) baseNotes = json.getString("baseNotes")
                                            if (json.has("fixation")) fixation = json.getInt("fixation")
                                            if (json.has("projection")) projection = json.getInt("projection")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Amber400, RoundedCornerShape(12.dp))
                        ) {
                            if (isAutoFilling) {
                                CircularProgressIndicator(color = Slate950, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Auto Preencher", tint = Slate950)
                            }
                        }
                    }'''

code = code.replace(name_field_old, name_field_new)

with open('app/src/main/java/com/aistudio/perfumatico/ui/screens/AddEditPerfumeDialog.kt', 'w') as f:
    f.write(code)
