package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/** A utility class for processing files and counting word occurrences. */
public class FileProcessor {
    private static final Pattern WORD_PATTERN = Pattern.compile("[]a-zA-Z]+");

    /**
     * Process multiple files concurrently to count word occurrences.
     * @param wordCounts
     * @param topN
     * @return
     */
    public Map<String, Integer> processFiles(List<String> filePaths, int threadPoolSize) {
        // Create a fixed thread pool
        ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadPoolSize);
        // A thread-safe map to store word counts
        Map<String, Integer> resultMap = new java.util.concurrent.ConcurrentHashMap<>();
        // A list to hold futures
        List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();

        // Submit tasks to process each file
        try {
            for (String filePath: filePaths) {
                // Submit a task for each file
                futures.add(executor.submit(() -> {
                    processFile(filePath, resultMap);
                }));
            }
            
            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();    
        } finally {
            // Shutdown the executor
            executor.shutdown();
            try {
                // Wait for existing tasks to terminate
                if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                } 
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return resultMap;
    }

    private void processFile(String filePath, Map<String, Integer> wordCounts) {
        try {
            // Read all lines from the file
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            // Process each line
            for (String line : lines) {
                // Find words using the regex pattern
                var matcher = WORD_PATTERN.matcher(line.toLowerCase());
                while (matcher.find()) {
                    // Get the matched word
                    String word = matcher.group();
                    // Update the word count in a thread-safe manner
                    wordCounts.merge(word, 1, Integer::sum);
                }
            }
        } catch (NoSuchFileException e) {
            System.err.println("File not found: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Map.Entry<String, Integer>> findTopWords(Map<String, Integer> wordCounts, int topN) {
        return wordCounts.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .toList();
    }
}