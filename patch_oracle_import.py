import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/OracleScreen.kt", "r") as f:
    content = f.read()

old_imports = """
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
"""

new_imports = """
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import android.location.Location
"""
content = content.replace(old_imports.strip(), new_imports.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/screens/OracleScreen.kt", "w") as f:
    f.write(content)
