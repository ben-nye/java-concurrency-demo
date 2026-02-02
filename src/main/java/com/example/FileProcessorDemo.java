package com.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Demo for FileProcessor (platform threads version).
 * For the virtual threads version, remove the threadPoolSize parameters.
 */
public class FileProcessorDemo {
    
    public static void main(String[] args) throws Exception {
        // Create test files
        createTestFiles();
        
        // Test the FileProcessor
        FileProcessor processor = new FileProcessor();
        
        System.out.println("=== Testing FileProcessor ===\n");
        
        // Test 1: Process multiple files
        System.out.println("Test 1: Processing files...");
        Map<String, Integer> wordCounts = processor.processFiles(
            Arrays.asList("test1.txt", "test2.txt", "test3.txt"),
            4  // Thread pool size (ignored if using virtual threads)
        );
        
        System.out.println("Total unique words: " + wordCounts.size());
        System.out.println("All word counts: " + wordCounts);
        System.out.println();
        
        // Test 2: Find top words
        System.out.println("Test 2: Top 5 most frequent words:");
        List<Map.Entry<String, Integer>> topWords = processor.findTopWords(wordCounts, 5);
        topWords.forEach(entry -> 
            System.out.println("  " + entry.getKey() + ": " + entry.getValue())
        );
        System.out.println();
        
        // Test 3: Non-existent file (error handling)
        System.out.println("Test 3: Testing error handling with non-existent file...");
        Map<String, Integer> result = processor.processFiles(
            Arrays.asList("nonexistent.txt", "test1.txt"),
            4  // Thread pool size
        );
        System.out.println("Processed successfully (non-existent file skipped)");
        System.out.println();
        
        // Cleanup
        Files.deleteIfExists(Paths.get("test1.txt"));
        Files.deleteIfExists(Paths.get("test2.txt"));
        Files.deleteIfExists(Paths.get("test3.txt"));
        
        System.out.println("✓ All tests completed!");
    }
    
    private static void createTestFiles() throws Exception {
        // Test file 1
        Files.write(Paths.get("test1.txt"), 
            "Hello World! This is a test file.\nJava is awesome, Java is powerful!".getBytes());
        
        // Test file 2
        Files.write(Paths.get("test2.txt"), 
            "Hello Java! Programming in Java is fun.\nJava, Java, Java!".getBytes());
        
        // Test file 3
        Files.write(Paths.get("test3.txt"), 
            "Testing concurrent file processing.\nConcurrent processing is efficient.".getBytes());
        
        System.out.println("Test files created.\n");
    }
}