package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SscPrepMainApp
import com.example.ui.SscViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mandatory edge to edge handling for immersive layout padding
        enableEdgeToEdge()
        
        setContent {
            val sscViewModel: SscViewModel = viewModel()
            SscPrepMainApp(viewModel = sscViewModel)
        }
    }
}
