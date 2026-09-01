package projekt.tf2liveticker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.dataStore
import projekt.tf2liveticker.Models.MatchDataList

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        setContent {
            MatchScreen()
        }

    }



    suspend fun updateMatchData(matchDataList: MatchDataList) {
        dataStore.updateData { matchDataList }
    }
}


val Context.dataStore by dataStore("MatchData.json", MatchDataSerializer)
