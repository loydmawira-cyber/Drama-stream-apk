package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.DramaNavGraph
import com.example.ui.theme.DramaStreamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DramaStreamTheme {
                val navController = rememberNavController()
                DramaNavGraph(navController = navController)
            }
        }
    }
}
