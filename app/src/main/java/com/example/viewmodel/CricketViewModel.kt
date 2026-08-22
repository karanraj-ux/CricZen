package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OnboardingManager
import com.example.data.CricketRepository
import com.example.data.FetchResult
import com.example.model.Match
import com.example.util.toAbbreviation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed interface CricketUiState {
    object Loading : CricketUiState
    data class Success(
        val matches: List<Match>,
        val lastUpdated: String,
        val isOffline: Boolean,
        val searchQuery: String = "",
        val selectedMatchId: String? = null,
        val preferredTeams: Set<String> = emptySet(),
        val preferredPlayers: Set<String> = emptySet(),
        val idolName: String = "",
        val wallpaperUri: String = "",
        val appMode: String = "Fan Mode",
        val pinnedMatchId: String = "",
        val playerNews: List<com.example.model.NewsArticle> = emptyList(),
        val selectedNewsUrl: String? = null,
        val dataSaverMode: Boolean = false,
        val matchPredictions: Map<String, Int> = emptyMap()
    ) : CricketUiState
    data class Error(val message: String) : CricketUiState
}

class CricketViewModel(
    private val onboardingManager: OnboardingManager,
    private val repository: CricketRepository
) : ViewModel() {
    
    
    private val _playerNews = MutableStateFlow<List<com.example.model.NewsArticle>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    
    private val _suggestedPlayers = MutableStateFlow<List<String>>(emptyList())
    val suggestedPlayers: StateFlow<List<String>> = _suggestedPlayers.asStateFlow()
    
    private val _selectedMatchId = MutableStateFlow<String?>(null)
    private val _selectedNewsUrl = MutableStateFlow<String?>(null)
    
    private val _fetchResult = MutableStateFlow<FetchResult>(FetchResult.Loading)
    private val _lastUpdated = MutableStateFlow("Just now")

    val isOnboardingCompleted = onboardingManager.isOnboardingCompleted
    val pipHintShown = onboardingManager.pipHintShown
    val fundingDismissed = onboardingManager.fundingDismissed
    val appOpensCount = onboardingManager.appOpensCount
    val feedbackDismissed = onboardingManager.feedbackDismissed

val uiState: StateFlow<CricketUiState> = combine(
        _fetchResult,
        combine(_searchQuery, _playerNews, _selectedNewsUrl) { q, p, n -> Triple(q, p, n) },
        onboardingManager.preferredTeams,
        onboardingManager.preferredPlayers,
        combine(
            _selectedMatchId, 
            _lastUpdated, 
            onboardingManager.idolName,
            combine(
                onboardingManager.wallpaperUri,
                onboardingManager.appMode,
                onboardingManager.widgetPinnedMatchId,
                onboardingManager.dataSaverMode
            ,
                onboardingManager.matchPredictions
            ) { wp, mode, pinned, dataSaver, preds ->
                FiveTuple(wp, mode, pinned, dataSaver, preds)
            }
        ) { id, time, idol, four ->  
             EightTuple(id, time, idol, four.a, four.b, four.c, four.d, four.e)
        }
    ) { fetchResult, queryNewsAndUrl, preferredTeams, preferredPlayers, extra ->
        val query = queryNewsAndUrl.first
        val playerNews = queryNewsAndUrl.second
        val selectedNewsUrl = queryNewsAndUrl.third
        val selectedId = extra.a
        val lastUpdated = extra.b
        val idolName = extra.c
        val wallpaperUri = extra.d
        val appMode = extra.e
        val pinnedMatchId = extra.f
        val isDataSaver = extra.g
        val predictions = extra.h
        
        when (fetchResult) {
            is FetchResult.Loading -> CricketUiState.Loading
            is FetchResult.Error -> CricketUiState.Error(fetchResult.message)
            is FetchResult.Success -> {
                var list = fetchResult.matches
                
                if (preferredTeams.isNotEmpty()) {
                    val preferredList = mutableListOf<Match>()
                    val otherList = mutableListOf<Match>()
                    for (match in list) {
                        val isPreferred = preferredTeams.any { pref ->
                            match.team1.contains(pref, ignoreCase = true) || match.team2.contains(pref, ignoreCase = true)
                        }
                        if (isPreferred) {
                            preferredList.add(match)
                        } else {
                            otherList.add(match)
                        }
                    }
                    list = preferredList + otherList
                }
                
                if (query.isNotBlank()) {
                    list = list.filter {
                        it.team1.contains(query, ignoreCase = true) ||
                        it.team2.contains(query, ignoreCase = true) ||
                        it.status.contains(query, ignoreCase = true)
                    }
                }
                CricketUiState.Success(
                    matches = list,
                    lastUpdated = lastUpdated,
                    isOffline = fetchResult.isOffline,
                    searchQuery = query,
                    selectedMatchId = selectedId,
                    preferredTeams = preferredTeams,
                    preferredPlayers = preferredPlayers,
                    idolName = idolName,
                    wallpaperUri = wallpaperUri,
                    appMode = appMode,
                    pinnedMatchId = pinnedMatchId,
                    playerNews = playerNews,
                    selectedNewsUrl = selectedNewsUrl,
                    dataSaverMode = isDataSaver,
                    matchPredictions = predictions
                )
            }
        }
    }
    .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CricketUiState.Loading)

    
    private val _isOffline = MutableStateFlow(false)

    init {
        // Observe DB continuously
        viewModelScope.launch {
            repository.getLiveMatchesFlow()
                .collect { matches ->
                    if (matches.isNotEmpty() || _fetchResult.value is FetchResult.Loading) {
                        _fetchResult.value = FetchResult.Success(matches, _isOffline.value)
                    }
                }
        }
        
        viewModelScope.launch {
            combine(
                onboardingManager.preferredTeams,
                onboardingManager.preferredPlayers,
                onboardingManager.idolName
            ) { teams, players, idol ->
                Triple(teams, players, idol)
            }.collectLatest { (teams, players, idol) ->
                fetchPersonalizedNews(teams, players, idol)
            }
        }
        
        startLiveApiFetching()
    }


    fun selectMatch(id: String?) {
        _selectedMatchId.value = id
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    
    fun refresh() {
        startLiveApiFetching(forceRefresh = true)
    }

    private var fetchJob: kotlinx.coroutines.Job? = null
    
    private fun startLiveApiFetching(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            val current = _fetchResult.value
            if (current is FetchResult.Success) {
                // Keep showing data, but maybe show a subtle loading state if needed.
                // We just trigger a sync.
            } else {
                _fetchResult.value = FetchResult.Loading
            }
        }
        
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            while (true) {
                try {
                    val prefPlayers = onboardingManager.preferredPlayers.first()
                    val prefTeams = onboardingManager.preferredTeams.first()
                    val isDataSaver = onboardingManager.dataSaverMode.first()
                    val pinnedMatchId = onboardingManager.widgetPinnedMatchId.first()
                    
                    if (isDataSaver && pinnedMatchId.isNotEmpty()) {
                        repository.syncSniperMatch(pinnedMatchId, prefPlayers)
                    } else {
                        repository.syncMatches(prefPlayers, prefTeams)
                    }
                    
                    _isOffline.value = false
                    val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    _lastUpdated.value = format.format(java.util.Date())
                    
                    val currentResult = _fetchResult.value
                    if (currentResult is FetchResult.Success) {
                        _fetchResult.value = currentResult.copy(
                            isOffline = false,
                        )
                    }
                } catch (e: Exception) {
                    _isOffline.value = true
                    val currentResult = _fetchResult.value
                    if (currentResult is FetchResult.Success) {
                        _fetchResult.value = currentResult.copy(isOffline = true)
                    } else if (currentResult is FetchResult.Loading) {
                        _fetchResult.value = FetchResult.Error(e.message ?: "Network Error")
                    }
                }
                val isDataSaver = onboardingManager.dataSaverMode.first()
                
                // Sleep Mode for Dead Matches
                val currentMatches = (_fetchResult.value as? FetchResult.Success)?.matches ?: emptyList()
                val hasLiveMatch = currentMatches.any { it.matchState.contains("LIVE", true) || it.matchState.contains("IN PROGRESS", true) }
                
                val fetchDelay = when {
                    !hasLiveMatch && currentMatches.isNotEmpty() -> 300000L // 5 minutes sleep if no live matches
                    isDataSaver -> 120000L
                    else -> 30000L
                }
                delay(fetchDelay)
            }
        }
    }


    fun completeOnboarding(selectedTeams: Set<String>, selectedPlayers: Set<String> = emptySet()) {
        viewModelScope.launch {
            onboardingManager.savePreferredTeams(selectedTeams)
            onboardingManager.savePreferredPlayers(selectedPlayers)
            onboardingManager.saveOnboardingCompleted(true)
        }
    }
    
    fun updatePreferredPlayers(players: Set<String>) {
        viewModelScope.launch {
            onboardingManager.savePreferredPlayers(players)
        }
    }
            
    fun updateIdolName(name: String) {
        viewModelScope.launch {
            onboardingManager.saveIdolName(name)
            fetchPersonalizedNews(idol = name)
        }
    }
    
    fun fetchPersonalizedNews(teams: Set<String> = emptySet(), players: Set<String> = emptySet(), idol: String = "") {
        viewModelScope.launch {
            val allKeywords = mutableSetOf<String>()
            if (idol.isNotBlank()) allKeywords.add(idol)
            allKeywords.addAll(teams)
            allKeywords.addAll(players)
            _playerNews.value = repository.getPersonalizedNews(allKeywords)
        }
    }
    
    fun updateWallpaperUri(uri: String, context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (uri.startsWith("content://")) {
                    val input = context.contentResolver.openInputStream(android.net.Uri.parse(uri))
                    val file = java.io.File(context.filesDir, "custom_wallpaper.jpg")
                    val output = java.io.FileOutputStream(file)
                    input?.copyTo(output)
                    input?.close()
                    output.close()
                    onboardingManager.saveWallpaperUri(file.absolutePath)
                } else {
                    onboardingManager.saveWallpaperUri(uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onboardingManager.saveWallpaperUri(uri)
            }
        }
    }

    
    fun updateSelectedNewsUrl(url: String?) {
        _selectedNewsUrl.value = url
    }

    
    fun saveMatchPrediction(matchId: String, prediction: Int) {
        viewModelScope.launch {
            onboardingManager.saveMatchPrediction(matchId, prediction)
        }
    }

    fun updateDataSaverMode(enabled: Boolean) {
        viewModelScope.launch {
            onboardingManager.saveDataSaverMode(enabled)
        }
    }

    fun updateAppMode(mode: String) {
        viewModelScope.launch {
            onboardingManager.saveAppMode(mode)
        }
    }

    fun dismissFunding() {
        viewModelScope.launch {
            onboardingManager.saveFundingDismissed(true)
        }
    }
    
    fun incrementAppOpens() {
        viewModelScope.launch {
            onboardingManager.incrementAppOpens()
        }
    }

    fun dismissFeedback() {
        viewModelScope.launch {
            onboardingManager.saveFeedbackDismissed(true)
        }
    }

    fun setPipHintShown(shown: Boolean) {
        viewModelScope.launch {
            onboardingManager.savePipHintShown(shown)
        }
    }

    fun fetchSuggestedPlayers(teams: Set<String>) {
        viewModelScope.launch {
            val players = repository.fetchDynamicPlayers(teams)
            _suggestedPlayers.value = players
        }
    }

    fun pinMatchToWidget(matchId: String, match: Match?, context: android.content.Context) {
        viewModelScope.launch {
            onboardingManager.saveWidgetPinnedMatchId(matchId)
            if (matchId.isNotEmpty() && match != null) {
                onboardingManager.saveWidgetPinnedMatchDetails(
                    match.team1, match.score1, match.overs1,
                    match.team2, match.score2, match.overs2,
                    match.matchState
                )
            }
            
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.worker.MatchUpdateWorker>().build()
            androidx.work.WorkManager.getInstance(context).enqueueUniqueWork("WidgetUpdate", androidx.work.ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}

data class FiveTuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
data class SixTuple<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)

data class EightTuple<A, B, C, D, E, F, G, H>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H)

