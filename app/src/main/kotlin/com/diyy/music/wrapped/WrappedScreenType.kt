package com.diyy.music.wrapped

sealed class WrappedScreenType {
    data object Welcome : WrappedScreenType()
    data object MinutesTease : WrappedScreenType()
    data object MinutesReveal : WrappedScreenType()
    data object TotalSongs : WrappedScreenType()
    data object TopSongReveal : WrappedScreenType()
    data object Top5Songs : WrappedScreenType()
    data object TotalAlbums : WrappedScreenType()
    data object TopAlbumReveal : WrappedScreenType()
    data object Top5Albums : WrappedScreenType()
    data object TotalArtists : WrappedScreenType()
    data object TopArtistReveal : WrappedScreenType()
    data object Top5Artists : WrappedScreenType()
    data object Playlist : WrappedScreenType()
    data object Conclusion : WrappedScreenType()
}
