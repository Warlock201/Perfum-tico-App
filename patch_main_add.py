import re

with open("app/src/main/java/com/aistudio/perfumatico/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.text.font.FontWeight", "import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.ui.platform.LocalContext\nimport android.widget.Toast\nimport com.aistudio.perfumatico.data.local.PerfumeEntity")

effect = """
    val context = LocalContext.current
    LaunchedEffect(chatViewModel) {
        chatViewModel.addPerfumeEvent.collect { (name, brand, status) ->
            val newPerfume = PerfumeEntity(
                id = "my_${System.currentTimeMillis()}",
                name = name,
                brand = brand,
                status = status,
                imageUrl = "",
                notes = "",
                family = ""
            )
            viewModel.savePerfume(newPerfume)
            Toast.makeText(context, "$name adicionado em $status!", Toast.LENGTH_SHORT).show()
        }
    }
"""

content = content.replace("val noUpdateAvailable by viewModel.noUpdateAvailable.collectAsState()", "val noUpdateAvailable by viewModel.noUpdateAvailable.collectAsState()\n" + effect)

with open("app/src/main/java/com/aistudio/perfumatico/MainActivity.kt", "w") as f:
    f.write(content)

