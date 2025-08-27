package com.logster;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;

import static com.logster.SettingsDialog.KEY_EXTENSION;

class FileIndexer implements PreferenceChangeListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int BATCH_SIZE = 1000;

    private final IndexWriter writer;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final List<String> indexExtension = new ArrayList<>();
    private final DateDetection dateDetection;
    private final Logster logster;
    private final LocalDateTime startDateTime;

    public FileIndexer(String indexDir, DateDetection dateDetection, Logster logster) throws IOException {
        this.dateDetection = dateDetection;
        this.logster = logster;
        this.startDateTime = LocalDateTime.now();

        Util.deleteDirectory(new File(indexDir));
        SettingsDialog.prefs.addPreferenceChangeListener(this);
        loadPreference();

        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
        config.setRAMBufferSizeMB(256.0);
        config.setUseCompoundFile(false);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        this.writer = new IndexWriter(dir, config);
    }

    private void loadPreference() {
        String text = SettingsDialog.prefs.get(KEY_EXTENSION, ".log,.txt");
        indexExtension.clear();
        indexExtension.addAll(Arrays.asList(text.split(",")));
        LOGGER.info("Indexable extensions: {}", String.join(",", indexExtension));
    }

    public boolean isIndexableFile(String name) {
        return indexExtension.stream().anyMatch(name::endsWith);
    }

    public void indexFolder(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;

        LOGGER.info("Scanning folder {}", folder);
        for (File f : files) {
            if (f.isDirectory()) {
                indexFolder(f);
            } else if (isIndexableFile(f.getName())) {
                executor.submit(() -> {
                    try {
                        indexFile(f);
                    } catch (IOException e) {
                        LOGGER.error("Failed to index file {}", f, e);
                    }
                });
            }
        }
    }

    private void indexFile(File file) throws IOException {
        LOGGER.info("Indexing file {}", file);
        long diff = ChronoUnit.MINUTES.between(startDateTime, LocalDateTime.now());
        String msg = (diff > 0) ? diff + " mins | Indexing " + file : "Indexing " + file;

        SwingUtilities.invokeLater(() -> logster.updateStatus(msg));

        List<Document> batch = new ArrayList<>();
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
                    doc.add(new LongPoint("timestamp", value));
                    doc.add(new StoredField("timestampStored", value));
                }

                batch.add(doc);
                if (batch.size() >= BATCH_SIZE) {
                    synchronized (writer) {
                        writer.addDocuments(batch);
                    }
                    batch.clear();
                }
                lineNo++;
            }
        }

        if (!batch.isEmpty()) {
            synchronized (writer) {
                writer.addDocuments(batch);
            }
        }
    }

    public void close() throws IOException {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Executor interrupted", e);
            Thread.currentThread().interrupt();
        }
        writer.close();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        loadPreference();
    }
}