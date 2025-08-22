package com.logster;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import java.nio.file.*;
import java.util.*;
import java.util.List;

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

    public List<SearchResult> search(String queryStr, int maxHits,Long from ,Long to) throws Exception {
        Query query = parser.parse(QueryParser.escape(queryStr));
        Query timeQuery = LongPoint.newRangeQuery("timestamp", from, to);
        BooleanQuery combinedQuery = new BooleanQuery.Builder()
                .add(timeQuery, BooleanClause.Occur.MUST)    // must match text
                .add(query, BooleanClause.Occur.MUST)    // must be in time range
                .build();
        TopDocs topDocs = searcher.search(combinedQuery, maxHits);
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
}