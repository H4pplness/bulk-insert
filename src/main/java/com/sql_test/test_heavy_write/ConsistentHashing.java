package com.sql_test.test_heavy_write;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashing<T> {
    private final HashFunction hashFunction;
    private final int numberOfReplicas;
    private final SortedMap<Integer, T> circle = new TreeMap<>();

    public ConsistentHashing(HashFunction hashFunction, int numberOfReplicas, Collection<T> nodes) {
        this.hashFunction = hashFunction;
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            add(node);
        }
    }

    public void add(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = hashFunction.hash(node.toString() + i);
            circle.put(hash, node);
        }
    }

    public void remove(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = hashFunction.hash(node.toString() + i);
            circle.remove(hash);
        }
    }

    public T get(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hashFunction.hash(key);
        if (!circle.containsKey(hash)) {
            SortedMap<Integer, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

    public interface HashFunction {
        int hash(Object key);
    }

    // Example of SHA-256 hash function
    public static class SHA256Hash implements HashFunction {
        @Override
        public int hash(Object key) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(key.toString().getBytes(StandardCharsets.UTF_8));
                // Convert first 4 bytes into an integer
                return ((digest[0] & 0xFF) << 24)
                        | ((digest[1] & 0xFF) << 16)
                        | ((digest[2] & 0xFF) << 8)
                        | (digest[3] & 0xFF);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        List<String> servers = List.of("NodeA", "NodeB", "NodeC", "NodeD");

        ConsistentHashing<String> ch = new ConsistentHashing<>(
                new SHA256Hash(), 100, servers
        );

        String[] keys = {"User1", "User2", "User3", "User4", "User5"};

        for (String key : keys) {
            System.out.println(key + " -> " + ch.get(key));
        }

        System.out.println("\nAdding ServerD...");
        ch.add("NodeE");
        ch.add("NodeF");
        ch.add("NodeG");
        ch.add("NodeH");

        for (String key : keys) {
            System.out.println(key + " -> " + ch.get(key));
        }
    }
}