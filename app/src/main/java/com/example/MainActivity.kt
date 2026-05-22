package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SscPrepMainApp
import com.example.ui.SscViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mandatory edge to edge handling for immersive layout padding
        enableEdgeToEdge()
        
        setContent {
            val application = applicationContext as Application
            val sscViewModel: SscViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SscViewModel(application) as T
                    }
                }
            )
            SscPrepMainApp(viewModel = sscViewModel)
        }
    }
}
