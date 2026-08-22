package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Match

@Composable
fun ChaseProgressBar(match: Match, customPrediction: Int? = null, onPredictionClick: (() -> Unit)? = null) {
    // Parse scores
    val score1 = parseScore(match.score1)
    val score2 = parseScore(match.score2)
    
    // Determine innings and target
    val isSecondInnings = match.score2.isNotBlank()
    val target = if (isSecondInnings) score1 + 1 else (customPrediction ?: calculateDynamicPrediction(match))
    
    // Premium Progress & Overshoot Logic
    val maxScoreDisplay = maxOf(target, score1, score2).coerceAtLeast(1)
    val targetFraction = (target.toFloat() / maxScoreDisplay).coerceIn(0.1f, 1f)
    val progress1 = (score1.toFloat() / maxScoreDisplay).coerceIn(0f, 1f)
    val progress2 = if (isSecondInnings) (score2.toFloat() / maxScoreDisplay).coerceIn(0f, 1f) else 0f
    
    val isOvershoot1 = score1 >= target
    val isOvershoot2 = score2 >= target

    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        
        // Team 1 Info (Left)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val flag1 = getFlagUrl(match.team1)
                    if (flag1 != null) {
                        AsyncImage(
                            model = flag1,
                            contentDescription = match.team1,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(match.team1, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                if (match.score1.isNotBlank()) {
                    Text(
                        "${match.score1} ${if (match.overs1.isNotBlank()) "(" + match.overs1 + ")" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Middle: Target / VS
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                if (isSecondInnings) {
                    Text("TARGET", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$target", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                } else {
                    Text("PROJ (TAP)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onPredictionClick?.invoke() })
                    Text("$target", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            // Team 2 Info (Right)
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(match.team2, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    val flag2 = getFlagUrl(match.team2)
                    if (flag2 != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AsyncImage(
                            model = flag2,
                            contentDescription = match.team2,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                if (match.score2.isNotBlank()) {
                    Text(
                        "${match.score2} ${if (match.overs2.isNotBlank()) "(" + match.overs2 + ")" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Premium Glassmorphism Progress Track
        val infiniteTransition = rememberInfiniteTransition()
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        val isCloseToTarget = if (isSecondInnings) (target - score2 in 1..20) else (target - score1 in 1..20)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            // Team 1 fill (from left)
            if (!isSecondInnings || progress1 > 0) {
                val barColor = if (isOvershoot1 && !isSecondInnings) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary
                val brush = Brush.horizontalGradient(
                    colors = listOf(barColor.copy(alpha = 0.7f), barColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress1)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(brush)
                )
            }
            
            // Team 2 fill (from left, overlapping or separate)
            if (isSecondInnings && progress2 > 0) {
                val barColor = if (isOvershoot2) Color(0xFFFFD700) else MaterialTheme.colorScheme.secondary
                val brush = Brush.horizontalGradient(
                    colors = listOf(barColor.copy(alpha = 0.7f), barColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress2)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(brush)
                )
            }
            
            // The Finish Line (Target Marker)
            if (targetFraction < 1f || isCloseToTarget) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(targetFraction)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(4.dp)
                            .background(if (isCloseToTarget) Color(0xFFFF3D00).copy(alpha = pulseAlpha) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
            }
        }
        
        // Idol Tracker Overlay
        if (match.notablePerformances.contains("★")) {
            val idolName = extractIdolName(match.notablePerformances)
            val idolScore = extractIdolScore(match.notablePerformances)
            if (idolName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "$idolName is batting! ($idolScore)", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

fun parseScore(scoreStr: String): Int {
    if (scoreStr.isBlank()) return 0
    // Try to extract the first number before slash, e.g. "210/4" -> 210, "210" -> 210, "210-4" -> 210
    val matchResult = Regex("(\\d+)[/-]?").find(scoreStr)
    return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
}

fun extractIdolName(perf: String): String {
    // Expected format: "★ V Kohli 75* ..."
    val starIndex = perf.indexOf("★")
    if (starIndex != -1) {
        val nextSpace = perf.indexOf(" ", starIndex + 2)
        val nameEnd = perf.indexOf(Regex("\\d").toString(), starIndex) // simplified
        // We'll just extract a basic substring for simplicity.
        val parts = perf.substring(starIndex + 1).trim().split(Regex("\\s+"))
        if (parts.size >= 2) {
             // Heuristic: take words until we hit a number
             val nameParts = mutableListOf<String>()
             var score = ""
             for (part in parts) {
                 if (part.any { it.isDigit() }) {
                     score = part
                     break
                 }
                 nameParts.add(part)
             }
             return nameParts.joinToString(" ")
        }
    }
    return ""
}

fun extractIdolScore(perf: String): String {
    val starIndex = perf.indexOf("★")
    if (starIndex != -1) {
        val parts = perf.substring(starIndex + 1).trim().split(Regex("\\s+"))
        for (part in parts) {
            if (part.any { it.isDigit() }) {
                return part
            }
        }
    }
    return ""
}

fun calculateDynamicPrediction(match: Match): Int {
    val series = match.seriesName.lowercase()
    val formatMultiplier = when {
        series.contains("t20") -> 160
        series.contains("odi") || series.contains("one day") -> 260
        series.contains("test") -> 350
        series.contains("t10") -> 100
        else -> 200
    }
    
    // Project based on run rate if possible
    val score1 = parseScore(match.score1)
    val matchResult = Regex("(\\d+)\\.?(\\d*)").find(match.overs1)
    val overs = matchResult?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
    
    if (score1 > 0 && overs > 2f) {
        val runRate = score1 / overs
        val projected = runRate * when {
            series.contains("t20") -> 20f
            series.contains("odi") -> 50f
            series.contains("test") -> 90f 
            else -> 20f 
        }
        return ((formatMultiplier + projected) / 2).toInt()
    }
    return formatMultiplier
}
