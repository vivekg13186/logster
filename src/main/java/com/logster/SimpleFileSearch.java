package com.logster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.logster.SettingsDialog.KEY_EXTENSION;

public class SimpleFileSearch {

    private long timeTakenInSeconds;
    public SimpleFileSearch(){

        loadPreference();
    }
    private final List<String> indexExtension = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger();
    private void loadPreference() {
        String text = SettingsDialog.prefs.get(KEY_EXTENSION, ".log,.txt");
        indexExtension.clear();
        indexExtension.addAll(Arrays.asList(text.split(",")));
        LOGGER.info("Indexable extensions: {}", String.join(",", indexExtension));
    }

    public List<SearchResult> search(String filePath,String queryStr, int maxHits) throws Exception{
        timeTakenInSeconds=0;
        List<SearchResult> result = new ArrayList<>();
        Path rootDir= Paths.get(filePath);
        TextSearchQuery searchQuery = new TextSearchQuery(queryStr);

        Instant start = Instant.now();

        try (Stream<Path> paths = Files.walk(rootDir)) {


             result = paths.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> !isIgnored(path))
                    .filter(path -> !isBinary(path))
                    .flatMap(path -> searchFile(path, searchQuery).stream())
                    .collect(Collectors.toList());
        }

        Instant end = Instant.now();
        timeTakenInSeconds = Duration.between(start, end).toSeconds();
        return  result;
    }
    private static List<SearchResult> searchFile(Path path,TextSearchQuery searchQuery ) {
        List<SearchResult> matches = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                boolean match = searchQuery.match(line);
                if(match){
                    matches.add(new SearchResult(path.toAbsolutePath().toString(),lineNum,line));
                }
                lineNum++;
            }
        } catch (IOException ignored) {
            // Skip unreadable files
        }
        return matches;
    }
    private   boolean isIgnored(Path path) {
        for (String ignore :indexExtension ) {
            if (path.toString().contains(ignore)) {
                return false;
            }
        }
        return true;
    }
    private static boolean isBinary(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[512];
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) return false;

            int nonPrintable = 0;
            for (int i = 0; i < bytesRead; i++) {
                byte b = buffer[i];
                if (b < 0x09 || (b > 0x0D && b < 0x20) || b > 0x7E) {
                    nonPrintable++;
                }
            }
            return nonPrintable > bytesRead * 0.3;
        } catch (IOException e) {
            return true;
        }
    }

    public long getTimeTakenInSeconds() {
        return timeTakenInSeconds;
    }
}
