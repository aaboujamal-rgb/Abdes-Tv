package com.abdestv.mobile.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AudioTrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.ConnectionType
import com.abdestv.core.model.EpgProgramEntity
import com.abdestv.core.model.IptvProfileEntity
import com.abdestv.core.model.SeriesEntity
import com.abdestv.core.model.VideoAspectRatio
import com.abdestv.core.model.WatchHistoryEntity
import com.abdestv.core.ui.theme.AbdesColors
import com.abdestv.core.viewmodel.SharedIptvViewModel

enum class MobileTab(val labelFr: String, val icon: ImageVector) {
    HOME("Accueil", Icons.Default.Home),
    LIVE("TV en Direct", Icons.Default.LiveTv),
    MOVIES("Films", Icons.Default.Movie),
    SERIES("Séries", Icons.Default.Tv),
    FAVORITES("Favoris", Icons.Default.Favorite),
    SEARCH("Recherche", Icons.Default.Search),
    SETTINGS("Paramètres", Icons.Default.Settings)
}

@Composable
fun MobileAppRoot(viewModel: SharedIptvViewModel) {
    val profiles by viewModel.profiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val liveChannels by viewModel.liveChannels.collectAsState()
    val vodMovies by viewModel.vodMovies.collectAsState()
    val m3uSeries by viewModel.m3uSeriesItems.collectAsState()
    val xtreamSeries by viewModel.xtreamSeriesItems.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val hiddenChannels by viewModel.hiddenChannels.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val epgPrograms by viewModel.epgPrograms.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val statusMessage by viewModel.statusMessageFr.collectAsState()
    val currentPlaying by viewModel.playerManager.currentChannel.collectAsState()
    val selectedSeries by viewModel.selectedSeriesForEpisodes.collectAsState()
    val seriesEpisodes by viewModel.seriesEpisodes.collectAsState()
    val isLoadingEpisodes by viewModel.isLoadingEpisodes.collectAsState()

    var currentTab by rememberSaveable { mutableStateOf(MobileTab.HOME) }
    var showAddProfileDialog by rememberSaveable { mutableStateOf(false) }
    var isPlayerFullscreen by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    val epgByChannelId = remember(epgPrograms) {
        val now = System.currentTimeMillis()
        epgPrograms
            .filter { it.startTimeEpochMs <= now && it.endTimeEpochMs >= now }
            .associateBy { it.epgChannelId }
    }

    if (currentPlaying != null) {
        MobileVideoPlayerScreen(
            viewModel = viewModel,
            channel = currentPlaying!!,
            currentEpg = epgByChannelId[currentPlaying!!.epgChannelId],
            isFullscreen = isPlayerFullscreen,
            onToggleFullscreen = {
                isPlayerFullscreen = !isPlayerFullscreen
                val activity = context as? Activity
                activity?.requestedOrientation = if (isPlayerFullscreen) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            },
            onClosePlayer = {
                isPlayerFullscreen = false
                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                viewModel.stopAndRecordPlayback()
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AbdesColors.DeepBlack),
        containerColor = AbdesColors.DeepBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MobileTopHeader(
                activeProfile = activeProfile,
                isSyncing = isSyncing,
                onRefresh = { viewModel.refreshActiveProfile() },
                onAddProfile = { showAddProfileDialog = true },
                onSearchClick = { currentTab = MobileTab.SEARCH }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AbdesColors.ObsidianSurface,
                contentColor = AbdesColors.TextPrimary,
                tonalElevation = 8.dp
            ) {
                MobileTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.labelFr
                            )
                        },
                        label = {
                            Text(
                                text = tab.labelFr,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AbdesColors.PurpleGlow,
                            indicatorColor = AbdesColors.PurpleDeep,
                            unselectedIconColor = AbdesColors.TextSecondary,
                            unselectedTextColor = AbdesColors.TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeProfile == null && profiles.isEmpty()) {
                WelcomeOnboardingCard(
                    onAddProfileClick = { showAddProfileDialog = true },
                    onLoadLegalDemoClick = { viewModel.loadLegalPublicTestStreams() }
                )
            } else {
                when (currentTab) {
                    MobileTab.HOME -> MobileHomeScreen(
                        liveChannels = liveChannels,
                        vodMovies = vodMovies,
                        xtreamSeries = xtreamSeries,
                        m3uSeries = m3uSeries,
                        watchHistory = watchHistory,
                        epgMap = epgByChannelId,
                        onPlayChannel = { ch, pos -> viewModel.playChannel(ch, pos) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onOpenSeries = { viewModel.openSeriesDetails(it) },
                        onNavigateTab = { currentTab = it }
                    )
                    MobileTab.LIVE -> MobileLiveTvScreen(
                        channels = liveChannels,
                        epgMap = epgByChannelId,
                        viewModel = viewModel
                    )
                    MobileTab.MOVIES -> MobileMoviesScreen(
                        movies = vodMovies,
                        viewModel = viewModel
                    )
                    MobileTab.SERIES -> MobileSeriesScreen(
                        xtreamSeries = xtreamSeries,
                        m3uSeries = m3uSeries,
                        viewModel = viewModel
                    )
                    MobileTab.FAVORITES -> MobileFavoritesScreen(
                        favorites = favorites,
                        onPlay = { viewModel.playChannel(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                    MobileTab.SEARCH -> MobileSearchScreen(
                        query = searchQuery,
                        results = searchResults,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onPlay = { viewModel.playChannel(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                    MobileTab.SETTINGS -> MobileSettingsScreen(
                        profiles = profiles,
                        activeProfile = activeProfile,
                        hiddenCount = hiddenChannels.size,
                        onSwitchProfile = { viewModel.switchProfile(it) },
                        onDeleteProfile = { viewModel.deleteProfile(it) },
                        onAddProfile = { showAddProfileDialog = true },
                        onRestoreHidden = { viewModel.restoreAllHiddenChannels() },
                        onClearHistory = { viewModel.clearHistory() },
                        onRefreshCatalog = { viewModel.refreshActiveProfile() },
                        onLoadTestStreams = { viewModel.loadLegalPublicTestStreams() }
                    )
                }
            }
        }
    }

    if (showAddProfileDialog) {
        AddIptvProfileDialog(
            onDismiss = { showAddProfileDialog = false },
            onSaveXtream = { name, url, user, pass, epg ->
                viewModel.loginXtreamCodes(name, url, user, pass, epg)
                showAddProfileDialog = false
            },
            onSaveM3uUrl = { name, m3u, epg ->
                viewModel.loginM3uUrl(name, m3u, epg)
                showAddProfileDialog = false
            },
            onSaveM3uFile = { name, uri, epg ->
                viewModel.loginM3uFile(name, uri, epg)
                showAddProfileDialog = false
            }
        )
    }

    if (selectedSeries != null) {
        SeriesEpisodesModal(
            series = selectedSeries!!,
            episodes = seriesEpisodes,
            isLoading = isLoadingEpisodes,
            onDismiss = { viewModel.closeSeriesDetails() },
            onPlayEpisode = { ep ->
                val s = selectedSeries!!
                viewModel.closeSeriesDetails()
                viewModel.playEpisode(s, ep)
            }
        )
    }
}

@Composable
private fun MobileTopHeader(
    activeProfile: IptvProfileEntity?,
    isSyncing: Boolean,
    onRefresh: () -> Unit,
    onAddProfile: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(AbdesColors.DeepBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AbdesColors.PurpleVibrant, AbdesColors.PurpleDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ABDES TV",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = activeProfile?.profileName ?: "Aucun profil connecté",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AbdesColors.PurpleGlow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = AbdesColors.PurplePrimary,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = Color.White)
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = Color.White)
            }
            IconButton(onClick = onAddProfile) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau profil", tint = AbdesColors.PurpleGlow)
            }
        }
    }
}

@Composable
private fun WelcomeOnboardingCard(
    onAddProfileClick: () -> Unit,
    onLoadLegalDemoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AbdesColors.PurpleDeep, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AbdesColors.PurpleBannerBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = AbdesColors.PurpleGlow,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Bienvenue sur Abdes TV",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connectez votre abonnement Xtream Codes ou importez votre playlist M3U / M3U8 et guide EPG XMLTV pour profiter du direct, des films et des séries en haute définition.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AbdesColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onAddProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AbdesColors.PurplePrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurer Xtream Codes ou M3U", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onLoadLegalDemoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Charger des flux de test publics (Démo légale)",
                        color = AbdesColors.PurpleGlow
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileHomeScreen(
    liveChannels: List<ChannelEntity>,
    vodMovies: List<ChannelEntity>,
    xtreamSeries: List<SeriesEntity>,
    m3uSeries: List<ChannelEntity>,
    watchHistory: List<WatchHistoryEntity>,
    epgMap: Map<String, EpgProgramEntity>,
    onPlayChannel: (ChannelEntity, Long) -> Unit,
    onToggleFavorite: (ChannelEntity) -> Unit,
    onOpenSeries: (SeriesEntity) -> Unit,
    onNavigateTab: (MobileTab) -> Unit
) {
    val featured = liveChannels.firstOrNull() ?: vodMovies.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        if (featured != null) {
            item {
                FeaturedHeroBanner(
                    channel = featured,
                    epg = epgMap[featured.epgChannelId],
                    onPlay = { onPlayChannel(featured, 0L) },
                    onFavorite = { onToggleFavorite(featured) }
                )
            }
        }

        if (watchHistory.isNotEmpty()) {
            item {
                SectionHeaderFr(title = "Continuer à regarder / Récemment regardés")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchHistory, key = { it.itemId }) { hist ->
                        WatchHistoryCard(
                            item = hist,
                            onClick = {
                                val matched = (liveChannels + vodMovies + m3uSeries)
                                    .firstOrNull { it.id == hist.itemId }
                                    ?: ChannelEntity(
                                        id = hist.itemId,
                                        profileId = hist.profileId,
                                        name = hist.title,
                                        categoryName = hist.categoryName,
                                        streamUrl = hist.streamUrl,
                                        logoUrl = hist.logoUrl,
                                        contentType = hist.contentType
                                    )
                                onPlayChannel(matched, hist.positionMs)
                            }
                        )
                    }
                }
            }
        }

        if (liveChannels.isNotEmpty()) {
            item {
                SectionHeaderFr(
                    title = "Chaînes TV en Direct (${liveChannels.size})",
                    actionText = "Tout voir",
                    onActionClick = { onNavigateTab(MobileTab.LIVE) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(liveChannels.take(20), key = { it.id }) { channel ->
                        MobileLiveChannelCard(
                            channel = channel,
                            epg = epgMap[channel.epgChannelId],
                            onClick = { onPlayChannel(channel, 0L) }
                        )
                    }
                }
            }
        }

        if (vodMovies.isNotEmpty()) {
            item {
                SectionHeaderFr(
                    title = "Films à la Demande (${vodMovies.size})",
                    actionText = "Tout voir",
                    onActionClick = { onNavigateTab(MobileTab.MOVIES) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vodMovies.take(20), key = { it.id }) { movie ->
                        MobilePosterCard(
                            title = movie.name,
                            subtitle = movie.categoryName,
                            posterUrl = movie.logoUrl,
                            badge = movie.rating.takeIf { it.isNotBlank() },
                            onClick = { onPlayChannel(movie, 0L) }
                        )
                    }
                }
            }
        }

        if (xtreamSeries.isNotEmpty() || m3uSeries.isNotEmpty()) {
            item {
                SectionHeaderFr(
                    title = "Séries & Épisodes",
                    actionText = "Tout voir",
                    onActionClick = { onNavigateTab(MobileTab.SERIES) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (xtreamSeries.isNotEmpty()) {
                        items(xtreamSeries.take(20), key = { it.seriesId }) { series ->
                            MobilePosterCard(
                                title = series.name,
                                subtitle = series.categoryName,
                                posterUrl = series.coverUrl,
                                badge = series.rating.takeIf { it.isNotBlank() },
                                onClick = { onOpenSeries(series) }
                            )
                        }
                    } else {
                        items(m3uSeries.take(20), key = { it.id }) { episode ->
                            MobilePosterCard(
                                title = episode.name,
                                subtitle = episode.categoryName,
                                posterUrl = episode.logoUrl,
                                badge = "Série",
                                onClick = { onPlayChannel(episode, 0L) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedHeroBanner(
    channel: ChannelEntity,
    epg: EpgProgramEntity?,
    onPlay: () -> Unit,
    onFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(AbdesColors.PurpleBannerBrush)
            .border(1.dp, AbdesColors.PurplePrimary.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
    ) {
        if (channel.logoUrl.isNotBlank()) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AbdesColors.HeroGradient)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = AbdesColors.LiveRed,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "EN DIRECT",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = channel.categoryName,
                    color = AbdesColors.PurpleGlow,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = channel.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (epg != null) {
                Text(
                    text = "À l'antenne : ${epg.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AbdesColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = AbdesColors.PurplePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Regarder", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onFavorite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = AbdesColors.PurpleGlow
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (channel.isFavorite) "Dans vos favoris" else "Favoris",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileLiveTvScreen(
    channels: List<ChannelEntity>,
    epgMap: Map<String, EpgProgramEntity>,
    viewModel: SharedIptvViewModel
) {
    val selectedCategory by viewModel.selectedLiveCategory.collectAsState()
    val categories = remember(channels) {
        listOf("Toutes") + channels.map { it.categoryName }.distinct().sorted()
    }
    val filtered = remember(channels, selectedCategory) {
        if (selectedCategory == "Toutes") channels
        else channels.filter { it.categoryName == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CategoryFilterBar(
            categories = categories,
            selected = selectedCategory,
            onSelect = { viewModel.selectLiveCategory(it) }
        )

        if (filtered.isEmpty()) {
            EmptyStateMessage("Aucune chaîne en direct disponible dans cette catégorie.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { channel ->
                    val epg = epgMap[channel.epgChannelId]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.playChannel(channel) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChannelLogoBox(logoUrl = channel.logoUrl, name = channel.name)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = epg?.title ?: channel.categoryName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (epg != null) AbdesColors.PurpleGlow else AbdesColors.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (epg != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { epg.progressAt() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(CircleShape),
                                        color = AbdesColors.PurplePrimary,
                                        trackColor = AbdesColors.CardElevated
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.toggleFavorite(channel) }) {
                                Icon(
                                    imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favori",
                                    tint = if (channel.isFavorite) AbdesColors.PurpleVibrant else AbdesColors.TextSecondary
                                )
                            }
                            IconButton(onClick = { viewModel.hideChannel(channel) }) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Masquer la chaîne",
                                    tint = AbdesColors.TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileMoviesScreen(
    movies: List<ChannelEntity>,
    viewModel: SharedIptvViewModel
) {
    val selectedCategory by viewModel.selectedMovieCategory.collectAsState()
    val categories = remember(movies) {
        listOf("Toutes") + movies.map { it.categoryName }.distinct().sorted()
    }
    val filtered = remember(movies, selectedCategory) {
        if (selectedCategory == "Toutes") movies
        else movies.filter { it.categoryName == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CategoryFilterBar(
            categories = categories,
            selected = selectedCategory,
            onSelect = { viewModel.selectMovieCategory(it) }
        )

        if (filtered.isEmpty()) {
            EmptyStateMessage("Aucun film VOD disponible dans cette catégorie.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 115.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { movie ->
                    MobilePosterCard(
                        title = movie.name,
                        subtitle = movie.categoryName,
                        posterUrl = movie.logoUrl,
                        badge = movie.rating.takeIf { it.isNotBlank() },
                        onClick = { viewModel.playChannel(movie) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileSeriesScreen(
    xtreamSeries: List<SeriesEntity>,
    m3uSeries: List<ChannelEntity>,
    viewModel: SharedIptvViewModel
) {
    val selectedCategory by viewModel.selectedSeriesCategory.collectAsState()
    val categories = remember(xtreamSeries, m3uSeries) {
        val cats = if (xtreamSeries.isNotEmpty()) {
            xtreamSeries.map { it.categoryName }
        } else {
            m3uSeries.map { it.categoryName }
        }
        listOf("Toutes") + cats.distinct().sorted()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CategoryFilterBar(
            categories = categories,
            selected = selectedCategory,
            onSelect = { viewModel.selectSeriesCategory(it) }
        )

        if (xtreamSeries.isEmpty() && m3uSeries.isEmpty()) {
            EmptyStateMessage("Aucune série disponible pour ce profil.")
        } else if (xtreamSeries.isNotEmpty()) {
            val filtered = if (selectedCategory == "Toutes") xtreamSeries
            else xtreamSeries.filter { it.categoryName == selectedCategory }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 115.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.seriesId }) { series ->
                    MobilePosterCard(
                        title = series.name,
                        subtitle = series.categoryName,
                        posterUrl = series.coverUrl,
                        badge = series.rating.takeIf { it.isNotBlank() },
                        onClick = { viewModel.openSeriesDetails(series) }
                    )
                }
            }
        } else {
            val filtered = if (selectedCategory == "Toutes") m3uSeries
            else m3uSeries.filter { it.categoryName == selectedCategory }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 115.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { ep ->
                    MobilePosterCard(
                        title = ep.name,
                        subtitle = ep.categoryName,
                        posterUrl = ep.logoUrl,
                        badge = "Série",
                        onClick = { viewModel.playChannel(ep) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileFavoritesScreen(
    favorites: List<ChannelEntity>,
    onPlay: (ChannelEntity) -> Unit,
    onToggleFavorite: (ChannelEntity) -> Unit
) {
    if (favorites.isEmpty()) {
        EmptyStateMessage("Vous n'avez pas encore ajouté de chaînes ou de films à vos favoris.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favorites, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlay(item) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChannelLogoBox(logoUrl = item.logoUrl, name = item.name)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AbdesColors.PurpleGlow
                            )
                        }
                        IconButton(onClick = { onToggleFavorite(item) }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Retirer des favoris",
                                tint = AbdesColors.PurpleVibrant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileSearchScreen(
    query: String,
    results: List<ChannelEntity>,
    onQueryChange: (String) -> Unit,
    onPlay: (ChannelEntity) -> Unit,
    onToggleFavorite: (ChannelEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher une chaîne, un film, une catégorie...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AbdesColors.PurpleGlow) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Effacer")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        if (query.isBlank()) {
            EmptyStateMessage("Saisissez un mot-clé pour rechercher instantanément dans tout votre catalogue IPTV.")
        } else if (results.isEmpty()) {
            EmptyStateMessage("Aucun résultat trouvé pour « $query ».")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlay(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChannelLogoBox(logoUrl = item.logoUrl, name = item.name)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text(item.categoryName, style = MaterialTheme.typography.bodyMedium, color = AbdesColors.TextSecondary)
                            }
                            IconButton(onClick = { onToggleFavorite(item) }) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (item.isFavorite) AbdesColors.PurpleVibrant else AbdesColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileSettingsScreen(
    profiles: List<IptvProfileEntity>,
    activeProfile: IptvProfileEntity?,
    hiddenCount: Int,
    onSwitchProfile: (Long) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onAddProfile: () -> Unit,
    onRestoreHidden: () -> Unit,
    onClearHistory: () -> Unit,
    onRefreshCatalog: () -> Unit,
    onLoadTestStreams: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profils IPTV Enregistrés", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Button(
                    onClick = onAddProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = AbdesColors.PurplePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ajouter")
                }
            }
        }

        items(profiles, key = { it.id }) { profile ->
            val isSelected = profile.id == activeProfile?.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) AbdesColors.CardElevated else AbdesColors.CardDark
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) AbdesColors.PurplePrimary else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSwitchProfile(profile.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.profileName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Type : ${profile.connectionType}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AbdesColors.PurpleGlow
                        )
                    }
                    IconButton(onClick = { onDeleteProfile(profile.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = AbdesColors.LiveRed)
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = AbdesColors.CardElevated)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Maintenance & Confidentialité", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onRefreshCatalog,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AbdesColors.PurpleGlow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synchroniser le catalogue et l'EPG XMLTV", color = Color.White)
                }
                OutlinedButton(
                    onClick = onRestoreHidden,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restaurer les chaînes masquées ($hiddenCount)", color = Color.White)
                }
                OutlinedButton(
                    onClick = onClearHistory,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Effacer l'historique « Continuer à regarder »", color = Color.White)
                }
                OutlinedButton(
                    onClick = onLoadTestStreams,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Charger la playlist de démonstration publique", color = AbdesColors.PurpleGlow)
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun MobileVideoPlayerScreen(
    viewModel: SharedIptvViewModel,
    channel: ChannelEntity,
    currentEpg: EpgProgramEntity?,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onClosePlayer: () -> Unit
) {
    val playerManager = viewModel.playerManager
    val isPlaying by playerManager.isPlaying.collectAsState()
    val isBuffering by playerManager.isBuffering.collectAsState()
    val errorMsg by playerManager.errorMessageFr.collectAsState()
    val aspectRatio by playerManager.aspectRatio.collectAsState()
    val audioTracks by playerManager.audioTracks.collectAsState()
    val subtitleTracks by playerManager.subtitleTracks.collectAsState()
    val volume by playerManager.volume.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showTracksDialog by remember { mutableStateOf(false) }

    BackHandler {
        onClosePlayer()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.exoPlayer
                    useController = false
                    keepScreenOn = true
                }
            },
            update = { view ->
                view.player = playerManager.exoPlayer
                view.resizeMode = when (aspectRatio) {
                    VideoAspectRatio.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    VideoAspectRatio.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    VideoAspectRatio.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    VideoAspectRatio.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = AbdesColors.PurplePrimary)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Chargement du flux vidéo...", color = Color.White)
            }
        }

        if (errorMsg != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Information de lecture", style = MaterialTheme.typography.titleMedium, color = AbdesColors.LiveRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg!!, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { playerManager.retryPlayback() },
                            colors = ButtonDefaults.buttonColors(containerColor = AbdesColors.PurplePrimary)
                        ) {
                            Text("Réessayer")
                        }
                        OutlinedButton(onClick = onClosePlayer) {
                            Text("Fermer", color = Color.White)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onClosePlayer) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentEpg?.title ?: channel.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AbdesColors.PurpleGlow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { playerManager.cycleAspectRatio() }) {
                            Icon(Icons.Default.AspectRatio, contentDescription = "Ratio d'affichage", tint = Color.White)
                        }
                        IconButton(onClick = { showTracksDialog = true }) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Audio et Sous-titres", tint = Color.White)
                        }
                        IconButton(onClick = onToggleFullscreen) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Plein écran",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Play / Pause / Zap Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.playPreviousChannel() },
                        modifier = Modifier
                            .size(54.dp)
                            .background(AbdesColors.CardDark.copy(alpha = 0.75f), CircleShape)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Chaîne précédente", tint = Color.White)
                    }

                    IconButton(
                        onClick = { playerManager.togglePlayPause() },
                        modifier = Modifier
                            .size(72.dp)
                            .background(AbdesColors.PurplePrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Lecture / Pause",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.playNextChannel() },
                        modifier = Modifier
                            .size(54.dp)
                            .background(AbdesColors.CardDark.copy(alpha = 0.75f), CircleShape)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Chaîne suivante", tint = Color.White)
                    }
                }

                // Bottom Volume & Format Info Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = AbdesColors.PurpleGlow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = volume,
                                onValueChange = { playerManager.setPlayerVolume(it) },
                                modifier = Modifier.width(160.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = AbdesColors.PurpleGlow,
                                    activeTrackColor = AbdesColors.PurplePrimary
                                )
                            )
                        }
                        AssistChip(
                            onClick = { playerManager.cycleAspectRatio() },
                            label = { Text(aspectRatio.labelFr, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = AbdesColors.CardDark)
                        )
                    }
                }
            }
        }
    }

    if (showTracksDialog) {
        AlertDialog(
            onDismissRequest = { showTracksDialog = false },
            containerColor = AbdesColors.CardDark,
            title = { Text("Pistes Audio & Sous-titres", color = Color.White) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("Pistes Audio", color = AbdesColors.PurpleGlow, fontWeight = FontWeight.Bold)
                    }
                    if (audioTracks.isEmpty()) {
                        item { Text("Piste audio par défaut active", color = AbdesColors.TextSecondary) }
                    } else {
                        items(audioTracks, key = { it.id }) { track ->
                            TextButton(
                                onClick = {
                                    playerManager.selectTrack(track)
                                    showTracksDialog = false
                                }
                            ) {
                                Text(
                                    text = (if (track.isSelected) "✓ " else "") + track.label,
                                    color = if (track.isSelected) AbdesColors.PurpleGlow else Color.White
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sous-titres", color = AbdesColors.PurpleGlow, fontWeight = FontWeight.Bold)
                    }
                    item {
                        TextButton(
                            onClick = {
                                playerManager.disableSubtitles()
                                showTracksDialog = false
                            }
                        ) {
                            Text("Désactiver les sous-titres", color = AbdesColors.TextSecondary)
                        }
                    }
                    items(subtitleTracks, key = { it.id }) { track ->
                        TextButton(
                            onClick = {
                                playerManager.selectTrack(track)
                                showTracksDialog = false
                            }
                        ) {
                            Text(
                                text = (if (track.isSelected) "✓ " else "") + track.label,
                                color = if (track.isSelected) AbdesColors.PurpleGlow else Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTracksDialog = false }) {
                    Text("Fermer", color = AbdesColors.PurpleGlow)
                }
            }
        )
    }
}

@Composable
private fun AddIptvProfileDialog(
    onDismiss: () -> Unit,
    onSaveXtream: (String, String, String, String, String) -> Unit,
    onSaveM3uUrl: (String, String, String) -> Unit,
    onSaveM3uFile: (String, Uri, String) -> Unit
) {
    var mode by remember { mutableStateOf(ConnectionType.XTREAM_CODES) }
    var profileName by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var epgUrl by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) selectedFileUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AbdesColors.CardDark,
        title = { Text("Nouveau Profil IPTV", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ConnectionType.entries.forEach { type ->
                        FilterChip(
                            selected = mode == type,
                            onClick = { mode = type },
                            label = { Text(type.labelFr, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AbdesColors.PurplePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Nom du profil (ex: Salon, Mon IPTV)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                when (mode) {
                    ConnectionType.XTREAM_CODES -> {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("URL du serveur (http://...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Nom d'utilisateur") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mot de passe") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ConnectionType.M3U_URL -> {
                        OutlinedTextField(
                            value = m3uUrl,
                            onValueChange = { m3uUrl = it },
                            label = { Text("URL de la playlist M3U / M3U8") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ConnectionType.M3U_FILE -> {
                        OutlinedButton(
                            onClick = { filePicker.launch(arrayOf("*/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedFileUri != null) "Fichier sélectionné ✓" else "Choisir un fichier .m3u / .m3u8",
                                color = Color.White
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = epgUrl,
                    onValueChange = { epgUrl = it },
                    label = { Text("URL EPG XMLTV (Optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (mode) {
                        ConnectionType.XTREAM_CODES -> {
                            if (serverUrl.isNotBlank() && username.isNotBlank()) {
                                onSaveXtream(profileName, serverUrl, username, password, epgUrl)
                            }
                        }
                        ConnectionType.M3U_URL -> {
                            if (m3uUrl.isNotBlank()) {
                                onSaveM3uUrl(profileName, m3uUrl, epgUrl)
                            }
                        }
                        ConnectionType.M3U_FILE -> {
                            selectedFileUri?.let { uri ->
                                onSaveM3uFile(profileName, uri, epgUrl)
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AbdesColors.PurplePrimary)
            ) {
                Text("Enregistrer & Connecter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = AbdesColors.TextSecondary)
            }
        }
    )
}

@Composable
private fun SeriesEpisodesModal(
    series: SeriesEntity,
    episodes: List<com.abdestv.core.model.EpisodeItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onPlayEpisode: (com.abdestv.core.model.EpisodeItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AbdesColors.CardDark,
        title = { Text(series.name, color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (series.plot.isNotBlank()) {
                    Text(
                        text = series.plot,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AbdesColors.TextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AbdesColors.PurplePrimary)
                    }
                } else if (episodes.isEmpty()) {
                    Text("Aucun épisode trouvé pour cette série.", color = AbdesColors.TextSecondary)
                } else {
                    LazyColumn(
                        modifier = Modifier.height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(episodes, key = { it.id }) { ep ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AbdesColors.CardElevated),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlayEpisode(ep) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AbdesColors.PurpleGlow)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "S${ep.seasonNum}E${ep.episodeNum} • ${ep.title}",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (ep.plot.isNotBlank()) {
                                            Text(
                                                text = ep.plot,
                                                color = AbdesColors.TextSecondary,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = AbdesColors.PurpleGlow)
            }
        }
    )
}

@Composable
private fun CategoryFilterBar(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AbdesColors.PurplePrimary,
                    selectedLabelColor = Color.White,
                    containerColor = AbdesColors.CardDark,
                    labelColor = AbdesColors.TextSecondary
                )
            )
        }
    }
}

@Composable
private fun SectionHeaderFr(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText, color = AbdesColors.PurpleGlow)
            }
        }
    }
}

@Composable
private fun MobileLiveChannelCard(
    channel: ChannelEntity,
    epg: EpgProgramEntity?,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(190.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ChannelLogoBox(logoUrl = channel.logoUrl, name = channel.name)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = channel.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AbdesColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = epg?.title ?: "Programme en direct",
                style = MaterialTheme.typography.bodyMedium,
                color = AbdesColors.PurpleGlow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { epg?.progressAt() ?: 0.5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = AbdesColors.PurplePrimary,
                trackColor = AbdesColors.CardElevated
            )
        }
    }
}

@Composable
private fun MobilePosterCard(
    title: String,
    subtitle: String,
    posterUrl: String,
    badge: String?,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .width(125.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(AbdesColors.PurpleBannerBrush),
                contentAlignment = Alignment.Center
            ) {
                if (posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = AbdesColors.PurpleGlow,
                        modifier = Modifier.size(36.dp)
                    )
                }
                if (!badge.isNullOrBlank()) {
                    Surface(
                        color = AbdesColors.PurpleDeep.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = AbdesColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WatchHistoryCard(
    item: WatchHistoryEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AbdesColors.CardDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ChannelLogoBox(logoUrl = item.logoUrl, name = item.title)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.categoryName,
                        fontSize = 11.sp,
                        color = AbdesColors.PurpleGlow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { item.progressFraction.coerceAtLeast(0.15f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = AbdesColors.PurpleVibrant,
                trackColor = AbdesColors.CardElevated
            )
        }
    }
}

@Composable
private fun ChannelLogoBox(logoUrl: String, name: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AbdesColors.CardElevated),
        contentAlignment = Alignment.Center
    ) {
        if (logoUrl.isNotBlank()) {
            AsyncImage(
                model = logoUrl,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        } else {
            Text(
                text = name.take(2).uppercase(),
                color = AbdesColors.PurpleGlow,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun EmptyStateMessage(messageFr: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = messageFr,
            style = MaterialTheme.typography.bodyLarge,
            color = AbdesColors.TextSecondary
        )
    }
}
