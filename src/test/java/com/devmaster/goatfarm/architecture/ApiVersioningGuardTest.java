package com.devmaster.goatfarm.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ApiVersioningGuardTest {

    @Test
    void restControllersMustExposeApiV1Prefix() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path root = projectRoot.resolve(Paths.get("src", "main", "java"));
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith("Controller.java"))
                    .filter(this::isApiControllerPath)
                    .forEach(path -> validateControllerMapping(projectRoot, path, violations));
        } catch (IOException e) {
            fail("Failed to scan controller files: " + e.getMessage());
        }

        assertTrue(violations.isEmpty(),
                "All @RestController classes in api/controller must include class-level /api/v1 mapping. Violations:\n"
                        + String.join("\n", violations));
    }

    private boolean isApiControllerPath(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return normalized.contains("/api/controller/");
    }

    private void validateControllerMapping(Path projectRoot, Path path, List<String> violations) {
        try {
            String content = Files.readString(path);
            if (!content.contains("@RestController")) {
                return;
            }
            if (content.contains("@RequestMapping(\"/public/")) {
                return;
            }
            if (!content.contains("@RequestMapping(")) {
                String relative = projectRoot.relativize(path).toString().replace('\\', '/');
                violations.add(relative + " -> missing class-level @RequestMapping");
                return;
            }
            if (!content.contains("@RequestMapping({\"/api/v1") && !content.contains("@RequestMapping(\"/api/v1")) {
                String relative = projectRoot.relativize(path).toString().replace('\\', '/');
                violations.add(relative + " -> missing /api/v1 prefix in class-level @RequestMapping");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path + ": " + e.getMessage(), e);
        }
    }
}
