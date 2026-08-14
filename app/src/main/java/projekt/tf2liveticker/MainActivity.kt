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
import android.util.Log
import java.net.InetAddress

fun testDns() {
    Thread {
        try {
            val addresses =
                java.net.InetAddress.getAllByName("api-v2.etf2l.org")

            for (address in addresses) {

                try {
                    val socket = java.net.Socket()

                    socket.connect(
                        java.net.InetSocketAddress(address, 443),
                        5000
                    )

                    android.util.Log.d(
                        "CONNECTION_TEST",
                        "ERFOLG: ${address.hostAddress}"
                    )

                    socket.close()

                } catch (e: Exception) {
                    Log.e("CONNECTION_TEST", "DNS Fehler: ${e.message}", e)
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("CONNECTION_TEST", "DNS Fehler", e)
        }
    }.start()
}
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        testDns()

        setContent {
            MatchScreen()
        }

    }
}



