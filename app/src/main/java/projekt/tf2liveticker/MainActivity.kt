package projekt.tf2liveticker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import projekt.tf2liveticker.RetrofitInstance.api

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MatchScreen()
        }

//        lifecycleScope.launch {
//            try {
//                val match = api.getMatches(1)
//
//                textView.text = match
//
//            } catch (e: Exception) {
//                textView.text = "Fehler: ${e.message}"
//            }
//        }
    }
}



