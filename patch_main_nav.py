import re

with open("app/src/main/java/com/aistudio/perfumatico/MainActivity.kt", "r") as f:
    content = f.read()

# 1. Add ORACLE to AppTab
old_tabs = """
enum class AppTab {
    COLLECTION,
    DISCOVER,
    CHAT
}
"""

new_tabs = """
enum class AppTab {
    COLLECTION,
    DISCOVER,
    ORACLE,
    CHAT
}
"""
content = content.replace(old_tabs.strip(), new_tabs.strip())

# 2. Add OracleScreen composable call
old_screen_switch = """
                        when (currentTab) {
                            AppTab.COLLECTION -> MyPerfumesScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                            AppTab.DISCOVER -> DiscoverScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                            AppTab.CHAT -> ChatScreen(viewModel = chatViewModel, modifier = Modifier.padding(innerPadding))
                        }
"""

new_screen_switch = """
                        when (currentTab) {
                            AppTab.COLLECTION -> MyPerfumesScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                            AppTab.DISCOVER -> DiscoverScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                            AppTab.ORACLE -> com.aistudio.perfumatico.ui.screens.OracleScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                            AppTab.CHAT -> ChatScreen(viewModel = chatViewModel, modifier = Modifier.padding(innerPadding))
                        }
"""
content = content.replace(old_screen_switch.strip(), new_screen_switch.strip())


# 3. Add Oracle icon to BottomNavBar
old_nav_items = """
                            NavigationBarItem(
                                selected = currentTab == AppTab.DISCOVER,
                                onClick = { currentTab = AppTab.DISCOVER },
                                icon = { Icon(Icons.Default.Explore, contentDescription = "Descubra") },
                                label = { Text("Descubra") },
"""

new_nav_items = """
                            NavigationBarItem(
                                selected = currentTab == AppTab.DISCOVER,
                                onClick = { currentTab = AppTab.DISCOVER },
                                icon = { Icon(Icons.Default.Explore, contentDescription = "Descubra") },
                                label = { Text("Descubra") },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Amber400, selectedTextColor = Amber400, indicatorColor = Slate800, unselectedIconColor = Slate400, unselectedTextColor = Slate500)
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.ORACLE,
                                onClick = { currentTab = AppTab.ORACLE },
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, contentDescription = "Oráculo") },
                                label = { Text("Oráculo") },
"""
content = content.replace(old_nav_items.strip(), new_nav_items.strip())

with open("app/src/main/java/com/aistudio/perfumatico/MainActivity.kt", "w") as f:
    f.write(content)
