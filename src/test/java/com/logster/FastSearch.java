package com.logster;

import org.ahocorasick.trie.Trie;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import java.util.stream.*;

public class FastSearch {
    static Trie trie;
    public static void main(String[] args) throws Exception {


        String regex = "error";

        Pattern pattern = Pattern.compile(regex);
        Path rootDir = Paths.get("C:\\Users\\vivek\\Downloads\\HDFS_v1");
          trie = Trie.builder()
                .onlyWholeWords()
                .addKeyword("error")
                .build();
        long start = System.nanoTime();
        try (Stream<Path> paths = Files.walk(rootDir)) {
            paths.parallel()
                    .filter(Files::isRegularFile)
                    .forEach(path -> searchFile(path, pattern));
        }

        long end = System.nanoTime();
        System.out.printf("Search took %.3f seconds%n", (end - start) / 1_000_000_000.0);
    }

    static void searchFile(Path path, Pattern pattern) {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                if (pattern.matcher(line).find()) {
                    System.out.printf("%s:%d:%s%n", path, lineNum, line);
                }
                /*if ( trie.containsMatch(line)) {
                    System.out.printf("%s:%d:%s%n", path, lineNum, line);
                }*/

                lineNum++;
            }
        } catch (IOException e) {
            // skip unreadable files
        }
    }
}
