import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

# Add permissions
if "ACCESS_COARSE_LOCATION" not in content:
    old_perm = '<uses-permission android:name="android.permission.INTERNET" />'
    new_perm = '''<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />'''
    content = content.replace(old_perm, new_perm)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
