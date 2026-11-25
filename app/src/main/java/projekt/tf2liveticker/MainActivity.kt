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
        NotificationHelper.createForegroundNotificationChannel(this)

        //openAppSettings()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC,
                    android.Manifest.permission.SCHEDULE_EXACT_ALARM),
                100,
            )
        }


        val mvm = MainViewModel()
        mvm.getAppStats(applicationContext);
        mvm.getMatchesFromEtf2l(applicationContext);
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
