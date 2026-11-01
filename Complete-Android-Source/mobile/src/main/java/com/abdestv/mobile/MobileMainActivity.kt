package com.abdestv.mobile

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdestv.core.ui.theme.AbdesTvTheme
import com.abdestv.core.viewmodel.SharedIptvViewModel
import com.abdestv.mobile.ui.MobileAppRoot

class MobileMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            AbdesTvTheme {
                val iptvViewModel: SharedIptvViewModel = viewModel()
                MobileAppRoot(viewModel = iptvViewModel)
            }
        }
    }
}
