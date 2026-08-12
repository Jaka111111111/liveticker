package de.hsfl.SchevenAlbert.LeonJakaApp

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun ChatMessage(author: String, text: String, isMyMessage: Boolean,
                isImage: Boolean, isLocation: Boolean = false, timestamp: String) {

    val context = LocalContext.current
    val color = when{
        isLocation -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        isMyMessage -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val itemContentColor = if (isMyMessage) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onTertiaryContainer
    val align = if (isMyMessage) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = align
    ) {
        Row(
            modifier = Modifier
                .background(color, RoundedCornerShape(12.dp))
                .clickable(enabled = isLocation) {
                    val cleanCoordinates = text.replace("Standort:", "", ignoreCase = true).trim()
                    val gmmIntentUri = "geo:0,0?q=${Uri.encode(cleanCoordinates)}".toUri()
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    context.startActivity(mapIntent)
                }
                .padding(10.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLocation) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Standort",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp)
                )}
            if (!isMyMessage) {
                Icon(Icons.Default.Person, contentDescription = null)
            }

            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(text = author, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium)
                //Text(text = text)

                if (isImage) {
                    AsyncImage(
                        model = text,
                        contentDescription = "Empfangenes Bild",
                        modifier = Modifier
                            .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp)
                            .padding(top = 4.dp)
                    )
                } else {
                    Text(text = text, style = MaterialTheme.typography.bodyLarge,
                        color = if (isLocation) MaterialTheme.colorScheme.primary else itemContentColor,
                        textDecoration = if (isLocation) TextDecoration.Underline else null)
                }

                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = itemContentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp)
                )
            }
        }
    }
}