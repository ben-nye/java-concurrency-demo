package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileProcessorVirtualThreads {
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-z]+");

    public Map<String, Integer> processFiles(List<String> filePaths) {
        // Virtual threads - one lightweight thread per task
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        Map<String, Integer> wordCounts = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (String filePath : filePaths) {
                futures.add(executor.submit(() -> processFile(filePath, wordCounts)));
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    System.err.println("Error processing file: " + e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread pool interrupted: " + e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return wordCounts;
    }

    private void processFile(String filePath, Map<String, Integer> wordCounts) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                var matcher = WORD_PATTERN.matcher(line.toLowerCase());
                while (matcher.find()) {
                    String word = matcher.group();
                    wordCounts.merge(word, 1, Integer::sum);
                }
            }
        } catch (NoSuchFileException e) {
            System.err.println("File not found: " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
        }
    }

    public List<Map.Entry<String, Integer>> findTopWords(Map<String, Integer> wordCounts, int topN) {
        return wordCounts.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }
}