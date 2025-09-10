import org.junit.jupiter.api.Test;
import BrailleTranscriptionLedger.LedgerGUI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

public class LedgerGuiAppHomeIntegrationTest {

    @Test
    public void resolvesAppHomeWhenPresent() throws Exception {
        Path tempRoot = Files.createTempDirectory("ledger-test-root");
        try {
            Path appHome = tempRoot.resolve("app_home");
            Files.createDirectories(appHome);
            // create a ledger file to simulate existing DB
            Files.createFile(appHome.resolve("ledger.db"));

            String[] args = new String[0];
            String resolved = LedgerGUI.resolveDbBasePath(
                args,
                (k) -> null,
                () -> {
                    Properties p = new Properties();
                    return p;
                },
                () -> System.getProperty("user.home"),
                (p) -> {
                    // Consider existence only if inside tempRoot path
                    return p != null && p.startsWith(appHome.toAbsolutePath().toString());
                },
                (r) -> tempRoot.toAbsolutePath().toString()
            );

            assertNotNull(resolved);
            assertTrue(resolved.contains(appHome.toAbsolutePath().toString()));
        } finally {
            // cleanup
            Files.walk(tempRoot)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception e) {}
                });
        }
    }
}
