package software.ulpgc.model;

import java.time.Duration;
import java.time.Year;
import java.util.List;
import java.util.Optional;

public record Title(String id,
                    TitleType type,
                    String primaryTitle,
                    String originalTitle,
                    boolean isAdult,
                    Optional<Year> startYear,
                    Optional<Year> endYear,
                    Optional<Duration> runtimeDuration,
                    List<Genre> genres
) {
    public enum TitleType {
        MOVIE, SHORT
    }
    public enum Genre {
        ACTION, DRAMA, COMEDY, THRILLER, ADVENTURE, ANIMATION, DOCUMENTARY, HORROR
    }
}