package com.karanrajux.criczen.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.karanrajux.criczen.util.performHeavyClick
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.karanrajux.criczen.model.Match
import com.karanrajux.criczen.viewmodel.CricketUiState
import com.karanrajux.criczen.viewmodel.CricketViewModel
import kotlin.math.abs
import com.karanrajux.criczen.ui.*
import com.karanrajux.criczen.ui.components.*
import com.karanrajux.criczen.ui.screens.*
import com.karanrajux.criczen.model.NewsArticle

/** A dashboard feed row: either a match card or an interleaved news card. */
private sealed interface FeedItem {
    data class MatchRow(val match: Match) : FeedItem
    data class NewsRow(val article: NewsArticle) : FeedItem
}

/** Mixes news cards into the match list — one after every 4 matches. */
private fun buildFeedItems(matches: List<Match>, news: List<NewsArticle>): List<FeedItem> {
    val feed = mutableListOf<FeedItem>()
    var newsIdx = 0
    matches.forEachIndexed { i, match ->
        feed.add(FeedItem.MatchRow(match))
        if (news.isNotEmpty() && (i + 1) % 4 == 0 && i != matches.lastIndex) {
            feed.add(FeedItem.NewsRow(news[newsIdx % news.size]))
            newsIdx++
        }
    }
    return feed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    state: CricketUiState.Success,
    onMatchClick: (Match) -> Unit,
    onPinClick: (Match) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onFanModeClick: () -> Unit,
    onToggleMode: () -> Unit,
    onToggleDataSaver: () -> Unit,
    onSavePrediction: (String, Int) -> Unit,
    onSupportClick: () -> Unit,
    onNewsClick: (String) -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val tabs = listOf("All Matches", "My Teams")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showPredictionDialog by remember { mutableStateOf<Match?>(null) }
    var predictionInput by remember { mutableStateOf("") }


    
    if (showPredictionDialog != null) {
        AlertDialog(
            onDismissRequest = { showPredictionDialog = null },
            title = { Text("Set Target Prediction", fontWeight = FontWeight.ExtraBold) },
            text = { 
                Column {
                    Text("Enter your predicted target score for this match.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = predictionInput,
                        onValueChange = { predictionInput = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        label = { Text("Target Score") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val pred = predictionInput.toIntOrNull() ?: 250
                    onSavePrediction(showPredictionDialog!!.id, pred)
                    showPredictionDialog = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPredictionDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
val matchesToShow = if (selectedTabIndex == 1 && state.preferredTeams.isNotEmpty()) {
        state.matches.filter { match ->
            state.preferredTeams.any { pref ->
                com.karanrajux.criczen.data.CricketConstants.matchesPreferredTeam(pref, match.team1, match.team2)
            }
        }
    } else {
        state.matches
    }

    // Dashboard feed: matches with news cards woven in every 4 rows.
    val feedItems = remember(matchesToShow, state.playerNews) {
        buildFeedItems(matchesToShow, state.playerNews)
    }
    val topStories = remember(state.playerNews) { state.playerNews.take(8) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        androidx.compose.material3.Text("CricZen", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    // Mode Toggle Switch
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (state.appMode == "Fan Mode") 
                                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                else 
                                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outline))
                            )
                            .clickable { performHeavyClick(context); onToggleMode() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (state.appMode == "Fan Mode") "Fan Mode" else "Standard", 
                            color = if (state.appMode == "Fan Mode") MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { performHeavyClick(context); onSettingsClick() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { performHeavyClick(context); onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( scrolledContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f))
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            
            if (state.dataSaverMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ultra-Low Data Mode is active.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                }
            }
            
            TabRow(
                selectedTabIndex = selectedTabIndex,
                
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        }
                    )
                }
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search matches, teams...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            var showWidgetBanner by remember { mutableStateOf(true) }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showWidgetBanner) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.surface)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Add the Home Screen Widget!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text("Get live scores directly on your home screen without opening the app.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                IconButton(onClick = { showWidgetBanner = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }
                if (state.appMode == "Fan Mode") {
                    // Match-day liveness: is the idol's team (or the idol) in a LIVE game?
                    val idolLiveMatch = remember(state.matches, state.idolName, state.preferredTeams) {
                        state.matches.firstOrNull { m ->
                            m.matchState.contains("LIVE", true) && (
                                state.preferredTeams.any { pref ->
                                    com.karanrajux.criczen.data.CricketConstants.matchesPreferredTeam(pref, m.team1, m.team2)
                                } || (state.idolName.isNotBlank() &&
                                    (m.status.contains(state.idolName, true) ||
                                        m.notablePerformances.contains(state.idolName, true)))
                                )
                        }
                    }
                    val idolHighlight = remember(idolLiveMatch, state.idolName) {
                        idolLiveMatch?.notablePerformances
                            ?.split("|")
                            ?.firstOrNull { it.contains(state.idolName, true) }
                            ?.trim()
                            ?.removePrefix("★")?.trim()
                    }
                    item {
                        IdolHeader(
                            idolName = state.idolName,
                            wallpaperUri = state.wallpaperUri,
                            onClick = onFanModeClick,
                            liveMatch = idolLiveMatch,
                            idolHighlight = idolHighlight
                        )
                    }
                    
                    item {
                        CricBotCompanion(
                            match = null,
                            idolName = state.idolName,
                            preferredTeams = state.preferredTeams,
                            modifier = Modifier.padding(bottom = 8.dp).padding(horizontal = 16.dp).fillMaxWidth()
                        )
                    }
                    

                    
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    "FAN FAVORITES", 
                                style = MaterialTheme.typography.labelMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val topPlayers = if (state.preferredPlayers.isNotEmpty()) {
                                    state.preferredPlayers.toList()
                                } else {
                                    listOf("Virat Kohli", "Rohit Sharma", "MS Dhoni", "Jasprit Bumrah", "Suryakumar Yadav", "Hardik Pandya")
                                }
                                topPlayers.forEach { player ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                                    ) {
                                        Text(
                                            text = player,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { performHeavyClick(context); onSupportClick() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("☕", fontSize = 14.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Support", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { performHeavyClick(context); onToggleDataSaver() }) {
                                    Text("Sniper Mode", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(if (state.dataSaverMode) Icons.Default.SignalCellularOff else Icons.Default.SignalCellular4Bar, contentDescription = "Data Saver", tint = if (state.dataSaverMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isOffline) "OFFLINE (CACHED DATA)" else "LIVE UPDATES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (state.isOffline) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Updated: ${state.lastUpdated}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Top Stories rail — news is now part of the dashboard, not a hidden tab.
                if (topStories.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOP STORIES",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "For you",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(end = 16.dp)
                            ) {
                                items(topStories, key = { it.link }) { article ->
                                    NewsCardRail(article = article, onClick = { onNewsClick(article.link) })
                                }
                            }
                        }
                    }
                }

                if (feedItems.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No matches found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    items(
                        feedItems,
                        key = {
                            when (it) {
                                is FeedItem.MatchRow -> "match_${it.match.id}"
                                is FeedItem.NewsRow -> "news_${it.article.link.hashCode()}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is FeedItem.MatchRow -> {
                                val match = item.match
                                val isPreferred = state.preferredTeams.any { com.karanrajux.criczen.data.CricketConstants.matchesPreferredTeam(it, match.team1, match.team2) }
                                val customPred = state.matchPredictions[match.id]
                                MatchCard(
                                    match = match,
                                    customPrediction = customPred,
                                    onPredictionClick = {
                                        predictionInput = (customPred ?: 250).toString()
                                        showPredictionDialog = match
                                    },
                                    isPreferred = isPreferred,
                                    isPinned = match.id == state.pinnedMatchId,
                                    onPinClick = { onPinClick(match) },
                                    onClick = { onMatchClick(match) }
                                )
                            }
                            is FeedItem.NewsRow -> {
                                NewsCardInline(
                                    article = item.article,
                                    onClick = { onNewsClick(item.article.link) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

