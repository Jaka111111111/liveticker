package projekt.tf2liveticker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import projekt.tf2liveticker.helper_classes.NotificationHelper


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createMatchNotificationChannel(this)
        NotificationHelper.createDebugNotificationChannel(this)

        //openAppSettings()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )

            requestPermissions(
                arrayOf(android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),
                200
            ) //???
        }


        val mvm = MainViewModel()
        setContent {
            MatchScreen(mvm)
        }

    }



    fun openAppSettings() {
        val packageName = "projekt.tf2liveticker"
        val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}
