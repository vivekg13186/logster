package com.logster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.logster.SettingsDialog.KEY_EXTENSION;

public class SimpleFileSearch {


    public SimpleFileSearch() {

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

    public void search(String filePath, String queryStr, SearchProgressListener listener, SearchController controller) throws Exception {
        long timeTakenInSeconds;


        Path rootDir = Paths.get(filePath);
        Pattern pattern = Pattern.compile(queryStr);

        Instant start = Instant.now();
        AtomicInteger resultCount = new AtomicInteger(0);
        AtomicBoolean limitReached = new AtomicBoolean(false);

        int maxResults = 10000;
        listener.onSearchStarted();

        List<Path> allPaths;
        try (Stream<Path> stream = Files.walk(Paths.get(filePath))) {
            allPaths = stream .toList();
        }
        int batchSize = 100;
        int allFiles = allPaths.size();

        for (int i = 0; i < allPaths.size(); i += batchSize) {

            if (controller.isCancelled()) break;

            List<Path> batch = allPaths.subList(i, Math.min(i + batchSize, allPaths.size()));

            int finalI = i;
            batch.parallelStream().forEach(path -> {
                if (controller.isCancelled()) {
                    listener.onCancelled();
                    return;
                }

                if(!Files.isRegularFile(path)||isIgnored(path)||isBinary(path))return;

                List<SearchResult> results = searchFile(path, pattern);
                for (SearchResult r : results) {
                    if (controller.isCancelled()) break;
                    int count = resultCount.incrementAndGet();
                    if (count <= maxResults) {
                        listener.onResultFound(r,allFiles, finalI);
                    } else {
                        listener.onCancelled();
                        controller.cancel();
                        break;
                    }
                }
            });
        }





        Instant end = Instant.now();
        timeTakenInSeconds = Duration.between(start, end).toSeconds();
        listener.onSearchCompleted(timeTakenInSeconds);


    }

    private static List<SearchResult> searchFile(Path path, Pattern pattern) {
        List<SearchResult> matches = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    List<MatchPosition> matchPositions = new ArrayList<>();
                    do {
                        matchPositions.add(new MatchPosition(matcher.start(), matcher.end()));
                    } while (matcher.find());
                    matches.add(new SearchResult(path.toAbsolutePath().toString(), lineNum, line, matchPositions));
                }

                lineNum++;

            }
        } catch (IOException ignored) {
            // Skip unreadable files
        }
        return matches;
    }

    private boolean isIgnored(Path path) {
        for (String ignore : indexExtension) {
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


}
