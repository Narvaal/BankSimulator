package br.com.ale.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    public static <T> T pickRandom(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty");
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
