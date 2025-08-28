package com.logster.search;


import java.util.List;

public class SearchResult  {
    private  String filePath;
    private  int lineNumber;
    private  String lineContent;
    private List<MatchPosition> matchPositions;
    private int lineCount;

    public SearchResult(String filePath, int lineNumber, String lineContent, List<MatchPosition> matchPositions) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.lineContent = lineContent;
        this.matchPositions = matchPositions;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLineContent() {
        return lineContent;
    }

    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }

    public List<MatchPosition> getMatchPositions() {
        return matchPositions;
    }

    public void setMatchPositions(List<MatchPosition> matchPositions) {
        this.matchPositions = matchPositions;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }
}