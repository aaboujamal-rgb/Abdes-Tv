package com.abdestv.core.parser

import android.util.Xml
import com.abdestv.core.model.EpgProgramEntity
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object XmltvParser {

    fun parse(inputStream: InputStream, profileId: Long, maxPrograms: Int = 5000): List<EpgProgramEntity> {
        val programs = mutableListOf<EpgProgramEntity>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var currentChannelId = ""
        var currentStartMs = 0L
        var currentEndMs = 0L
        var currentTitle = ""
        var currentDesc = ""
        var currentCategory = ""
        var insideProgramme = false

        val nowMs = System.currentTimeMillis()
        val minEndMs = nowMs - 2 * 3600 * 1000L
        val maxStartMs = nowMs + 48 * 3600 * 1000L

        while (eventType != XmlPullParser.END_DOCUMENT && programs.size < maxPrograms) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "programme" -> {
                            insideProgramme = true
                            currentChannelId = parser.getAttributeValue(null, "channel") ?: ""
                            currentStartMs = parseXmltvTimestamp(parser.getAttributeValue(null, "start"))
                            currentEndMs = parseXmltvTimestamp(parser.getAttributeValue(null, "stop"))
                            currentTitle = ""
                            currentDesc = ""
                            currentCategory = ""
                        }
                        "title" -> if (insideProgramme) {
                            currentTitle = parser.nextText().trim()
                        }
                        "desc" -> if (insideProgramme) {
                            currentDesc = parser.nextText().trim()
                        }
                        "category" -> if (insideProgramme) {
                            currentCategory = parser.nextText().trim()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("programme", ignoreCase = true) && insideProgramme) {
                        insideProgramme = false
                        if (currentChannelId.isNotBlank() &&
                            currentTitle.isNotBlank() &&
                            currentEndMs >= minEndMs &&
                            currentStartMs <= maxStartMs
                        ) {
                            programs.add(
                                EpgProgramEntity(
                                    id = "epg_${profileId}_${currentChannelId}_${currentStartMs}",
                                    profileId = profileId,
                                    epgChannelId = currentChannelId,
                                    title = currentTitle,
                                    description = currentDesc,
                                    startTimeEpochMs = currentStartMs,
                                    endTimeEpochMs = currentEndMs,
                                    category = currentCategory
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return programs
    }

    private fun parseXmltvTimestamp(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val trimmed = raw.trim()
        return try {
            if (trimmed.length >= 19 && trimmed.contains(" ")) {
                val sdf = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)
                sdf.parse(trimmed)?.time ?: 0L
            } else if (trimmed.length >= 14) {
                val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(trimmed.substring(0, 14))?.time ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}
