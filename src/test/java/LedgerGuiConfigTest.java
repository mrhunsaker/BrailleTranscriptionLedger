import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.File;
import java.util.Comparator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class LedgerGuiConfigTest {

    @Test
    public void testCmdLinePrecedence() {
        String[] args = new String[] {"--dbpath=./custom/dbpath"};
        String resolved = BrailleTranscriptionLedger.LedgerGUI.resolveDbBasePath(args);
        assertEquals("./custom/dbpath", resolved);
    }

    @Test
    public void testConfigFileRead() throws IOException {
        Path cfg = Path.of("config.properties");
        try {
            Files.writeString(cfg, "db.path=./fromconfig/ledger\n");
            String resolved = BrailleTranscriptionLedger.LedgerGUI.resolveDbBasePath(new String[0]);
            assertEquals("./fromconfig/ledger", resolved);
        } finally {
            Files.deleteIfExists(cfg);
        }
    }

    @Test
    public void testEnvPrecedenceWithSupplier() {
        String[] args = new String[0];
        String resolved = BrailleTranscriptionLedger.LedgerGUI.resolveDbBasePath(
            args,
            (k) -> {
                if ("LEDGER_DB_PATH".equals(k)) return "C:/env/ledger";
                return null;
            },
            () -> new Properties(),
            () -> System.getProperty("user.home"),
            (p) -> false,
            (r) -> "."
        );
        assertEquals("C:/env/ledger", resolved);
    }

    @Test
    public void testResolveDbBasePathUsesTestRootOverride() throws Exception {
        Path tmp = Files.createTempDirectory("ledger-test-");
        try {
            Path appHomeLedger = tmp.resolve("app_home").resolve("ledger");
            Files.createDirectories(appHomeLedger);
            // Set the test hook
            BrailleTranscriptionLedger.LedgerGUI.TEST_ROOT_OVERRIDE = tmp.toString();

            String resolved = BrailleTranscriptionLedger.LedgerGUI.resolveDbBasePath(
                new String[0],
                (k) -> null,
                () -> new Properties(),
                () -> System.getProperty("user.home"),
                (p) -> Files.exists(Path.of(p)),
                (r) -> {
                    if (BrailleTranscriptionLedger.LedgerGUI.TEST_ROOT_OVERRIDE != null) return BrailleTranscriptionLedger.LedgerGUI.TEST_ROOT_OVERRIDE;
                    return r;
                }
            );

            assertEquals(appHomeLedger.toAbsolutePath().toString(), resolved);
        } finally {
            BrailleTranscriptionLedger.LedgerGUI.TEST_ROOT_OVERRIDE = null;
            // cleanup
            Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    public void testEnvOverridesConfig() {
        String[] args = new String[0];
        String resolved = BrailleTranscriptionLedger.LedgerGUI.resolveDbBasePath(
            args,
            (k) -> {
                if ("LEDGER_DB_PATH".equals(k)) return "C:/env/override";
                return null;
            },
            () -> {
                Properties p = new Properties();
                p.setProperty("db.path", "./fromconfig/ledger");
                return p;
            },
            () -> System.getProperty("user.home"),
            (p) -> false,
            (r) -> "."
        );
        assertEquals("C:/env/override", resolved);
    }
}
