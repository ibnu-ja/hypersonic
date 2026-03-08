package moe.seiga.hypersonic.common;

import java.time.Duration;
import java.util.List;

public record SongInfo(
        String uri,
        String title,
        List<String> artists,
        Duration duration,
        Integer queueId,
        Integer queuePos,
        String album
) {
    public String artistsAsString() {
        if (artists == null || artists.isEmpty()) {
            return null;
        }
        return String.join(", ", artists);
    }
}
