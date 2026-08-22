package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.Match
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BotEmotion {
    NEUTRAL, HAPPY, NERVOUS, SLEEPY, EXCITED, POKED, DIZZY
}

@Composable
fun CricBotCompanion(match: Match? = null, idolName: String = "", preferredTeams: Set<String> = emptySet(), modifier: Modifier = Modifier) {
    var isPoked by remember { mutableStateOf(false) }
    var isDragged by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val baseEmotion = if (match != null) {
        val statusLower = match.status.lowercase()
        val perfsLower = match.notablePerformances.lowercase()
        
        when {
            statusLower.contains("rain") || statusLower.contains("stump") || statusLower.contains("abandon") || statusLower.contains("delay") -> BotEmotion.SLEEPY
            statusLower.contains("won by") || statusLower.contains("win") -> BotEmotion.HAPPY
            statusLower.contains("need") || statusLower.contains("req") || statusLower.contains("trail") || perfsLower.contains("run rate") -> BotEmotion.NERVOUS
            perfsLower.contains("wicket") || perfsLower.contains("out") || perfsLower.contains("*") || perfsLower.contains("★") || statusLower.contains("out") -> BotEmotion.EXCITED
            else -> BotEmotion.NEUTRAL
        }
    } else {
        BotEmotion.NEUTRAL
    }

    val emotion = if (isDragged) BotEmotion.DIZZY else if (isPoked) BotEmotion.POKED else baseEmotion

    val backgroundColor by animateColorAsState(
        targetValue = when (emotion) {
            BotEmotion.NEUTRAL -> Color(0xFFE3F2FD) 
            BotEmotion.HAPPY -> Color(0xFFE8F5E9) 
            BotEmotion.NERVOUS -> Color(0xFFFFF8E1) 
            BotEmotion.SLEEPY -> Color(0xFFF5F5F5) 
            BotEmotion.EXCITED -> Color(0xFFFCE4EC) 
            BotEmotion.POKED -> Color(0xFFF3E5F5)
            BotEmotion.DIZZY -> Color(0xFFFFF3E0)
        },
        animationSpec = tween(300),
        label = "bgColor"
    )
    
    val faceColor by animateColorAsState(
        targetValue = when (emotion) {
            BotEmotion.NEUTRAL -> Color(0xFF2196F3)
            BotEmotion.HAPPY -> Color(0xFF4CAF50)
            BotEmotion.NERVOUS -> Color(0xFFFF9800)
            BotEmotion.SLEEPY -> Color(0xFF9E9E9E)
            BotEmotion.EXCITED -> Color(0xFFE91E63)
            BotEmotion.POKED -> Color(0xFF9C27B0)
            BotEmotion.DIZZY -> Color(0xFFFF5722)
        },
        animationSpec = tween(300),
        label = "faceColor"
    )

    // Finite Transition Animations (Not looping to save memory)
    val yAnim = remember { Animatable(0f) }
    val xAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(emotion) {
        // Reset state on change
        yAnim.snapTo(0f)
        xAnim.snapTo(0f)
        scaleAnim.snapTo(1f)

        when (emotion) {
            BotEmotion.EXCITED, BotEmotion.POKED -> {
                // Celebration jump (Finite loop)
                repeat(3) {
                    yAnim.animateTo(-12f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                    yAnim.animateTo(0f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                }
            }
            BotEmotion.HAPPY -> {
                // Gentle happy bounce and scale
                scaleAnim.animateTo(1.15f, animationSpec = tween(200))
                yAnim.animateTo(-8f, animationSpec = tween(200))
                scaleAnim.animateTo(1f, animationSpec = tween(200))
                yAnim.animateTo(0f, animationSpec = tween(200))
            }
            BotEmotion.NERVOUS -> {
                // Anxious shiver (Finite loop)
                repeat(6) {
                    xAnim.animateTo(-4f, animationSpec = tween(50))
                    xAnim.animateTo(4f, animationSpec = tween(50))
                }
                xAnim.animateTo(0f, animationSpec = tween(50))
            }
            BotEmotion.SLEEPY -> {
                // Slight dip down
                yAnim.animateTo(4f, animationSpec = tween(600))
            }
            BotEmotion.DIZZY -> {
                // Wobbly scale
                scaleAnim.animateTo(0.9f, animationSpec = tween(100))
                scaleAnim.animateTo(1.1f, animationSpec = tween(100))
                scaleAnim.animateTo(1f, animationSpec = tween(100))
            }
            BotEmotion.NEUTRAL -> {
                // Just settle
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                scope.launch {
                    isPoked = true
                    delay(2000)
                    isPoked = false
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colorful Face Gradient
            val faceGradient = Brush.linearGradient(
                colors = listOf(
                    faceColor.copy(alpha = 0.35f),
                    faceColor.copy(alpha = 0.1f)
                )
            )
            
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragOffset.value.x.roundToInt(), dragOffset.value.y.roundToInt()) }
                    .offset(x = xAnim.value.dp, y = yAnim.value.dp)
                    .scale(scaleAnim.value)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragged = true },
                            onDragEnd = {
                                isDragged = false
                                scope.launch {
                                    dragOffset.animateTo(
                                        targetValue = Offset.Zero,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            },
                            onDragCancel = {
                                isDragged = false
                                scope.launch { dragOffset.animateTo(Offset.Zero) }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    val newX = (dragOffset.value.x + dragAmount.x).coerceIn(-150f, 150f)
                                    val newY = (dragOffset.value.y + dragAmount.y).coerceIn(-150f, 150f)
                                    dragOffset.snapTo(Offset(newX, newY))
                                }
                            }
                        )
                    }
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(faceGradient)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val eyeText = when (emotion) {
                            BotEmotion.NEUTRAL -> "•"
                            BotEmotion.HAPPY -> "^"
                            BotEmotion.NERVOUS -> "O"
                            BotEmotion.SLEEPY -> "-"
                            BotEmotion.EXCITED -> ">"
                            BotEmotion.POKED -> "♥"
                            BotEmotion.DIZZY -> "@"
                        }
                        val rightEyeText = if (emotion == BotEmotion.EXCITED) "<" else eyeText
                        Text(eyeText, fontWeight = FontWeight.ExtraBold, color = faceColor)
                        Text(rightEyeText, fontWeight = FontWeight.ExtraBold, color = faceColor)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Colorful Cheeks!
                        val showCheeks = emotion in listOf(BotEmotion.HAPPY, BotEmotion.EXCITED, BotEmotion.POKED, BotEmotion.DIZZY)
                        val cheekAlpha by animateFloatAsState(targetValue = if (showCheeks) 0.7f else 0f, label = "cheekAlpha")
                        
                        Box(modifier = Modifier.size(6.dp, 4.dp).clip(CircleShape).background(Color(0xFFFF5252).copy(alpha = cheekAlpha)))
                        
                        val mouthText = when (emotion) {
                            BotEmotion.NEUTRAL -> "−"
                            BotEmotion.HAPPY -> "‿"
                            BotEmotion.NERVOUS -> "〰"
                            BotEmotion.SLEEPY -> "−"
                            BotEmotion.EXCITED -> "O"
                            BotEmotion.POKED -> "w"
                            BotEmotion.DIZZY -> "〰"
                        }
                        Text(mouthText, fontWeight = FontWeight.ExtraBold, color = faceColor)
                        
                        Box(modifier = Modifier.size(6.dp, 4.dp).clip(CircleShape).background(Color(0xFFFF5252).copy(alpha = cheekAlpha)))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            val dialogueText = remember(emotion, match, idolName, preferredTeams) {
                when (emotion) {
                    BotEmotion.NEUTRAL -> if (match != null) "Watching the progress track... Awaiting the next big move!" else com.example.util.ZennyMemoryBank.getMemory(idolName = idolName, preferredTeams = preferredTeams)
                    BotEmotion.HAPPY -> "Look at them go! Flying down the progress track!"
                    BotEmotion.NERVOUS -> "Phew... they are falling behind the line! Circuits are sweating."
                    BotEmotion.SLEEPY -> com.example.util.ZennyMemoryBank.getMemory(matchStatus = match?.status ?: "rain", idolName = idolName, preferredTeams = preferredTeams)
                    BotEmotion.EXCITED -> "WICKET! The track stops here. Did you see that?"
                    BotEmotion.POKED -> "Hey! Stop poking my circuits! Just kidding, I love the attention. :)"
                    BotEmotion.DIZZY -> "Whoooa! You're stretching my sensors! Put me back!"
                }
            }
            
            Column {
                Text("Zenny (CricBot)", fontWeight = FontWeight.Bold, color = faceColor, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(dialogueText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
