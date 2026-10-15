# DramaStream — Vertical Micro-Drama Streaming App

DramaStream is a native Android micro-drama streaming platform delivering high-tension, cliffhanger-driven episodic series in an immersive 9:16 vertical feed, powered by an episode unlocking economy, Room persistence, and offline/streaming video playback.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core architectural and UX decisions have been aligned based on user choices and platform guidelines:

- **Confirmed Decision 1 (Video Source Strategy)**: Bundled sample MP4 video with intelligent online CDN streaming fallback. Bundled local video in `res/raw/sample_video.mp4` guarantees immediate zero-latency playback even offline, while remote URL support allows real dynamic streams.
- **Confirmed Decision 2 (Dependency Injection Architecture)**: Clean Architecture MVVM with Service Locator & ViewModel Factory pattern. This provides clean decoupled layers (Data, Domain, Presentation) with 100% build reliability in the container without brittle annotation-processor plugin sync issues.
- **Confirmed Decision 3 (Paywall Trigger on Locked Episodes)**: Auto-pausing video playback and smoothly sliding up the Material 3 `ModalBottomSheet` paywall whenever the user swipes to a locked episode (Episode 4+ for non-VIPs).
- **Confirmed Decision 4 (Database & Identity)**: Room database for local caching of series, episodes, watch history, and unlocked states; Android Jetpack DataStore Preferences for user coin wallet balance (default 500 coins) and VIP subscription state.

---

## 1. Overview & Core Concept

- **What It Does**: Serves addictive, short-form episodic dramas (60–90 seconds per episode) in a vertical swipe-native interface. Users browse trending micro-series across genres like Billionaire Romance, Werewolf Revenge, and Identity Swap, watch the first 3 episodes free, unlock subsequent episodes for 50 coins, or subscribe to VIP for unlimited access.
- **Target Audience / Persona**: Women aged 25–45 captivated by romance, dramatic revenge, and identity-swap twists, who crave quick emotional payoffs on mobile during commutes, breaks, or evening downtime.
- **Key Value**: Frictionless, continuous vertical viewing with instant chapter progression, transparent coin-based microtransactions, and personalized watchlists.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Discovery & Browse Flow**:
   - The user opens DramaStream to a rich, cinematic Home feed.
   - Featured billboard carousel at the top spotlights trending series (*"The CEO's Secret Bride"*).
   - Filter chips and genre shelves categorize series (*Billionaire Romance*, *Werewolf Romance*, *Revenge*, *Identity Swap*).
   - Bottom navigation allows effortless switching between **Home**, **Search / Browse**, **My List**, and **Profile**.
2. **Series Deep-Dive Flow**:
   - Tapping any series card opens the **Series Detail Screen** with an expansive hero banner, genre badges, synopsis, and an episode carousel/grid.
   - Visual badges mark episodes as Free, Unlocked, or Locked (with coin badge).
   - Tapping "Start Watching" or any episode launches the immersive player directly at that episode.
3. **Immersive Vertical Video Player Flow**:
   - 9:16 full-screen playback with gesture controls (tap to play/pause, double tap to like).
   - Minimalist HUD: Coin balance pill in top-right, episode title, progress slider, episode selector bottom-sheet trigger, and like/bookmark floating actions.
   - Swipe up to advance to the next episode, swipe down to view the previous episode.
   - Auto-advance on episode completion.
4. **Paywall & Monetization Flow**:
   - Swiping to or selecting a locked episode (Episode 4+ without VIP) pauses playback and slides up the **Paywall ModalBottomSheet**.
   - Offers two clear paths: "Unlock with 50 Coins" (deducts from wallet) or "Go VIP" ($19.99/mo for unlimited library access).
   - Quick "Top Up Coins" packs ($5 for 500 coins, $10 for 1,100 coins, $20 for 2,500 coins) simulate realistic in-app purchases with instant wallet crediting and auto-resuming playback.
5. **Account & Profile Flow**:
   - Displays real-time coin balance, VIP membership badge with expiration, watch history ("Continue Watching" with progress percentages), and favorite series.
   - Settings for theme details, playback auto-advance toggle, and coin reset/top-up debugging tools.

### Visual Identity & Theme

- **Aesthetic Direction**: Cinematic luxury dark aesthetic. Bold, glamorous, and high-contrast, inspired by premium streaming experiences.
- **Color Palette**:
  - Background: Deep Obsidian Black (`#0A0A0A`) and Midnight Surface (`#14141E`).
  - Primary Accent: Royal Amethyst Purple (`#7C3AED` / `#6B21A8`).
  - Secondary / CTA Accent: Vibrant Hot Pink (`#EC4899`) for primary unlock buttons and VIP tags.
  - Text & Accents: Pure White (`#FFFFFF`), Muted Slate (`#94A3B8`), and Gold Coin (`#FBBF24`).
- **Typography & Hierarchy**:
  - Display Titles: Material 3 `headlineMedium` / `titleLarge` with bold editorial weight.
  - Body & Meta: Clean, legible `bodyMedium` and `labelSmall` with high legibility on dark backgrounds.
- **Component Styling & Layout**:
  - Glassmorphic translucent pills for player overlays (`Color(0x66000000)` with border).
  - Rounded corners (`16.dp` for cards, `28.dp` for buttons and sheets).
  - High-touch targets (>= 48dp) and responsive paddings honoring window insets.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Player Stack with Media3 ExoPlayer & Compose VerticalPager**:
  - *Chosen Approach*: Jetpack Compose `VerticalPager` synchronized with an ExoPlayer pool.
  - *Why*: Delivers TikTok/ReelShort-style swipe latency with instant buffer pre-fetching for adjacent episodes.
  - *Alternatives Considered*: Single player instance with manual touch interceptors (prone to gesture conflicts and jerky swipe visuals).
- **Decision 2: Room Pre-population & Seed Data**:
  - *Chosen Approach*: Automated Room `RoomDatabase.Callback` populating all 5 requested series (50+ episodes) on first launch, combined with sample raw video and verified fallback assets.
  - *Why*: Instant out-of-the-box immersion without waiting for network requests or empty screens.
- **Decision 3: Dual Storage (Room for Catalog/History + DataStore for Wallet)**:
  - *Chosen Approach*: Relational Room database for Series/Episode entities, bookmark relations, and watch timestamps; DataStore Preferences for reactive, atomic coin balance and VIP subscription state.
  - *Why*: Clean separation of transactional user identity/wallet state from catalog content.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │  HomeScreen  │  │ DetailScreen  │  │ PlayerScreen │  │ProfileView │ │
│  └──────┬───────┘  └───────┬───────┘  └──────┬───────┘  └─────┬──────┘ │
│         │                  │                 │                │        │
│  ┌──────┴──────────────────┴─────────────────┴────────────────┴──────┐ │
│  │                 Reusable M3 UI Components & Sheets                │ │
│  │   • PaywallSheet    • SeriesCard    • EpisodeItem   • CoinBadge   │ │
│  └─────────────────────────────────┬─────────────────────────────────┘ │
└────────────────────────────────────┼───────────────────────────────────┘
                                     │ StateFlow / Actions
┌────────────────────────────────────▼───────────────────────────────────┐
│                           Presentation Layer                           │
│  ┌──────────────────────┐   ┌───────────────────┐   ┌────────────────┐ │
│  │    HomeViewModel     │   │  DetailViewModel  │   │PlayerViewModel │ │
│  └──────────┬───────────┘   └─────────┬─────────┘   └────────┬───────┘ │
└─────────────┼─────────────────────────┼──────────────────────┼─────────┘
              │                         │                      │
┌─────────────▼─────────────────────────▼──────────────────────▼─────────┐
│                              Domain Layer                              │
│  • GetSeriesUseCase     • GetEpisodeListUseCase    • UnlockEpisodeUC   │
│  • GetUserWalletUseCase • PurchaseCoinsUseCase     • SetVipStatusUC    │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                               Data Layer                               │
│  ┌─────────────────────────┐             ┌───────────────────────────┐ │
│  │    DramaRepository      │             │     UserDataRepository    │ │
│  └──────────┬──────────────┘             └─────────────┬─────────────┘ │
│             │                                          │               │
│  ┌──────────▼──────────────┐             ┌─────────────▼─────────────┐ │
│  │   Room SQLite Database  │             │   Jetpack DataStore Prefs │ │
│  │ • SeriesDao             │             │ • Coin Balance (Int)      │ │
│  │ • EpisodeDao            │             │ • Is VIP (Boolean)        │ │
│  │ • WatchHistoryDao       │             │ • Settings                │ │
│  └─────────────────────────┘             └───────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model Entities

- **`SeriesEntity`**: `id: String` (PK), `title: String`, `synopsis: String`, `coverUrl: String`, `category: String`, `tags: List<String>`, `isTrending: Boolean`, `isNew: Boolean`, `totalEpisodes: Int`.
- **`EpisodeEntity`**: `id: String` (PK), `seriesId: String` (FK), `episodeNumber: Int`, `title: String`, `durationSeconds: Int`, `coinCost: Int` (50, 0 for 1..3), `isUnlocked: Boolean`, `videoUrl: String`.
- **`WatchHistoryEntity`**: `id: String` (PK), `seriesId: String`, `episodeId: String`, `lastPositionMs: Long`, `timestamp: Long`.
- **`UserWallet`**: `coins: Int` (default 500), `isVip: Boolean` (default false), `vipExpiresAt: Long?`.

### Key Verification & Deliverables Plan

1. **Gradle & Manifest Configuration**:
   - `build.gradle.kts`, `app/build.gradle.kts` with KSP, Room, DataStore, Media3 ExoPlayer, Coil, Navigation Compose.
   - `AndroidManifest.xml` with Internet, AccessNetworkState, and Hardware Acceleration.
2. **Clean MVVM Core**:
   - Entities, DAOs, Room database with pre-population callback.
   - UserPreferences DataStore manager.
   - Repositories, Domain models, and UseCases (`UnlockEpisodeUseCase`, `GetSeriesUseCase`, etc.).
3. **UI Components & Screens**:
   - Full M3 dark theme with custom gradients, typography, and button styles.
   - `PaywallModalBottomSheet` with coin packages, VIP subscription CTA, and instant unlock.
   - `PlayerScreen` with vertical swipe paging, auto-advance, HUD overlay, and locked gate.
   - `HomeScreen`, `DetailScreen`, `ProfileScreen` with full navigation.
4. **App Icon & Branding**:
   - Adaptive icon foreground with "D" + play icon vector, deep purple to pink gradient background, and Play Store 512x512 asset.
5. **CI/CD & Documentation**:
   - `.github/workflows/android.yml`, `README.md`, `LICENSE`, `keystore.properties`, `signing.gradle.kts`.
6. **Unit & Logic Tests**:
   - Unit tests for `UnlockEpisodeUseCase`, coin deduction, and VIP bypass.
   - Database seed verification test.
