package com.abdestv.tv.ui

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
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

enum class TvSection(val labelFr: String, val icon: ImageVector) {
    HOME("Accueil", Icons.Default.Home),
    LIVE("TV en Direct", Icons.Default.LiveTv),
    MOVIES("Films", Icons.Default.Movie),
    SERIES("Séries", Icons.Default.Tv),
    FAVORITES("Favoris", Icons.Default.Favorite),
    SEARCH("Recherche", Icons.Default.Search),
    SETTINGS("Paramètres", Icons.Default.Settings)
}

/**
 * Modificateur D-Pad pour Android TV : ajoute une bordure lumineuse violette, un léger zoom
 * et la gestion des touches OK / Entrée / Centre de la télécommande.
 */
fun Modifier.tvFocusableCard(
    shape: Shape = RoundedCornerShape(16.dp),
    scaleFocused: Float = 1.05f,
    onClick: () -> Unit
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) scaleFocused else 1.0f, label = "tvScale")

    this
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }
        .border(
            width = if (isFocused) 3.dp else 1.dp,
            color = if (isFocused) AbdesColors.PurpleGlow else Color.White.copy(alpha = 0.08f),
            shape = shape
        )
        .clip(shape)
        .background(if (isFocused) AbdesColors.CardElevated else AbdesColors.CardDark)
        .onKeyEvent { event ->
            if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER ||
                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
            ) {
                onClick()
                true
            } else {
                false
            }
        }
        .focusable()
        .clickable { onClick() }
}

@Composable
fun TvAppRoot(viewModel: SharedIptvViewModel) {
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

    var currentSection by rememberSaveable { mutableStateOf(TvSection.HOME) }
    var showAddProfileModal by rememberSaveable { mutableStateOf(false) }

    val epgByChannelId = remember(epgPrograms) {
        val now = System.currentTimeMillis()
        epgPrograms
            .filter { it.startTimeEpochMs <= now && it.endTimeEpochMs >= now }
            .associateBy { it.epgChannelId }
    }

    if (currentPlaying != null) {
        TvVideoPlayerScreen(
            viewModel = viewModel,
            channel = currentPlaying!!,
            currentEpg = epgByChannelId[currentPlaying!!.epgChannelId],
            onClose = { viewModel.stopAndRecordPlayback() }
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AbdesColors.DeepBlack)
    ) {
        // Left D-Pad Navigation Sidebar
        TvLeftSidebar(
            currentSection = currentSection,
            activeProfileName = activeProfile?.profileName ?: "Aucun profil",
            isSyncing = isSyncing,
            onSelectSection = { currentSection = it },
            onAddProfile = { showAddProfileModal = true },
            onRefresh = { viewModel.refreshActiveProfile() }
        )

        // Main 10-Foot Landscape Content Stage
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            if (activeProfile == null && profiles.isEmpty()) {
                TvWelcomeStage(
                    onConfigureClick = { showAddProfileModal = true },
                    onLoadDemoClick = { viewModel.loadLegalPublicTestStreams() }
                )
            } else {
                when (currentSection) {
                    TvSection.HOME -> TvHomeStage(
                        liveChannels = liveChannels,
                        vodMovies = vodMovies,
                        xtreamSeries = xtreamSeries,
                        m3uSeries = m3uSeries,
                        watchHistory = watchHistory,
                        epgMap = epgByChannelId,
                        onPlayChannel = { ch, pos -> viewModel.playChannel(ch, pos) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onOpenSeries = { viewModel.openSeriesDetails(it) }
                    )
                    TvSection.LIVE -> TvCatalogRowsStage(
                        titleFr = "TV en Direct • Par Catégorie",
                        items = liveChannels,
                        epgMap = epgByChannelId,
                        isPoster = false,
                        onPlay = { viewModel.playChannel(it) },
                        onFavorite = { viewModel.toggleFavorite(it) },
                        onHide = { viewModel.hideChannel(it) }
                    )
                    TvSection.MOVIES -> TvCatalogRowsStage(
                        titleFr = "Films à la Demande (VOD)",
                        items = vodMovies,
                        epgMap = emptyMap(),
                        isPoster = true,
                        onPlay = { viewModel.playChannel(it) },
                        onFavorite = { viewModel.toggleFavorite(it) },
                        onHide = { viewModel.hideChannel(it) }
                    )
                    TvSection.SERIES -> TvSeriesRowsStage(
                        xtreamSeries = xtreamSeries,
                        m3uSeries = m3uSeries,
                        onOpenSeries = { viewModel.openSeriesDetails(it) },
                        onPlayChannel = { viewModel.playChannel(it) }
                    )
                    TvSection.FAVORITES -> TvCatalogRowsStage(
                        titleFr = "Mes Chaînes & Contenus Favoris",
                        items = favorites,
                        epgMap = epgByChannelId,
                        isPoster = false,
                        onPlay = { viewModel.playChannel(it) },
                        onFavorite = { viewModel.toggleFavorite(it) },
                        onHide = { viewModel.hideChannel(it) }
                    )
                    TvSection.SEARCH -> TvSearchStage(
                        query = searchQuery,
                        results = searchResults,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onPlay = { viewModel.playChannel(it) }
                    )
                    TvSection.SETTINGS -> TvSettingsStage(
                        profiles = profiles,
                        activeProfile = activeProfile,
                        hiddenCount = hiddenChannels.size,
                        statusMessage = statusMessage,
                        onSwitchProfile = { viewModel.switchProfile(it) },
                        onDeleteProfile = { viewModel.deleteProfile(it) },
                        onAddProfile = { showAddProfileModal = true },
                        onRestoreHidden = { viewModel.restoreAllHiddenChannels() },
                        onClearHistory = { viewModel.clearHistory() },
                        onRefresh = { viewModel.refreshActiveProfile() },
                        onLoadTestStreams = { viewModel.loadLegalPublicTestStreams() }
                    )
                }
            }
        }
    }

    if (showAddProfileModal) {
        TvAddProfileDialog(
            onDismiss = { showAddProfileModal = false },
            onSaveXtream = { name, url, user, pass, epg ->
                viewModel.loginXtreamCodes(name, url, user, pass, epg)
                showAddProfileModal = false
            },
            onSaveM3uUrl = { name, m3u, epg ->
                viewModel.loginM3uUrl(name, m3u, epg)
                showAddProfileModal = false
            }
        )
    }

    if (selectedSeries != null) {
        TvSeriesEpisodesDialog(
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
private fun TvLeftSidebar(
    currentSection: TvSection,
    activeProfileName: String,
    isSyncing: Boolean,
    onSelectSection: (TvSection) -> Unit,
    onAddProfile: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(235.dp)
            .fillMaxHeight()
            .background(AbdesColors.ObsidianSurface)
            .padding(vertical = 24.dp, horizontal = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ABDES TV",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = activeProfileName,
                        fontSize = 11.sp,
                        color = AbdesColors.PurpleGlow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            TvSection.entries.forEach { section ->
                val isSelected = section == currentSection
                var isFocused by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .onFocusChanged { isFocused = it.isFocused }
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isFocused -> AbdesColors.PurplePrimary
                                isSelected -> AbdesColors.PurpleDeep.copy(alpha = 0.65f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (isFocused) 2.dp else 0.dp,
                            color = if (isFocused) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .focusable()
                        .clickable { onSelectSection(section) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = section.labelFr,
                        tint = if (isFocused || isSelected) Color.White else AbdesColors.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = section.labelFr,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isFocused || isSelected) Color.White else AbdesColors.TextSecondary
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isSyncing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AbdesColors.PurpleGlow,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synchronisation...", fontSize = 11.sp, color = AbdesColors.PurpleGlow)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tvFocusableCard(shape = RoundedCornerShape(12.dp), onClick = onRefresh)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AbdesColors.PurpleGlow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualiser EPG", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tvFocusableCard(shape = RoundedCornerShape(12.dp), onClick = onAddProfile)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nouveau Profil", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TvWelcomeStage(
    onConfigureClick: () -> Unit,
    onLoadDemoClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(640.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(AbdesColors.CardDark)
                .border(2.dp, AbdesColors.PurplePrimary, RoundedCornerShape(26.dp))
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ABDES TV • ANDROID TV",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Expérience IPTV Cinéma optimisée pour télécommande D-Pad. Connectez votre serveur Xtream Codes ou votre playlist M3U / M3U8 pour accéder aux chaînes en direct, films et séries.",
                style = MaterialTheme.typography.bodyLarge,
                color = AbdesColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(
                    modifier = Modifier
                        .tvFocusableCard(shape = RoundedCornerShape(14.dp), onClick = onConfigureClick)
                        .background(AbdesColors.PurplePrimary)
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Text("Ajouter un Profil Xtream / M3U", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .tvFocusableCard(shape = RoundedCornerShape(14.dp), onClick = onLoadDemoClick)
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Text("Charger les Flux Publics de Test", color = AbdesColors.PurpleGlow, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TvHomeStage(
    liveChannels: List<ChannelEntity>,
    vodMovies: List<ChannelEntity>,
    xtreamSeries: List<SeriesEntity>,
    m3uSeries: List<ChannelEntity>,
    watchHistory: List<WatchHistoryEntity>,
    epgMap: Map<String, EpgProgramEntity>,
    onPlayChannel: (ChannelEntity, Long) -> Unit,
    onToggleFavorite: (ChannelEntity) -> Unit,
    onOpenSeries: (SeriesEntity) -> Unit
) {
    val spotlight = liveChannels.firstOrNull() ?: vodMovies.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        if (spotlight != null) {
            item {
                TvHeroSpotlight(
                    channel = spotlight,
                    epg = epgMap[spotlight.epgChannelId],
                    onPlay = { onPlayChannel(spotlight, 0L) },
                    onToggleFavorite = { onToggleFavorite(spotlight) }
                )
            }
        }

        if (watchHistory.isNotEmpty()) {
            item {
                Text(
                    text = "Continuer à regarder",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    items(watchHistory, key = { it.itemId }) { hist ->
                        TvLandscapeCard(
                            title = hist.title,
                            subtitle = "Reprendre • ${hist.categoryName}",
                            logoUrl = hist.logoUrl,
                            progress = hist.progressFraction.coerceAtLeast(0.15f),
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
                Text(
                    text = "Chaînes TV en Direct",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    items(liveChannels.take(25), key = { it.id }) { channel ->
                        val epg = epgMap[channel.epgChannelId]
                        TvLandscapeCard(
                            title = channel.name,
                            subtitle = epg?.title ?: channel.categoryName,
                            logoUrl = channel.logoUrl,
                            progress = epg?.progressAt() ?: 0.5f,
                            onClick = { onPlayChannel(channel, 0L) }
                        )
                    }
                }
            }
        }

        if (vodMovies.isNotEmpty()) {
            item {
                Text(
                    text = "Films Récemment Ajoutés",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    items(vodMovies.take(25), key = { it.id }) { movie ->
                        TvPosterCard(
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
                Text(
                    text = "Séries à la Une",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    if (xtreamSeries.isNotEmpty()) {
                        items(xtreamSeries.take(25), key = { it.seriesId }) { s ->
                            TvPosterCard(
                                title = s.name,
                                subtitle = s.categoryName,
                                posterUrl = s.coverUrl,
                                badge = s.rating.takeIf { it.isNotBlank() },
                                onClick = { onOpenSeries(s) }
                            )
                        }
                    } else {
                        items(m3uSeries.take(25), key = { it.id }) { ep ->
                            TvPosterCard(
                                title = ep.name,
                                subtitle = ep.categoryName,
                                posterUrl = ep.logoUrl,
                                badge = "Série",
                                onClick = { onPlayChannel(ep, 0L) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvHeroSpotlight(
    channel: ChannelEntity,
    epg: EpgProgramEntity?,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(AbdesColors.PurpleBannerBrush)
            .border(1.5.dp, AbdesColors.PurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.75f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AbdesColors.LiveRed, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "SÉLECTION ABDES TV",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = channel.categoryName,
                    color = AbdesColors.PurpleGlow,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = channel.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = epg?.title ?: "Diffusion en haute définition • Flux instantané Media3",
                style = MaterialTheme.typography.bodyLarge,
                color = AbdesColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .tvFocusableCard(shape = RoundedCornerShape(12.dp), onClick = onPlay)
                        .background(AbdesColors.PurplePrimary)
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Regarder Maintenant", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .tvFocusableCard(shape = RoundedCornerShape(12.dp), onClick = onToggleFavorite)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = AbdesColors.PurpleGlow
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (channel.isFavorite) "Retirer des Favoris" else "Ajouter aux Favoris",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvCatalogRowsStage(
    titleFr: String,
    items: List<ChannelEntity>,
    epgMap: Map<String, EpgProgramEntity>,
    isPoster: Boolean,
    onPlay: (ChannelEntity) -> Unit,
    onFavorite: (ChannelEntity) -> Unit,
    onHide: (ChannelEntity) -> Unit
) {
    val grouped = remember(items) {
        items.groupBy { it.categoryName }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "$titleFr (${items.size})",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun contenu disponible dans cette section.", color = AbdesColors.TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                grouped.entries.forEach { (categoryName, categoryItems) ->
                    item(key = categoryName) {
                        Text(
                            text = "$categoryName (${categoryItems.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = AbdesColors.PurpleGlow,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            items(categoryItems, key = { it.id }) { item ->
                                if (isPoster) {
                                    TvPosterCard(
                                        title = item.name,
                                        subtitle = item.categoryName,
                                        posterUrl = item.logoUrl,
                                        badge = item.rating.takeIf { it.isNotBlank() },
                                        onClick = { onPlay(item) }
                                    )
                                } else {
                                    val epg = epgMap[item.epgChannelId]
                                    TvLandscapeCard(
                                        title = item.name,
                                        subtitle = epg?.title ?: item.categoryName,
                                        logoUrl = item.logoUrl,
                                        progress = epg?.progressAt() ?: 0.5f,
                                        onClick = { onPlay(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvSeriesRowsStage(
    xtreamSeries: List<SeriesEntity>,
    m3uSeries: List<ChannelEntity>,
    onOpenSeries: (SeriesEntity) -> Unit,
    onPlayChannel: (ChannelEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Catalogue des Séries",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (xtreamSeries.isEmpty() && m3uSeries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucune série disponible.", color = AbdesColors.TextSecondary)
            }
        } else if (xtreamSeries.isNotEmpty()) {
            val grouped = remember(xtreamSeries) { xtreamSeries.groupBy { it.categoryName } }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                grouped.forEach { (cat, list) ->
                    item(key = cat) {
                        Text(cat, style = MaterialTheme.typography.titleLarge, color = AbdesColors.PurpleGlow)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            items(list, key = { it.seriesId }) { series ->
                                TvPosterCard(
                                    title = series.name,
                                    subtitle = series.categoryName,
                                    posterUrl = series.coverUrl,
                                    badge = series.rating.takeIf { it.isNotBlank() },
                                    onClick = { onOpenSeries(series) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val grouped = remember(m3uSeries) { m3uSeries.groupBy { it.categoryName } }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                grouped.forEach { (cat, list) ->
                    item(key = cat) {
                        Text(cat, style = MaterialTheme.typography.titleLarge, color = AbdesColors.PurpleGlow)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            items(list, key = { it.id }) { ep ->
                                TvPosterCard(
                                    title = ep.name,
                                    subtitle = ep.categoryName,
                                    posterUrl = ep.logoUrl,
                                    badge = "Série",
                                    onClick = { onPlayChannel(ep) }
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
private fun TvSearchStage(
    query: String,
    results: List<ChannelEntity>,
    onQueryChange: (String) -> Unit,
    onPlay: (ChannelEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Recherche Rapide", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Rechercher une chaîne, un film ou une catégorie...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
        ) {
            items(results, key = { it.id }) { item ->
                TvLandscapeCard(
                    title = item.name,
                    subtitle = item.categoryName,
                    logoUrl = item.logoUrl,
                    progress = 0.5f,
                    onClick = { onPlay(item) }
                )
            }
        }
    }
}

@Composable
private fun TvSettingsStage(
    profiles: List<IptvProfileEntity>,
    activeProfile: IptvProfileEntity?,
    hiddenCount: Int,
    statusMessage: String?,
    onSwitchProfile: (Long) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onAddProfile: () -> Unit,
    onRestoreHidden: () -> Unit,
    onClearHistory: () -> Unit,
    onRefresh: () -> Unit,
    onLoadTestStreams: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Paramètres & Gestion des Profils IPTV", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(statusMessage, color = AbdesColors.PurpleGlow, style = MaterialTheme.typography.bodyLarge)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .tvFocusableCard(onClick = onAddProfile)
                        .background(AbdesColors.PurplePrimary)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("+ Ajouter un Profil IPTV", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .tvFocusableCard(onClick = onRefresh)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("Synchroniser Catalogue & EPG", color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .tvFocusableCard(onClick = onRestoreHidden)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("Restaurer Chaînes Masquées ($hiddenCount)", color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .tvFocusableCard(onClick = onClearHistory)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("Effacer l'Historique", color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .tvFocusableCard(onClick = onLoadTestStreams)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text("Flux Publics de Test", color = AbdesColors.PurpleGlow)
                }
            }
        }

        items(profiles, key = { it.id }) { profile ->
            val isSelected = profile.id == activeProfile?.id
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .tvFocusableCard(onClick = { onSwitchProfile(profile.id) })
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = (if (isSelected) "● " else "") + profile.profileName,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isSelected) AbdesColors.PurpleGlow else Color.White
                    )
                    Text(
                        text = "Connexion : ${profile.connectionType}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AbdesColors.TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .tvFocusableCard(onClick = { onDeleteProfile(profile.id) })
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Supprimer", color = AbdesColors.LiveRed)
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun TvVideoPlayerScreen(
    viewModel: SharedIptvViewModel,
    channel: ChannelEntity,
    currentEpg: EpgProgramEntity?,
    onClose: () -> Unit
) {
    val playerManager = viewModel.playerManager
    val isPlaying by playerManager.isPlaying.collectAsState()
    val isBuffering by playerManager.isBuffering.collectAsState()
    val errorMsg by playerManager.errorMessageFr.collectAsState()
    val aspectRatio by playerManager.aspectRatio.collectAsState()
    val audioTracks by playerManager.audioTracks.collectAsState()
    val subtitleTracks by playerManager.subtitleTracks.collectAsState()

    var showOsd by remember { mutableStateOf(true) }
    var showTracksModal by remember { mutableStateOf(false) }

    BackHandler {
        if (showOsd) {
            showOsd = false
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_CHANNEL_UP -> {
                            viewModel.playNextChannel()
                            showOsd = true
                            true
                        }
                        KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            viewModel.playPreviousChannel()
                            showOsd = true
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            playerManager.togglePlayPause()
                            showOsd = true
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (!showOsd) {
                                showOsd = true
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
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
                Text("Chargement du flux en direct...", color = Color.White)
            }
        }

        if (errorMsg != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AbdesColors.CardDark.copy(alpha = 0.95f))
                    .border(2.dp, AbdesColors.LiveRed, RoundedCornerShape(18.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Erreur de lecture", style = MaterialTheme.typography.titleLarge, color = AbdesColors.LiveRed)
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMsg!!, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .tvFocusableCard(onClick = { playerManager.retryPlayback() })
                            .background(AbdesColors.PurplePrimary)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Réessayer", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .tvFocusableCard(onClick = onClose)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Retour au catalogue", color = Color.White)
                    }
                }
            }
        }

        if (showOsd) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                        )
                    )
                    .padding(horizontal = 36.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = currentEpg?.title ?: channel.categoryName,
                                style = MaterialTheme.typography.titleMedium,
                                color = AbdesColors.PurpleGlow
                            )
                        }
                        Text(
                            text = "Format : ${aspectRatio.labelFr}",
                            color = AbdesColors.TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        TvOsdButton(
                            label = if (isPlaying) "Pause" else "Lecture",
                            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            onClick = { playerManager.togglePlayPause() }
                        )
                        TvOsdButton(
                            label = "Chaîne -",
                            icon = Icons.Default.SkipPrevious,
                            onClick = { viewModel.playPreviousChannel() }
                        )
                        TvOsdButton(
                            label = "Chaîne +",
                            icon = Icons.Default.SkipNext,
                            onClick = { viewModel.playNextChannel() }
                        )
                        TvOsdButton(
                            label = "Format Écran",
                            icon = Icons.Default.AspectRatio,
                            onClick = { playerManager.cycleAspectRatio() }
                        )
                        TvOsdButton(
                            label = "Audio / Sous-titres",
                            icon = Icons.Default.Subtitles,
                            onClick = { showTracksModal = true }
                        )
                        TvOsdButton(
                            label = if (channel.isFavorite) "Favori ✓" else "+ Favori",
                            icon = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            onClick = { viewModel.toggleFavorite(channel) }
                        )
                        TvOsdButton(
                            label = "Fermer",
                            icon = Icons.Default.Close,
                            onClick = onClose
                        )
                    }
                }
            }
        }
    }

    if (showTracksModal) {
        AlertDialog(
            onDismissRequest = { showTracksModal = false },
            containerColor = AbdesColors.CardDark,
            title = { Text("Sélection des Pistes Audio & Sous-titres", color = Color.White) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text("Audio", color = AbdesColors.PurpleGlow, fontWeight = FontWeight.Bold) }
                    items(audioTracks, key = { it.id }) { track ->
                        TextButton(onClick = {
                            playerManager.selectTrack(track)
                            showTracksModal = false
                        }) {
                            Text(
                                text = (if (track.isSelected) "✓ " else "") + track.label,
                                color = Color.White
                            )
                        }
                    }
                    item { Text("Sous-titres", color = AbdesColors.PurpleGlow, fontWeight = FontWeight.Bold) }
                    item {
                        TextButton(onClick = {
                            playerManager.disableSubtitles()
                            showTracksModal = false
                        }) {
                            Text("Désactiver les sous-titres", color = AbdesColors.TextSecondary)
                        }
                    }
                    items(subtitleTracks, key = { it.id }) { track ->
                        TextButton(onClick = {
                            playerManager.selectTrack(track)
                            showTracksModal = false
                        }) {
                            Text(
                                text = (if (track.isSelected) "✓ " else "") + track.label,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTracksModal = false }) {
                    Text("Fermer", color = AbdesColors.PurpleGlow)
                }
            }
        )
    }
}

@Composable
private fun TvOsdButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .tvFocusableCard(shape = RoundedCornerShape(12.dp), onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TvLandscapeCard(
    title: String,
    subtitle: String,
    logoUrl: String,
    progress: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(235.dp)
            .height(132.dp)
            .tvFocusableCard(shape = RoundedCornerShape(16.dp), onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AbdesColors.DeepBlack),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    } else {
                        Text(
                            text = title.take(2).uppercase(),
                            color = AbdesColors.PurpleGlow,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AbdesColors.PurpleGlow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = AbdesColors.PurplePrimary,
                trackColor = AbdesColors.DeepBlack
            )
        }
    }
}

@Composable
private fun TvPosterCard(
    title: String,
    subtitle: String,
    posterUrl: String,
    badge: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(155.dp)
            .tvFocusableCard(shape = RoundedCornerShape(16.dp), onClick = onClick)
    ) {
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
                    modifier = Modifier.size(42.dp)
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
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
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

@Composable
private fun TvAddProfileDialog(
    onDismiss: () -> Unit,
    onSaveXtream: (String, String, String, String, String) -> Unit,
    onSaveM3uUrl: (String, String, String) -> Unit
) {
    var isXtream by remember { mutableStateOf(true) }
    var profileName by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var epgUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AbdesColors.CardDark,
        title = { Text("Configurer un Profil IPTV (Android TV)", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isXtream,
                        onClick = { isXtream = true },
                        label = { Text("Xtream Codes API") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AbdesColors.PurplePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isXtream,
                        onClick = { isXtream = false },
                        label = { Text("Playlist M3U / M3U8") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AbdesColors.PurplePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Nom du profil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isXtream) {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("URL du serveur Xtream (http://...)") },
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
                } else {
                    OutlinedTextField(
                        value = m3uUrl,
                        onValueChange = { m3uUrl = it },
                        label = { Text("URL de la playlist M3U / M3U8") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    if (isXtream && serverUrl.isNotBlank() && username.isNotBlank()) {
                        onSaveXtream(profileName, serverUrl, username, password, epgUrl)
                    } else if (!isXtream && m3uUrl.isNotBlank()) {
                        onSaveM3uUrl(profileName, m3uUrl, epgUrl)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AbdesColors.PurplePrimary)
            ) {
                Text("Connecter")
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
private fun TvSeriesEpisodesDialog(
    series: SeriesEntity,
    episodes: List<com.abdestv.core.model.EpisodeItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onPlayEpisode: (com.abdestv.core.model.EpisodeItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AbdesColors.CardDark,
        title = { Text("${series.name} • Épisodes", color = Color.White) },
        text = {
            if (isLoading) {
                CircularProgressIndicator(color = AbdesColors.PurplePrimary)
            } else if (episodes.isEmpty()) {
                Text("Aucun épisode disponible.", color = AbdesColors.TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(episodes, key = { it.id }) { ep ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .tvFocusableCard(
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = { onPlayEpisode(ep) }
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "S${ep.seasonNum}E${ep.episodeNum} • ${ep.title}",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
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
