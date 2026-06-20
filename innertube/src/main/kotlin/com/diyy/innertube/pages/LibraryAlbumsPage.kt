package com.diyy.innertube.pages

import com.diyy.innertube.models.Album
import com.diyy.innertube.models.AlbumItem
import com.diyy.innertube.models.Artist
import com.diyy.innertube.models.ArtistItem
import com.diyy.innertube.models.MusicResponsiveListItemRenderer
import com.diyy.innertube.models.MusicTwoRowItemRenderer
import com.diyy.innertube.models.PlaylistItem
import com.diyy.innertube.models.SongItem
import com.diyy.innertube.models.YTItem
import com.diyy.innertube.models.oddElements
import com.diyy.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            return AlbumItem(
                        browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                        playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId ?: return null,
                        title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                        artists = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                        thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
        }
    }
}
