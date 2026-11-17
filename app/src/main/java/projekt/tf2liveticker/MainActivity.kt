package projekt.tf2liveticker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createMatchNotificationChannel(this)
        NotificationHelper.createDebugNotificationChannel(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }


        val mvm = MainViewModel()
        setContent {
            val context = LocalContext.current
            mvm.getAppStats(context)
            mvm.getMatchesFromEtf2l(context)
            MatchScreen(mvm)
        }

    }



    suspend fun updateMatchData(matchDataList: MatchDataList) {
        matchDataStore.updateData { matchDataList }
    }
}
