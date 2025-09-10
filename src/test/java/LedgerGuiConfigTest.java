import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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
}
