package com.devmaster.goatfarm.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HexagonalArchitectureGuardTest {

    private static final Pattern API_IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+.*\\.api\\..*;");
    // Lista temporária de violações atuais; deve encolher módulo a módulo.
    private static final Set<String> ALLOWED_VIOLATIONS = new HashSet<>(List.of(
            "src/main/java/com/devmaster/goatfarm/authority/business/AuthBusiness.java",
            "src/main/java/com/devmaster/goatfarm/authority/business/usersbusiness/UserBusiness.java",
            "src/main/java/com/devmaster/goatfarm/farm/business/GoatFarmBusiness.java",
            "src/main/java/com/devmaster/goatfarm/farm/business/bo/GoatFarmUpdateRequestVO.java",
            "src/main/java/com/devmaster/goatfarm/goat/business/GoatBusiness.java",
            "src/main/java/com/devmaster/goatfarm/reproduction/business/reproductionservice/ReproductionBusiness.java",
            "src/main/java/com/devmaster/goatfarm/milk/business/milkproductionservice/MilkProductionBusiness.java",
            "src/main/java/com/devmaster/goatfarm/milk/business/lactationservice/LactationBusiness.java",
            "src/main/java/com/devmaster/goatfarm/genealogy/business/genealogyservice/GenealogyBusiness.java",
            "src/main/java/com/devmaster/goatfarm/events/business/eventservice/EventBusiness.java",
            "src/main/java/com/devmaster/goatfarm/address/business/AddressBusiness.java",
            "src/main/java/com/devmaster/goatfarm/phone/business/phoneservice/PhoneBusiness.java",
            "src/main/java/com/devmaster/goatfarm/article/business/articleservice/ArticleBusiness.java"
    ));

    @Test
    void businessLayerMustNotImportApiLayer() {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path root = projectRoot.resolve(Paths.get("src", "main", "java"));
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(this::isBusinessPath)
                    .forEach(path -> collectViolations(projectRoot, path, violations));
        } catch (IOException e) {
            fail("Falha ao varrer arquivos Java: " + e.getMessage());
        }

        assertTrue(violations.isEmpty(),
                "business não pode importar api.*; use business/mapper.\n" + String.join("\n", violations));
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
                    String relative = projectRoot.relativize(path).toString().replace('\\', '/');
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
