import re

with open(".github/workflows/build_apk.yml", "r") as f:
    content = f.read()

# In the workflow, we need to create a dummy google-services.json to prevent the Google Services plugin from crashing
dummy_json_step = """
    - name: Create dummy google-services.json
      run: |
        cat << 'DUMMY' > app/google-services.json
        {
          "project_info": { "project_number": "123", "project_id": "dummy" },
          "client": [ {
            "client_info": { "mobilesdk_app_id": "1:123:android:abc", "android_client_info": { "package_name": "com.aistudio.perfumatico.kqpv" } },
            "api_key": [ { "current_key": "dummy" } ]
          } ]
        }
        DUMMY
"""

content = content.replace("    - name: Create .env properties file", dummy_json_step.lstrip("\n") + "    - name: Create .env properties file")

with open(".github/workflows/build_apk.yml", "w") as f:
    f.write(content)

print("Fixed GitHub Action google-services.json generation")
