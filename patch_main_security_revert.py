import re

with open('app/src/main/java/com/aistudio/perfumatico/MainActivity.kt', 'r') as f:
    code = f.read()

old_code = '''    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // PROTEÇÃO CONTRA PRINTS E GRAVAÇÃO DE TELA (Segurança)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()'''

new_code = '''    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // PROTEÇÃO CONTRA PRINTS (Ativado apenas em Release para não quebrar o emulador de preview)
        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        enableEdgeToEdge()'''

code = code.replace(old_code, new_code)

with open('app/src/main/java/com/aistudio/perfumatico/MainActivity.kt', 'w') as f:
    f.write(code)
