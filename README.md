# DramaStream 🎬✨

DramaStream is a high-performance native Android micro-drama streaming app crafted with **Kotlin** and **Jetpack Compose**. Designed for the modern vertical video era (inspired by platforms like ReelShort and DramaBox), DramaStream serves high-tension, cliffhanger-driven series tailored for fast emotional payoffs.

---

## 🌟 Key Features

- **9:16 Vertical Video Player**: Full-screen, swipe-native playback using `VerticalPager` powered by **Media3 ExoPlayer**, with auto-advance and buffer pre-fetching.
- **Episodic Monetization & Coin Economy**:
  - First 3 episodes free for every series.
  - Episodes 4+ cost 50 coins to unlock.
  - Seamless **Paywall Modal** (Material 3 `ModalBottomSheet`) for coin top-up packs ($4.99 for 500 coins, $9.99 for 1,100 coins, $19.99 for 2,500 coins).
  - **VIP Membership** ($19.99/mo) for instant unlimited access to the entire catalog without coin deductions.
- **Account Registration & Supabase Integration**:
  - Register with **Google** or Email/Password powered by Supabase.
  - **+200 Free Welcome Coins** instantly credited to your wallet upon registration!
  - `SUPABASE_URL` and `SUPABASE_ANON_KEY` managed securely via Secrets Gradle Plugin and `.env`.
- **Refer & Earn Program**:
  - Unique shareable referral code (e.g. `DRAMA-LOYD-77`) with Android share sheet integration.
  - **+150 Free Coins** awarded when a friend claims your code.
  - Friend bonus input to claim +150 coins.
  - Real-time referral tracker (Friends Joined & Coins Earned).
  - Built-in simulation mode for live demo testing in the emulator.
- **Cinematic Dark Theme**: Obsidian black backdrop (`#0A0A0A`) with electric amethysts (`#7C3AED`) and hot pink CTAs (`#EC4899`).
- **Catalog of 5 Micro-Dramas**:
  1. *The CEO's Secret Bride* (Billionaire Romance, 10 Episodes)
  2. *Moonbound: Alpha's Revenge* (Werewolf Romance, 12 Episodes)
  3. *Replaced at the Altar* (Revenge, 8 Episodes)
  4. *My Triplet Babies' Billionaire Daddy* (Secret Baby, 10 Episodes)
  5. *The Heiress They Discarded* (Identity Swap, 9 Episodes)
- **Watch History & Progress Tracking**: Real-time progress bar, continue-watching carousels, and persistent resume points.
- **Local Persistence & Reactive Flow**: **Room SQLite** for relational series/episode content and **DataStore Preferences** for the user's wallet.

---

## 🏗️ Architecture

The app follows **Clean Architecture** and **MVVM** principles:

```
UI (Jetpack Compose, M3, Navigation Compose)
       │
Presentation (ViewModels & StateFlow)
       │
Domain (UseCases: UnlockEpisodeUseCase, GetSeriesUseCase, UserWallet)
       │
Data (DramaRepositoryImpl, UserDataRepositoryImpl, Room SQLite, DataStore)
```

- **DI Layer**: Service Locator pattern with `ViewModelProvider.Factory`.
- **Media Engine**: `androidx.media3:media3-exoplayer`.
- **Image Engine**: `io.coil-kt:coil-compose`.

---

## 🚀 Building & Running

### Requirements
- Android Studio Ladybug / Meerkat or later
- Android SDK 36 (Android 15)
- Java 17 / 21
- Minimum SDK: Android 8.0 (API 26)

### Build Commands
```bash
# Compile debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest
```

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
