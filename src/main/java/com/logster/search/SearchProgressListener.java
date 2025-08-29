package com.logster.search;

public interface SearchProgressListener {
    void onResultFound(SearchResult result);
    void onSearchStarted();
    void onSearchCompleted(long timeTakenInSeconds);


    void onMaxLimit();

}
