package architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Automated Clean Architecture &amp; Dependency Rule guardrail test (aligned with CSC207 Week 7/8).
 *
 * <p>Scans every source file under {@code src/main/java} and fails if any import crosses layers
 * outward, skips layers illegally (e.g., {@code interface_adapter -> entity}), or couples two
 * sibling {@code use_case.*} packages together.
 */
class CleanArchitectureDependencyTest {

    private static final Path MAIN_JAVA_ROOT = Paths.get("src", "main", "java");
    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("^\\s*import\\s+(?:static\\s+)?([\\w.]+);\\s*$");

    @Test
    void testDependencyRuleAcrossAllMainSourceFiles() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(MAIN_JAVA_ROOT)) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> checkFile(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                "Clean Architecture dependency violations detected:\n - "
                        + String.join("\n - ", violations));
    }

    private void checkFile(Path javaFile, List<String> violations) {
        String relative = MAIN_JAVA_ROOT.relativize(javaFile).toString().replace('\\', '/');
        String topLayer = relative.contains("/") ? relative.substring(0, relative.indexOf('/')) : "";
        String useCasePackage = extractUseCaseSubpackage(relative);
        boolean isController = relative.endsWith("Controller.java");

        List<String> lines;
        try {
            lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            violations.add(relative + ": unable to read file (" + ex.getMessage() + ")");
            return;
        }

        for (String line : lines) {
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String imported = matcher.group(1);
            validateImport(relative, topLayer, useCasePackage, isController, imported, violations);
        }
    }

    private void validateImport(
            String relativePath,
            String topLayer,
            String useCasePackage,
            boolean isController,
            String imported,
            List<String> violations) {
        switch (topLayer) {
            case "entity":
                rejectPrefixes(
                        relativePath,
                        imported,
                        violations,
                        "use_case.",
                        "interface_adapter.",
                        "view.",
                        "data_access.",
                        "app.",
                        "javax.swing.",
                        "java.awt.",
                        "java.sql.",
                        "okhttp3.",
                        "org.json.");
                break;

            case "use_case":
                rejectPrefixes(
                        relativePath,
                        imported,
                        violations,
                        "interface_adapter.",
                        "view.",
                        "data_access.",
                        "app.",
                        "javax.swing.",
                        "java.awt.",
                        "java.sql.",
                        "okhttp3.",
                        "org.json.");
                if (imported.startsWith("use_case.")
                        && !imported.startsWith("use_case." + useCasePackage + ".")) {
                    violations.add(
                            relativePath
                                    + " illegally imports sibling use case: "
                                    + imported);
                }
                break;

            case "interface_adapter":
                rejectPrefixes(
                        relativePath,
                        imported,
                        violations,
                        "entity.",
                        "view.",
                        "data_access.",
                        "app.",
                        "javax.swing.",
                        "java.awt.",
                        "java.sql.",
                        "okhttp3.",
                        "org.json.");
                if (imported.startsWith("use_case.")
                        && (imported.endsWith("Interactor")
                                || imported.endsWith("DataAccessInterface")
                                || imported.endsWith("Gateway"))) {
                    violations.add(
                            relativePath
                                    + " must depend only on Use Case boundaries/DTOs, not: "
                                    + imported);
                }
                if (isController && imported.startsWith("interface_adapter.")) {
                    violations.add(
                            relativePath
                                    + " (Controller) should not depend on ViewModel/State: "
                                    + imported);
                }
                break;

            case "view":
                rejectPrefixes(
                        relativePath,
                        imported,
                        violations,
                        "entity.",
                        "use_case.",
                        "data_access.",
                        "app.",
                        "java.sql.",
                        "okhttp3.",
                        "org.json.");
                break;

            case "data_access":
                rejectPrefixes(
                        relativePath,
                        imported,
                        violations,
                        "interface_adapter.",
                        "view.",
                        "app.",
                        "javax.swing.",
                        "java.awt.");
                if (imported.startsWith("use_case.") && imported.endsWith("Interactor")) {
                    violations.add(
                            relativePath + " must not depend on concrete Interactor: " + imported);
                }
                break;

            case "app":
                // Composition root may assemble all layers.
                break;

            default:
                violations.add(
                        relativePath + " sits outside recognized Clean Architecture packages.");
                break;
        }
    }

    private void rejectPrefixes(
            String relativePath,
            String imported,
            List<String> violations,
            String... forbiddenPrefixes) {
        for (String prefix : forbiddenPrefixes) {
            if (imported.startsWith(prefix)) {
                violations.add(relativePath + " illegally imports " + imported);
            }
        }
    }

    private String extractUseCaseSubpackage(String relativePath) {
        String[] parts = relativePath.split("/");
        if (parts.length >= 3 && "use_case".equals(parts[0])) {
            return parts[1];
        }
        return "";
    }
}
