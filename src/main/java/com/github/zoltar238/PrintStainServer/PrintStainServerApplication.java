package com.github.zoltar238.PrintStainServer;

import com.github.zoltar238.PrintStainServer.utils.OsChecker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PrintStainServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrintStainServerApplication.class, args);
        OsChecker.checkOs();
    }

}
