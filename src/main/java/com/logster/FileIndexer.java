package com.logster;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.*;

class FileIndexer {private final Pattern timePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final IndexWriter writer;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public FileIndexer(String indexDir) throws IOException {
        Util.deleteDirectory(new File(indexDir));

        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        writer = new IndexWriter(dir, config);
    }

    public  boolean isIndexableFile(String name){
        return  name.endsWith(".java")||name.endsWith(".txt")||name.endsWith(".csv")||name.endsWith(".log")||name.endsWith(".xml")||name.endsWith(".json");
    }
    public void indexFolder(File folder)   {


        for (File f : Objects.requireNonNull(folder.listFiles())) {

            if (f.isDirectory()) {
                indexFolder(f);
            } else if (isIndexableFile(f.getName())) {
                executor.submit(() -> {
                    try { indexFile(f); } catch (IOException _) {   }
                });
            }
        }
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing file"+file);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                Document doc = new Document();
                doc.add(new StringField("filePath", file.getAbsolutePath(), Field.Store.YES));
                doc.add(new IntPoint("lineNumber", lineNo));
                doc.add(new StoredField("lineNumberStored", lineNo));
                doc.add(new TextField("content", line, Field.Store.YES));

                // Check for timestamp
                Matcher m = timePattern.matcher(line);
                if (m.find()) {
                    try {
                        Date timestamp = sdf.parse(m.group());
                        doc.add(new LongPoint("timestamp", timestamp.getTime()));   // for range queries
                        doc.add(new StoredField("timestampStored", timestamp.getTime())); // store for retrieval
                    } catch (Exception e) { /* ignore parse errors */ }
                }

                synchronized (writer) { writer.addDocument(doc); }
                lineNo++;
            }
        }
    }

    public void close() throws IOException {
        executor.shutdown();
        try { executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); } catch (InterruptedException _) {  }
        writer.close();
    }
}