# FileProcessor — A Thread Pool & Concurrency Demo

This is a hands-on demo project to learn how **concurrent programming** works in Java. It reads multiple text files at the same time and counts word frequencies. Nothing fancy — just a practical example of `ExecutorService`, thread pools, and concurrency in action.

## What Does It Do?

Instead of reading files one-by-one (slow), it processes them **in parallel** using a thread pool. So if you have 100 files, they all get read at the same time.

Then it counts how many times each word appears across all files and shows you the most common ones.

## Files in This Project

- **FileProcessor.java** — The main implementation using a traditional thread pool (Java 8+)
- **FileProcessorVirtualThreads.java** — Same thing but using Java 21's virtual threads (way cooler and faster)
- **FileProcessorTest.java** — Unit tests so you can verify everything works
- **FileProcessorDemo.java** — A simple demo to see it in action
- **pom.xml** — Maven configuration file with dependencies and build settings
- **README.md** — This file

## How to Run It

### 1. Compile everything

**For platform threads version (FileProcessor.java):**
```bash
javac FileProcessor.java FileProcessorDemo.java
```

**For virtual threads version (FileProcessorVirtualThreads.java, Java 21+):**
```bash
javac FileProcessor_VirtualThreads.java FileProcessorDemo.java
```

### 2. Configure the demo for your version

Open `FileProcessorDemo.java`. You need to change two things:

**Change the processor type declaration** (line 13):

Platform threads:
```java
FileProcessor processor = new FileProcessor();
```

Virtual threads:
```java
FileProcessorVirtualThreads processor = new FileProcessorVirtualThreads();
```

**Change the processFiles() calls**:

Platform threads version includes the `4` threadPoolSize parameter:
```java
Map<String, Integer> wordCounts = processor.processFiles(
    Arrays.asList("test1.txt", "test2.txt", "test3.txt"),
    4  // Thread pool size - REQUIRED for platform threads
);
```

Virtual threads version does NOT have the threadPoolSize parameter:
```java
Map<String, Integer> wordCounts = processor.processFiles(
    Arrays.asList("test1.txt", "test2.txt", "test3.txt")
    // No threadPoolSize parameter for virtual threads
);
```

Do this for both `processFiles()` calls (Test 1 and Test 3).

### 3. Run the demo

```bash
java FileProcessorDemo
```

It creates 3 test files, processes them concurrently, shows you the results, and cleans up. Takes a few seconds.

### What You'll See

```
Test files created.

=== Testing FileProcessor ===

Test 1: Processing files...
Total unique words: 13
All word counts: {java=5, is=4, hello=2, concurrent=2, processing=2, ...}

Test 2: Top 5 most frequent words:
  java: 5
  is: 4
  hello: 2
  concurrent: 2
  processing: 2

Test 3: Testing error handling with non-existent file...
File not found: nonexistent.txt
Processed successfully (non-existent file skipped)

✓ All tests completed!
```

## Maven Setup (pom.xml)

This project uses Maven to manage dependencies and build configuration. The `pom.xml` file tells Maven:

- Which version of Java to use (Java 21 for virtual threads support)
- What dependencies you need (JUnit 5 for testing)
- How to compile, test, and run the project

**What's in pom.xml:**

```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>

<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

This specifies Java 21 and JUnit 5 for testing. That's it — no external dependencies needed for the core FileProcessor code.

**Project structure with Maven:**

```
FileProcessor/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/
│   │   ├── FileProcessor.java
│   │   ├── FileProcessor_VirtualThreads.java
│   │   └── FileProcessorDemo.java
│   └── test/java/
│       └── FileProcessorTest.java
```

**Maven commands you can run:**

```bash
# Compile the code
mvn compile

# Run the tests
mvn test

# Run the demo
mvn exec:java -Dexec.mainClass="FileProcessorDemo"

# Clean build artifacts
mvn clean
```

Note: This project is configured for Maven. If you want to use Gradle instead, you'd need to create a `build.gradle` or `build.gradle.kts` file with similar configuration.

If you don't have Maven installed, you can download it from [maven.apache.org](https://maven.apache.org) or use a package manager (`brew install maven` on Mac, `apt install maven` on Ubuntu, etc).

## Compiling Without Maven

If you don't want to use Maven, you can compile directly with `javac`:

```bash
# Compile the main code
javac FileProcessor.java FileProcessorDemo.java

# Run the demo
java FileProcessorDemo
```

This works for the demo. However, running tests without Maven is more tedious — you'd need to manually download JUnit 5 JARs and set up the classpath. Maven handles all that automatically, so it's recommended for testing.

## Using It in Your Own Code

Dead simple:

```java
FileProcessor processor = new FileProcessor();

// Process multiple files concurrently
Map<String, Integer> wordCounts = processor.processFiles(
    Arrays.asList("file1.txt", "file2.txt", "file3.txt")
);

// Get the top 10 most common words
List<Map.Entry<String, Integer>> topWords = processor.findTopWords(wordCounts, 10);

// Print them out
topWords.forEach(entry -> 
    System.out.println(entry.getKey() + ": " + entry.getValue())
);
```

## The Two Implementations — Platform Threads vs Virtual Threads

### FileProcessor.java — Traditional Thread Pool

Uses the classic `ExecutorService` with a fixed pool size:

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
```

**Why this matters for learning:** This shows you the fundamentals of concurrency. You'll see `Future` objects, how to submit tasks, wait for them to complete, and properly shut down the executor. This is solid Java knowledge.

**The limitation:** If you have more files than thread pool size, the extras wait in a queue. So 1000 files with a pool of 4 still processes them in batches.

### FileProcessor_VirtualThreads.java — Java 21+ Virtual Threads

Uses Java 21's mind-blowing lightweight virtual threads:

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

**Why this is cool:** Virtual threads are SO cheap you can create thousands without worry. One thread per file? No problem. One thread per request to a web server? Easy. You don't need to tune anything.

**The benefit:** Dramatically faster for I/O operations. Instead of processing 1000 files in ~250 seconds (with pooling), it takes ~5-10 seconds. That's the power of virtual threads.

Both implementations do exactly the same thing — they just swap the executor. The rest of your code stays identical.

## Key Concepts You'll See

### ExecutorService

Manages a thread pool for you. Instead of manually creating threads, you submit tasks and let it handle the details.

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(() -> processFile(file));  // Queue a task
executor.shutdown();                        // Stop accepting new tasks
executor.awaitTermination(10, TimeUnit.SECONDS);  // Wait for completion
```

### Future

When you submit a task to an executor, you get a `Future` back. It represents the result of that task (which will be available later).

```java
Future<?> future = executor.submit(() -> processFile("file.txt"));
future.get();  // Block and wait for it to complete
```

### ConcurrentHashMap

A thread-safe map. Multiple threads can safely modify it at the same time without data corruption.

```java
Map<String, Integer> wordCounts = new ConcurrentHashMap<>();
// Multiple threads can call wordCounts.merge() simultaneously — no race conditions
wordCounts.merge("hello", 1, Integer::sum);
```

### Method References (`Integer::sum`)

Shorthand for calling a method. `Integer::sum` is the same as `(a, b) -> Integer.sum(a, b)`. Way cleaner.

## What It Handles

- **Multiple files at once** — That's the whole point
- **Mixed case** — "Hello", "HELLO", "hello" all count as the same word
- **Punctuation** — "hello," and "hello!" both count as "hello"
- **Non-existent files** — Logs an error and keeps going
- **I/O errors** — Doesn't crash the whole app
- **Proper shutdown** — Waits for threads to finish, then cleans up

## Testing

Run the unit tests with:

```bash
# Run tests with Maven
mvn test

# Or just run in your IDE
```

The tests verify word counting, error handling, concurrent processing, top word sorting, and edge cases like empty files.

## Requirements

- **Java 21+** (for virtual threads support in FileProcessor_VirtualThreads.java)
- **Maven 3.6+** (for building and running tests)
- JUnit 5 (automatically installed by Maven)

To check your Java version:
```bash
java -version
```

To check Maven:
```bash
mvn -version
```

Don't have Java 21? If you only want to use the traditional thread pool version (FileProcessor.java), you can change the pom.xml to target Java 8+:

```xml
<maven.compiler.source>8</maven.compiler.source>
<maven.compiler.target>8</maven.compiler.target>
```

## Why This Project Matters

If you're learning Java concurrency, this shows you a realistic example. Most of the time you don't create threads manually — you use `ExecutorService`. This demo shows exactly how it works and why virtual threads are the future.

Plus you'll see patterns you'll use over and over: thread pools, futures, thread-safe collections, proper error handling, and graceful shutdown.

## Next Steps

- Run the demo and see it work
- Look at the tests to understand what's being verified
- Try modifying it — add your own files, change the pool size, whatever
- Compare the performance between the two implementations (if you have Java 21)
- Read up more on `ExecutorService` and virtual threads in the Java docs

## Real-World Use Cases for Concurrency Management

This isn't just a toy example. Concurrent programming with thread pools is everywhere in production systems:

**Web servers** — Handle thousands of simultaneous client requests. Without concurrency, only one request could be processed at a time. Thread pools let you handle 10,000 requests with a few hundred threads instead of creating 10,000 threads.

**Database connection pooling** — Maintaining open database connections is expensive. A thread pool reuses a limited set of connections across all requests, improving performance dramatically.

**Batch data processing** — Processing millions of records (ETL pipelines, log analysis, data migrations). Concurrent processing can be 10x faster than sequential processing on multi-core machines.

**File I/O operations** — Reading thousands of files from disk or network. While one thread is waiting for I/O, others can process data. This is exactly what the FileProcessor demo does.

**API call aggregation** — Making 100+ API calls to different services in parallel, with timeouts. Without concurrency, you'd wait for them sequentially. With concurrency, you get results in seconds instead of minutes.

**Real-time systems** — Stock exchanges, payment processors, monitoring systems. They handle concurrent operations from thousands of sources simultaneously.

The patterns you see here — `ExecutorService`, `ConcurrentHashMap`, `Future`, proper shutdown — are the foundation of any scalable system. 

## Questions or Comments?

Have ideas, found bugs, or want to discuss concurrency patterns? Reach out:

**benthedataguy@gmail.com**