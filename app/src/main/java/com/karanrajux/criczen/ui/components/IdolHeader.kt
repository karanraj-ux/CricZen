package com.karanrajux.criczen.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.karanrajux.criczen.model.Match
import com.karanrajux.criczen.util.toAbbreviation

/**
 * The Fan Zone header. Comes alive on match days: when the idol's team (or the
 * idol themselves) is in a LIVE match, the card gets a pulsing border, a live
 * score chip, and the idol's current highlight line.
 */
@Composable
fun IdolHeader(
    idolName: String,
    wallpaperUri: String,
    onClick: () -> Unit,
    liveMatch: Match? = null,
    idolHighlight: String? = null
) {
    val isConfigured = idolName.isNotBlank() || wallpaperUri.isNotBlank()
    val isLive = liveMatch != null

    val pulse by rememberInfiniteTransition(label = "idolPulse").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(bottom = 16.dp)
            .then(
                if (isLive) Modifier.border(
                    3.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = pulse),
                    RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (!isConfigured) {
            // Cheap gray background for unconfigured state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Add Photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tap to set up Fan Zone & add a photo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (wallpaperUri.isNotBlank()) {
                    AsyncImage(
                        model = wallpaperUri,
                        contentDescription = "Idol Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dark premium gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)),
                                    startY = 100f
                                )
                            )
                    )
                } else {
                    // Fallback just in case they have a name but no photo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                )
                            )
                    )

                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 20.dp, y = -20.dp)
                    )
                }

                // Live score chip — the idol's world is happening RIGHT NOW.
                if (isLive && liveMatch != null) {
                    val scoreLine = buildString {
                        append(liveMatch.team1.toAbbreviation())
                        if (liveMatch.score1.isNotBlank()) {
                            append(" ${liveMatch.score1}")
                            if (liveMatch.overs1.isNotBlank()) append(" ${liveMatch.overs1}")
                        }
                        append(" vs ${liveMatch.team2.toAbbreviation()}")
                        if (liveMatch.score2.isNotBlank()) {
                            append(" ${liveMatch.score2}")
                            if (liveMatch.overs2.isNotBlank()) append(" ${liveMatch.overs2}")
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE • $scoreLine",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    // The idol's live highlight line, e.g. "★ Kohli 45* (30b)".
                    if (isLive && !idolHighlight.isNullOrBlank()) {
                        Text(
                            text = "★ $idolHighlight",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = if (idolName.isNotBlank()) idolName else "My Idol",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Cursive,
                        color = MaterialTheme.colorScheme.onPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (isLive) "Your idol is playing now" else "Fan Zone",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}
