import re

with open('app/src/main/java/com/aistudio/perfumatico/MainActivity.kt', 'r') as f:
    code = f.read()

# Add import for WindowManager
if 'import android.view.WindowManager' not in code:
    code = code.replace('import android.os.Bundle', 'import android.os.Bundle\nimport android.view.WindowManager')

# Add FLAG_SECURE
old_on_create = '''    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()'''

new_on_create = '''    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // PROTEÇÃO CONTRA PRINTS E GRAVAÇÃO DE TELA (Segurança)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()'''

code = code.replace(old_on_create, new_on_create)

with open('app/src/main/java/com/aistudio/perfumatico/MainActivity.kt', 'w') as f:
    f.write(code)
