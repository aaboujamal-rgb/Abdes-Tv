package com.abdestv.tv

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdestv.core.ui.theme.AbdesTvTheme
import com.abdestv.core.viewmodel.SharedIptvViewModel
import com.abdestv.tv.ui.TvAppRoot

class TvMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            AbdesTvTheme {
                val iptvViewModel: SharedIptvViewModel = viewModel()
                TvAppRoot(viewModel = iptvViewModel)
            }
        }
    }
}
