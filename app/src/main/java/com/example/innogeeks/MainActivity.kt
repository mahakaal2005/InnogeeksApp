package com.example.innogeeks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.innogeeks.feature_onboarding.presentation.login.LoginRoot
import com.example.innogeeks.ui.theme.InnogeeksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InnogeeksTheme {
                // TEMPORARY: launch straight into Login so we can see it run. Replaced by a
                // proper nav graph later (splash -> intro -> auth). Empty nav callbacks for now.
                LoginRoot(
                    onNavigateToHome = {},
                    onNavigateToSignUp = {}
                )
            }
        }
    }
}

