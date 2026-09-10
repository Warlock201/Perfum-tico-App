sed -i '/Box(/ {
N
/Box(\n\s*Box(/!b
s/Box(\n\s*Box(/Box(/
}' app/src/main/java/com/aistudio/perfumatico/ui/screens/PerfumeDetailDialog.kt
sed -i '/Box(/ {
N
/Box(\n\s*Box(/!b
s/Box(\n\s*Box(/Box(/
}' app/src/main/java/com/aistudio/perfumatico/ui/screens/CatalogScreen.kt
