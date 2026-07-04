package com.example.datastack;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "service", "data-stack-demo",
                "endpoints", new String[]{"/health", "/last", "/demo"});
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "time", Instant.now().toString());
    }

    @GetMapping("/last")
    public DemoResult last() {
        return demoService.lastResult();
    }

    @PostMapping("/demo")
    public DemoResult demo() {
        return demoService.runDemo();
    }
}
