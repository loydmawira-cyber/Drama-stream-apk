package com.example.ui.player

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.domain.model.Episode
import com.example.ui.components.CoinBadge
import com.example.ui.components.PaywallSheet
import com.example.ui.theme.CoinGold
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VerticalPlayerScreen(
    seriesId: String,
    initialEpisodeNumber: Int,
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(seriesId, initialEpisodeNumber) {
        viewModel.initPlayer(seriesId, initialEpisodeNumber)
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paywallSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState.isLoading || uiState.episodes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DramaBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PinkAccent)
        }
        return
    }

    val episodes = uiState.episodes
    val pagerState = rememberPagerState(
        initialPage = (initialEpisodeNumber - 1).coerceIn(0, episodes.size - 1),
        pageCount = { episodes.size }
    )

    // Sync Pager state with ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onEpisodePageChanged(page)
        }
    }

    // Media3 ExoPlayer Instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
        }
    }

    var progressFraction by remember { mutableFloatStateOf(0f) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(10000L) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.onEpisodeFinished()
                    scope.launch {
                        val next = pagerState.currentPage + 1
                        if (next < episodes.size) {
                            pagerState.animateScrollToPage(next)
                        }
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Update player source when current episode or unlock state changes
    val currentEp = episodes.getOrNull(pagerState.currentPage)
    val isCurrentUnlocked = currentEp != null && uiState.isEpisodeUnlocked(currentEp)

    LaunchedEffect(currentEp, isCurrentUnlocked) {
        if (currentEp != null) {
            if (isCurrentUnlocked) {
                val mediaItem = MediaItem.fromUri(Uri.parse(currentEp.videoUrl))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = uiState.isPlaying
            } else {
                exoPlayer.pause()
            }
        }
    }

    LaunchedEffect(uiState.isPlaying) {
        if (isCurrentUnlocked) {
            exoPlayer.playWhenReady = uiState.isPlaying
        } else {
            exoPlayer.pause()
        }
    }

    // Progress polling loop
    LaunchedEffect(exoPlayer, isCurrentUnlocked) {
        while (true) {
            if (isCurrentUnlocked && exoPlayer.isPlaying) {
                val cur = exoPlayer.currentPosition
                val dur = exoPlayer.duration.coerceAtLeast(1L)
                currentPositionMs = cur
                durationMs = dur
                progressFraction = (cur.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                viewModel.updateProgress(cur, dur)
            }
            delay(250)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DramaBlack,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Vertical Pager with full-screen pages
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vertical_video_pager")
            ) { pageIndex ->
                val episode = episodes[pageIndex]
                val isUnlocked = uiState.isEpisodeUnlocked(episode)

                Box(modifier = Modifier.fillMaxSize()) {
                    if (pageIndex == pagerState.currentPage && isUnlocked) {
                        // Real Media3 PlayerView for active unlocked page
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { view ->
                                view.player = exoPlayer
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { viewModel.togglePlayPause() }
                                )
                        )
                    } else {
                        // Blurred poster placeholder with Locked overlay
                        Box(modifier = Modifier.fillMaxSize()) {
                            uiState.series?.coverResId?.let { coverId ->
                                Image(
                                    painter = painterResource(id = coverId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blur(if (!isUnlocked) 18.dp else 0.dp)
                                )
                            }

                            // Dark Scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x99000000))
                            )

                            if (!isUnlocked) {
                                // Locked Gate Overlay in center
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x4D000000))
                                            .border(2.dp, PinkAccent, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = PinkAccent,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Episode ${episode.episodeNumber} is Locked",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Unlock with 50 Coins or get DramaStream VIP",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = { viewModel.unlockEpisode(episode) },
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("center_unlock_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PinkAccent)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LockOpen,
                                            contentDescription = null,
                                            tint = DramaBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Unlock with 50 Coins",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = DramaBlack
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom & Top Gradient Scrims for HUD readability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xCC000000), Color.Transparent)
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xCC000000), Color(0xF20A0A0A))
                                )
                            )
                    )
                }
            }

            // HUD: Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = uiState.series?.title ?: "DramaStream",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "Episode ${pagerState.currentPage + 1} of ${episodes.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PinkAccent
                        )
                    }
                }

                CoinBadge(
                    coinBalance = uiState.userWallet.coinBalance,
                    isVip = uiState.userWallet.isVip,
                    onClick = onNavigateToProfile
                )
            }

            // HUD: Right Side Floating Actions
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like Button
                FloatingActionButtonPill(
                    icon = if (uiState.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (uiState.isLiked) "12.4k" else "12.3k",
                    iconTint = if (uiState.isLiked) PinkAccent else Color.White,
                    onClick = { viewModel.toggleLike() }
                )

                // Bookmark / Watchlist
                FloatingActionButtonPill(
                    icon = if (uiState.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    label = "Save",
                    iconTint = if (uiState.isBookmarked) CoinGold else Color.White,
                    onClick = { viewModel.toggleBookmark() }
                )

                // Share Button
                FloatingActionButtonPill(
                    icon = Icons.Default.Share,
                    label = "Share",
                    iconTint = Color.White,
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Share link copied!") }
                    }
                )

                // Episodes Drawer Button
                FloatingActionButtonPill(
                    icon = Icons.Default.FormatListNumbered,
                    label = "Episodes",
                    iconTint = Color.White,
                    onClick = onNavigateBack
                )
            }

            // HUD: Bottom Info & Scrubbing Progress Bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                currentEp?.let { ep ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PinkAccent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EP ${ep.episodeNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DramaBlack
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ep.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = PinkAccent,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Swipe up for next episode",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "${currentPositionMs / 1000}s / ${durationMs / 1000}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Center Play/Pause indicator when paused
            AnimatedVisibility(
                visible = !uiState.isPlaying && isCurrentUnlocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Paywall Modal Bottom Sheet when swiped to locked episode
        if (uiState.showPaywall) {
            PaywallSheet(
                targetEpisode = uiState.lockedTargetEpisode ?: currentEp,
                currentCoinBalance = uiState.userWallet.coinBalance,
                isVip = uiState.userWallet.isVip,
                sheetState = paywallSheetState,
                onDismiss = { viewModel.dismissPaywall() },
                onUnlockWithCoins = { ep -> viewModel.unlockEpisode(ep) },
                onPurchaseCoins = { amount -> viewModel.purchaseCoins(amount) },
                onSubscribeVip = { viewModel.subscribeVip() },
                onRegisterClick = onNavigateToProfile,
                onReferralClick = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun FloatingActionButtonPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x66000000)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = Color.White
        )
    }
}
