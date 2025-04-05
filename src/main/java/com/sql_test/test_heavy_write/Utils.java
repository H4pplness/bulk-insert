package com.sql_test.test_heavy_write;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public static <T> List<List<T>> partitionList(List<T> list,int batch){
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batch) {
            partitions.add(list.subList(i, Math.min(i + batch, list.size())));
        }
        return partitions;
    }

    public static LocalDateTime getRandomDateTime(LocalDateTime start, LocalDateTime end) {
        long startEpoch = start.toEpochSecond(ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(ZoneOffset.UTC);

        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC);
    }

    public static LocalDate getRandomDate(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endExclusive.toEpochDay();

        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomEpochDay);
    }
}
