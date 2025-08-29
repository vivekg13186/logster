package com.logster.search;


import java.util.List;

public record SearchResult(String filePath, int lineNumber, String lineContent, List<MatchPosition> matchPositions) {


}