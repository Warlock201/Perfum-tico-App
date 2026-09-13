import re

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "r") as f:
    content = f.read()

# Add a "Novo" sticker
old_name_row = """
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = perfume.name,
                            color = Slate100,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
"""

new_name_row = """
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = perfume.name,
                            color = Slate100,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        val isNew = (System.currentTimeMillis() - perfume.createdAt) < 14L * 24 * 60 * 60 * 1000
                        if (isNew) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Emerald500.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500)
                            ) {
                                Text(
                                    text = "NOVO",
                                    color = Emerald400,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
"""
content = content.replace(old_name_row.strip(), new_name_row.strip())

with open("app/src/main/java/com/aistudio/perfumatico/ui/components/CommonComponents.kt", "w") as f:
    f.write(content)
