package com.logster;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.prefs.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;

import static com.logster.SettingsDialog.KEY_EXTENSION;

class FileIndexer implements PreferenceChangeListener {
    private static final Logger LOGGER = LogManager.getLogger();


    private final IndexWriter writer;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final List<String> indexExtension = new ArrayList<>();
    private final DateDetection dateDetection;

    private final Logster logster;
    private final LocalDateTime startDateTime;
    public FileIndexer(String indexDir, DateDetection dateDetection, Logster logster) throws IOException {
        this.dateDetection = dateDetection;
        startDateTime = LocalDateTime.now();
        this.logster=logster;
        Util.deleteDirectory(new File(indexDir));
        SettingsDialog.prefs.addPreferenceChangeListener(this);
        loadPreference();
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        writer = new IndexWriter(dir, config);

    }

    private void loadPreference() {
        String text = SettingsDialog.prefs.get(KEY_EXTENSION, ".log,.txt");
        String[] result = text.split(",");
        indexExtension.clear();
        indexExtension.addAll(Arrays.asList(result));
        LOGGER.info(String.join(",", indexExtension));
    }

    public boolean isIndexableFile(String name) {
        for (String e : indexExtension) {
            if (name.endsWith(e)) return true;
        }

        return false;
    }

    public void indexFolder(File folder) {
        if (folder.listFiles() == null) return;
        LOGGER.info("Scanning folder {}", folder);


        for (File f : Objects.requireNonNull(folder.listFiles())) {

            if (f.isDirectory()) {
                indexFolder(f);
            } else if (isIndexableFile(f.getName())) {
                executor.submit(() -> {
                    try {
                        indexFile(f);
                    } catch (IOException _) {
                    }
                });
            }
        }
    }

    private void indexFile(File file) throws IOException {

        LOGGER.info("Indexing file {}", file);
        long diff = ChronoUnit.MINUTES.between(startDateTime,LocalDateTime.now());

        String msg1 ="Indexing file " +file;
        String msg =(diff>0) ? diff + " (mins) | "+msg1 : msg1;


        SwingUtilities.invokeLater(() -> logster.updateStatus(msg));
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                Document doc = new Document();
                doc.add(new StringField("filePath", file.getAbsolutePath(), Field.Store.YES));
                doc.add(new IntPoint("lineNumber", lineNo));
                doc.add(new StoredField("lineNumberStored", lineNo));
                doc.add(new TextField("content", line, Field.Store.YES));


                LocalDateTime timestamp = dateDetection.parseLine(line);
                if (timestamp != null) {

                    long value = Util.toEpochMilli(timestamp);
                    doc.add(new LongPoint("timestamp", value));   // for range queries
                    doc.add(new StoredField("timestampStored", value)); // store for retrieval

                }


                synchronized (writer) {
                    writer.addDocument(doc);
                }
                lineNo++;
            }
        }
    }

    public void close() throws IOException {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e);
        }
        writer.close();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        loadPreference();
    }
}