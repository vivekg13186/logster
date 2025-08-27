package com.logster;


import java.util.List;

public class SearchResult {
    final String filePath;
    final int lineNumber;
    private  String lineContent;
    final List<MatchPosition> matchPositions;

    public SearchResult(String filePath, int lineNumber, String lineContent,List<MatchPosition> positions) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.lineContent = lineContent;
        this.matchPositions = positions;
    }

    public String getLineContent() {
        return lineContent;
    }

    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }
}