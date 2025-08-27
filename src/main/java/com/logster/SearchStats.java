package com.logster;

import java.util.List;

public class SearchStats {

    public final long timeInSeconds;
    public final List<SearchResult> searchResults;

    public SearchStats(long timeInSeconds, List<SearchResult> searchResults) {
        this.timeInSeconds = timeInSeconds;
        this.searchResults = searchResults;
    }
}
