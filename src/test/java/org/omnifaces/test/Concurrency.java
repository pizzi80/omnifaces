package org.omnifaces.test;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.IntStream.range;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Concurrency {

    public static final int DEFAULT_ITERATIONS = 1000;
    
    private Concurrency() {
        throw new AssertionError();
    }

    public static void testThreadSafety(Consumer<Integer> task) {
        testThreadSafety(task, DEFAULT_ITERATIONS);
    }

    public static void testThreadSafety(Consumer<Integer> task, int iterations) {
        testThreadSafety((i, tasks) -> tasks.add(runAsync(() -> task.accept(i))), iterations);
    }

    public static void testThreadSafety(BiConsumer<Integer, Set<CompletableFuture<Void>>> task, int iterations) {
        Set<CompletableFuture<Void>> tasks = ConcurrentHashMap.newKeySet();
        range(0, iterations).forEach(i -> task.accept(i, tasks));
        awaitCompletion(tasks);
    }

    private static void awaitCompletion(Set<CompletableFuture<Void>> tasks) {
        tasks.forEach(t -> {
            try {
                t.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
