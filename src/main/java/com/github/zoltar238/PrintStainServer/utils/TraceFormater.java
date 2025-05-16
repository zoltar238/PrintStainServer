package com.github.zoltar238.PrintStainServer.utils;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class TraceFormater {
    public static String format(Throwable ex) {
        return Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n    ", "    ", ""));
    }
}
