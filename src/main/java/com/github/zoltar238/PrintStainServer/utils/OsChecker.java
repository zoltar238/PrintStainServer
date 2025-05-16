package com.github.zoltar238.PrintStainServer.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OsChecker {
    public static boolean ISWINDOWS;

    public static void checkOs() {
        String os = System.getProperty("os.name").toLowerCase();
        ISWINDOWS = os.contains("win");
    }
}
