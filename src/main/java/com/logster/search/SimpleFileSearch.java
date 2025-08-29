package com.logster.search;

import com.logster.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.logster.config.AppConfiguration.getMaxResult;


public class SimpleFileSearch {


    private final List<String> ignoreExtension;
    public SimpleFileSearch(List<String> ignoreExtension) {
        this.ignoreExtension = ignoreExtension;

    }


    private static final Logger logger = LoggerFactory.getLogger(SimpleFileSearch.class);



    public void search(String filePath, String queryStr, SearchProgressListener listener, SearchController controller, DateDetection dateDetection, long startTime, long endTime) throws Exception {
        logger.info("file {} query {}",filePath,queryStr);
        List<Path> batch = new ArrayList<>(1000);
        long timeTakenInSeconds;


        Pattern pattern = Pattern.compile(queryStr);

        Instant start = Instant.now();
        AtomicInteger resultCount = new AtomicInteger(0);



        listener.onSearchStarted();

        Files.walkFileTree(Path.of(filePath), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                batch.add(file);
                if (batch.size() == 1000) {
                    processBatch(new ArrayList<>(batch),controller,pattern,dateDetection,startTime,endTime,resultCount,listener);
                    batch.clear();
                }

                return controller.isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (!batch.isEmpty()) {
                    processBatch(new ArrayList<>(batch),controller,pattern,dateDetection,startTime,endTime,resultCount,listener);
                    batch.clear();
                }

                return controller.isCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }
        });


        Instant end = Instant.now();
        timeTakenInSeconds = Duration.between(start, end).toSeconds();
        listener.onSearchCompleted(timeTakenInSeconds);


    }

    private void processBatch(List<Path> batch,SearchController controller,Pattern pattern,DateDetection dateDetection,long startTime,long endTime,AtomicInteger resultCount,SearchProgressListener listener ){

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
                if (count <= getMaxResult()) {
                    listener.onResultFound(r,1, 1);
                } else {
                    controller.cancel();
                    listener.onMaxLimit();
                    break;
                }
            }
        });
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
                    if(line.length()>100){
                        line = line.substring(0,100);
                        matchPositions.clear();
                    }
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
        for (String ignore : ignoreExtension) {
            if (path.toString().endsWith(ignore)) {
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
