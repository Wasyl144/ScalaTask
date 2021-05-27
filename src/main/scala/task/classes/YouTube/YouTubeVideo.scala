package task

final case class YouTubeVideo(
    videoId: String,
    dirtySubtitles: String,
    plainSubtitles: String,
    wikipediaDetails: List[WikipediaArticles]
)