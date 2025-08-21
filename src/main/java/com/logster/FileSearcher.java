package com.logster;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
class FileSearcher {
    private final IndexSearcher searcher;
    private final QueryParser parser;

    public FileSearcher(String indexDir) throws Exception {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        DirectoryReader reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
        parser = new QueryParser("content", new StandardAnalyzer());
    }

    public List<SearchResult> search(String queryStr, int maxHits) throws Exception {
        Query query = parser.parse(QueryParser.escape(queryStr));
        TopDocs topDocs = searcher.search(query, maxHits);
        List<SearchResult> results = new ArrayList<>();

        for (ScoreDoc sd : topDocs.scoreDocs) {
            Document doc = searcher.storedFields().document(sd.doc); // updated for Lucene 10+
            results.add(new SearchResult(
                    doc.get("filePath"),
                    Integer.parseInt(doc.get("lineNumberStored")),
                    doc.get("content")
            ));
        }
        return results;
    }

    public List<SearchResult> searchByTime(Date start, Date end) throws Exception {
        Query timeQuery = LongPoint.newRangeQuery("timestamp", start.getTime(), end.getTime());
        TopDocs topDocs = searcher.search(timeQuery, 1000);
        List<SearchResult> results = new ArrayList<>();

        for (ScoreDoc sd : topDocs.scoreDocs) {
            Document doc = searcher.storedFields().document(sd.doc); // updated
            results.add(new SearchResult(
                    doc.get("filePath"),
                    Integer.parseInt(doc.get("lineNumberStored")),
                    doc.get("content")
            ));
        }
        return results;
    }
}