import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    code = f.read()

# Replace <application> tag
if 'android:networkSecurityConfig' not in code:
    code = code.replace('<application', '<application\n        android:networkSecurityConfig="@xml/network_security_config"')

# Add permissions
if 'USE_BIOMETRIC' not in code:
    perms = '''    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.USE_FINGERPRINT" />'''
    code = code.replace('<uses-permission android:name="android.permission.INTERNET" />', perms)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(code)
