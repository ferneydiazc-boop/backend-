package com.acordes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.acordes.app.data.remote.ApiClient
import com.acordes.app.ui.navigation.AppNavigation
import com.acordes.app.ui.theme.AcordesTheme
import com.acordes.app.ui.theme.Fondo

/** Punto de entrada: solo inicializa la configuración y muestra la navegación. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.iniciar(getSharedPreferences("acordes", MODE_PRIVATE))
        setContent { AcordesTheme { Surface(Modifier.fillMaxSize(), color = Fondo) { AppNavigation() } } }
    }
}
