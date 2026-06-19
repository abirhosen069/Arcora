package com.arcora

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.arcora.data.security.ArcOraActivityHolder
import com.arcora.presentation.ArcOraApp
import com.arcora.presentation.theme.ArcOraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcOraTheme {
                ArcOraApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ArcOraActivityHolder.setCurrentActivity(this)
    }

    override fun onPause() {
        ArcOraActivityHolder.clearCurrentActivity(this)
        super.onPause()
    }
}