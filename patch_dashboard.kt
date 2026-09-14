--- app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt
+++ app/src/main/java/com/aistudio/perfumatico/ui/screens/DashboardScreen.kt
@@ -19,6 +19,7 @@
 fun DashboardScreen(
     viewModel: PerfumeViewModel,
     modifier: Modifier = Modifier
 ) {
     val perfumes by viewModel.myPerfumes.collectAsState()
+    val todaySotd by viewModel.todaySotd.collectAsState()
 
@@ -53,6 +54,23 @@
             .testTag("dashboard_screen"),
         verticalArrangement = Arrangement.spacedBy(16.dp)
     ) {
+        if (todaySotd != null) {
+            val sotdPerfume = perfumes.find { it.id == todaySotd!!.perfumeId }
+            if (sotdPerfume != null) {
+                Text(
+                    text = "☀️ PERFUME DO DIA",
+                    color = Amber400,
+                    fontSize = 11.sp,
+                    fontWeight = FontWeight.Black,
+                    letterSpacing = 1.sp
+                )
+                com.aistudio.perfumatico.ui.components.PerfumeCard(
+                    perfume = sotdPerfume,
+                    onClick = { viewModel.openPerfumeDetails(sotdPerfume) },
+                    isTodaySotd = true,
+                    onStarClick = { viewModel.toggleSignature(sotdPerfume) }
+                )
+                Spacer(modifier = Modifier.height(4.dp))
+            }
+        }
+
         Text(
