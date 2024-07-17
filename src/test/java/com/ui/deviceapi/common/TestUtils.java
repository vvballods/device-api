package com.ui.deviceapi.common;

import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class TestUtils {
    public static String getRandomMacAddress() {
        Random r = new Random();
        return Stream.generate(() -> r.nextInt(255))
                .limit(6)
                .map(n -> String.format("%02x", n))
                .collect(joining(":"))
                .toUpperCase();
    }
}
