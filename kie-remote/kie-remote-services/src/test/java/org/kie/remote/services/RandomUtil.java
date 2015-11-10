package org.kie.remote.services;

import java.util.Random;

public class RandomUtil {

    private static Random random = new Random();

    public static int nextInt() {
        return random.nextInt();
    }

    public static long nextLong() {
        return random.nextLong();
    }

    public static int nextInt(int n) {
        return random.nextInt(n);
    }

}
