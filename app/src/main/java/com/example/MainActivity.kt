package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.GPACalculatorRepository
import com.example.ui.GPACalculatorApp
import com.example.ui.GPAViewModel
import com.example.ui.GPAViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup SQLite Room Database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = GPACalculatorRepository(database)
        
        // Instantiate GPAViewModel with factory
        val viewModel = ViewModelProvider(
            this,
            GPAViewModelFactory(repository)
        )[GPAViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    GPACalculatorApp(viewModel = viewModel)
                }
            }
        }
    }
}

