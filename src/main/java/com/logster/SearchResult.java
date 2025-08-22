package com.logster;



public class SearchResult {
    final String filePath;
    final int lineNumber;
    final String lineContent;

    public SearchResult(String filePath, int lineNumber, String lineContent) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.lineContent = lineContent;
    }





}