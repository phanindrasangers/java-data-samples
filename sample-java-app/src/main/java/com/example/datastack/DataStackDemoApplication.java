package com.example.datastack;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataStackDemoApplication implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataStackDemoApplication.class);

    private final DemoService demoService;

    public DataStackDemoApplication(DemoService demoService) {
        this.demoService = demoService;
    }

    public static void main(String[] args) {
        SpringApplication.run(DataStackDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (Arrays.asList(args.getSourceArgs()).contains("-d")) {
            DemoResult result = demoService.runDemo();
            if (result.success()) {
                log.info("Demo mode completed: {}", result);
            } else {
                log.error("Demo mode failed: {}", result);
            }
        }
    }
}
