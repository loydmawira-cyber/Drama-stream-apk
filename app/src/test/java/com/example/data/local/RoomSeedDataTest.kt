package com.example.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomSeedDataTest {

    @Test
    fun `verify all 5 required series exist in seed data`() {
        val seriesList = SeedData.getInitialSeries()
        assertEquals(5, seriesList.size)

        val titles = seriesList.map { it.title }
        assertTrue(titles.contains("The CEO's Secret Bride"))
        assertTrue(titles.contains("Moonbound: Alpha's Revenge"))
        assertTrue(titles.contains("Replaced at the Altar"))
        assertTrue(titles.contains("My Triplet Babies' Billionaire Daddy"))
        assertTrue(titles.contains("The Heiress They Discarded"))
    }

    @Test
    fun `verify first 3 episodes of each series are free and subsequent are 50 coins`() {
        val episodes = SeedData.getInitialEpisodes("com.example")
        val episodesBySeries = episodes.groupBy { it.seriesId }

        for ((seriesId, epList) in episodesBySeries) {
            val sorted = epList.sortedBy { it.episodeNumber }
            assertTrue(sorted.size >= 8)

            // Episodes 1..3 must be free (coinCost == 0, isUnlocked == true)
            for (i in 0 until 3) {
                val ep = sorted[i]
                assertEquals("Episode ${ep.episodeNumber} of $seriesId should cost 0 coins", 0, ep.coinCost)
                assertTrue("Episode ${ep.episodeNumber} of $seriesId should be unlocked", ep.isUnlocked)
            }

            // Episodes 4+ must cost 50 coins and be locked
            for (i in 3 until sorted.size) {
                val ep = sorted[i]
                assertEquals("Episode ${ep.episodeNumber} of $seriesId should cost 50 coins", 50, ep.coinCost)
                assertTrue("Episode ${ep.episodeNumber} of $seriesId should be locked initially", !ep.isUnlocked)
            }
        }
    }
}
