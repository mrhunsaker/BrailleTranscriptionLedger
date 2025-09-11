import org.junit.jupiter.api.Test;
import BrailleTranscriptionLedger.LedgerGUI;
import java.nio.file.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

public class SubmitFlowIntegrationTest {

    @Test
    public void submitCreatesProjectFolderAndMovesDb() throws Exception {
        Path tempRoot = Files.createTempDirectory("ledger-submit-test");
        String originalUserDir = System.getProperty("user.dir");
        try {
            // make temp root the working directory
            System.setProperty("user.dir", tempRoot.toAbsolutePath().toString());

            // create a dummy ledger.db file in root to simulate existing DB
            Path ledgerDb = tempRoot.resolve("ledger.db");
            Files.createFile(ledgerDb);

            // Tell application code to treat tempRoot as the program root
            LedgerGUI.TEST_ROOT_OVERRIDE = tempRoot.toAbsolutePath().toString();
            // Start GUI (do not display)
            LedgerGUI gui = new LedgerGUI(new String[0]);

            // Use reflection to set private fields required by submitData
            setPrivateField(gui, "dateField", "2025-09-10");
            // set setupProjectNameTextField
            setPrivateField(gui, "setupProjectNameTextField", "Test Project Submit");

            // set student/school/subject/teacher/tvi if possible by selecting first entries
            selectFirstIfPossible(gui, "studentField");
            selectFirstIfPossible(gui, "schoolField");
            selectFirstIfPossible(gui, "subjectField");
            selectFirstIfPossible(gui, "teacherField");
            selectFirstIfPossible(gui, "tviField");

            // Call submitData via reflection
            Method m = LedgerGUI.class.getDeclaredMethod("submitData");
            m.setAccessible(true);
            m.invoke(gui);

            Path appHomeLedger = tempRoot.resolve("app_home").resolve("ledger.db");
            Path projectFolder = tempRoot.resolve("app_home").resolve("projects").resolve("Test_Project_Submit");

            assertTrue(Files.exists(projectFolder), "Project folder should be created");
            assertTrue(Files.exists(appHomeLedger), "Ledger DB should be moved into app_home/ledger.db");

            // Verify folder_path was persisted into Project_Name table.
            // Use the GUI instance's DB_URL so we query the same H2 instance.
            java.lang.reflect.Field dbField = LedgerGUI.class.getDeclaredField("DB_URL");
            dbField.setAccessible(true);
            String jdbc = (String) dbField.get(gui);
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(jdbc)) {
                java.sql.PreparedStatement ps = conn.prepareStatement("SELECT folder_path FROM Project_Name WHERE name = ?");
                ps.setString(1, "Test Project Submit");
                java.sql.ResultSet rs = ps.executeQuery();
                assertTrue(rs.next(), "Project_Name row should exist");
                String stored = rs.getString("folder_path");
                assertNotNull(stored, "folder_path should be non-null");
                assertEquals(projectFolder.toAbsolutePath().toString(), stored);
            }

        } finally {
            // cleanup and restore working dir
            System.setProperty("user.dir", originalUserDir);
            Files.walk(tempRoot).sorted((a,b)->b.compareTo(a)).forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception e) {} });
        }
    }

    private void setPrivateField(Object obj, String fieldName, String value) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        Object field = f.get(obj);
        if (field instanceof javax.swing.JTextField) {
            ((javax.swing.JTextField) field).setText(value);
        } else if (field instanceof javax.swing.JComboBox) {
            // try to set selected item if model contains value
            javax.swing.JComboBox cb = (javax.swing.JComboBox) field;
            cb.setSelectedItem(value);
        }
    }

    private void selectFirstIfPossible(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object field = f.get(obj);
            if (field instanceof javax.swing.JComboBox) {
                javax.swing.JComboBox cb = (javax.swing.JComboBox) field;
                if (cb.getModel().getSize() > 0) cb.setSelectedIndex(0);
            } else if (field instanceof javax.swing.JList) {
                javax.swing.JList list = (javax.swing.JList) field;
                if (list.getModel().getSize() > 0) list.setSelectedIndex(0);
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
