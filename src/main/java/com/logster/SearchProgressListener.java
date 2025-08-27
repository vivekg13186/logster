package com.logster;

public interface SearchProgressListener {
    void onResultFound(SearchResult result,int noOfFiles,int processedFile);
    void onSearchStarted();
    void onSearchCompleted(long timeTakenInSeconds);
    void onCancelled();

}
