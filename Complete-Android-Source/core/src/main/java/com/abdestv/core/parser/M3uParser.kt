package com.abdestv.core.parser

import com.abdestv.core.model.ChannelEntity
import com.abdestv.core.model.ContentType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class M3uParseResult(
    val channels: List<ChannelEntity>,
    val detectedXmltvUrl: String?
)

object M3uParser {

    private val ATTR_REGEX = Regex("([a-zA-Z0-9_-]+)=[\"']([^\"']*)[\"']")
    private val SERIES_REGEX = Regex("(?i)\\bS\\d{1,2}\\s*E\\d{1,3}\\b")

    fun parse(inputStream: InputStream, profileId: Long): M3uParseResult {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val channels = mutableListOf<ChannelEntity>()
        var detectedXmltvUrl: String? = null

        var currentName = ""
        var currentLogo = ""
        var currentGroup = "TV en Direct"
        var currentTvgId = ""
        var currentUserAgent = "AbdesTV/2.0 (Android; Media3)"
        var index = 0

        reader.forEachLine { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachLine

            when {
                line.startsWith("#EXTM3U", ignoreCase = true) -> {
                    val attrs = parseAttributes(line)
                    detectedXmltvUrl = attrs["url-tvg"] ?: attrs["x-tvg-url"] ?: attrs["tvg-url"]
                }
                line.startsWith("#EXTINF:", ignoreCase = true) -> {
                    val attrs = parseAttributes(line)
                    currentTvgId = attrs["tvg-id"] ?: attrs["tvg-name"] ?: ""
                    currentLogo = attrs["tvg-logo"] ?: attrs["logo"] ?: ""
                    currentGroup = (attrs["group-title"] ?: attrs["group"] ?: "Général")
                        .replace(";", " • ")
                        .ifBlank { "Général" }
                    val commaIdx = line.lastIndexOf(',')
                    val extractedTitle = if (commaIdx >= 0 && commaIdx < line.length - 1) {
                        line.substring(commaIdx + 1).trim()
                    } else {
                        attrs["tvg-name"] ?: "Flux #${index + 1}"
                    }
                    currentName = extractedTitle.ifBlank { "Flux #${index + 1}" }
                }
                line.startsWith("#EXTGRP:", ignoreCase = true) -> {
                    val grp = line.substringAfter(":").trim()
                    if (grp.isNotBlank()) currentGroup = grp
                }
                line.startsWith("#EXTVLCOPT:http-user-agent=", ignoreCase = true) -> {
                    val ua = line.substringAfter("=").trim()
                    if (ua.isNotBlank()) currentUserAgent = ua
                }
                !line.startsWith("#") -> {
                    val streamUrl = line
                    if (streamUrl.startsWith("http://", ignoreCase = true) ||
                        streamUrl.startsWith("https://", ignoreCase = true) ||
                        streamUrl.startsWith("rtsp://", ignoreCase = true) ||
                        streamUrl.startsWith("rtmp://", ignoreCase = true)
                    ) {
                        index++
                        val contentType = classifyContentType(currentName, currentGroup, streamUrl)
                        val cleanId = "m3u_${profileId}_${streamUrl.hashCode()}_$index"
                        channels.add(
                            ChannelEntity(
                                id = cleanId,
                                profileId = profileId,
                                streamId = index,
                                name = currentName.ifBlank { "Chaîne #$index" },
                                categoryId = currentGroup.lowercase(),
                                categoryName = currentGroup,
                                streamUrl = streamUrl,
                                logoUrl = currentLogo,
                                epgChannelId = currentTvgId,
                                contentType = contentType.name,
                                userAgent = currentUserAgent
                            )
                        )
                        currentName = ""
                        currentLogo = ""
                        currentTvgId = ""
                    }
                }
            }
        }
        return M3uParseResult(
            channels = channels,
            detectedXmltvUrl = detectedXmltvUrl
        )
    }

    private fun parseAttributes(line: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        ATTR_REGEX.findAll(line).forEach { match ->
            val key = match.groupValues[1].lowercase()
            val value = match.groupValues[2].trim()
            map[key] = value
        }
        return map
    }

    private fun classifyContentType(name: String, group: String, url: String): ContentType {
        val lowerGroup = group.lowercase()
        val lowerUrl = url.lowercase().substringBefore("?")
        return when {
            SERIES_REGEX.containsMatchIn(name) ||
                lowerGroup.contains("série") ||
                lowerGroup.contains("serie") ||
                lowerUrl.contains("/series/") -> ContentType.SERIES
            lowerGroup.contains("vod") ||
                lowerGroup.contains("film") ||
                lowerGroup.contains("movie") ||
                lowerGroup.contains("cinéma") ||
                lowerUrl.contains("/movie/") ||
                lowerUrl.endsWith(".mp4") ||
                lowerUrl.endsWith(".mkv") ||
                lowerUrl.endsWith(".avi") -> ContentType.MOVIE
            else -> ContentType.LIVE
        }
    }

    fun buildPublicLegalSampleM3u(): String = listOf(
        "#EXTM3U",
        "#EXTINF:-1 tvg-id=\"F24FR\" tvg-name=\"France 24 Français (Flux Test HLS)\" tvg-logo=\"https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/France24.svg/512px-France24.svg.png\" group-title=\"Actualités & Direct\",France 24 Français (Flux Public HLS)",
        "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "#EXTINF:-1 tvg-id=\"ARTE_TEST\" tvg-name=\"Arte Cinéma Test 1080p\" tvg-logo=\"https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Arte_Logo_2017.svg/512px-Arte_Logo_2017.svg.png\" group-title=\"Culture & Découverte\",Arte Cinéma (Flux Test Multi-Audio HLS)",
        "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
        "#EXTINF:-1 tvg-id=\"SPORT_UHD\" tvg-name=\"Abdes Sport 4K Démo\" tvg-logo=\"https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400&q=80\" group-title=\"Sports HD\",Abdes Sport Direct 60fps (Flux Test HLS)",
        "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
        "#EXTINF:-1 tvg-id=\"BBB_VOD\" tvg-name=\"Big Buck Bunny (Open Movie)\" tvg-logo=\"https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster_big.jpg/400px-Big_buck_bunny_poster_big.jpg\" group-title=\"Films • Animation\",Big Buck Bunny (1080p VOD)",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "#EXTINF:-1 tvg-id=\"SINTEL_VOD\" tvg-name=\"Sintel (Blender Open Movie)\" tvg-logo=\"https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Sintel_poster.jpg/400px-Sintel_poster.jpg\" group-title=\"Films • Fantastique\",Sintel (1080p VOD)",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        "#EXTINF:-1 tvg-id=\"TOS_VOD\" tvg-name=\"Tears of Steel (Sci-Fi)\" tvg-logo=\"https://upload.wikimedia.org/wikipedia/commons/thumb/7/70/Tos-poster.png/400px-Tos-poster.png\" group-title=\"Films • Science-Fiction\",Tears of Steel (1080p VOD)",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        "#EXTINF:-1 tvg-id=\"COSMOS_S01E01\" tvg-name=\"Cosmos Laundromat S01E01\" tvg-logo=\"https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80\" group-title=\"Séries • Science-Fiction\",Cosmos Laundromat S01E01 - Premier Cycle",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "#EXTINF:-1 tvg-id=\"COSMOS_S01E02\" tvg-name=\"Cosmos Laundromat S01E02\" tvg-logo=\"https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80\" group-title=\"Séries • Science-Fiction\",Cosmos Laundromat S01E02 - L'Évasion",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
    ).joinToString("\n")
}
