import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "r") as f:
    content = f.read()

# Replace the isNew logic
old_is_new = """
                        val isNew = (System.currentTimeMillis() - perfume.createdAt) < 14L * 24 * 60 * 60 * 1000
"""
new_is_new = """
                        val isNew = perfume.markedNewAt > 0L && (System.currentTimeMillis() - perfume.markedNewAt) < 14L * 24 * 60 * 60 * 1000
"""
content = content.replace(old_is_new, new_is_new)


# Insert Litragem below Name. It is inside the column.
# Wait, let's find the family/Insp row:
# Row(
#    verticalAlignment = Alignment.CenterVertically,
#    horizontalArrangement = Arrangement.spacedBy(6.dp)
# ) {
#    Surface(
#        color = Slate800,

old_family_row = """
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
"""

new_family_row = """
                    val typeText = if (perfume.status == "Decant") "Decant" else "Frasco"
                    val volumeText = try {
                        val arr = org.json.JSONArray(perfume.bottlesJson)
                        val first = arr.get(0)
                        if (first is org.json.JSONObject) first.getString("size") else first.toString()
                    } catch (e: Exception) {
                        if (perfume.bottlesJson.contains("50ml")) "50ml" else "100ml"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$typeText • $volumeText",
                                color = Slate200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        Surface(
"""
content = content.replace(old_family_row.strip(), new_family_row.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "w") as f:
    f.write(content)
