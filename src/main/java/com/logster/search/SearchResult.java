package com.logster.search;


import java.util.List;

public class SearchResult  {
    private final String filePath;
    private final int lineNumber;
    private final String lineContent;
    private final List<MatchPosition> matchPositions;


    public SearchResult(String filePath, int lineNumber, String lineContent, List<MatchPosition> matchPositions) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.lineContent = lineContent;
        this.matchPositions = matchPositions;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLineContent() {
        return lineContent;
    }

    public List<MatchPosition> getMatchPositions() {
        return matchPositions;
    }


}