package com.example.testing1

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testing1.screen.AuthScreen
import com.example.testing1.screen.HomeScreen
import com.example.testing1.screen.LoginScreen
import com.example.testing1.screen.SignupScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    val isLoggedIn = Firebase.auth.currentUser!=null
    val firstPage = if (isLoggedIn) "home" else "auth"

    NavHost(navController = navController,startDestination = firstPage) {

        composable(route = "auth")        {
            AuthScreen(modifier, navController)
        }

        composable(route = "login")        {
            LoginScreen(modifier,navController)
        }

        composable(route = "signup")        {
            SignupScreen(modifier,navController)
        }

        composable(route = "home")        {
            HomeScreen(modifier,navController)
        }
    }
}