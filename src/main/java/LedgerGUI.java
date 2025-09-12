/****************************************************************************
 * Copyright 2024  Michael Ryan Hunsaker, M.Ed., Ph.D.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package BrailleTranscriptionLedger;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.awt.Desktop;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Logback classes used for dynamic log level changes
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * The `LedgerGUI` class is a Swing-based application that provides a graphical user interface for managing a ledger.
 * The ledger is used to track the work performed by students on various projects, including the date, student, subject,
 * school, project, time spent, and completion status.
 * <p>
 * The application allows the user to input new ledger entries, view the existing entries in a table, and generate a
 * PDF report for a specified date range and selected projects.
 * <p>
 * The application uses an H2 embedded database to store the ledger data and supports various features, such as:
 * <ul>
 *     <li>Saving new ledger entries to the database</li>
 *     <li>Retrieving and displaying existing ledger entries in a table</li>
 *     <li>Generating a PDF report for a specified date range and selected projects</li>
 *     <li>Customizing the application's appearance using various IntelliJ IDEA-inspired themes</li>
 * </ul>
 * <p>
 * The `LedgerGUI` class extends the `JFrame` class and provides the main entry point for the application. It
 * initializes the database, creates the GUI components, and handles user interactions.
 *
 * @author Michael Ryan Hunsaker, M.Ed., Ph.D.
 * @version 2025.07.01
 * @since 2025.07.01
 *
 * <h2>Configuration keys</h2>
 * The application reads a small set of keys from a `config.properties` file in the program root. Keys include:
 * <ul>
 *   <li>{@code db.path} — base path used to locate the embedded H2 database (examples: {@code ./app_home/ledger}, {@code D:/data/ledger})</li>
 *   <li>{@code window.maximized} — {@code true|false} whether the main window should start maximized</li>
 *   <li>{@code log.level} — logging level (DEBUG, INFO, WARN, ERROR, FATAL)</li>
 * </ul>
 * @see <a href="README.md#configuration-keys">README.md#configuration-keys</a> for configuration precedence and CLI overrides
 */
public class LedgerGUI extends JFrame {

    // Localization
    /**
     * Resource bundle for label localization.
     */
    private ResourceBundle labels = ResourceBundle.getBundle(
        "messages",
        Locale.ENGLISH
    );

    /**
     * Current UI locale.
     */
    private Locale currentLocale = Locale.ENGLISH;

    /**
     * References for dynamic label updates.
     */
    private JLabel studentLabel, schoolLabel, projectLabel, timeLabel, teacherLabel, tviLabel, subjectLabel, notesLabel;

    /**
     * Main tabbed pane for UI.
     */
    private JTabbedPane mainTabs;
    // Add other label/button/tab references as needed

    // Localization (already declared above, remove duplicates)
    // Store references to all label components for dynamic update (already declared above, remove duplicates)

    private static final Logger logger = LoggerFactory.getLogger(
        LedgerGUI.class
    );

    /**
     * Stores the most recently inserted detail_id from Project_Details.
     */
    private int currentDetailId = -1;

    /**
     * A static map that stores the available IntelliJ IDEA-inspired themes for the application.
     */
    private static final Map<
        String,
        Class<? extends LookAndFeel>
    > INTELLIJ_THEMES = new TreeMap<>();

    static {
        // FlatLaf core themes
        INTELLIJ_THEMES.put("Flat Light", FlatLightLaf.class);
        INTELLIJ_THEMES.put("Flat Dark", FlatDarkLaf.class);
        INTELLIJ_THEMES.put("Flat IntelliJ", FlatIntelliJLaf.class);
        INTELLIJ_THEMES.put("Flat Darcula", FlatDarculaLaf.class);

        // IntelliJ themes
        INTELLIJ_THEMES.put("Arc", FlatArcIJTheme.class);
        INTELLIJ_THEMES.put("Arc Orange", FlatArcOrangeIJTheme.class);
        INTELLIJ_THEMES.put("Carbon", FlatCarbonIJTheme.class);
        INTELLIJ_THEMES.put("Cobalt 2", FlatCobalt2IJTheme.class);
        INTELLIJ_THEMES.put("Cyan Light", FlatCyanLightIJTheme.class);
        INTELLIJ_THEMES.put("Dark Purple", FlatDarkPurpleIJTheme.class);
        INTELLIJ_THEMES.put("Dracula", FlatDraculaIJTheme.class);
        INTELLIJ_THEMES.put("Gray", FlatGrayIJTheme.class);
        INTELLIJ_THEMES.put(
            "Gruvbox Dark Hard",
            FlatGruvboxDarkHardIJTheme.class
        );
        INTELLIJ_THEMES.put("Hiberbee Dark", FlatHiberbeeDarkIJTheme.class);
        INTELLIJ_THEMES.put("High Contrast", FlatHighContrastIJTheme.class);
        INTELLIJ_THEMES.put("Light Flat", FlatLightFlatIJTheme.class);
        INTELLIJ_THEMES.put(
            "Material Design Dark",
            FlatMaterialDesignDarkIJTheme.class
        );
        INTELLIJ_THEMES.put("Monocai", FlatMonocaiIJTheme.class);
        INTELLIJ_THEMES.put("Nord", FlatNordIJTheme.class);
        INTELLIJ_THEMES.put("One Dark", FlatOneDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Dark", FlatSolarizedDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Light", FlatSolarizedLightIJTheme.class);
        INTELLIJ_THEMES.put("Spacegray", FlatSpacegrayIJTheme.class);
        INTELLIJ_THEMES.put("Vuesion", FlatVuesionIJTheme.class);
    }

    /**
     * A text field for entering the date.
     */
    private final JTextField dateField = new JTextField();
    /**
     * Text field for displaying or editing the last updated value.
     */
    private final JTextField updatedField = new JTextField(
        LocalDate.now().toString()
    );
    /**
     * Error labels for validation feedback
     */
    private final JLabel dateErrorLabel = new JLabel();
    /**
     * Error label for the Student field. Displays validation messages.
     */
    private final JLabel studentErrorLabel = new JLabel();
    /**
     * Error label for the School field. Displays validation messages.
     */
    private final JLabel schoolErrorLabel = new JLabel();
    /**
     * Error label for the Project field. Displays validation messages.
     */
    private final JLabel projectErrorLabel = new JLabel();
    /**
     * Error label for the Time field. Displays validation messages.
     */
    private final JLabel timeErrorLabel = new JLabel();
    /**
     * Error label for the Teacher field. Displays validation messages.
     */
    private final JLabel teacherErrorLabel = new JLabel();
    /**
     * Error label for the TVI field. Displays validation messages.
     */
    private final JLabel tviErrorLabel = new JLabel();
    /**
     * Error label for the Subject field. Displays validation messages.
     */
    private final JLabel subjectErrorLabel = new JLabel();
    /**
     * Error label for the Notes field. Displays validation messages.
     */
    private final JLabel notesErrorLabel = new JLabel();

    /**
     * Combo box for indicating if the project is completed (Yes/No).
     */
    private JComboBox<String> completedField;

    // Accessibility live region for announcements
    /**
     * Accessibility live region for announcements to screen readers.
     */
    private final JLabel liveRegionLabel = new JLabel();

    // Store help dialog font for accessibility
    /**
     * Font used for help dialogs to improve accessibility.
     */
    private Font helpDialogFont = new Font("SansSerif", Font.PLAIN, 16);

    // Menu components promoted to fields for accessibility/localization
    /**
     * Main menu bar for the application.
     */
    private JMenuBar menuBar;
    /**
     * File menu for file operations.
     */
    private JMenu fileMenu;
    /**
     * Accessibility menu for accessibility options.
     */
    private JMenu accessibilityMenu;
    /**
     * Theme menu for UI theme selection.
     */
    private JMenu themeMenu;
    /**
     * Help menu for accessing help dialogs.
     */
    private JMenu helpMenu;
    /**
     * Menu item for keyboard shortcuts help.
     */
    private JMenuItem keyboardShortcutsMenuItem;
    /**
     * Menu item for tutorial/how-to.
     */
    private JMenuItem tutorialMenuItem;
    /**
     * Menu item to skip to main content for accessibility.
     */
    private JMenuItem skipToMainContentItem;

    // Localization
    /**
     * Resource bundle for localization of UI text.
     */
    private ResourceBundle bundle = ResourceBundle.getBundle(
        "messages",
        Locale.getDefault()
    );

    /**
     * A list for selecting teachers associated with the project.
     */
    private JList<String> teacherField;
    /**
     * A list for selecting TVIs (Teachers of the Visually Impaired) associated with the project.
     */
    private JList<String> tviField;
    /**
     * A combo box for selecting the LEA (Local Education Agency) associated with the student.
     */
    private JComboBox<String> leaField;
    /**
     * A combo box for selecting the media type (e.g., braille, graphics, DAISY).
     */
    private JComboBox<String> mediaTypeField;
    /**
     * A combo box for selecting the proof status (e.g., no, in progress, revising, done).
     */
    private JComboBox<String> proofStatusField = new JComboBox<>(
        loadOptionsFromFile("json_files/proof_status.json", "proof_status")
    );
    // Logging for proofStatusField moved to constructor
    /**
     * A combo box for selecting the delivery mode (e.g., delivery, pick up, email).
     */
    private JComboBox<String> deliveryModeField = new JComboBox<>(
        loadOptionsFromFile("json_files/delivery_mode.json", "delivery_mode")
    );
    // Logging for deliveryModeField moved to constructor
    /**
     * A combo box for selecting the project name.
     */
    private final JComboBox<String> projectNameField;

    /**
     * Text field for entering the project name in Project Setup.
     */
    private JTextField setupProjectNameTextField;

    /**
     * A panel for Project Status.
     */
    private JPanel projectStatusPanel = new JPanel(new GridBagLayout());

    /**
     * A text area for entering notes.
     */
    private JTextArea notesField;
    /**
     * A combo box for selecting the project element.
     */
    private final JComboBox<String> projectElementField;
    /**
     * A text field for entering the project time.
     */
    private final JTextField projectTimeField;
    /**
     * A text field for entering the time.
     */
    private final JTextField timeField;
    /**
     * A combo box for seleccombo box for selectinging th the project.
     */
    private JComboBox<String> projectField;
    /**
     * A combo box for selecting the school.
     */
    private final JComboBox<String> schoolField;
    /**
     * A combo box for selecting the Student.
     */
    private final JComboBox<String> studentField;
    /**
     * A combo box for selecting the subject.
     */
    private final JComboBox<String> subjectField;
    /**
     * A combo box for indicating the completion status (Yes/No).
     */
    private JComboBox<String> completeComboBox;
    /**
     * A button for submitting the data.
     */
    private final JButton submitButton;
    /**
     * A button for generating a PDF report.
     */
    private final JButton generatePdfButton;
    /**
     * The table displaying the ledger entries.
     */
    private final JTable dataTable;
    /**
     * The table model for the data table.
     */
    /**
     * The model for the data table, used to manage ledger entries.
     */
    private final DefaultTableModel tableModel;
    /**
     * The URL of the H2 embedded database used by the application.
     */
    /**
     * The URL for connecting to the H2 embedded database.
     */
    // Database URL - computed at runtime so app_home move is respected
    private String DB_URL;

    /**
     * Test hook: when set, the code will treat this path as the application root
     * (used by tests to simulate different working directories).
     */
    public static String TEST_ROOT_OVERRIDE = null;

    /**
     * Non-modal status bar label shown at the bottom of the main frame.
     */
    private javax.swing.JLabel statusBar;

    /**
     * Constructs a new `LedgerGUI` instance and initializes the application's components.
    *
    * @since 2025.07.01
     */
    public LedgerGUI() {
        this(new String[0]);
    }

    /**
     * Constructor that accepts command-line args so DB path can be configured.
    *
    * @param args Command-line arguments passed to the program. May include
    *             options such as {@code --dbpath=<path>} or {@code --loglevel=<level>}.
    * @since 2025.07.01
     */
    public LedgerGUI(String[] args) {
    // Resolve DB base path using precedence rules
    String dbBase = resolveDbBasePath(args);
    DB_URL = "jdbc:h2:" + dbBase + ";AUTO_SERVER=TRUE";
    logger.info("Connecting to database at URL: {}", DB_URL);
        // Set global font for all UI components to 32pt before creating any components
        FontUIResource globalFont = new FontUIResource(
            "SansSerif",
            Font.PLAIN,
            16
        );
        for (
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            keys.hasMoreElements();

        ) {
            Object key = keys.nextElement();
            if (key != null && key.toString().toLowerCase().contains("font")) {
                UIManager.put(key, globalFont);
            }
        }

        initializeDatabaseTables();
        ProjectSetup = new JPanel();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthPercentage = 0.67; // 67% of screen width
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int appWidth = (int) (screenSize.width * widthPercentage);
        int appHeight = screenSize.height;
        setTitle("Accessible Document Generation Ledger");
        setSize(appWidth, appHeight);
        setLocation(
            (screenWidth - appWidth) / 2,
            (screenHeight - appHeight) / 2
        );
        // Determine whether to start maximized. Precedence:
        // 1) CLI args: --no-maximize or --start-maximize
        // 2) config.properties -> window.maximized (true/false)
        // 3) default: true (maximize)
        boolean startMaximized = true; // default
        boolean seenNo = false;
        boolean seenYes = false;
        if (args != null) {
            for (String a : args) {
                if ("--no-maximize".equals(a)) seenNo = true;
                if ("--start-maximize".equals(a)) seenYes = true;
            }
        }
        if (seenNo) {
            startMaximized = false;
        } else if (seenYes) {
            startMaximized = true;
        } else {
            // read config.properties if present
            try {
                Path cfg = Paths.get("config.properties");
                if (Files.exists(cfg)) {
                    Properties p = new Properties();
                    try (java.io.InputStream in = Files.newInputStream(cfg)) {
                        p.load(in);
                        String val = p.getProperty("window.maximized");
                        if (val != null && !val.isBlank()) {
                            startMaximized = Boolean.parseBoolean(val.trim());
                        }
                    }
                }
            } catch (Exception e) {
                // ignore and use default
            }
        }
        if (startMaximized) setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JTabbedPane tabbedPane = new JTabbedPane();
    // Use scrollable tab layout so a scrollbar (scroll buttons) appears
    // when there are more tabs than fit the available width.
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    // Keep a reference to the main tabbed pane
    mainTabs = tabbedPane;
    setLayout(new BorderLayout());
    // initialize non-modal status bar
    statusBar = new javax.swing.JLabel(" ");
    statusBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 6, 4, 6));
    getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);

        // Create MenuBar
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        accessibilityMenu = new JMenu(getString("accessibility_menu"));
        themeMenu = new JMenu(getString("themes_menu"));
        helpMenu = new JMenu("Help");

        // Ensure table headers and tooltips use global font
        UIManager.put(
            "TableHeader.font",
            new FontUIResource("SansSerif", Font.PLAIN, 16)
        );
        UIManager.put(
            "ToolTip.font",
            new FontUIResource("SansSerif", Font.PLAIN, 16)
        );

        // Keyboard Shortcuts menu item
        keyboardShortcutsMenuItem = new JMenuItem("Keyboard Shortcuts");
        keyboardShortcutsMenuItem.addActionListener(e ->
            showKeyboardShortcutsDialog()
        );
        accessibilityMenu.add(keyboardShortcutsMenuItem);

        // Skip to Main Content menu item
        skipToMainContentItem = new JMenuItem("Skip to Main Content");
        skipToMainContentItem.setMnemonic(KeyEvent.VK_M); // Alt+M
        skipToMainContentItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK)
        );
        skipToMainContentItem.addActionListener(e -> {
            tabbedPane.setSelectedIndex(0); // Focus first tab
            dateField.requestFocusInWindow(); // Focus first field in form
            announceToScreenReader("Main content focused.");
        });
        accessibilityMenu.add(skipToMainContentItem);

        // Accessibility live region for announcements
        liveRegionLabel.setText("");

        // Move theme menu into Accessibility menu
        accessibilityMenu.add(themeMenu);

        JMenuItem aboutMenuItem = new JMenuItem(getString("about_title"));
        aboutMenuItem.setFocusable(true);
        aboutMenuItem.addActionListener((ActionEvent e) -> {
            JPanel aboutPanel = new JPanel(new BorderLayout());
            JLabel aboutLabel = new JLabel(
                "<html>" +
                getString("about_text").replace("\n", "<br>") +
                "</html>"
            );
            aboutLabel.setFocusable(true);
            aboutPanel.add(aboutLabel, BorderLayout.CENTER);
            showAccessibleDialog(
                LedgerGUI.this,
                getString("about_title"),
                aboutPanel
            );
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setFocusable(true);
        exitMenuItem.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        // Add FlatLaf Core Themes header
        JMenuItem coreHeader = new JMenuItem("FlatLaf Core Themes");
        coreHeader.setEnabled(false);
        themeMenu.add(coreHeader);

        String[] coreThemes = {
            "Flat Light",
            "Flat Dark",
            "Flat IntelliJ",
            "Flat Darcula",
        };
        for (String themeName : coreThemes) {
            JMenuItem item = new JMenuItem(themeName);
            item.setFocusable(true);
            item.addActionListener(e -> setIntelliJTheme(themeName));
            themeMenu.add(item);
        }

        // Add Light Themes header
        JMenuItem lightHeader = new JMenuItem("Light Themes");
        lightHeader.setEnabled(false);
        themeMenu.add(lightHeader);

        String[] lightThemes = {
            "Arc",
            "Arc Orange",
            "Cyan Light",
            "Gray",
            "Light Flat",
            "Solarized Light",
            "Vuesion",
        };
        for (String themeName : lightThemes) {
            JMenuItem item = new JMenuItem(themeName);
            item.setFocusable(true);
            item.addActionListener(e -> setIntelliJTheme(themeName));
            themeMenu.add(item);
        }

        // Add Dark Themes header
        JMenuItem darkHeader = new JMenuItem("Dark Themes");
        darkHeader.setEnabled(false);
        themeMenu.add(darkHeader);

        String[] darkThemes = {
            "Carbon",
            "Cobalt 2",
            "Dark Purple",
            "Dracula",
            "Gruvbox Dark Hard",
            "Hiberbee Dark",
            "High Contrast",
            "Material Design Dark",
            "Monocai",
            "Nord",
            "One Dark",
            "Solarized Dark",
            "Spacegray",
        };
        for (String themeName : darkThemes) {
            JMenuItem item = new JMenuItem(themeName);
            item.setFocusable(true);
            item.addActionListener(e -> setIntelliJTheme(themeName));
            themeMenu.add(item);
        }

    // Preferences menu item (allows editing config.properties)
    JMenuItem preferencesMenuItem = new JMenuItem("Preferences");
    preferencesMenuItem.setFocusable(true);
    preferencesMenuItem.addActionListener(e -> showPreferencesDialog());
    fileMenu.add(preferencesMenuItem);
    fileMenu.add(aboutMenuItem);
    fileMenu.add(exitMenuItem);

        // Help menu with keyboard shortcuts
        JMenuItem shortcutsMenuItem = new JMenuItem(
            "Keyboard Shortcuts (Ctrl+.)"
        );
        shortcutsMenuItem.setFocusable(true);
        shortcutsMenuItem.addActionListener(e -> showHelpDialog());
        helpMenu.add(shortcutsMenuItem);

        // Tutorial menu item
        tutorialMenuItem = new JMenuItem("Tutorial");
        tutorialMenuItem.setFocusable(true);
        tutorialMenuItem.addActionListener(e -> showTutorialDialog());
        helpMenu.add(tutorialMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(accessibilityMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar); // Use setJMenuBar() to add the menu bar to the JFrame

        // Force UI update for all components to apply global font
        SwingUtilities.updateComponentTreeUI(this);

        // Setup error labels for validation feedback
        setupErrorLabels();

        // Setup live region for screen reader announcements
        setupLiveRegion();

        // Load persisted language preference
        loadLanguagePreference();

        // Load persisted font preferences
        // Input Panel

    ProjectSetup = new JPanel(new GridBagLayout());
    ProjectSetup.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ProjectTracking = new JPanel(new GridLayout(0, 2, 10, 10));
        ProjectTracking.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        // Group date field and help button in a panel
        JPanel trackingDatePanel = new JPanel(new BorderLayout());
        dateField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        trackingDatePanel.add(dateField, BorderLayout.CENTER);
    // date tooltip is set on the date field itself
        addLabelAndField(
            ProjectTracking,
            "<html>Date: <br><i>(YYYY-MM-DD)</i></html>",
            trackingDatePanel
        );
        dateField.setText(LocalDate.now().toString());
        dateField.setToolTipText(getString("date_tooltip"));
        dateField
            .getAccessibleContext()
            .setAccessibleDescription(getString("date_accessible"));
    // Project Name input panel for Project Setup
        setupProjectNameTextField = new JTextField();
        setupProjectNameTextField.setFont(
            new Font("SansSerif", Font.PLAIN, 16)
        );
        setupProjectNameTextField.setToolTipText("Enter a new Project Name");

    JPanel setupProjectNamePanel = new JPanel(new BorderLayout());
    setupProjectNamePanel.add(setupProjectNameTextField, BorderLayout.CENTER);

        addLabelAndField(
            ProjectSetup,
            "<html>Project Name:<br><i>(Enter a new project name)</i></html>",
            setupProjectNamePanel
        );

        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.add(dateField, BorderLayout.CENTER);
    // date tooltip shown on field itself

        addLabelAndField(
            ProjectSetup,
            "<html>Date: <br><i>(YYYY-MM-DD)</i></html>",
            datePanel
        );
        String[] studentOptions = loadOptionsFromFile(
            "json_files/students.json",
            "students"
        );
        String[] teacherOptions = loadOptionsFromFile(
            "json_files/teachers.json",
            "teachers"
        );
        String[] leaOptions = loadOptionsFromFile(
            "json_files/LEAs.json",
            "LEAs"
        );
        String[] tviOptions = loadOptionsFromFile(
            "json_files/tvis.json",
            "tvis"
        );
        String[] mediaTypeOptions = { "Braille", "Large Print", "Audio" };
        studentField = new JComboBox<>(studentOptions);
        studentField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    // Ensure compact height for dropdowns
    setFixedHeight(studentField, 28);
        studentField.setToolTipText(getString("student_tooltip"));
        studentField
            .getAccessibleContext()
            .setAccessibleDescription(getString("student_accessible"));
        JPanel studentPanel = new JPanel(new BorderLayout());
        studentPanel.add(studentField, BorderLayout.CENTER);
        addLabelAndField(
            ProjectSetup,
            "<html>Student <br> <i>Select Student from dropdown list</i></html>",
            studentPanel
        );

        String[] subjectOptions = loadOptionsFromFile(
            "json_files/subjects.json",
            "subjects"
        );
        subjectField = new JComboBox<>(subjectOptions);
        subjectField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    setFixedHeight(subjectField, 28);
        subjectField.setToolTipText(getString("subject_tooltip"));
        subjectField
            .getAccessibleContext()
            .setAccessibleDescription(getString("subject_accessible"));
        JPanel subjectPanel = new JPanel(new BorderLayout());
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        addLabelAndField(
            ProjectSetup,
            "<html>Academic Subject<br><i>Select Subject from Dropdown List</i></html>",
            subjectPanel
        );

        String[] schoolOptions = loadOptionsFromFile(
            "json_files/schools.json",
            "schools"
        );
        schoolField = new JComboBox<>(
            loadOptionsFromFile("json_files/schools.json", "schools")
        );
        schoolField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    setFixedHeight(schoolField, 28);

        schoolField.setToolTipText(getString("school_tooltip"));
        schoolField
            .getAccessibleContext()
            .setAccessibleDescription(getString("school_accessible"));
        // Group school field in a panel (help button removed; tooltip kept on the field)
        JPanel schoolPanel = new JPanel(new BorderLayout());
        schoolPanel.add(schoolField, BorderLayout.CENTER);
        addLabelAndField(ProjectSetup, "School", schoolPanel);

        ProjectTracking = new JPanel(new GridLayout(0, 2, 10, 10));
        ProjectTracking.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        // Initialize Project Status Panel
        projectStatusPanel.setBorder(
            BorderFactory.createTitledBorder("Project Status")
        );

        logger.info("Initializing Proof Status field...");
        if (proofStatusField.getItemCount() == 0) {
            logger.error(
                "Error: Proof Status field is empty after loading from proof_status.json."
            );
        } else {
            logger.info(
                "Proof Status field loaded successfully with {} items.",
                proofStatusField.getItemCount()
            );
        }
        proofStatusField.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // --- Language selector menu ---
        // Duplicate language menu setup removed to prevent repeated menu and items
        proofStatusField.setToolTipText(getString("proof_status_help"));
        proofStatusField
            .getAccessibleContext()
            .setAccessibleDescription(getString("proof_status_help"));
        JPanel proofStatusPanel = new JPanel(new BorderLayout());
        proofStatusPanel.add(proofStatusField, BorderLayout.CENTER);
        addLabelAndField(projectStatusPanel, "Proof Status", proofStatusPanel);
        //addLabelAndField(inputPanel, "Delivery Mode", deliveryModeField);

        logger.info("Initializing Teachers field...");
        teacherField = new JList<>(teacherOptions);
        if (teacherField == null) {
            logger.error("Error: Teachers field is null.");
        }
        teacherField.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );
        JScrollPane teacherScrollPane = new JScrollPane(teacherField);
    teacherScrollPane.setPreferredSize(new Dimension(200, 100));
    // JList inside JScrollPane - constrain visible row height
    teacherField.setFixedCellHeight(18);
        teacherScrollPane.setToolTipText(getString("teacher_tooltip"));
        teacherScrollPane
            .getAccessibleContext()
            .setAccessibleDescription(getString("teacher_accessible"));
        JPanel teacherPanel = new JPanel(new BorderLayout());
        teacherPanel.add(teacherScrollPane, BorderLayout.CENTER);
        addLabelAndField(ProjectSetup, "Teachers", teacherPanel);

        logger.info("Initializing LEA field...");
        leaField = new JComboBox<>(leaOptions);
        leaField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        leaField.setPreferredSize(new Dimension(200, 28));
    setFixedHeight(leaField, 28);
        leaField.setToolTipText(getString("lea_tooltip"));
        leaField
            .getAccessibleContext()
            .setAccessibleDescription(getString("lea_accessible"));
        JPanel leaPanel = new JPanel(new BorderLayout());
        leaPanel.add(leaField, BorderLayout.CENTER);
        addLabelAndField(ProjectSetup, "LEA", leaPanel);

        logger.info("Initializing TVIs field...");
        tviField = new JList<>(new String[] { "TVI 1", "TVI 2" });
        if (tviField == null) {
            logger.error("Error: TVIs field is null.");
        }
        tviField.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );
        JScrollPane tviScrollPane = new JScrollPane(tviField);
    tviScrollPane.setPreferredSize(new Dimension(200, 100));
    tviField.setFixedCellHeight(18);
        tviScrollPane.setToolTipText(getString("tvi_tooltip"));
        tviScrollPane
            .getAccessibleContext()
            .setAccessibleDescription(getString("tvi_accessible"));
        JPanel tviPanel = new JPanel(new BorderLayout());
        tviPanel.add(tviScrollPane, BorderLayout.CENTER);
        addLabelAndField(ProjectSetup, "TVIs", tviPanel);
        logger.info("Initializing Media Type field...");
        // Initialize media type dropdown from JSON file, then validate contents.
        mediaTypeField = new JComboBox<>(
            loadOptionsFromFile("json_files/media_type.json", "media_types")
        );
        if (mediaTypeField == null || mediaTypeField.getItemCount() == 0) {
            logger.error("Error: Media Type field is null or empty after loading media_type.json.");
        } else {
            logger.info("Media Type field loaded successfully with {} items.", mediaTypeField.getItemCount());
        }
        mediaTypeField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    setFixedHeight(mediaTypeField, 28);
        mediaTypeField.setToolTipText(getString("media_type_tooltip"));
        mediaTypeField
            .getAccessibleContext()
            .setAccessibleDescription(getString("media_type_accessible"));
        JPanel mediaTypePanel = new JPanel(new BorderLayout());
        mediaTypePanel.add(mediaTypeField, BorderLayout.CENTER);
        addLabelAndField(ProjectSetup, "Media Type", mediaTypePanel);
        logger.info("Initializing Project Type field...");
        projectField = new JComboBox<>(getProjectTypesFromJson());
        projectField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        if (projectField == null) {
            logger.error("Error: Project Type field is null.");
        }
        projectField.setToolTipText(getString("project_tooltip"));
    setFixedHeight(projectField, 28);
        projectField
            .getAccessibleContext()
            .setAccessibleDescription(getString("project_accessible"));
        JPanel projectPanel = new JPanel(new BorderLayout());
        projectPanel.add(projectField, BorderLayout.CENTER);
        addLabelAndField(ProjectSetup, "Project Type", projectPanel);
        logger.info("Initializing Time field...");
        timeField = new JTextField();
        if (timeField == null) {
            logger.error("Error: Time field is null.");
        }
        timeField.setToolTipText(getString("time_tooltip"));
    setFixedHeight(timeField, 28);
        timeField
            .getAccessibleContext()
            .setAccessibleDescription(getString("time_accessible"));
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.add(timeField, BorderLayout.CENTER);
        // Removed duplicate time field from Project Status panel

        // Move fields to Project Status Panel
        logger.info("Initializing Updated field...");
        if (updatedField == null) {
            logger.error("Error: Updated field is null.");
        }
        updatedField.setPreferredSize(new Dimension(200, 28));
        updatedField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        setFixedHeight(updatedField, 28);
        JPanel updatedPanel = new JPanel(new BorderLayout());
        updatedPanel.add(updatedField, BorderLayout.CENTER);
        addLabelAndField(
            projectStatusPanel,
            "<html>Updated: <br>(YYYY-MM-DD)</html>",
            updatedPanel
        );
        updatedField.setVisible(true); // Ensure visibility

    notesField = new JTextArea(5, 20); // Multiline text area
        notesField.setLineWrap(true);
        notesField.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesField);
        notesScrollPane.setPreferredSize(new Dimension(200, 60));
        notesScrollPane.setToolTipText(getString("notes_tooltip"));
        notesScrollPane
            .getAccessibleContext()
            .setAccessibleDescription(getString("notes_accessible"));
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.add(notesScrollPane, BorderLayout.CENTER);
        addLabelAndField(projectStatusPanel, "Notes", notesPanel);

    deliveryModeField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    JPanel deliveryPanel = new JPanel(new BorderLayout());
    deliveryPanel.add(deliveryModeField, BorderLayout.CENTER);
    addLabelAndField(ProjectSetup, "Delivery Mode", deliveryPanel);

        completeComboBox = new JComboBox<>(new String[] { "No", "Yes" });
        completeComboBox.setSelectedIndex(0); // Default to "No"
        completeComboBox.setFont(new Font("SansSerif", Font.PLAIN, 16));
    setFixedHeight(completeComboBox, 28);
        completeComboBox.setToolTipText(
            "Select Yes if the project or task is complete, No otherwise."
        );
        completeComboBox
            .getAccessibleContext()
            .setAccessibleDescription(
                "Select Yes if the task is complete, No otherwise."
            );
    JPanel completePanel = new JPanel(new BorderLayout());
    completePanel.add(completeComboBox, BorderLayout.CENTER);
    addLabelAndField(projectStatusPanel, "Complete", completePanel);

    submitButton = new JButton("  Submit");
    // Make submit button compact height but keep width
    setFixedHeight(submitButton, 28);
    submitButton.setPreferredSize(new Dimension(150, 28));
        submitButton.setMnemonic(KeyEvent.VK_S);
        submitButton.addActionListener(e -> submitData());
        submitButton
            .getAccessibleContext()
            .setAccessibleDescription(
                "Submit the entered data to the database"
            );
    generatePdfButton = new JButton("Generate PDF Report");
        generatePdfButton.setMnemonic(KeyEvent.VK_G);
    setFixedHeight(generatePdfButton, 28);
        generatePdfButton.addActionListener(e -> showDateRangeDialog());
        generatePdfButton
            .getAccessibleContext()
            .setAccessibleDescription(
                "Generate a PDF report for the project tracking data"
            );
        ProjectTracking.add(generatePdfButton);
        generatePdfButton
            .getAccessibleContext()
            .setAccessibleDescription(
                "Generate a PDF report for a specified date range"
            );

    // Place submit button in its own panel at the bottom of the Project Setup scroll area
    JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    submitPanel.add(submitButton);

        JPanel projectSetupPanel = new JPanel(new BorderLayout());
        projectSetupPanel.add(ProjectSetup, BorderLayout.CENTER);
        projectSetupPanel.add(submitPanel, BorderLayout.SOUTH);

        // Table
        tableModel = new DefaultTableModel(
            new Object[][] {}, // Empty data
            new String[] {
                // Column names
                "Date",
                "Student",
                "Subject",
                "School",
                "Project",
                "Time",
            }
        );
        dataTable = new JTable(tableModel);
        dataTable
            .getAccessibleContext()
            .setAccessibleDescription("Table showing ledger entries");
        dataTable.setEnabled(true);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        JPanel filePickerPanel = new JPanel(new GridLayout(0, 2, 10, 50));
        String[] fileTypes = loadOptionsFromFile(
            "json_files/media_type.json",
            "media_types"
        );
        for (String fileType : fileTypes) {
            JButton filePickerButton = new JButton(
                "Select " + fileType + " File"
            );
            filePickerButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String relativePath = selectedFile
                        .getAbsolutePath()
                        .replaceFirst(
                            ".*brailleFileDirectory",
                            "brailleFileDirectory"
                        );
                    submitFileToDatabase(fileType, relativePath);
                }
            });
            filePickerPanel.add(filePickerButton);
        }

        // Accessible drag-and-drop area
        JPanel dragDropPanel = new JPanel();
        dragDropPanel.setBorder(
            BorderFactory.createTitledBorder(
                "Drop files here or use the button"
            )
        );
        dragDropPanel.setFocusable(true);
        dragDropPanel
            .getAccessibleContext()
            .setAccessibleName("File Drop Area");
        dragDropPanel
            .getAccessibleContext()
            .setAccessibleDescription(
                "Drop files here or use the file picker button. Tab to focus."
            );
        JLabel dragDropInstructions = new JLabel(
            "<html>You can use Tab to focus the file picker button, or drag and drop files here.<br>Screen reader users: focus this area for instructions.</html>"
        );
        dragDropPanel.add(dragDropInstructions);
        dragDropPanel.addFocusListener(
            new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                /**
                 * Focus gained handler for accessible components.
                 * @since 2025.07.01
                 */
                    announceToScreenReader(
                        "File drop area focused. You can drop files here or use the file picker button."
                    );
                }
            }
        );
        // Drag-and-drop logic can be added here if needed
        filePickerPanel.add(dragDropPanel);

    // Remove help buttons and move their tooltip text to the fields
    removeHelpButtonsAndMoveTooltips(ProjectSetup);
    removeHelpButtonsAndMoveTooltips(projectStatusPanel);

    // Wrap the Project Setup panel in a scroll pane so its contents can scroll
        JScrollPane projectSetupScroll = new JScrollPane(
            projectSetupPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        tabbedPane.addTab("Project Setup", projectSetupScroll);
        add(tabbedPane, BorderLayout.CENTER);

        initializeDatabase();
        loadStartupOptions();
        loadDataFromFiles();
        loadDataFromDatabase();

        ProjectTracking = new JPanel(new GridLayout(0, 2, 10, 10));
        ProjectTracking.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        // Table for Project Tracking
        // Reorder: Project Name at the top
        projectNameField = new JComboBox<>(getProjectNames());
        projectNameField.setPreferredSize(new Dimension(200, 28));
        projectNameField.setToolTipText(getString("project_name_help"));
        projectNameField
            .getAccessibleContext()
            .setAccessibleDescription(getString("project_name_help"));
    JPanel projectNamePanel = new JPanel(new BorderLayout());
    projectNamePanel.add(projectNameField, BorderLayout.CENTER);

        refreshProjectNames(); // Now safe to call, projectNameField is initialized

        // Remove Project Status title/border
        projectStatusPanel.setBorder(null);

        // Add fields in desired order
        projectStatusPanel.removeAll();
        addLabelAndField(projectStatusPanel, "Project Name", projectNamePanel);

        // Updated field
        updatedField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        updatedField.setToolTipText(
            "Enter the last updated date for this project."
        );
        updatedField
            .getAccessibleContext()
            .setAccessibleDescription(
                "Enter the last updated date for this project."
            );
    JPanel updatedPanelStatus = new JPanel(new BorderLayout());
    updatedPanelStatus.add(updatedField, BorderLayout.CENTER);
        addLabelAndField(projectStatusPanel, "Updated", updatedPanelStatus);

        // Element of Project
        projectElementField = new JComboBox<>(
            loadOptionsFromFile(
                "json_files/project_element.json",
                "project_elements"
            )
        );
    projectElementField.setPreferredSize(new Dimension(200, 28));
    setFixedHeight(projectElementField, 28);
        projectElementField.setToolTipText(getString("project_element_help"));
        projectElementField
            .getAccessibleContext()
            .setAccessibleDescription(getString("project_element_help"));
        JPanel projectElementPanel = new JPanel(new BorderLayout());
        projectElementPanel.add(projectElementField, BorderLayout.CENTER);
        addLabelAndField(
            projectStatusPanel,
            "Element of Project",
            projectElementPanel
        );

        // Time
        projectTimeField = new JTextField();
    projectTimeField.setPreferredSize(new Dimension(200, 28));
    setFixedHeight(projectTimeField, 28);
        projectTimeField.setToolTipText(getString("project_time_help"));
        projectTimeField
            .getAccessibleContext()
            .setAccessibleDescription(getString("project_time_help"));
        JPanel projectTimePanel = new JPanel(new BorderLayout());
        projectTimePanel.add(projectTimeField, BorderLayout.CENTER);
        addLabelAndField(projectStatusPanel, "Time (HH:MM)", projectTimePanel);

        // Proof Status
        proofStatusField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        proofStatusField.setToolTipText(getString("proof_status_help"));
        proofStatusField
            .getAccessibleContext()
            .setAccessibleDescription(getString("proof_status_help"));
        JPanel proofStatusPanelStatus = new JPanel(new BorderLayout());
        proofStatusPanelStatus.add(proofStatusField, BorderLayout.CENTER);
        addLabelAndField(
            projectStatusPanel,
            "Proof Status",
            proofStatusPanelStatus
        );

        // Completed
        completedField = new JComboBox<>(new String[] { "No", "Yes" });
        completedField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        completedField.setToolTipText(
            "Select Yes if the project is completed, No otherwise."
        );
        completedField
            .getAccessibleContext()
            .setAccessibleDescription(
                "Select Yes if the project is completed, No otherwise."
            );
    JPanel completedPanelStatus = new JPanel(new BorderLayout());
    completedPanelStatus.add(completedField, BorderLayout.CENTER);
    addLabelAndField(projectStatusPanel, "Completed", completedPanelStatus);

    JButton addTrackingButton = new JButton("Add Tracking Info");
    setFixedHeight(addTrackingButton, 28);
    addTrackingButton.setPreferredSize(new Dimension(220, 28));
        addTrackingButton.addActionListener(e -> addTrackingInfo());

    // Configure Generate PDF Button - make wider so caption fits
    generatePdfButton.setPreferredSize(new Dimension(220, 28));

        // Add Project Status Panel as main panel
        JPanel projectStatusMainPanel = new JPanel(new BorderLayout());
        projectStatusMainPanel.add(projectStatusPanel, BorderLayout.NORTH);

        // Bottom button panel (align with Submit placement in Project Setup)
        JPanel projectStatusBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        projectStatusBtnPanel.add(addTrackingButton);
        projectStatusBtnPanel.add(generatePdfButton);
        projectStatusMainPanel.add(projectStatusBtnPanel, BorderLayout.SOUTH);

        // Wrap the Project Status main panel in a scroll pane to allow scrolling
        JScrollPane projectStatusScroll = new JScrollPane(
            projectStatusMainPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        tabbedPane.addTab("Project Status", projectStatusScroll);

        // Create Projects tab with the data table and an Open Project button
        JPanel projectsPanel = new JPanel(new BorderLayout());
        projectsPanel.add(scrollPane, BorderLayout.CENTER);
        JButton openProjectButton = new JButton("Open Project");
        openProjectButton.addActionListener(e -> {
            int row = dataTable.getSelectedRow();
            if (row == -1) {
                setStatus("Please select a project row first.");
                announceToScreenReader("Please select a project row first.");
                return;
            }
            String projectName = dataTable.getValueAt(row, 4).toString();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT folder_path FROM Project_Name WHERE name = ?"
                )) {
                pstmt.setString(1, projectName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String folder = rs.getString("folder_path");
                    if (folder == null || folder.isEmpty()) {
                        setStatus("No folder path stored for this project.");
                        announceToScreenReader("No folder path stored for this project.");
                        return;
                    }
                    Desktop.getDesktop().open(new File(folder));
                } else {
                    setStatus("Project folder not found in database.");
                    announceToScreenReader("Project folder not found in database.");
                }
            } catch (Exception ex) {
                setStatus("Error opening project folder: " + ex.getMessage());
                announceToScreenReader("Error opening project folder.");
            }
        });
        JPanel projectsBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        projectsBtnPanel.add(openProjectButton);
        projectsPanel.add(projectsBtnPanel, BorderLayout.SOUTH);
        // Wrap the Projects panel (table) in a scroll pane for inner scrolling
        JScrollPane projectsScroll = new JScrollPane(
            projectsPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        tabbedPane.addTab("Projects", projectsScroll);

        // Set up focus traversal
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        setFocusCycleRoot(true);

        // Make all major components focusable for tab navigation
        dateField.setFocusable(true);
        updatedField.setFocusable(true);
        if (teacherField != null) teacherField.setFocusable(true);
        if (tviField != null) tviField.setFocusable(true);
        if (leaField != null) leaField.setFocusable(true);
        if (mediaTypeField != null) mediaTypeField.setFocusable(true);
        if (proofStatusField != null) proofStatusField.setFocusable(true);
        if (deliveryModeField != null) deliveryModeField.setFocusable(true);
        if (projectNameField != null) projectNameField.setFocusable(true);
        if (notesField != null) notesField.setFocusable(true);
        if (projectElementField != null) projectElementField.setFocusable(true);
        if (projectTimeField != null) projectTimeField.setFocusable(true);
        if (timeField != null) timeField.setFocusable(true);
        if (projectField != null) projectField.setFocusable(true);
        if (schoolField != null) schoolField.setFocusable(true);
        if (studentField != null) studentField.setFocusable(true);
        if (subjectField != null) subjectField.setFocusable(true);
        completeComboBox.setFocusable(true);
        submitButton.setFocusable(true);
        generatePdfButton.setFocusable(true);
        dataTable.setFocusable(true);

        // Table accessibility: enable keyboard navigation and selection
        dataTable.setRowSelectionAllowed(true);
        dataTable.setColumnSelectionAllowed(true);
        dataTable.setCellSelectionEnabled(true);
        dataTable.getTableHeader().setFocusable(true);
        dataTable.getTableHeader().setReorderingAllowed(false);

        // Add global key listener for Ctrl+.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
                e -> {
                    if (
                        e.getID() == KeyEvent.KEY_PRESSED &&
                        e.isControlDown() &&
                        e.getKeyCode() == KeyEvent.VK_PERIOD
                    ) {
                        showHelpDialog();
                        return true;
                    }
                    return false;
                }
            );
    }

    /**
     * Show a dialog with keyboard shortcuts and accessibility help.
     */
    private void showHelpDialog() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        JLabel helpLabel = new JLabel(getString("help_text"));
        helpLabel.setFocusable(true);
    contentPanel.add(helpLabel, BorderLayout.CENTER);
    showAccessibleDialog(this, getString("help_title"), contentPanel, false);
    }

    /**
     * Show a dialog with the tutorial/how-to content loaded from tutorial.html.
     */
    private void showTutorialDialog() {
        String tutorialText =
            "Braille Transcription Ledger - Quick Start Tutorial\n\n" +
            "Welcome to the Accessible Document Generation Ledger! This tutorial will guide you through the basic features and workflow of the program.\n\n" +
            "1. Overview\n" +
            "This application helps you track and manage transcription projects, students, teachers, and related data in an accessible way.\n\n" +
            "2. Main Features\n" +
            "- Add/Edit Ledger Entries: Enter details about students, projects, time spent, and more.\n" +
            "- Keyboard Accessibility: All interactive elements are accessible via keyboard shortcuts.\n" +
            "- Generate PDF Reports: Export your data for sharing or record-keeping.\n" +
            "- Theme Support: Choose from various visual themes for comfort and accessibility.\n\n" +
            "3. Basic Workflow\n" +
            "- Start a New Entry: Fill in the Date, Student, School, Project, Time, and other required fields. Use the Tab key to move between fields.\n" +
            "- Submit Data: Click the Submit button or press Ctrl + Enter to save your entry.\n" +
            "- Generate a PDF Report: Click Generate PDF or press Ctrl + G to create a report of your entries.\n" +
            "- Navigate the Table: Use Arrow Keys to move through the data table.\n\n" +
            "4. Accessibility & Shortcuts\n" +
            "- Ctrl + . : Open the Help menu\n" +
            "- Ctrl + Enter : Submit the form\n" +
            "- Ctrl + G : Generate PDF\n" +
            "- Tab / Shift + Tab : Move between fields\n" +
            "- Alt : Focus the menu bar\n" +
            "- Esc : Close dialogs\n\n" +
            "Tip: All help buttons (?) next to fields provide additional guidance.\n\n" +
            "5. Troubleshooting\n" +
            "- If you encounter errors, check that all required fields are filled in correctly.\n" +
            "- Use the Help menu for keyboard shortcuts and accessibility tips.\n" +
            "- For further assistance, consult the documentation or contact support.\n\n" +
            "6. Additional Resources\n" +
            "Visit the project repository: https://github.com/ryhunsaker/BrailleTranscriptionLedger\n\n" +
            "© 2025 Michael Ryan Hunsaker, M.Ed., Ph.D. All rights reserved.";

        JTextArea textArea = new JTextArea(tutorialText);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(700, 500));
    showAccessibleDialog(this, "Program Tutorial", scrollPane, false);
    }

    /**
     * Load the project's config.properties (if present) and return a Properties object.
     */
    private Properties loadConfigProperties() {
        Properties p = new Properties();
        Path cfg = Paths.get("config.properties");
        if (Files.exists(cfg)) {
            try (java.io.InputStream in = Files.newInputStream(cfg)) {
                p.load(in);
            } catch (IOException e) {
                logger.warn("Failed to load config.properties: {}", e.getMessage());
            }
        }
        return p;
    }

    /**
     * Save the provided Properties to config.properties in the program root.
     */
    private void saveConfigProperties(Properties p) {
        Path cfg = Paths.get("config.properties");
        try (java.io.OutputStream out = Files.newOutputStream(cfg)) {
            p.store(out, "BrailleTranscriptionLedger configuration");
            setStatus("Configuration saved.");
        } catch (IOException e) {
            logger.error("Failed to save config.properties: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to save configuration: " + e.getMessage());
        }
    }

    /**
     * Show a small Preferences dialog allowing the user to edit window.maximized and db.path.
     */
    private void showPreferencesDialog() {
    Properties p = loadConfigProperties();
        JPanel main = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;

    // Current active DB URL display
    c.gridx = 0;
    c.gridy = 0;
    JLabel currentDbLabel = new JLabel("Active DB: " + (DB_URL != null ? DB_URL : "(not set)"));
    c.gridwidth = 2;
    main.add(currentDbLabel, c);
    c.gridwidth = 1;
    c.gridy = 1;

    main.add(new JLabel("Start maximized:"), c);
    c.gridx = 1;
        String maxVal = p.getProperty("window.maximized", "true");
        JCheckBox maximizedBox = new JCheckBox();
        maximizedBox.setSelected(Boolean.parseBoolean(maxVal));
        main.add(maximizedBox, c);

    c.gridx = 0;
    c.gridy = 2;
        main.add(new JLabel("Database path (db.path):"), c);
        c.gridx = 1;
        String dbPathVal = p.getProperty("db.path", "app_home/ledger");
        JTextField dbPathField = new JTextField(dbPathVal, 40);
        main.add(dbPathField, c);

        // LOG_LEVEL option
    c.gridx = 0;
    c.gridy = 3;
        main.add(new JLabel("Log Level (LOG_LEVEL):"), c);
        c.gridx = 1;
        String logVal = p.getProperty("log.level", System.getenv().getOrDefault("LOG_LEVEL", "INFO"));
        JComboBox<String> logLevelCombo = new JComboBox<>(new String[] {"ERROR", "WARN", "INFO", "DEBUG", "TRACE"});
        logLevelCombo.setSelectedItem(logVal.toUpperCase());
        main.add(logLevelCombo, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton restoreBtn = new JButton("Restore Defaults");
        JButton cancelBtn = new JButton("Cancel");

    saveBtn.addActionListener(e -> {
            // Validate db.path: must be writable or creatable
            String v = dbPathField.getText().trim();
            if (v.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Database path cannot be empty.");
                return;
            }
            Path testPath = Paths.get(v.endsWith(".db") ? v : v);
            try {
                // If parent dirs don't exist, attempt to create them
                Path parent = testPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                // Attempt to create a temp file next to path to verify writability
                Path probe = (Files.isDirectory(testPath)) ? testPath.resolve(".probe") : testPath.getParent() != null ? testPath.getParent().resolve(".probe") : Paths.get(".", ".probe");
                Files.writeString(probe, "probe");
                Files.deleteIfExists(probe);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "The specified db.path is not writable: " + ex.getMessage());
                return;
            }

            p.setProperty("window.maximized", Boolean.toString(maximizedBox.isSelected()));
            p.setProperty("db.path", v);
            p.setProperty("log.level", ((String) logLevelCombo.getSelectedItem()).toUpperCase());
            saveConfigProperties(p);

            // Apply window maximized setting immediately
            if (maximizedBox.isSelected()) {
                setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            } else {
                setExtendedState(getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
            }

            // Apply log level immediately if Logback present
            try {
                LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
                ch.qos.logback.classic.Logger root = lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                Level level = Level.toLevel((String) logLevelCombo.getSelectedItem(), Level.INFO);
                root.setLevel(level);
                logger.info("Log level set to {}", level);
            } catch (Exception ex) {
                logger.warn("Unable to set log level dynamically: {}", ex.getMessage());
            }

            // If DB path changed, attempt to reconnect (safe attempt) using lifecycle-managed reconnect
            String newDb = v;
            String currentBase = DB_URL != null ? DB_URL.replaceFirst("jdbc:h2:", "").replaceFirst(";.*", "") : null;
            if (currentBase == null || !stripDbSuffix(newDb).equals(stripDbSuffix(currentBase))) {
                try {
                    reconnectDatabase(newDb);
                    currentDbLabel.setText("Active DB: " + DB_URL);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to reconnect to the specified database: " + ex.getMessage());
                    return;
                }
            }

            Window w = SwingUtilities.getWindowAncestor(main);
            if (w instanceof JDialog) ((JDialog) w).dispose();
        });

        restoreBtn.addActionListener(e -> {
            // restore defaults
            maximizedBox.setSelected(true);
            dbPathField.setText("app_home/ledger");
            logLevelCombo.setSelectedItem("INFO");
        });

        cancelBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(main);
            if (w instanceof JDialog) ((JDialog) w).dispose();
        });

        btns.add(restoreBtn);
        btns.add(saveBtn);
        btns.add(cancelBtn);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(main, BorderLayout.CENTER);
        wrapper.add(btns, BorderLayout.SOUTH);

        showAccessibleDialog(this, "Preferences", wrapper, false);
    }

    /**
     * Setup the hidden live region label for screen reader announcements.
     */
    private void setupLiveRegion() {
        liveRegionLabel.setVisible(false); // Hidden from visual UI
        liveRegionLabel
            .getAccessibleContext()
            .setAccessibleName("Status Message");
        liveRegionLabel
            .getAccessibleContext()
            .setAccessibleDescription(
                "Live region for screen reader announcements"
            );
        this.add(liveRegionLabel, BorderLayout.SOUTH); // Add to frame, but hidden
    }

    /**
     * Announce a message to screen readers via the live region.
     */
    private void announceToScreenReader(String message) {
        liveRegionLabel.setText(message);
        liveRegionLabel
            .getAccessibleContext()
            .firePropertyChange(
                AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                null,
                message
            );
    }

    /**
     * Lifecycle-managed reconnect to a new H2 database base path.
     * This method attempts to gracefully shutdown any local H2 TCP server (if running),
     * update the DB_URL, reinitialize database schema, and reload UI state.
     * It is synchronized to avoid concurrent reconnection attempts.
     */
    /**
     * Lifecycle-managed reconnect to a new H2 database base path.
     * This method attempts to gracefully shutdown any local H2 TCP server (if running),
     * update the DB_URL, reinitialize database schema, and reload UI state.
     * It is synchronized to avoid concurrent reconnection attempts.
     *
     * @param newDbBase database base path (may include ./ or absolute path)
     * @throws Exception when the connection test or schema initialization fails
     * @see #resolveDbBasePath(String[])
     * @since 2025.07.01
     */
    private synchronized void reconnectDatabase(String newDbBase) throws Exception {
        // Attempt to shutdown H2 at current DB_URL (best-effort)
        try {
            if (DB_URL != null) {
                // Attempt to run SHUTDOWN if the DB file is accessible
                try (Connection c = DriverManager.getConnection(DB_URL)) {
                    try (Statement s = c.createStatement()) {
                        s.execute("SHUTDOWN");
                    }
                } catch (SQLException se) {
                    // ignore - H2 may be in embedded file mode without server
                }
            }
        } catch (Exception ex) {
            logger.warn("Ignoring error during DB shutdown: {}", ex.getMessage());
        }

        // Build new DB_URL and test connection
        String base = stripDbSuffix(newDbBase);
        String newUrl = "jdbc:h2:" + base + ";AUTO_SERVER=TRUE";
        try (Connection conn = DriverManager.getConnection(newUrl)) {
            try (Statement st = conn.createStatement()) {
                // Ensure necessary schema exists
                st.executeUpdate("CREATE TABLE IF NOT EXISTS Project_Name (id IDENTITY PRIMARY KEY, name VARCHAR, folder_path TEXT)");
            }
        }

        // If successful, assign DB_URL and reinitialize DB-dependent UI
        DB_URL = newUrl;
        // Reinitialize database structures in memory/upgrade if needed
        initializeDatabase();
        // Reload projects and table data
        loadDataFromDatabase();
        refreshProjectNames();
        setStatus("Connected to: " + DB_URL);
    }

    /**
     * Show an accessible dialog (wrapper that defaults to modal).
     */
    private void showAccessibleDialog(
        JFrame parent,
        String title,
        JComponent content
    ) {
        showAccessibleDialog(parent, title, content, true);
    }

    /**
     * Show an accessible dialog with explicit modal control.
     * @param parent The parent frame.
     * @param title The dialog title.
     * @param content The main content component.
     * @param modal Whether the dialog should be modal (true) or modeless (false).
     */
    private void showAccessibleDialog(
        JFrame parent,
        String title,
        JComponent content,
        boolean modal
    ) {
        JDialog dialog = new JDialog(parent, title, modal);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.add(content, BorderLayout.CENTER);

        // Announce dialog to screen readers
        announceToScreenReader(title + " dialog opened.");

        // Focus handling: request focus for content when dialog gains focus
        dialog.setFocusable(true);
        dialog.addWindowFocusListener(
            new WindowAdapter() {
                public void windowGainedFocus(WindowEvent e) {
                /**
                 * Window focus gained handler.
                 * @since 2025.07.01
                 */
                    content.requestFocusInWindow();
                }
            }
        );

        // Esc key closes dialog
        dialog
            .getRootPane()
            .registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );

        // Add Close button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(helpDialogFont);
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set font for all components in the dialog
        setFontRecursively(content, helpDialogFont);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Recursively set font for all components in a container.
     */
    private void setFontRecursively(Component comp, Font font) {
        comp.setFont(font);
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                setFontRecursively(child, font);
            }
        }
    }

    /**
     * Set a fixed height for a component while preserving or allowing flexible width.
     */
    private void setFixedHeight(JComponent comp, int height) {
        Dimension pref = comp.getPreferredSize();
        int width = pref != null && pref.width > 0 ? pref.width : 200;
        comp.setPreferredSize(new Dimension(width, height));
        comp.setMinimumSize(new Dimension(40, height));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    /**
     * Create a safe folder name from project name by removing unsafe chars.
     */
    private String sanitizeForFileName(String name) {
        if (name == null) return "project";
        // replace any non-alphanumeric, space, dot, underscore, or hyphen with underscore
        return name.trim().replaceAll("[^A-Za-z0-9 ._-]", "_").replaceAll("\\s+", "_");
    }

    /**
     * Resolve the database base path (without .db suffix) using precedence:
     * <ol>
     *   <li>command-line {@code --dbpath=&lt;path&gt;}</li>
     *   <li>environment variable {@code LEDGER_DB_PATH}</li>
     *   <li>property {@code db.path} in {@code config.properties} at program root</li>
     *   <li>user home {@code ~/.brailleledger/ledger}</li>
     *   <li>{@code app_home/ledger} if present in program root</li>
     *   <li>default {@code ./ledger}</li>
     * </ol>
     *
     * @param args Command-line arguments that may contain {@code --dbpath=}.
     * @return The DB base path (without a trailing {@code .db}) to use for H2.
    * @see <a href="README.md#configuration-keys">README.md#configuration-keys</a>
     */
    public static String resolveDbBasePath(String[] args) {
    /**
     * Resolve the base filesystem path used for the embedded H2 database according
     * to precedence rules: CLI args -> LEDGER_DB_PATH env var -> config.properties -> user home -> app_home -> cwd.
     * <p>
     * Example usage:
     * <pre>
     * String base = LedgerGUI.resolveDbBasePath(new String[]{"--dbpath=./data/ledger"});
     * </pre>
     * @since 2025.07.01
     */
        // Delegate to the supplier-aware overload using system providers
        return resolveDbBasePath(
            args,
            System::getenv,
            () -> {
                Path config = Paths.get("config.properties");
                Properties p = new Properties();
                if (Files.exists(config)) {
                    try (java.io.InputStream in = Files.newInputStream(config)) {
                        p.load(in);
                    } catch (IOException e) {
                        // ignore
                    }
                }
                return p;
            },
            () -> System.getProperty("user.home"),
            path -> Files.exists(Paths.get(path)),
            (root) -> {
                if (TEST_ROOT_OVERRIDE != null) return TEST_ROOT_OVERRIDE;
                try {
                    return new File(".").getCanonicalPath();
                } catch (IOException e) {
                    return ".";
                }
            }
        );
    }

    /**
     * Overload that accepts providers so tests can inject env/config values.
     *
     * @param args Command-line arguments (may be {@code null}).
     * @param envProvider Function used to read environment variables (key -> value).
     * @param configSupplier Supplier that returns a loaded {@link Properties}
     *                       instance from {@code config.properties}.
     * @param userHomeSupplier Supplier for the user's home directory path.
     * @param existsChecker Function to check whether a path exists (path -> boolean).
     * @param rootPathProvider Function to obtain the program root path (ignored in normal runs).
     * @return The DB base path (without a trailing {@code .db}) chosen by precedence.
     */
    public static String resolveDbBasePath(
    /**
     * Key-based overload used for testing; resolves DB base path using a supplier.
     * @since 2025.07.01
     */
        String[] args,
        java.util.function.Function<String, String> envProvider,
        java.util.function.Supplier<Properties> configSupplier,
        java.util.function.Supplier<String> userHomeSupplier,
        java.util.function.Function<String, Boolean> existsChecker,
        java.util.function.Function<String, String> rootPathProvider
    ) {
        // 1) command-line
        if (args != null) {
            for (String a : args) {
                if (a != null && a.startsWith("--dbpath=")) {
                    return stripDbSuffix(a.substring("--dbpath=".length()));
                }
            }
        }

        // 2) environment variable
        try {
            String env = envProvider.apply("LEDGER_DB_PATH");
            if (env != null && !env.isBlank()) return stripDbSuffix(env);
        } catch (Exception e) {
            // ignore
        }

        // 3) config.properties via supplier
        try {
            Properties p = configSupplier.get();
            if (p != null) {
                String val = p.getProperty("db.path");
                if (val != null && !val.isBlank()) return stripDbSuffix(val);
            }
        } catch (Exception e) {
            // ignore
        }

        // 4) user home
        try {
            String userHome = userHomeSupplier.get();
            if (userHome != null && !userHome.isBlank()) {
                String homePath = Paths.get(userHome, ".brailleledger", "ledger").toAbsolutePath().toString();
                if (existsChecker.apply(homePath) || existsChecker.apply(homePath + ".db")) return stripDbSuffix(homePath);
            }
        } catch (Exception e) {
            // ignore
        }

        // 5) app_home
        try {
            String root = rootPathProvider.apply(".");
            Path appHome = Paths.get(root, "app_home", "ledger");
            Path appHomeDb = Paths.get(root, "app_home", "ledger.db");
            if (existsChecker.apply(appHome.toAbsolutePath().toString()) || existsChecker.apply(appHomeDb.toAbsolutePath().toString())) {
                return stripDbSuffix(appHome.toAbsolutePath().toString());
            }
        } catch (Exception e) {
            // ignore
        }

        // 6) default
        return stripDbSuffix("./ledger");
    }

    private static String stripDbSuffix(String path) {
        if (path == null) return "./ledger";
        if (path.endsWith(".db")) return path.substring(0, path.length() - 3);
        return path;
    }

    /**
     * Recursively collect all components from a container.
     */
    private java.util.List<Component> collectAllComponents(Container c) {
        java.util.List<Component> list = new ArrayList<>();
        for (Component child : c.getComponents()) {
            list.add(child);
            if (child instanceof Container) {
                list.addAll(collectAllComponents((Container) child));
            }
        }
        return list;
    }

    /**
     * Sets the global UI font size for all components.
     * @param sizeName The size label ("Default", "Large", "Extra Large", "XXL").
     */
    private void setUIFontSize(String sizeName) {
        int size = 16;
        FontUIResource fontRes = new FontUIResource(
            "SansSerif",
            Font.PLAIN,
            size
        );
        setGlobalUIFont(fontRes);

        // Apply font size to major form components and help dialogs
        Font fieldFont = new Font("SansSerif", Font.PLAIN, size);
        if (dateField != null) dateField.setFont(fieldFont);
        if (studentField != null) studentField.setFont(fieldFont);
        if (subjectField != null) subjectField.setFont(fieldFont);
        if (schoolField != null) schoolField.setFont(fieldFont);
        if (teacherField != null) teacherField.setFont(fieldFont);
        if (leaField != null) leaField.setFont(fieldFont);
        if (tviField != null) tviField.setFont(fieldFont);
        if (mediaTypeField != null) mediaTypeField.setFont(fieldFont);
        if (projectField != null) projectField.setFont(fieldFont);
        if (timeField != null) timeField.setFont(fieldFont);
        if (notesField != null) notesField.setFont(fieldFont);
        if (deliveryModeField != null) deliveryModeField.setFont(fieldFont);
        if (projectNameField != null) projectNameField.setFont(fieldFont);
        if (projectElementField != null) projectElementField.setFont(fieldFont);
        if (projectTimeField != null) projectTimeField.setFont(fieldFont);
        if (updatedField != null) updatedField.setFont(
            new Font("SansSerif", Font.PLAIN, 16)
        );
        if (completeComboBox != null) completeComboBox.setFont(fieldFont);
        // Ensure all dropdowns and text fields in Project Setup and Project Status use 32pt
        if (proofStatusField != null) proofStatusField.setFont(
            new Font("SansSerif", Font.PLAIN, 16)
        );
        if (deliveryModeField != null) deliveryModeField.setFont(
            new Font("SansSerif", Font.PLAIN, 16)
        );
        if (projectNameField != null) projectNameField.setFont(
            new Font("SansSerif", Font.PLAIN, 16)
        );

        // Error labels and help buttons
        if (dateErrorLabel != null) dateErrorLabel.setFont(fieldFont);
        if (studentErrorLabel != null) studentErrorLabel.setFont(fieldFont);
        if (schoolErrorLabel != null) schoolErrorLabel.setFont(fieldFont);
        if (projectErrorLabel != null) projectErrorLabel.setFont(fieldFont);
        if (timeErrorLabel != null) timeErrorLabel.setFont(fieldFont);
        if (teacherErrorLabel != null) teacherErrorLabel.setFont(fieldFont);
        if (tviErrorLabel != null) tviErrorLabel.setFont(fieldFont);
        if (subjectErrorLabel != null) subjectErrorLabel.setFont(fieldFont);
        if (notesErrorLabel != null) notesErrorLabel.setFont(fieldFont);
        if (liveRegionLabel != null) liveRegionLabel.setFont(fieldFont);
    }

    // --- Keyboard Shortcuts Dialog and Logic ---

    /**
     * List of shortcut actions available in the application.
     */
    private final String[] shortcutActions = {
        "Help",
        "Submit",
        "Generate PDF",
        "Skip to Main Content",
    };

    /**
     * List of common keyboard shortcuts for accessibility and productivity.
     */
    private final String[] commonShortcuts = {
        "Ctrl+.",
        "Ctrl+Enter",
        "Alt+M",
        "Ctrl+G",
        "Alt+S",
        "Alt+H",
    };

    /**
     * Map of shortcut action names to their corresponding dropdowns for customization.
     */
    private Map<String, JComboBox<String>> shortcutDropdowns = new HashMap<>();
    /**
     * Map of shortcut action names to their corresponding custom shortcut buttons.
     */
    private Map<String, JButton> customShortcutButtons = new HashMap<>();

    private void showKeyboardShortcutsDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        shortcutDropdowns.clear();
        customShortcutButtons.clear();

        for (String action : shortcutActions) {
            panel.add(new JLabel(action));
            JComboBox<String> combo = new JComboBox<>(commonShortcuts);
            combo.setSelectedItem(getShortcutPref(action));
            shortcutDropdowns.put(action, combo);
            panel.add(combo);

            JButton customBtn = new JButton("Define custom keystroke");
            customBtn.addActionListener(e ->
                showCustomKeystrokeDialog(action, combo)
            );
            customShortcutButtons.put(action, customBtn);
            panel.add(customBtn);
        }

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            for (String action : shortcutActions) {
                setShortcutPref(
                    action,
                    (String) shortcutDropdowns.get(action).getSelectedItem()
                );
            }
            updateAllShortcuts();
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(saveBtn);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

    showAccessibleDialog(this, "Keyboard Shortcuts", mainPanel, false);
    }

    private void showCustomKeystrokeDialog(
        String action,
        JComboBox<String> combo
    ) {
        JDialog dialog = new JDialog(this, "Define Custom Keystroke", true);
        JLabel info = new JLabel("Press your desired key combination...");
        JTextField keyField = new JTextField();
        keyField.setEditable(false);
        keyField.setFocusable(true);

        dialog.setLayout(new BorderLayout());
        dialog.add(info, BorderLayout.NORTH);
        dialog.add(keyField, BorderLayout.CENTER);

        final KeyStroke[] captured = new KeyStroke[1];

        keyField.addKeyListener(
            new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                /**
                 * Key pressed handler for global shortcuts.
                 * @since 2025.07.01
                 */
                    captured[0] = KeyStroke.getKeyStrokeForEvent(e);
                    keyField.setText(
                        KeyEvent.getModifiersExText(e.getModifiersEx()) +
                        "+" +
                        KeyEvent.getKeyText(e.getKeyCode())
                    );
                }
            }
        );

        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> {
            if (captured[0] != null) {
                setShortcutPref(action, keyField.getText());
                combo.setSelectedItem(keyField.getText());
                updateAllShortcuts();
            }
            dialog.dispose();
        });
        dialog.add(okBtn, BorderLayout.SOUTH);

    }

    private void updateAllShortcuts() {
        // Example: update menu item accelerators and global key listeners
        // Help
        KeyStroke helpStroke = parseKeyStroke(getShortcutPref("Help"));
        if (helpStroke != null) {
            // You may need to store a reference to your helpMenuItem
            // helpMenuItem.setAccelerator(helpStroke);
        }
        // Submit
        KeyStroke submitStroke = parseKeyStroke(getShortcutPref("Submit"));
        // Generate PDF
        KeyStroke pdfStroke = parseKeyStroke(getShortcutPref("Generate PDF"));
        // Skip to Main Content
        KeyStroke skipStroke = parseKeyStroke(
            getShortcutPref("Skip to Main Content")
        );
        // You can update KeyboardFocusManager or register actions as needed
        // For brevity, only the structure is shown here
    }

    private KeyStroke parseKeyStroke(String shortcut) {
        if (shortcut == null) return null;
        shortcut = shortcut.replace("Ctrl", "control").replace("Alt", "alt");
        return KeyStroke.getKeyStroke(shortcut.replace("+", " "));
    }

    private String getShortcutPref(String action) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LedgerGUI.class);
        return prefs.get("shortcut_" + action, getDefaultShortcut(action));
    }

    private void setShortcutPref(String action, String shortcut) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(LedgerGUI.class);
        prefs.put("shortcut_" + action, shortcut);
    }

    private String getDefaultShortcut(String action) {
        switch (action) {
            case "Help":
                return "Ctrl+.";
            case "Submit":
                return "Ctrl+Enter";
            case "Generate PDF":
                return "Ctrl+G";
            case "Skip to Main Content":
                return "Alt+M";
            default:
                return "Ctrl+.";
        }
    }

    /**
     * Sets the global UI font type for all components.
     * @param typeName The type label ("Sans-serif", "Serif", "Monospace").
     */
    private void setUIFontType(String typeName) {
        // No-op: font type selection removed
    }

    /**
     * Helper to get the current global font.
     */
    private Font getCurrentUIFont() {
        Font font = UIManager.getFont("Label.font");
        return font != null ? font : new Font("SansSerif", Font.PLAIN, 14);
    }

    /**
     * Helper to set the global font for all UI components.
     */
    private void setGlobalUIFont(FontUIResource fontRes) {
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            if (key != null && key.toString().toLowerCase().contains("font")) {
                UIManager.put(key, fontRes);
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    // Persistence for font preferences
    /**
     * User's preferred font size for the UI.
     */
    private String fontSizePref = "Default";
    /**
     * User's preferred font type for the UI.
     */
    private String fontTypePref = "Sans-serif";

    private void saveFontPreferences() {
        java.util.prefs.Preferences prefs =
            java.util.prefs.Preferences.userNodeForPackage(LedgerGUI.class);
        prefs.put("fontSizePref", fontSizePref);
        prefs.put("fontTypePref", fontTypePref);
    }

    private void loadFontPreferences() {
        // No-op: font preferences removed
    }

    /**
     * Adds a label and a field component to the specified panel.
     *
     * @param panel   the panel to add the label and field to
     * @param labelText the text for the label


    // Localization helper
    private String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Sets the application's language and updates UI text.
     * @param locale The locale to set.
     */
    private void setLanguage(Locale locale) {
        bundle = ResourceBundle.getBundle("messages", locale);
        saveLanguagePreference(locale);
        updateAllLocalizedText();
        announceToScreenReader(getString("language_changed"));
    }

    /**
     * Saves the selected language preference.
     * @param locale The locale to save.
     */
    private void saveLanguagePreference(Locale locale) {
        java.util.prefs.Preferences prefs =
            java.util.prefs.Preferences.userNodeForPackage(LedgerGUI.class);
        prefs.put("language", locale.toLanguageTag());
    }

    /**
     * Loads the persisted language preference and applies it.
     */
    private void loadLanguagePreference() {
        java.util.prefs.Preferences prefs =
            java.util.prefs.Preferences.userNodeForPackage(LedgerGUI.class);
        String langTag = prefs.get(
            "language",
            Locale.getDefault().toLanguageTag()
        );
        setLanguage(Locale.forLanguageTag(langTag));
    }

    // Update all UI text to localized strings (stub for now)
    private void updateAllLocalizedText() {
        // Example: update menu labels, error labels, etc.
        accessibilityMenu.setText(getString("accessibility_menu"));
        themeMenu.setText(getString("themes_menu"));
        keyboardShortcutsMenuItem.setText(getString("keyboard_shortcuts_menu"));
        skipToMainContentItem.setText(getString("skip_to_main"));
        // Zoom menu items removed
        // Update error labels if visible
        if (dateErrorLabel.isVisible()) dateErrorLabel.setText(
            getString("date_error")
        );
        if (studentErrorLabel.isVisible()) studentErrorLabel.setText(
            getString("student_error")
        );
        if (schoolErrorLabel.isVisible()) schoolErrorLabel.setText(
            getString("school_error")
        );
        if (projectErrorLabel.isVisible()) projectErrorLabel.setText(
            getString("project_error")
        );
        if (timeErrorLabel.isVisible()) timeErrorLabel.setText(
            getString("time_error")
        );
        if (teacherErrorLabel.isVisible()) teacherErrorLabel.setText(
            getString("teacher_error")
        );
        if (tviErrorLabel.isVisible()) tviErrorLabel.setText(
            getString("tvi_error")
        );
        if (subjectErrorLabel.isVisible()) subjectErrorLabel.setText(
            getString("subject_error")
        );
        if (notesErrorLabel.isVisible()) notesErrorLabel.setText(
            getString("notes_error")
        );
        // You can expand this to update all UI text as needed
    }

    /**
     * Setup error labels for validation feedback.
     */
    private void setupErrorLabels() {
        Color errorColor = Color.RED;
        dateErrorLabel.setForeground(errorColor);
        studentErrorLabel.setForeground(errorColor);
        schoolErrorLabel.setForeground(errorColor);
        projectErrorLabel.setForeground(errorColor);
        timeErrorLabel.setForeground(errorColor);
        teacherErrorLabel.setForeground(errorColor);
        tviErrorLabel.setForeground(errorColor);
        subjectErrorLabel.setForeground(errorColor);
        notesErrorLabel.setForeground(errorColor);

        dateErrorLabel.setVisible(false);
        studentErrorLabel.setVisible(false);
        schoolErrorLabel.setVisible(false);
        projectErrorLabel.setVisible(false);
        timeErrorLabel.setVisible(false);
        teacherErrorLabel.setVisible(false);
        tviErrorLabel.setVisible(false);
        subjectErrorLabel.setVisible(false);
        notesErrorLabel.setVisible(false);

        dateErrorLabel.getAccessibleContext().setAccessibleName("Date Error");
        studentErrorLabel
            .getAccessibleContext()
            .setAccessibleName("Student Error");
        schoolErrorLabel
            .getAccessibleContext()
            .setAccessibleName("School Error");
        projectErrorLabel
            .getAccessibleContext()
            .setAccessibleName("Project Error");
        timeErrorLabel.getAccessibleContext().setAccessibleName("Time Error");
        teacherErrorLabel
            .getAccessibleContext()
            .setAccessibleName("Teacher Error");
        tviErrorLabel.getAccessibleContext().setAccessibleName("TVI Error");
        subjectErrorLabel
            .getAccessibleContext()
            .setAccessibleName("Subject Error");
        notesErrorLabel.getAccessibleContext().setAccessibleName("Notes Error");

        // Add error labels to panels (after fields)
        ProjectSetup.add(dateErrorLabel);
        ProjectSetup.add(studentErrorLabel);
        ProjectSetup.add(schoolErrorLabel);
        ProjectSetup.add(projectErrorLabel);
        ProjectSetup.add(timeErrorLabel);
        ProjectSetup.add(teacherErrorLabel);
        ProjectSetup.add(tviErrorLabel);
        ProjectSetup.add(subjectErrorLabel);
        ProjectSetup.add(notesErrorLabel);
    }

    /**
     * Adds a label and a field component to the specified panel.
     *
     * @param panel   the panel to add the label and field to
     * @param labelText the text for the label
     * @param field   the field component to add
     */
    private void addLabelAndField(JPanel panel, String labelText, JComponent field) {
        if (field == null) {
            throw new IllegalArgumentException("Field component cannot be null");
        }
        JLabel label = new JLabel(labelText);
        label.setLabelFor(field);
        label.setFocusable(false);
        field.setFocusable(true);
        field.getAccessibleContext().setAccessibleName(labelText.replaceAll("<[^>]*>", ""));
        field
            .getAccessibleContext()
            .setAccessibleDescription(
                "Enter " + labelText.replaceAll("<[^>]*>", "").toLowerCase()
            );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(label, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
    gbc.gridx = 1;
    gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    /**
     * Remove help buttons ("?") from the given container and ensure their
     * tooltip text is transferred to the corresponding input field's tooltip.
     */
    private void removeHelpButtonsAndMoveTooltips(Container root) {
        java.util.List<Component> all = collectAllComponents(root);
        for (Component c : all) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if ("?".equals(b.getText())) {
                    String tip = b.getToolTipText();
                    Container parent = b.getParent();
                    if (parent != null) {
                        // try to find sibling input component
                        for (Component sib : parent.getComponents()) {
                            if (sib != b && sib instanceof JComponent) {
                                JComponent jc = (JComponent) sib;
                                if (tip != null && tip.length() > 0) {
                                    jc.setToolTipText(tip);
                                }
                            }
                        }
                    }
                    // remove button from parent
                    if (parent != null) parent.remove(b);
                }
            }
        }
    }

    /**
     * Cleans the input by removing any characters that might cause issues with H2 SQL.
     *
     * @param input the input string to clean
     * @return the cleaned input string
     */
    /**
     * Loads options from a JSON file based on the specified key.
     *
     * @param fileName The name of the JSON file.
     * @param key The key to extract options from the file.
     * @return An array of options loaded from the file.
     */
    private String[] loadOptionsFromFile(String fileName, String key) {
        List<String> options = new ArrayList<>();
        try (
            BufferedReader reader = new BufferedReader(
                new FileReader(fileName)
            );
        ) {
            logger.info("Attempting to load options from file: {}", fileName);
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            logger.debug("File content loaded: {}", jsonContent.toString());
            JSONObject jsonObject = new JSONObject(jsonContent.toString());
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            logger.debug("Extracting options using key: {}", key);
            for (int i = 0; i < jsonArray.length(); i++) {
                options.add(jsonArray.getString(i));
            }
            logger.info("Options loaded successfully: {}", options);
        } catch (IOException | JSONException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading options from file: " + fileName
            );
            logger.error(
                "Exception occurred while loading options: {}",
                e.getMessage()
            );
        }
        return options.toArray(new String[0]);
    }

    // --- Localization support ---
    // getString(String key) already defined, remove duplicate

    private void setLanguage(String langCode) {
        Locale locale = langCode.equals("es")
            ? Locale.forLanguageTag("es")
            : Locale.ENGLISH;
        labels = ResourceBundle.getBundle("messages", locale);
        currentLocale = locale;
        updateAllLabels();
        logger.info("Language switched to {}", langCode);
    }

    private void updateAllLabels() {
        // Example: update tab titles and field labels
        if (mainTabs != null) {
            mainTabs.setTitleAt(0, getString("app_title"));
            // Add other tab titles as needed
        }
        if (studentLabel != null) studentLabel.setText(
            getString("student_label")
        );
        if (schoolLabel != null) schoolLabel.setText(getString("school_label"));
        if (projectLabel != null) projectLabel.setText(
            getString("project_label")
        );
        if (timeLabel != null) timeLabel.setText(getString("time_label"));
        if (teacherLabel != null) teacherLabel.setText(
            getString("teacher_label")
        );
        if (tviLabel != null) tviLabel.setText(getString("tvi_label"));
        if (subjectLabel != null) subjectLabel.setText(
            getString("subject_label")
        );
        if (notesLabel != null) notesLabel.setText(getString("notes_label"));
        if (submitButton != null) submitButton.setText(
            getString("button.submit")
        );
        // Add other label/button/tab updates as needed
    }

    /**
     * Loads project types from project_type.json for Project Setup tab.
     * @return An array of project types.
     */
    private String[] getProjectTypesFromJson() {
        return loadOptionsFromFile(
            "json_files/project_type.json",
            "project_types"
        );
    }

    private void loadStartupOptions() {
        File jsonDirectory = new File("BrailleTranscriptionLedger");
        File[] jsonFiles = jsonDirectory.listFiles((dir, name) ->
            name.endsWith(".json")
        );

        if (jsonFiles != null) {
            for (File jsonFile : jsonFiles) {
                String fileName = jsonFile.getName();
                String key = fileName.replace(".json", "");
                String[] options = loadOptionsFromFile(jsonFile.getPath(), key);

                switch (key) {
                    case "media_type":
                        mediaTypeField = new JComboBox<>(options);
                        break;
                    case "delivery_mode":
                        deliveryModeField = new JComboBox<>(options);
                        break;
                    case "project_type":
                        projectField = new JComboBox<>(options);
                        break;
                    // Add more cases as needed for other JSON files
                }
            }
        }
    }

    /**
     * Shows a dialog for entering the date range and selecting the projects to generate a PDF report.
     */
    private void showDateRangeDialog() {
        JTextField startDateField = new JTextField(getPreviousMonth16th(), 10);
        JTextField endDateField = new JTextField(getCurrentMonth15th(), 10);
        String[] studentOptions = loadOptionsFromFile(
            "json_files/students.json",
            "students"
        );
        JComboBox<String> studentMaterial = new JComboBox<>(studentOptions);
        // Create checkboxes for project options
        String[] projectOptions = getProjectNamesFromDatabase();
        JCheckBox[] projectCheckboxes = new JCheckBox[projectOptions.length];

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        panel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        panel.add(startDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        panel.add(endDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Student Receiving Materials"), gbc);
        gbc.gridx = 1;
        panel.add(studentMaterial, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Select Projects:"), gbc);

        gbc.gridy++;
        for (int i = 0; i < projectOptions.length; i++) {
            projectCheckboxes[i] = new JCheckBox(projectOptions[i]);
            projectCheckboxes[i].setSelected(true); // Default to selected
            panel.add(projectCheckboxes[i], gbc);
            gbc.gridy++;
        }

        int result = JOptionPane.showConfirmDialog(
            null,
            panel,
            "Enter Date Range and Select Projects",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String startDate = startDateField.getText();
            String endDate = endDateField.getText();
            List<String> selectedProjects = new ArrayList<>();
            for (JCheckBox checkbox : projectCheckboxes) {
                if (checkbox.isSelected()) {
                    selectedProjects.add(checkbox.getText());
                }
            }
            generatePdfReport(startDate, endDate, selectedProjects);
        }
    }

    /**
     * Retrieves the 15th day of the current month as a string in the format "yyyy-MM-dd".
     *
     * @return the current month's 15th day as a string
     */
    public String getCurrentMonth15th() {
    /**
     * Return the fifteenth day for the current month as a formatted string.
     * @return formatted date string
     * @since 2025.07.01
     */
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 15); // Set day to 15
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * Retrieves the 16th day of the previous month as a string in the format "yyyy-MM-dd".
     *
     * @return the previous month's 16th day as a string
     */
    public String getPreviousMonth16th() {
    /**
     * Return the 16th day of the previous month as a formatted string.
     * @return formatted date string
     * @since 2025.07.01
     */
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Go back one month
        cal.set(Calendar.DAY_OF_MONTH, 16); // Set day to 15
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * The main entry point for the application.
     * Sets the default look and feel, creates a new `LedgerGUI` instance, and makes it visible.
     *
     * @param args the command-line arguments (unused)
     */
    /**
     * The main entry point for the application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
    /**
     * Application entry point.
     * @param args CLI args forwarded to the application (see README for supported options)
     * @since 2025.07.01
     */
        // Parse log level from command-line arguments
        String logLevelStr = "WARN"; // Default to WARN
        boolean wantHelp = false;
        for (String arg : args) {
            if (arg.startsWith("--loglevel=")) {
                logLevelStr = arg
                    .substring("--loglevel=".length())
                    .toUpperCase();
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                wantHelp = true;
            }
        }
        System.setProperty("LOG_LEVEL", logLevelStr);

        // Set the look and feel to the system look and feel
        String defaultTheme = "Dracula"; // or any other theme name from the map
        try {
            UIManager.setLookAndFeel(
                INTELLIJ_THEMES.get(defaultTheme)
                    .getDeclaredConstructor()
                    .newInstance()
            );
            UIManager.put(
                "defaultFont",
                new FontUIResource(
                    new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 16)
                )
            ); // Example font size 16
        } catch (
            IllegalAccessException
            | IllegalArgumentException
            | InstantiationException
            | NoSuchMethodException
            | SecurityException
            | InvocationTargetException
            | UnsupportedLookAndFeelException ex
        ) {}

        if (wantHelp) {
            String help = "Available arguments:\n" +
                "  --dbpath=<path>   Set explicit DB base path (without .db)\n" +
                "  --loglevel=<level> Set log level (INFO,WARN,DEBUG)\n" +
                "  --help, -h        Show this help";
            System.out.println(help);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            LedgerGUI gui = new LedgerGUI(args);
            gui.setVisible(true);
            // after visible, set a non-modal status message
            gui.setStatus("Using database: " + gui.DB_URL);
        });
    }

    /**
     * Set the status bar text (non-modal). Safe to call before statusBar exists.
    * @param msg The status message to display in the non-modal status bar.
    */
    public void setStatus(String msg) {
    /**
     * Set text of the non-modal status bar at the bottom of the main window.
     * @param msg Message to display
     * @since 2025.07.01
     */
        if (statusBar != null) {
            statusBar.setText(msg);
        } else {
            // fallback: log if statusBar not initialized yet
            logger.info(msg);
        }
    }

    /**
     * Sets the specified IntelliJ IDEA-inspired theme for the application.
     *
     * @param themeName the name of the theme to set
     */

    private void setIntelliJTheme(String themeName) {
        try {
            Class<? extends LookAndFeel> themeClass = INTELLIJ_THEMES.get(
                themeName
            );
            if (themeClass != null) {
                UIManager.setLookAndFeel(
                    themeClass.getDeclaredConstructor().newInstance()
                );
                UIManager.put(
                    "defaultFont",
                    new FontUIResource(
                        new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 16)
                    )
                ); // Example font size 16
                SwingUtilities.updateComponentTreeUI(this);
            } else {
                System.err.println("Theme not found: " + themeName);
            }
        } catch (
            IllegalAccessException
            | IllegalArgumentException
            | InstantiationException
            | NoSuchMethodException
            | SecurityException
            | InvocationTargetException
            | UnsupportedLookAndFeelException ex
        ) {}
    }

    /**
     * Initializes the H2 embedded database used by the application.
     */
    /**
     * Initializes the database connection and ensures the database is ready for use.
     */
    private void initializeDatabase() {
        loadDataFromFiles();
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
        ) {
            // Database initialization logic here
            // Ensure folder_path column exists for Project_Name (handle upgrades)
            try {
                stmt.executeUpdate(
                    "ALTER TABLE Project_Name ADD COLUMN IF NOT EXISTS folder_path TEXT"
                );
            } catch (SQLException e) {
                // ignore - column may already exist or DB doesn't support IF NOT EXISTS on ALTER
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error initializing database: " + e.getMessage()
            );
        }
    }

    /**
     * Retrieves the list of project names from the database.
     *
     * @return An array of project names.
     */
    private String[] getProjects() {
        return getProjectNamesFromDatabase();
    }

    // Fetch project names from the database for dropdowns/menus
    private String[] getProjectNamesFromDatabase() {
        List<String> names = new ArrayList<>();
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT name FROM Project_Name ORDER BY name"
            );
        ) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Error fetching project names: {}", e.getMessage());
        }
        return names.toArray(new String[0]);
    }

    /**
     * Loads data from JSON files into GUI components.
     */
    /**
     * Loads data from external files into the application.
     */

    /**
     * Fetches all project names from the database for use in the PDF dialog.
     * @return An array of project names.
     */
    // Removed broken getProjectNamesFromDatabase method

    private void loadDataFromFiles() {
        try {
            String[] students = loadOptionsFromFile(
                "json_files/students.json",
                "students"
            );
            studentField.setModel(new DefaultComboBoxModel<>(students));

            String[] teachers = loadOptionsFromFile(
                "json_files/teachers.json",
                "teachers"
            );
            teacherField.setListData(teachers);

            String[] leas = loadOptionsFromFile("json_files/LEAs.json", "LEAs");
            leaField.setModel(new DefaultComboBoxModel<>(leas));

            String[] tvis = loadOptionsFromFile("json_files/tvis.json", "tvis");
            tviField.setListData(tvis);

            String[] subjects = loadOptionsFromFile(
                "json_files/subjects.json",
                "subjects"
            );
            subjectField.setModel(new DefaultComboBoxModel<>(subjects));

            String[] schools = loadOptionsFromFile(
                "json_files/schools.json",
                "schools"
            );
            schoolField.setModel(new DefaultComboBoxModel<>(schools));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading data: " + e.getMessage()
            );
        }
    }

    private String[] getProjectNames() {
        List<String> projectNames = new ArrayList<>();
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT name FROM Project_Name ORDER BY name"
            );
        ) {
            while (rs.next()) {
                projectNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Error fetching project names: {}", e.getMessage());
            // Return empty array if database query fails
            return new String[0];
        }
        return projectNames.toArray(new String[0]);
    }

    /**
     * Refreshes the project names combo box with current database values.
     */
    private void refreshProjectNames() {
        String[] projectNames = getProjectNames();
        projectNameField.removeAllItems();
        for (String name : projectNames) {
            projectNameField.addItem(name);
        }
        // No need to refresh ProjectSetup input, it's a JTextField now
    }

    private int getProjectId(String projectName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT project_name_id FROM Project_Name WHERE name = ?"
            );
        ) {
            pstmt.setString(1, projectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("project_name_id");
            }
        } catch (SQLException e) {
            logger.error("Error getting TVI ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getStudentId(String studentName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT student_id FROM Students WHERE name = ?"
            );
        ) {
            pstmt.setString(1, studentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("student_id");
            }
        } catch (SQLException e) {
            logger.error("Error fetching project ID: {}", e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getTeacherId(String teacherName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT teacher_id FROM Teachers WHERE name = ?"
            );
        ) {
            pstmt.setString(1, teacherName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("teacher_id");
            }
        } catch (SQLException e) {
            logger.error("Error fetching student ID: {}", e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getSchoolId(String schoolName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT school_id FROM Schools WHERE name = ?"
            );
        ) {
            pstmt.setString(1, schoolName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("school_id");
            }
        } catch (SQLException e) {
            logger.error("Error fetching teacher ID: {}", e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getSubjectId(String subjectName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT subject_id FROM Subjects WHERE name = ?"
            );
        ) {
            pstmt.setString(1, subjectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("subject_id");
            }
        } catch (SQLException e) {
            logger.error("Error fetching school ID: {}", e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getTviId(String tviName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT tvi_id FROM TVIs WHERE name = ?"
            );
        ) {
            pstmt.setString(1, tviName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("tvi_id");
            }
        } catch (SQLException e) {
            logger.error("Error fetching subject ID: {}", e.getMessage());
        }
        return -1;
    }

    private int getLeaId(String leaName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT lea_id FROM LEAs WHERE name = ?"
            );
        ) {
            pstmt.setString(1, leaName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("lea_id");
            }
        } catch (SQLException e) {
            logger.error("Error getting LEA ID: {}", e.getMessage());
        }
        return -1;
    }

    private String cleanInput(String input) {
        return input.trim();
    }

    private Object[] createTrackingTable(
        List<String[]> data,
        Document document,
        PdfWriter writer,
        HeaderFooterPageEvent event
    ) {
        try {
            return createTable(data, document, writer, event);
        } catch (DocumentException e) {
            // Return an empty table and zero if there's an error
            return new Object[] { new PdfPTable(6), 0.0 };
        }
    }

    private void initializeDatabaseTables() {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
        ) {
            String sqlProjectTracking =
                "CREATE TABLE IF NOT EXISTS Project_Tracking (" +
                "tracking_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "detail_id INTEGER NOT NULL, " +
                "project_name_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "updated TEXT, " +
                "completed TEXT, " +
                "notes TEXT, " +
                "complete BOOLEAN, " +
                "proof_status TEXT, " +
                "element TEXT, " +
                "time INTEGER, " +
                "FOREIGN KEY (detail_id) REFERENCES Project_Details(detail_id), " +
                "FOREIGN KEY (project_name_id) REFERENCES Project_Name(project_name_id), " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id)" +
                ")";
            try {
                stmt.execute(sqlProjectTracking);
            } catch (SQLException e) {
                logger.error(
                    "Error creating Project_Tracking table: {}",
                    e.getMessage()
                );
            }
            // H2 enforces foreign keys by default; no PRAGMA needed
            String sqlProjectName =
                "CREATE TABLE IF NOT EXISTS Project_Name (" +
                "project_name_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "folder_path TEXT" +
                ")";
            try {
                stmt.execute(sqlProjectName);
            } catch (SQLException e) {
                logger.error(
                    "Error creating Project_Name table: {}",
                    e.getMessage()
                );
            }
            String sqlSchools =
                "CREATE TABLE IF NOT EXISTS Schools (" +
                "school_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL)";
            try {
                stmt.execute(sqlSchools);
            } catch (SQLException e) {
                logger.error(
                    "Error creating Schools table: {}",
                    e.getMessage()
                );
            }
            String sqlTeachers =
                "CREATE TABLE IF NOT EXISTS Teachers (" +
                "teacher_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "school_id INTEGER, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE SET NULL)";
            try {
                stmt.execute(sqlTeachers);
            } catch (SQLException e) {
                logger.error(
                    "Error creating Teachers table: {}",
                    e.getMessage()
                );
            }
            String sqlSubjects =
                "CREATE TABLE IF NOT EXISTS Subjects (" +
                "subject_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL, " +
                "teacher_id INTEGER NOT NULL, " +
                "school_id INTEGER NOT NULL, " +
                "FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE CASCADE)";
            try {
                stmt.execute(sqlSubjects);
            } catch (SQLException e) {
                logger.error(
                    "Error creating Subjects table: {}",
                    e.getMessage()
                );
            }
            // Restore missing table definitions

            String sqlLEAs =
                "CREATE TABLE IF NOT EXISTS LEAs (" +
                "lea_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL UNIQUE" +
                ")";
            try {
                stmt.execute(sqlLEAs);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating LEAs table: " + e.getMessage()
                );
            }

            String sqlTVIs =
                "CREATE TABLE IF NOT EXISTS TVIs (" +
                "tvi_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "school_id INTEGER NOT NULL, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE SET NULL)";
            try {
                stmt.execute(sqlTVIs);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating TVIs table: " + e.getMessage()
                );
            }

            String sqlStudents =
                "CREATE TABLE IF NOT EXISTS Students (" +
                "student_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "school_id INTEGER NOT NULL, " +
                "lea_id INTEGER, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (lea_id) REFERENCES LEAs(lea_id) ON DELETE SET NULL" +
                ")";
            try {
                stmt.execute(sqlStudents);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Students table: " + e.getMessage()
                );
            }

            String sqlMediaTypes =
                "CREATE TABLE IF NOT EXISTS Media_Types (" +
                "media_type_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "type TEXT NOT NULL, " +
                "project_name_id INTEGER NOT NULL, " +
                "FOREIGN KEY (project_name_id) REFERENCES Project_Name(project_name_id) ON DELETE CASCADE)";
            try {
                stmt.execute(sqlMediaTypes);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Media_Types table: " + e.getMessage()
                );
            }

            String sqlProjectDetails =
                "CREATE TABLE IF NOT EXISTS Project_Details (" +
                "detail_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "project_name_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "teacher_id INTEGER, " +
                "subject_id INTEGER NOT NULL, " +
                "school_id INTEGER NOT NULL, " +
                "minutes_spent INTEGER, " +
                "proof_status TEXT, " +
                "complete BOOLEAN, " +
                "delivered BOOLEAN, " +
                "delivery_mode TEXT, " +
                "subject TEXT, " +
                "time TEXT, " +
                "date TEXT NOT NULL, " +
                "notes TEXT, " +
                "FOREIGN KEY (project_name_id) REFERENCES Project_Name(project_name_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE SET NULL" +
                ")";
            try {
                stmt.execute(sqlProjectDetails);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Project_Details table: " + e.getMessage()
                );
            }

            String sqlStudentTeachers =
                "CREATE TABLE IF NOT EXISTS Student_Teachers (" +
                "student_id INTEGER NOT NULL, " +
                "teacher_id INTEGER NOT NULL, " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE CASCADE" +
                ")";
            try {
                stmt.execute(sqlStudentTeachers);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Student_Teachers table: " + e.getMessage()
                );
            }
        } catch (SQLException e) {
            System.err.println(
                "Error initializing database tables: " + e.getMessage()
            );
        }
    }

    // End of initializeDatabaseTables method

    /**
     * Checks if a project name exists in the Project_Name table.
     */
    private boolean projectNameExists(String name) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM Project_Name WHERE name = ?"
            );
        ) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            // Ignore, treat as not existing
        }
        return false;
    }

    /**
     * Loads the existing ledger data from the database and populates the data table.
     */
    /**
     * Loads data from the database into the application.
     */
    private JPanel ProjectTracking;
    /**
     * Panel for project setup UI components.
     */
    private JPanel ProjectSetup;

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT pn.name AS project_name, " +
                "MAX(pt.updated) AS latest_date, " +
                "s.name AS student, " +
                "sub.name AS subject, " +
                "sch.name AS school, " +
                "SUM(pt.time) AS total_time " +
                "FROM Project_Tracking pt " +
                "JOIN Project_Details pd ON pt.detail_id = pd.detail_id " +
                "JOIN Project_Name pn ON pd.project_name_id = pn.project_name_id " +
                "JOIN Students s ON pd.student_id = s.student_id " +
                "JOIN Subjects sub ON pd.subject_id = sub.subject_id " +
                "JOIN Schools sch ON pd.school_id = sch.school_id " +
                "GROUP BY pn.name, s.name, sub.name, sch.name"
            );
        ) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("latest_date"), // Date
                    rs.getString("student"), // Student
                    rs.getString("subject"), // Subject
                    rs.getString("school"), // School
                    rs.getString("project_name"), // Project
                    rs.getInt("total_time"), // Time
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading data: " + e.getMessage()
            );
        }
    }

    /**
     * Handles the submission of new ledger data.
     * Saves the data to the database and updates the data table.
     */
    /**
     * Adds tracking information to the database.
     */
    private void addTrackingInfo() {
        // Project Status fields only
        String projectName = projectNameField.getSelectedItem() != null
            ? projectNameField.getSelectedItem().toString()
            : "";
        String projectElement = projectElementField.getSelectedItem() != null
            ? projectElementField.getSelectedItem().toString()
            : "";
        String projectTime = projectTimeField.getText();
        String notes = notesField.getText();
        String updated = updatedField.getText();
        boolean complete = completeComboBox
            .getSelectedItem()
            .toString()
            .equals("Yes");
        String proofStatus = proofStatusField.getSelectedItem() != null
            ? proofStatusField.getSelectedItem().toString()
            : "";
        String completed = "";
        if (
            completedField != null && completedField.getSelectedItem() != null
        ) {
            completed = completedField.getSelectedItem().toString();
        }
        // Declare and initialize student, schoolId, leaId for tracking
        String student = (studentField.getSelectedItem() != null)
            ? studentField.getSelectedItem().toString()
            : "";
        String school = (schoolField.getSelectedItem() != null)
            ? schoolField.getSelectedItem().toString()
            : "";
        String lea = (leaField.getSelectedItem() != null)
            ? leaField.getSelectedItem().toString()
            : "";
        int schoolId = -1;
        int leaId = -1;
        try {
            schoolId = getOrInsertSchoolId(school);
            leaId = getOrInsertLeaId(lea);
        } catch (SQLException e) {
            showLogWindow("Database Error", "Error resolving school/LEA IDs: " + e.getMessage());
            return;
        }
        logger.info("Submit pressed: projectName='{}'", projectName);
        int projectNameId = -1;
        try {
            projectNameId = getOrInsertProjectNameId(projectName, updated);
        } catch (SQLException e) {
            showLogWindow("Database Error", "Error resolving project name ID: " + e.getMessage());
            return;
        }
        logger.info("Resolved projectNameId={}", projectNameId);
        int studentId = -1;
        try {
            studentId = getOrInsertStudentId(student, schoolId, leaId);
        } catch (SQLException e) {
            showLogWindow("Database Error", "Error resolving student ID: " + e.getMessage());
            return;
        }

        if (projectName == null || projectName.trim().isEmpty()) {
            logger.error(
                "Project name is empty or null. Cannot proceed with insert."
            );
            showLogWindow(
                "Error",
                "Project name is required and cannot be empty."
            );
            return;
        }
        if (projectNameId <= 0) {
            logger.error(
                "Project name ID could not be found or created for '{}'.",
                projectName
            );
            showLogWindow(
                "Error",
                "Project name ID could not be found or created. Please check the project name."
            );
            return;
        }

        StringBuilder errorLog = new StringBuilder();
        boolean hasError = false;

        if (projectName.isEmpty()) {
            errorLog.append("Project Name is required.\n");
            hasError = true;
        }
        if (projectElement.isEmpty()) {
            errorLog.append("Project Element is required.\n");
            hasError = true;
        }
        if (projectTime == null || projectTime.isEmpty()) {
            errorLog.append("Project Time is required.\n");
            hasError = true;
        }
        if (notes.length() > 500) {
            errorLog.append("Notes must be less than 500 characters.\n");
            hasError = true;
        }

        if (hasError) {
            showLogWindow("Tracking Info Error", errorLog.toString());
            return;
        }

        StringBuilder confirmationLog = new StringBuilder();
        confirmationLog.append("Saved Project Status Tracking Info:\n");
        confirmationLog
            .append("Project Name: ")
            .append(projectName)
            .append("\n");
        confirmationLog.append("Element: ").append(projectElement).append("\n");
        confirmationLog.append("Time: ").append(projectTime).append("\n");
        confirmationLog.append("Notes: ").append(notes).append("\n");
        confirmationLog.append("Updated: ").append(updated).append("\n");
        confirmationLog
            .append("Complete: ")
            .append(complete ? "Yes" : "No")
            .append("\n");
        confirmationLog
            .append("Proof Status: ")
            .append(proofStatus)
            .append("\n");

        // Find detail_id for the selected project name
        int detailId = -1;
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement findDetailStmt = conn.prepareStatement(
                "SELECT pd.detail_id FROM Project_Details pd JOIN Project_Name pn ON pd.project_name_id = pn.project_name_id WHERE pn.name = ?"
            );
        ) {
            findDetailStmt.setString(1, projectName);
            ResultSet rs = findDetailStmt.executeQuery();
            if (rs.next()) {
                detailId = rs.getInt("detail_id");
            }
        } catch (SQLException e) {
            showLogWindow("Error finding project detail_id", e.getMessage());
            return;
        }

        if (detailId == -1) {
            showLogWindow(
                "Tracking Info Error",
                "Could not find project detail_id for selected project name."
            );
            return;
        }

        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Project_Tracking (detail_id, project_name_id, student_id, updated, completed, notes, complete, proof_status, element, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
        ) {
            pstmt.setInt(1, detailId);
            pstmt.setInt(2, projectNameId);
            pstmt.setInt(3, studentId);
            pstmt.setString(4, updated);
            pstmt.setString(5, completed);
            pstmt.setString(6, notes);
            pstmt.setBoolean(7, complete);
            pstmt.setString(8, proofStatus);
            pstmt.setString(9, projectElement);
            // Convert HH:MM to total minutes for DB storage
            int minutes = 0;
            if (projectTime != null && projectTime.contains(":")) {
                String[] parts = projectTime.split(":");
                try {
                    minutes =
                        Integer.parseInt(parts[0]) * 60 +
                        Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    minutes = 0; // fallback to 0 if parsing fails
                }
            } else if (projectTime != null && !projectTime.isEmpty()) {
                try {
                    minutes = Integer.parseInt(projectTime);
                } catch (NumberFormatException ex) {
                    minutes = 0;
                }
            }
            pstmt.setInt(10, minutes);
            pstmt.executeUpdate();
            // Build confirmation summary, similar to submit confirmation
            confirmationLog.insert(
                0,
                "Project tracking info added successfully.\n\n"
            );
            showLogWindow(
                "Tracking Info Confirmation",
                confirmationLog.toString()
            );
            // Refresh Projects tab table after adding tracking info
            loadDataFromDatabase();
        } catch (SQLException e) {
            showLogWindow("Database Error", e.getMessage());
        }
    }

    // Method to submit file paths to the database
    /**
     * Submits a file path to the database for tracking purposes.
     *
     * @param fileType The type of file being submitted (e.g., originals, graphics).
     * @param filePath The path of the file to be submitted.
     */
    private void submitFileToDatabase(String fileType, String filePath) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = fileType.equals("originals")
                ? conn.prepareStatement(
                    "INSERT INTO SourceFile (file_path) VALUES (?)"
                )
                : conn.prepareStatement(
                    "INSERT INTO TargetFile (file_type, file_path) VALUES (?, ?)"
                );
        ) {
            if (fileType.equals("originals")) {
                pstmt.setString(1, filePath);
            } else {
                pstmt.setString(1, fileType);
                pstmt.setString(2, filePath);
            }
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(
                this,
                "File path for " + fileType + " added successfully."
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error adding file path: " + e.getMessage()
            );
        }
    }

    /**
     * Submits data entered in the application to the database.
     */
    private void submitData() {
        // Clear all error labels before validation
        dateErrorLabel.setVisible(false);
        studentErrorLabel.setVisible(false);
        schoolErrorLabel.setVisible(false);
        projectErrorLabel.setVisible(false);
        timeErrorLabel.setVisible(false);
        teacherErrorLabel.setVisible(false);
        tviErrorLabel.setVisible(false);
        subjectErrorLabel.setVisible(false);
        notesErrorLabel.setVisible(false);

        // Project Setup fields only
        String date = dateField.getText();
        String student = (studentField.getSelectedItem() != null)
            ? studentField.getSelectedItem().toString()
            : "";
        String school = (schoolField.getSelectedItem() != null)
            ? schoolField.getSelectedItem().toString()
            : "";
        String setupProjectName = setupProjectNameTextField.getText().trim();
        String teacher = (teacherField.getSelectedValue() != null)
            ? teacherField.getSelectedValue().toString()
            : "";
        String tvi = (tviField.getSelectedValue() != null)
            ? tviField.getSelectedValue().toString()
            : "";
        String subject = (subjectField.getSelectedItem() != null)
            ? subjectField.getSelectedItem().toString()
            : "";
        String notes = notesField.getText();
        String updated = updatedField.getText();
        boolean complete = completeComboBox
            .getSelectedItem()
            .toString()
            .equals("Yes");
        String lea = (leaField.getSelectedItem() != null)
            ? leaField.getSelectedItem().toString()
            : "";
        String mediaType = mediaTypeField.getSelectedItem() != null
            ? mediaTypeField.getSelectedItem().toString()
            : "";
        String proofStatus = proofStatusField.getSelectedItem() != null
            ? proofStatusField.getSelectedItem().toString()
            : "";
        String deliveryMode = deliveryModeField.getSelectedItem() != null
            ? deliveryModeField.getSelectedItem().toString()
            : "";
        String completed = completedField != null &&
            completedField.getSelectedItem() != null
            ? completedField.getSelectedItem().toString()
            : "";

        // Insert or get IDs for all related tables
        int schoolId = -1;
        int leaId = -1;
        int teacherId = -1;
        int tviId = -1;
        int studentId = -1;
        int subjectId = -1;
        try {
            schoolId = getOrInsertSchoolId(school);
            leaId = getOrInsertLeaId(lea);
            teacherId = getOrInsertTeacherId(teacher, schoolId);
            tviId = getOrInsertTviId(tvi, schoolId);
            studentId = getOrInsertStudentId(student, schoolId, leaId);
            subjectId = getOrInsertSubjectId(subject, teacherId, schoolId);
        } catch (SQLException e) {
            showLogWindow("Database Error", "Error resolving entity IDs: " + e.getMessage());
            announceToScreenReader("Database error: " + e.getMessage());
            return;
        }
        int projectNameId = -1;
        try {
            projectNameId = getOrInsertProjectNameId(setupProjectName, date);
        } catch (SQLException e) {
            showLogWindow("Database Error", "Error creating project name: " + e.getMessage());
            announceToScreenReader("Error creating project name: " + e.getMessage());
            return;
        }
        int mediaTypeId = -1;
        try {
            mediaTypeId = getOrInsertMediaTypeId(mediaType, projectNameId);
        } catch (SQLException e) {
            showLogWindow("Database Error", "Error resolving media type ID: " + e.getMessage());
            return;
        }

        // Insert into Student_Teachers (many-to-many)
        insertStudentTeacher(studentId, teacherId);

        // Insert into Project_Details
        int detailId = -1;
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Project_Details (project_name_id, student_id, teacher_id, subject_id, school_id, minutes_spent, proof_status, complete, delivered, delivery_mode, subject, time, date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
        ) {
            pstmt.setInt(1, projectNameId);
            pstmt.setInt(2, studentId);
            pstmt.setInt(3, teacherId);
            pstmt.setInt(4, subjectId);
            pstmt.setInt(5, schoolId);
            pstmt.setInt(6, 0); // minutes_spent default to 0
            pstmt.setString(7, proofStatus);
            pstmt.setBoolean(8, complete);
            pstmt.setBoolean(9, false); // delivered default to false
            pstmt.setString(10, deliveryMode);
            pstmt.setString(11, subject); // subject default to empty string if not set
            pstmt.setString(12, projectTimeField.getText()); // time as entered
            pstmt.setString(13, date); // ensure date is set for NOT NULL column
            pstmt.setString(14, notes);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                detailId = rs.getInt(1);
            }
            this.currentDetailId = detailId;
        } catch (SQLException | NumberFormatException e) {
            showLogWindow(
                "Database Error",
                "Error saving data: " + e.getMessage()
            );
            announceToScreenReader("Error saving data: " + e.getMessage());
            return;
        }

        // Create project folder under app_home/projects and move ledger DB if necessary
        try {
            String root;
            if (TEST_ROOT_OVERRIDE != null) {
                root = TEST_ROOT_OVERRIDE;
            } else {
                root = new File(".").getCanonicalPath();
            }
            Path appHome = Paths.get(root, "app_home");
            Path projectsDir = appHome.resolve("projects");
            if (!Files.exists(projectsDir)) Files.createDirectories(projectsDir);
            String folderName = sanitizeForFileName(setupProjectName);
            Path projectFolder = projectsDir.resolve(folderName);
            if (!Files.exists(projectFolder)) Files.createDirectories(projectFolder);

            // Move ledger files to app_home if not already moved
            Path ledgerDb = Paths.get(root, "ledger.mv.db");
            Path ledgerDbAlt = Paths.get(root, "ledger.db");
            Path targetLedger = appHome.resolve("ledger.db");
            if (Files.exists(ledgerDb) && !Files.exists(targetLedger)) {
                Files.move(ledgerDb, targetLedger, StandardCopyOption.REPLACE_EXISTING);
            } else if (Files.exists(ledgerDbAlt) && !Files.exists(targetLedger)) {
                Files.move(ledgerDbAlt, targetLedger, StandardCopyOption.REPLACE_EXISTING);
            }

            // Store project folder path in DB
            try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE Project_Name SET folder_path = ? WHERE project_name_id = ?"
                )) {
                pstmt.setString(1, projectFolder.toAbsolutePath().toString());
                pstmt.setInt(2, projectNameId);
                pstmt.executeUpdate();
            }
        } catch (IOException | SQLException ex) {
            logger.error("Error creating project folder or moving DB: {}", ex.getMessage());
        }

        // Removed Project_Tracking insert block: Project Details Submit should not write to Project_Tracking

        loadDataFromDatabase();
        refreshProjectNames(); // Refresh project names in case new ones were added

        // Switch to Projects tab to show updated table
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).setSelectedIndex(2);
                break;
            }
        }

        boolean hasError = false;
        StringBuilder errorLog = new StringBuilder();

        // Date validation
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            dateErrorLabel.setText("Invalid date format. Use YYYY-MM-DD.");
            dateErrorLabel.setVisible(true);
            announceToScreenReader(
                "Invalid date format. Use YYYY dash MM dash DD."
            );
            errorLog.append("Invalid date format. Use YYYY-MM-DD.\n");
            hasError = true;
        }

        // Student validation
        if (student.isEmpty()) {
            studentErrorLabel.setText("Student is required.");
            studentErrorLabel.setVisible(true);
            announceToScreenReader("Student is required.");
            errorLog.append("Student is required.\n");
            hasError = true;
        }

        // School validation
        if (school.isEmpty()) {
            schoolErrorLabel.setText("School is required.");
            schoolErrorLabel.setVisible(true);
            announceToScreenReader("School is required.");
            errorLog.append("School is required.\n");
            hasError = true;
        }

        // Project validation
        if (setupProjectName.isEmpty()) {
            projectErrorLabel.setText("Project is required.");
            projectErrorLabel.setVisible(true);
            announceToScreenReader("Project is required.");
            errorLog.append("Project is required.\n");
            hasError = true;
        }

        // Teacher validation
        if (teacher.isEmpty()) {
            teacherErrorLabel.setText("Teacher is required.");
            teacherErrorLabel.setVisible(true);
            announceToScreenReader("Teacher is required.");
            errorLog.append("Teacher is required.\n");
            hasError = true;
        }

        // TVI validation
        if (tvi.isEmpty()) {
            tviErrorLabel.setText("TVI is required.");
            tviErrorLabel.setVisible(true);
            announceToScreenReader("TVI is required.");
            errorLog.append("TVI is required.\n");
            hasError = true;
        }

        // Subject validation
        if (subject.isEmpty()) {
            subjectErrorLabel.setText("Subject is required.");
            subjectErrorLabel.setVisible(true);
            announceToScreenReader("Subject is required.");
            errorLog.append("Subject is required.\n");
            hasError = true;
        }

        // Notes validation (optional, but can check for length)
        if (notes.length() > 500) {
            notesErrorLabel.setText("Notes must be less than 500 characters.");
            notesErrorLabel.setVisible(true);
            announceToScreenReader("Notes must be less than 500 characters.");
            errorLog.append("Notes must be less than 500 characters.\n");
            hasError = true;
        }

        if (hasError) {
            showLogWindow("Submission Error", errorLog.toString());
            return;
        }

        // Confirmation log for Project Setup only
        StringBuilder confirmationLog = new StringBuilder();
        confirmationLog.append("Saved Project Setup Data:\n");
        confirmationLog.append("Date: ").append(date).append("\n");
        confirmationLog.append("Student: ").append(student).append("\n");
        confirmationLog.append("School: ").append(school).append("\n");
        confirmationLog
            .append("Project: ")
            .append(setupProjectName)
            .append("\n");
        confirmationLog.append("Teacher: ").append(teacher).append("\n");
        confirmationLog.append("TVI: ").append(tvi).append("\n");
        confirmationLog.append("Subject: ").append(subject).append("\n");
        confirmationLog.append("Notes: ").append(notes).append("\n");
        confirmationLog.append("Updated: ").append(updated).append("\n");
        confirmationLog
            .append("Complete: ")
            .append(complete ? "Yes" : "No")
            .append("\n");
        confirmationLog.append("Media Type: ").append(mediaType).append("\n");
        confirmationLog
            .append("Proof Status: ")
            .append(proofStatus)
            .append("\n");
        confirmationLog
            .append("Delivery Mode: ")
            .append(deliveryMode)
            .append("\n");

        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Project_Details (project_name_id, student_id, teacher_id, subject_id, school_id, proof_status, complete, delivery_mode, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
        ) {
            pstmt.setInt(1, projectNameId);
            pstmt.setInt(2, studentId);
            pstmt.setInt(3, teacherId);
            pstmt.setInt(4, subjectId);
            pstmt.setInt(5, schoolId);
            pstmt.setString(6, proofStatus);
            pstmt.setBoolean(7, complete);
            pstmt.setString(8, deliveryMode);
            pstmt.setString(9, dateField.getText());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                detailId = rs.getInt(1);
            }
            this.currentDetailId = detailId;
        } catch (SQLException | NumberFormatException e) {
            showLogWindow(
                "Database Error",
                "Error saving data: " + e.getMessage()
            );
            announceToScreenReader("Error saving data: " + e.getMessage());
            return;
        }

        loadDataFromDatabase();
        refreshProjectNames(); // Refresh project names in case new ones were added

        // Switch to Projects tab to show updated table
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JTabbedPane) {
                ((JTabbedPane) c).setSelectedIndex(2);
                break;
            }
        }

        showLogWindow("Submission Confirmation", confirmationLog.toString());
        announceToScreenReader("Data submitted successfully.");
        clearInputFields();
    }

    private void showLogWindow(String title, String message) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Clears the input fields in the application.
     */
    /**
     * Clears all input fields in the application.
     */
    private void clearInputFields() {
        LocalDate currentDate = LocalDate.now();
        // Format the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);
        dateField.setText(formattedDate);

        completeComboBox.setSelectedIndex(0); // Reset to "No"
    }

    /**
     * Generates a PDF report for the specified date range and selected projects.
     *
     * @param startDate         the start date for the report
     * @param endDate           the end date for the report
     * @param selectedProjects  the list of selected projects to include in the report
     */
    /**
     * Generates a PDF report for the specified date range and selected projects.
     * The PDF is written to the user's Downloads folder and opened automatically when possible.
     *
     * Example:
     * <pre>
     * generatePdfReport("2025-08-01","2025-08-31", List.of("Project A","Project B"));
     * </pre>
     *
     * @param startDate the start date for the report (YYYY-MM-DD)
     * @param endDate the end date for the report (YYYY-MM-DD)
     * @param selectedProjects the list of selected projects to include in the report
     * @throws FileNotFoundException when the temporary PDF cannot be created
     * @throws DocumentException when there is an iText error while building the PDF
     * @see HeaderFooterPageEvent
     * @since 2025.07.01
     */
    private void generatePdfReport(
        String startDate,
        String endDate,
        List<String> selectedProjects
    ) {
        String filePath = "";
        try {
            // Get the user's Downloads directory
            String userHome = System.getProperty("user.home");
            String downloadsDir = userHome + "/Downloads/";

            String fileName =
                "LedgerReport_" + startDate + "_to_" + endDate + ".pdf";

            filePath = downloadsDir + fileName;

            Document document = new Document(PageSize.LETTER, 36, 36, 108, 72); // top margin increased to 1.5 inches (108 points)
            PdfWriter writer = PdfWriter.getInstance(
                document,
                new FileOutputStream(filePath)
            );

            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            // Add title "INVOICE"
            Paragraph title = new Paragraph(
                "INVOICE: " + startDate + " to " + endDate,
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA,
                    16,
                    com.itextpdf.text.Font.BOLD
                )
            );
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(28.35f); // 1 cm spacing after header
            title.setSpacingAfter(5.67f); // 0.2 cm spacing before table
            document.add(title);

            // Add table
            List<String[]> trackingData = fetchTrackingDataFromDatabase(
                startDate,
                endDate,
                selectedProjects
            );
            Object[] tableAndTotal = createTrackingTable(
                trackingData,
                document,
                writer,
                event
            );
            PdfPTable table = (PdfPTable) tableAndTotal[0];
            double totalTime = (Double) tableAndTotal[1];

            document.add(table);

            // Add total
            DecimalFormat df = new DecimalFormat("#,##0.00");
            Paragraph totalParagraph = new Paragraph(
                "Total Billed Hours: " + df.format(totalTime),
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA,
                    12,
                    com.itextpdf.text.Font.BOLD
                )
            );
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);

            // Add signature line
            /* Paragraph signatureLine = new Paragraph(
                "Signature: _______________________",
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA,
                    12
                )
            );
            signatureLine.setAlignment(Element.ALIGN_RIGHT);
            signatureLine.setSpacingBefore(50f); // 1 cm spacing after table
            document.add(signatureLine); */
            document.close();
        } catch (DocumentException | FileNotFoundException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error generating PDF: " + e.getMessage()
            );
        }
        try {
            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Desktop not supported. Unable to open PDF automatically."
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "PDF file not found: " + filePath
                );
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "File written to  " + filePath);
        }
    }

    /**
     * Creates a PDF table for the ledger data and returns the table and the total time.
     *
     * @param data      the data to be included in the table
     * @param document  the PDF document to add the table to
     * @param writer    the PDF writer to use
     * @param event     the header/footer event handler
     * @return an array containing the created PDF table and the total time
     * @throws DocumentException if there is an error creating the PDF table
     */
    private Object[] createTable(
        List<String[]> data,
        Document document,
        PdfWriter writer,
        HeaderFooterPageEvent event
    ) throws DocumentException {
        double totalTime = 0.0;
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2, 2, 2, 1 });

        // Add table headers
        for (String header : new String[] {
            "Date",
            "Student",
            "Project",
            "Time",
        }) {
            PdfPCell cell = new PdfPCell(
                new Phrase(
                    header,
                    new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        12,
                        com.itextpdf.text.Font.BOLD
                    )
                )
            );
            cell.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
            cell.setBorderColor(BaseColor.LIGHT_GRAY);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Calculate available height for table
        float totalHeight =
            document.getPageSize().getHeight() -
            document.topMargin() -
            document.bottomMargin();
        float headerHeight = 108; // 1.5 inches
        float titleHeight = 16 + 28.35f + 5.67f; // Title font size + spacing before and after
        float footerHeight = 50; // Adjust based on your footer height
        float signatureHeight = 20; // Height for signature line
        float availableHeight =
            totalHeight -
            headerHeight -
            titleHeight -
            footerHeight -
            signatureHeight;

        float rowHeight = 20; // Adjust based on your row height
        int maxRows = (int) (availableHeight / rowHeight);

        // Add data
        for (int i = 0; i < Math.min(maxRows, data.size()); i++) {
            String[] row = data.get(i);
            System.out.println(
                "Adding row to PDF table: " + java.util.Arrays.toString(row)
            );
            for (int j = 0; j < row.length; j++) {
                PdfPCell pdfCell = new PdfPCell(
                    new Phrase(
                        row[j],
                        new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            10
                        )
                    )
                );
                pdfCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                pdfCell.setPadding(5);
                table.addCell(pdfCell);

                // Sum up the time (assuming it's in the last column)
                if (j == row.length - 1) {
                    try {
                        totalTime += Double.parseDouble(row[j]);
                    } catch (NumberFormatException e) {
                        // Handle parsing error if necessary
                    }
                }
            }
        }

        // Fill remaining rows with empty cells if necessary
        for (int i = data.size(); i < maxRows; i++) {
            for (int j = 0; j < 4; j++) {
                PdfPCell pdfCell = new PdfPCell(new Phrase(" "));
                pdfCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                pdfCell.setPadding(5);
                table.addCell(pdfCell);
            }
        }

        return new Object[] { table, totalTime };
    }

    /**
     * A custom page event handler for adding headers and footers to the PDF report.
     */
    private class HeaderFooterPageEvent extends PdfPageEventHelper {

        private PdfTemplate t;
        private com.itextpdf.text.Image total;

    /**
     * Called when the PDF document is opened. Prepares templates used for
     * total page counts and other document-level resources.
     *
     * @param writer   the PdfWriter for the document
     * @param document the Document being written
     */
    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
    /**
     * iText PDF page event hook: called when a document is opened.
     * @since 2025.07.01
     */
            t = writer.getDirectContent().createTemplate(30, 16);
            try {
                total = com.itextpdf.text.Image.getInstance(t);
                total.setRole(PdfName.ARTIFACT);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
            // Helper functions for inserting or getting IDs
        }

    /**
     * Called at the end of each page. Adds header and footer content.
     *
     * @param writer   the PdfWriter for the document
     * @param document the Document being written
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
    /**
     * iText PDF page event hook: called at the end of each page.
     * @since 2025.07.01
     */
            addHeader(writer, document);
            addFooter(writer, document);
        }

        private void addHeader(PdfWriter writer, Document document) {
            PdfPTable header = new PdfPTable(1);
            try {
                // set defaults
                header.setWidths(new int[] { 24 });
                header.setTotalWidth(527);
                header.setLockedWidth(true);
                header.getDefaultCell().setFixedHeight(108); // 1.5 inches
                header
                    .getDefaultCell()
                    .setBorder(com.itextpdf.text.Rectangle.BOTTOM);
                header.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

                // add text
                PdfPCell text = new PdfPCell();
                text.setPaddingBottom(15);
                text.setPaddingLeft(10);
                text.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
                text.setBorderColor(BaseColor.LIGHT_GRAY);
                text.addElement(
                    new Phrase(
                        " ",
                        new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            12
                        )
                    )
                );
                text.addElement(
                    new Phrase(
                        "Michael Ryan Hunsaker, M.Ed., Ph.D.",
                        new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            12
                        )
                    )
                );
                text.addElement(
                    new Phrase(
                        "Davis School District",
                        new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            12
                        )
                    )
                );
                text.addElement(
                    new Phrase(
                        "Farmington, UT 84025",
                        new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            12
                        )
                    )
                );
                header.addCell(text);

                // write content
                header.writeSelectedRows(
                    0,
                    -1,
                    34,
                    803,
                    writer.getDirectContent()
                );
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        private void addFooter(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(3);
            try {
                // set defaults
                footer.setWidths(new int[] { 24, 2, 1 });
                footer.setTotalWidth(527);
                footer.setLockedWidth(true);
                footer.getDefaultCell().setFixedHeight(40);
                footer
                    .getDefaultCell()
                    .setBorder(com.itextpdf.text.Rectangle.TOP);
                footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

                // add copyright
                footer.addCell(
                    new Phrase(
                        "© 2025 Michael Ryan Hunsaker, M.Ed., Ph.D. All Rights Reserved.",
                        new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            8
                        )
                    )
                );

                // add current page count
                footer
                    .getDefaultCell()
                    .setHorizontalAlignment(Element.ALIGN_RIGHT);
                footer.addCell(
                    new Phrase(
                        String.format("Page %d of", writer.getPageNumber()),
                        new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            8
                        )
                    )
                );

                // add placeholder for total page count
                PdfPCell totalPageCount = new PdfPCell(total);
                totalPageCount.setBorder(com.itextpdf.text.Rectangle.TOP);
                totalPageCount.setBorderColor(BaseColor.LIGHT_GRAY);
                footer.addCell(totalPageCount);

                // write page
                PdfContentByte canvas = writer.getDirectContent();
                canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
                footer.writeSelectedRows(0, -1, 34, 50, canvas);
                canvas.endMarkedContentSequence();
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

    /**
     * Called when the PDF document is closed. Finalizes the total page
     * count template.
     *
     * @param writer   the PdfWriter for the document
     * @param document the Document being written
     */
    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
    /**
     * iText PDF page event hook: called when the document is closed.
     * @since 2025.07.01
     */
            int totalLength = String.valueOf(writer.getPageNumber()).length();
            int totalWidth = totalLength * 5;
            ColumnText.showTextAligned(
                t,
                Element.ALIGN_RIGHT,
                new Phrase(
                    String.valueOf(writer.getPageNumber()),
                    new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        8
                    )
                ),
                totalWidth,
                6,
                0
            );
        }
    }

    /**
     * Fetches the ledger data from the database based on the specified start date, end date, and selected projects.
     *
     * @param startDate         the start date for the data
     * @param endDate           the end date for the data
     * @param selectedProjects  the list of selected projects to include
     * @return the fetched ledger data
     */
    private List<String[]> fetchTrackingDataFromDatabase(
        String startDate,
        String endDate,
        List<String> selectedProjects
    ) {
        List<String[]> trackingData = new ArrayList<>();
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT pt.updated AS date, s.name AS student, pn.name AS project_name, pt.time " +
                "FROM Project_Tracking pt " +
                "JOIN Project_Name pn ON pt.project_name_id = pn.project_name_id " +
                "JOIN Students s ON pt.student_id = s.student_id " +
                "WHERE pn.name IN (" +
                String.join(
                    ",",
                    Collections.nCopies(selectedProjects.size(), "?")
                ) +
                ") AND pt.updated BETWEEN ? AND ? ORDER BY pt.updated"
            );
        ) {
            // Set project name parameters first
            for (int i = 0; i < selectedProjects.size(); i++) {
                pstmt.setString(i + 1, selectedProjects.get(i));
            }
            // Then set date parameters
            pstmt.setString(selectedProjects.size() + 1, startDate);
            pstmt.setString(selectedProjects.size() + 2, endDate);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                    rs.getString("date"),
                    rs.getString("student"),
                    rs.getString("project_name"),
                    rs.getString("time"),
                };
                trackingData.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error fetching tracking data: " + e.getMessage()
            );
        }
        System.out.println(
            "Tracking data rows fetched: " + trackingData.size()
        );
        return trackingData;
    }

    /**
     * Retrieves a localized string from the resource bundle.
     * @param key The key for the string in the resource bundle.
     * @return The localized string.
     */
    private String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("Missing resource key: " + key);
            return "!" + key + "!"; // Return a clear indicator if the key is missing
        }
    }

    // Helper functions for inserting or getting IDs

    /**
     * Get or insert a school and return its id.
     * @param schoolName the name of the school
     * @return school id or -1 if not found
     * @throws SQLException on DB errors
     * @since 2025.07.01
     */
    private int getOrInsertSchoolId(String schoolName) throws SQLException {
        if (schoolName == null || schoolName.isEmpty()) return -1;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT school_id FROM Schools WHERE name = ?"
            );
            select.setString(1, schoolName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("school_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Schools (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, schoolName);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
        } catch (SQLException e) {
            System.err.println("School insert/get error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get or insert an LEA and return its id.
     * @param leaName the lea name
     * @return lea id or -1
     * @throws SQLException on DB errors
     * @since 2025.07.01
     */
    private int getOrInsertLeaId(String leaName) throws SQLException {
        if (leaName == null || leaName.isEmpty()) return -1;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT lea_id FROM LEAs WHERE name = ?"
            );
            select.setString(1, leaName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("lea_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO LEAs (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, leaName);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
        } catch (SQLException e) {
            System.err.println("LEA insert/get error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get or insert a teacher and return its id.
     * @param teacherName teacher name
     * @param schoolId associated school id
     * @return teacher id or -1
     * @throws SQLException on DB errors
     * @since 2025.07.01
     */
    private int getOrInsertTeacherId(String teacherName, int schoolId) throws SQLException {
        if (teacherName == null || teacherName.isEmpty()) return -1;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT teacher_id FROM Teachers WHERE name = ?"
            );
            select.setString(1, teacherName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("teacher_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Teachers (name, school_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, teacherName);
            insert.setInt(2, schoolId);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
        } catch (SQLException e) {
            System.err.println("Teacher insert/get error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get or insert a TVI and return its id.
     * @param tviName tvi name
     * @param schoolId school id
     * @return tvi id or -1
     * @throws SQLException on DB errors
     * @since 2025.07.01
     */
    private int getOrInsertTviId(String tviName, int schoolId) throws SQLException {
        if (tviName == null || tviName.isEmpty()) return -1;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT tvi_id FROM TVIs WHERE name = ?"
            );
            select.setString(1, tviName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("tvi_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO TVIs (name, school_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, tviName);
            insert.setInt(2, schoolId);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
        } catch (SQLException e) {
            System.err.println("TVI insert/get error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get or insert a student and return its id.
     * @param studentName student name
     * @param schoolId school id
     * @param leaId lea id
     * @return student id or -1
     * @throws SQLException on DB errors
     * @since 2025.07.01
     */
    private int getOrInsertStudentId(
        String studentName,
        int schoolId,
        int leaId
    ) throws SQLException {
        if (studentName == null || studentName.isEmpty()) return -1;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT student_id FROM Students WHERE name = ?"
            );
            select.setString(1, studentName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("student_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Students (name, school_id, lea_id) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, studentName);
            insert.setInt(2, schoolId);
            insert.setInt(3, leaId);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
        } catch (SQLException e) {
            System.err.println("Student insert/get error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get or insert a subject and return its id.
     * @param subjectName subject name
     * @param teacherId teacher id
     * @param schoolId school id
     * @return subject id or -1
     * @throws SQLException on DB errors
     * @since 2025.07.01
     */
    private int getOrInsertSubjectId(
        String subjectName,
        int teacherId,
        int schoolId
    ) throws SQLException {
        if (subjectName == null || subjectName.isEmpty()) return -1;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT subject_id FROM Subjects WHERE name = ?"
            );
            select.setString(1, subjectName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("subject_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Subjects (name, teacher_id, school_id) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, subjectName);
            insert.setInt(2, teacherId);
            insert.setInt(3, schoolId);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
        } catch (SQLException e) {
            System.err.println("Subject insert/get error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Insert or get the project name ID for a given project name.
     * @param projectName the project name
     * @param date the date string to associate when creating
     * @return the project_name_id
     * @throws SQLException when DB operations fail
     * @since 2025.07.01
     */
    private int getOrInsertProjectNameId(String projectName, String date) throws SQLException {
        if (projectName == null || projectName.isEmpty()) {
            logger.error(
                "getOrInsertProjectNameId: projectName is null or empty."
            );
            return -1;
        }
        if (date == null || date.isEmpty()) {
            date = java.time.LocalDate.now().toString(); // fallback to today
            logger.info(
                "getOrInsertProjectNameId: date was null/empty, using current date: {}",
                date
            );
        }
    try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT project_name_id FROM Project_Name WHERE name = ?"
            );
            select.setString(1, projectName);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("project_name_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Project_Name (name, date) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, projectName);
            insert.setString(2, date);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
    }
    return -1;
    }

    // --- Logging methods ---

    /**
     * Get or insert a media type and return its id.
     * @param mediaType media type name
     * @param projectNameId associated project name id
     * @return media type id or -1
     * @throws SQLException on DB errors
     * @since 2025.07.01
     */
    private int getOrInsertMediaTypeId(String mediaType, int projectNameId) throws SQLException {
        if (
            mediaType == null || mediaType.isEmpty() || projectNameId == -1
        ) return -1;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT media_type_id FROM Media_Types WHERE type = ? AND project_name_id = ?"
            );
            select.setString(1, mediaType);
            select.setInt(2, projectNameId);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return rs.getInt("media_type_id");
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Media_Types (type, project_name_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, mediaType);
            insert.setInt(2, projectNameId);
            insert.executeUpdate();
            ResultSet gen = insert.getGeneratedKeys();
            if (gen.next()) return gen.getInt(1);
        } catch (SQLException e) {
            System.err.println(
                "Media_Type insert/get error: " + e.getMessage()
            );
        }
        return -1;
    }

    private void insertStudentTeacher(int studentId, int teacherId) {
        if (studentId == -1 || teacherId == -1) return;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement select = conn.prepareStatement(
                "SELECT * FROM Student_Teachers WHERE student_id = ? AND teacher_id = ?"
            );
            select.setInt(1, studentId);
            select.setInt(2, teacherId);
            ResultSet rs = select.executeQuery();
            if (rs.next()) return; // already exists
            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Student_Teachers (student_id, teacher_id) VALUES (?, ?)"
            );
            insert.setInt(1, studentId);
            insert.setInt(2, teacherId);
            insert.executeUpdate();
        } catch (SQLException e) {
            System.err.println(
                "Student_Teacher insert/get error: " + e.getMessage()
            );
        }
    }
}
