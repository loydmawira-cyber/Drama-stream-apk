package com.example.data.local

import com.example.R
import com.example.data.local.entity.EpisodeEntity
import com.example.data.local.entity.SeriesEntity

object SeedData {

    fun getInitialSeries(): List<SeriesEntity> {
        return listOf(
            SeriesEntity(
                id = "series_ceo_secret_bride",
                title = "The CEO's Secret Bride",
                synopsis = "Forced into a contract marriage with the ruthless billionaire Julian Vance to save her family's bankrupt gallery, Evelyn never expected his icy exterior hid a protective passion. When a sudden gala scandal threatens to expose their false wedding, secrets from both their pasts explode.",
                coverResId = R.drawable.poster_ceo_bride,
                category = "Billionaire Romance",
                tagsString = "Billionaire, Fake Marriage, Secret Identity, High Drama",
                isTrending = true,
                isNew = false,
                totalEpisodes = 10
            ),
            SeriesEntity(
                id = "series_moonbound_alpha_revenge",
                title = "Moonbound: Alpha's Revenge",
                synopsis = "Exiled from her pack after false accusations of treason, Aria returns on the eve of the Blood Moon with ancient lunar power. But Alpha Kieran, her former mate who cast her out, discovers her scent has changed—and she is now the destined Moon Queen he must kneel before.",
                coverResId = R.drawable.poster_moonbound,
                category = "Werewolf Romance",
                tagsString = "Werewolf, Alpha, Forbidden Mate, Sweet Revenge",
                isTrending = true,
                isNew = true,
                totalEpisodes = 12
            ),
            SeriesEntity(
                id = "series_replaced_at_altar",
                title = "Replaced at the Altar",
                synopsis = "Standing in her designer wedding gown, Claire watches in horror as her fiancé switches vows with her deceitful stepsister. Left abandoned at the altar, she accepts an audacious marriage proposal from the enigmatic billionaire sitting in the front row—the ex-fiancé's most feared rival.",
                coverResId = R.drawable.poster_replaced_altar,
                category = "Revenge",
                tagsString = "Betrayal, Revenge, Instant Wedding, Power Couple",
                isTrending = false,
                isNew = true,
                totalEpisodes = 8
            ),
            SeriesEntity(
                id = "series_triplet_babies_billionaire_daddy",
                title = "My Triplet Babies' Billionaire Daddy",
                synopsis = "Four years after an unforgettable one-night encounter with a mysterious stranger, single mom Maya walks into a corporate board meeting only to discover the conglomerate's elusive CEO is the spitting image of her three chaotic toddler triplets.",
                coverResId = R.drawable.poster_triplet_babies,
                category = "Secret Baby",
                tagsString = "Secret Baby, Billionaire Daddy, Family Romance, Romance Comedy",
                isTrending = true,
                isNew = false,
                totalEpisodes = 10
            ),
            SeriesEntity(
                id = "series_heiress_they_discarded",
                title = "The Heiress They Discarded",
                synopsis = "Treated as a lowly maid and cast out into the winter rain by her adoptive family, Serena is revealed to be the sole rightful heir to the multibillion-dollar Sterling Empire. Now she returns to reclaim her dynasty, one board meeting at a time.",
                coverResId = R.drawable.poster_heiress_discarded,
                category = "Identity Swap",
                tagsString = "Identity Swap, Hidden Heiress, Revenge, Luxury Drama",
                isTrending = false,
                isNew = true,
                totalEpisodes = 9
            )
        )
    }

    fun getInitialEpisodes(packageName: String): List<EpisodeEntity> {
        val seriesList = getInitialSeries()
        val episodes = mutableListOf<EpisodeEntity>()
        val baseVideoUri = "android.resource://$packageName/${R.raw.sample_video}"

        val episodeTitles = mapOf(
            "series_ceo_secret_bride" to listOf(
                "The Billionaire's Proposal",
                "Contract Under the Moonlight",
                "A Diamond Mask",
                "The Penthouse Encounter",
                "Whispers at the Gala",
                "Trapped in the Elevator",
                "The Rival's Threat",
                "Confession in the Rain",
                "The Blackmail Tape",
                "Forever His Bride"
            ),
            "series_moonbound_alpha_revenge" to listOf(
                "Exile of the Silver Wolf",
                "Blood Moon Rising",
                "The Alpha's Call",
                "Shadows in the Mist",
                "The Ancient Mark",
                "Challenging the Pack",
                "The Hidden Mate",
                "Battle at Midnight",
                "The Alpha Kneels",
                "Lunar Eclipse",
                "Queen of the Wilds",
                "Eternal Moon Alliance"
            ),
            "series_replaced_at_altar" to listOf(
                "Betrayal in White",
                "The Mysterious Guest",
                "Signing the Deed",
                "Revenge Banquet",
                "His Dangerous Smile",
                "The Stepsister's Panic",
                "The Corporate Takeover",
                "A Queen's Triumph"
            ),
            "series_triplet_babies_billionaire_daddy" to listOf(
                "A Mysterious Encounter",
                "Three Little Troublemakers",
                "Face to Face with the CEO",
                "The DNA Test",
                "Playtime at Headquarters",
                "Mischief and Romance",
                "Protecting the Triplets",
                "The Grandfather's Blessing",
                "Jealous Ex Returns",
                "One Happy Family"
            ),
            "series_heiress_they_discarded" to listOf(
                "Cast into the Rain",
                "The Golden Crest",
                "Return to the Gala",
                "The Sterling Empire",
                "Unmasking the Imposter",
                "Frozen Stocks",
                "The Chairman's Will",
                "A Family's Ruin",
                "Crown of the Heiress"
            )
        )

        for (series in seriesList) {
            val titles = episodeTitles[series.id] ?: emptyList()
            for (epNum in 1..series.totalEpisodes) {
                val epTitle = titles.getOrNull(epNum - 1) ?: "Episode $epNum"
                val isFree = epNum <= 3
                val cost = if (isFree) 0 else 50
                episodes.add(
                    EpisodeEntity(
                        id = "${series.id}_ep_$epNum",
                        seriesId = series.id,
                        episodeNumber = epNum,
                        title = epTitle,
                        durationSeconds = 60 + (epNum * 3 % 30),
                        coinCost = cost,
                        isUnlocked = isFree,
                        videoUrl = baseVideoUri
                    )
                )
            }
        }

        return episodes
    }
}
