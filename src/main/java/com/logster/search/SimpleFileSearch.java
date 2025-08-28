package com.logster.search;

import com.logster.Logster;
import com.logster.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


import static java.nio.file.Files.walk;

public class SimpleFileSearch {


    private final List<String> ignoreExtension;
    public SimpleFileSearch(List<String> ignoreExtension) {
        this.ignoreExtension = ignoreExtension;

    }


    private static final Logger logger = LoggerFactory.getLogger(SimpleFileSearch.class);



    public void search(String filePath, String queryStr, SearchProgressListener listener, SearchController controller, DateDetection dateDetection, long startTime, long endTime) throws Exception {
        logger.error("file {} query {}",filePath,queryStr);
        long timeTakenInSeconds;



        Pattern pattern = Pattern.compile(queryStr);

        Instant start = Instant.now();
        AtomicInteger resultCount = new AtomicInteger(0);


        int maxResults = 10000;
        listener.onSearchStarted();

        List<Path> allPaths;
        try (Stream<Path> stream = walk(Paths.get(filePath))) {
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

                    return;
                }

                if(!Files.isRegularFile(path)||isIgnored(path)||isBinary(path))return;

                List<SearchResult> results = searchFile(path, pattern,dateDetection,startTime,endTime);
                for (SearchResult r : results) {
                    if (controller.isCancelled()){

                        break;
                    }
                    int count = resultCount.incrementAndGet();
                    if (count <= maxResults) {
                        listener.onResultFound(r,allFiles, finalI);
                    } else {
                        controller.cancel();
                        listener.onMaxLimit(maxResults);
                        break;
                    }
                }
            });
        }

        Instant end = Instant.now();
        timeTakenInSeconds = Duration.between(start, end).toSeconds();
        listener.onSearchCompleted(timeTakenInSeconds);


    }

    private static List<SearchResult> searchFile(Path path, Pattern pattern,DateDetection dateDetection,long startTime,long endTime) {
        List<SearchResult> matches = new ArrayList<>();
        int lineNum = 1;
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;

            while ((line = br.readLine()) != null) {

                if(dateDetection!=null){
                    LocalDateTime lineTime = dateDetection.parseLine(line);
                    if(lineTime!=null){
                          long diff = Util.toEpochMilli(lineTime);
                          if(diff<startTime || diff>endTime){
                              break; //not in our time range
                          }
                    }else{
                        break;//not time in line
                    }
                }
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
        for(SearchResult m :matches){
            m.setLineCount(lineNum-1);
        }
        return matches;
    }

    private boolean isIgnored(Path path) {
        for (String ignore : ignoreExtension) {
            if (path.toString().contains(ignore)) {
                return true;
            }
        }
        return false;
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
