package com.example.testing1.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Addchart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.testing1.pages.AddPage
import com.example.testing1.pages.GoalsPage
import com.example.testing1.pages.HistoryPage
import com.example.testing1.pages.HomePage
import com.example.testing1.pages.TotalsPage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private val BgDark    = Color(0xFF121212)
private val Primary   = Color(0xFF6C63FF)
private val TextColor = Color(0xFFFFFFFF)

data class NavItem(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {

    val navItemList = listOf(
        NavItem("Home",    Icons.Default.Home),
        NavItem("Add",     Icons.Default.Add),
        NavItem("Totals",  Icons.Default.Addchart),
        NavItem("Goals",   Icons.Default.AttachMoney),
        NavItem("History", Icons.Default.History),
    )

    var selectedIndex    by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Logout confirmation dialog ─────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Log out?", fontWeight = FontWeight.Bold) },
            text    = { Text("You will be returned to the start screen.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    Firebase.auth.signOut()
                    navController.navigate("auth") {
                        // Clear the entire back stack so Back doesn't return to home
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Log out", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        // ── Top bar with page title + logout button ────────────────────────
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedIndex) {
                            0 -> "Penny Wise"
                            1 -> "Add Expense"
                            2 -> "Spending Totals"
                            3 -> "Goals & Targets"
                            4 -> "History"
                            else -> "Penny Wise"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColor
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log out",
                            tint = TextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E1E1E)) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected  = index == selectedIndex,
                        onClick   = { selectedIndex = index },
                        icon      = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label,
                                tint = if (index == selectedIndex) Primary
                                else Color(0xFF888888)
                            )
                        },
                        label = {
                            Text(
                                navItem.label,
                                color = if (index == selectedIndex) Primary
                                else Color(0xFF888888),
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(modifier = modifier.padding(innerPadding), selectedIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int) {
    when (selectedIndex) {
        0 -> HomePage(modifier)
        1 -> AddPage(modifier)
        2 -> TotalsPage(modifier)
        3 -> GoalsPage(modifier)
        4 -> HistoryPage(modifier)
    }
}