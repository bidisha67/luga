package com.example.lugamandu.view


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.lugamandu.R
import com.example.lugamandu.ui.theme.White
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {
    val context= LocalContext.current
    val activity= context as Activity
    LaunchedEffect(Unit) {
        // Hold the splash screen for 3 seconds to show branding
        delay(3000)
        // Navigate to LoginActivity
        val intent= Intent(context,
            LoginActivity::class.java)
        context.startActivity(intent)
        // Remove SplashActivity from the backstack so the user can't navigate back to it
        activity.finish()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logoo),
                null,
                modifier= Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
            )
        }
    }
}