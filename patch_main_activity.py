import re

with open("app/src/main/java/com/aistudio/perfumatico/MainActivity.kt", "r") as f:
    content = f.read()

old_switch = """
            when (currentTab) {
                MainTab.COLLECTION -> MyPerfumesScreen(viewModel = viewModel)
                MainTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                MainTab.DISCOVER -> DiscoverScreen(viewModel = viewModel)
                MainTab.CATALOG -> CatalogScreen(viewModel = viewModel)
                MainTab.CHATBOT -> ChatScreen(viewModel = chatViewModel)
            }
"""

new_switch = """
            when (currentTab) {
                MainTab.COLLECTION -> MyPerfumesScreen(viewModel = viewModel)
                MainTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                MainTab.DISCOVER -> DiscoverScreen(viewModel = viewModel)
                MainTab.ORACLE -> com.aistudio.perfumatico.ui.screens.OracleScreen(viewModel = viewModel)
                MainTab.CATALOG -> CatalogScreen(viewModel = viewModel)
                MainTab.CHATBOT -> ChatScreen(viewModel = chatViewModel)
            }
"""

content = content.replace(old_switch.strip(), new_switch.strip())

with open("app/src/main/java/com/aistudio/perfumatico/MainActivity.kt", "w") as f:
    f.write(content)
