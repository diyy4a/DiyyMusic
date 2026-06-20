package com.diyy.music.playback

enum class AndroidAutoSection(val id: String) {
    LIKED("liked"),
    SONGS("songs"),
    ARTISTS("artists"),
    ALBUMS("albums"),
    PLAYLISTS("playlists"),
}

fun serializeSections(sections: List<Pair<AndroidAutoSection, Boolean>>): String =
    sections.joinToString(",") { (section, enabled) -> "${section.id}:$enabled" }

fun deserializeSections(raw: String): List<Pair<AndroidAutoSection, Boolean>> {
    if (raw.isBlank()) return AndroidAutoSection.entries.map { it to true }
    return raw.split(",").mapNotNull { token ->
        val parts = token.split(":")
        if (parts.size != 2) return@mapNotNull null
        val section = AndroidAutoSection.entries.find { it.id == parts[0] } ?: return@mapNotNull null
        section to (parts[1].toBooleanStrictOrNull() ?: true)
    }
}
