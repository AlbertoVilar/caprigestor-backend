package com.devmaster.goatfarm.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HexagonalArchitectureGuardTest {

    private static final Pattern API_IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+.*\\.api\\..*;");
    // DO NOT EXPAND: a allowlist deve apenas diminuir módulo a módulo.
    private static final int EXPECTED_ALLOWLIST_SIZE = 2;
    // Caminhos relativos a src/main/java (sem wildcards).
    private static final Set<String> ALLOWED_VIOLATIONS = Set.of(
            "com/devmaster/goatfarm/address/business/AddressBusiness.java",
            "com/devmaster/goatfarm/phone/business/phoneservice/PhoneBusiness.java"
    );

    @Test
    void businessLayerMustNotImportApiLayer() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path root = projectRoot.resolve(Paths.get("src", "main", "java"));
        List<String> violations = new ArrayList<>();

        assertTrue(ALLOWED_VIOLATIONS.size() == EXPECTED_ALLOWLIST_SIZE,
                "Allowlist changed. Refactor modules to remove violations; do not expand allowlist.");

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isBusinessPath)
                    .forEach(path -> collectViolations(projectRoot, path, violations));
        } catch (IOException e) {
            fail("Falha ao varrer arquivos Java: " + e.getMessage());
        }

        assertTrue(violations.isEmpty(),
                "Business não pode importar api.*. Crie business/mapper (MapStruct) e mova o mapeamento para lá.\n"
                        + String.join("\n", violations));
    }

    private boolean isBusinessPath(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return normalized.contains("/business/");
    }

    private void collectViolations(Path projectRoot, Path path, List<String> violations) {
        try (Stream<String> lines = Files.lines(path)) {
            int[] lineNumber = {0};
            lines.forEach(line -> {
                lineNumber[0]++;
                if (API_IMPORT_PATTERN.matcher(line).find()) {
                    String normalized = path.toString().replace('\\', '/');
                    String relative = projectRoot.resolve("src/main/java").relativize(path)
                            .toString().replace('\\', '/');
                    if (!ALLOWED_VIOLATIONS.contains(relative)) {
                        violations.add(normalized + ":" + lineNumber[0] + ":" + line.trim());
                    }
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
