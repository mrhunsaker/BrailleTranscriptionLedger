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
import com.formdev.flatlaf.intellijthemes.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The `LedgerGUI` class is a Swing-based application that provides a graphical user interface for managing a ledger.
 * The ledger is used to track the work performed by students on various projects, including the date, student, subject,
 * school, project, time spent, and completion status.
 * <p>
 * The application allows the user to input new ledger entries, view the existing entries in a table, and generate a
 * PDF report for a specified date range and selected projects.
 * <p>
 * The application uses a SQLite database to store the ledger data and supports various features, such as:
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
 * @version 2024.0.0
 */

public class LedgerGUI extends JFrame {

    /**
     * A static map that stores the available IntelliJ IDEA-inspired themes for the application.
     */
    private static final Map<
        String,
        Class<? extends LookAndFeel>
    > INTELLIJ_THEMES = new TreeMap<>();

    static {
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
    private final JTextField updatedField = new JTextField(
        LocalDate.now().toString()
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
     * A combo box for selecting the media type (e.g., braille, graphics, DAISY).
     */
    private JComboBox<String> mediaTypeField;
    /**
     * A combo box for selecting the proof status (e.g., no, in progress, revising, done).
     */
    private JComboBox<String> proofStatusField = new JComboBox<>(
        loadOptionsFromFile("proof_status.json", "proof_status")
    );
    // Logging for proofStatusField moved to constructor
    /**
     * A combo box for selecting the delivery mode (e.g., delivery, pick up, email).
     */
    private JComboBox<String> deliveryModeField = new JComboBox<>(
        loadOptionsFromFile("delivery_mode.json", "delivery_mode")
    );
    // Logging for deliveryModeField moved to constructor
    /**
     * A combo box for selecting the project name.
     */
    private final JComboBox<String> projectNameField;
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
     * A checkbox for indicating the completion status.
     */
    private final JCheckBox completeCheckBox;
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
     * The URL of the SQLite database used by the application.
     */
    /**
     * The URL for connecting to the SQLite database.
     */
    private static final String DB_URL = "jdbc:sqlite:ledger.db";

    /**
     * Constructs a new `LedgerGUI` instance and initializes the application's components.
     */
    public LedgerGUI() {
        initializeDatabaseTables();
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabbedPane = new JTabbedPane();
        setLayout(new BorderLayout());

        // Create MenuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu themeMenu = new JMenu("Themes");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(
                LedgerGUI.this,
                """
                Accessible Document Generation Ledger
                Version 2025.0.0
                \u00a9 2025 Michael Ryan Hunsaker, M.Ed., Ph.D.
                All rights reserved.""",
                "About",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        for (String themeName : INTELLIJ_THEMES.keySet()) {
            JMenuItem item = new JMenuItem(themeName);
            item.addActionListener(e -> setIntelliJTheme(themeName));
            themeMenu.add(item);
        }
        fileMenu.add(aboutMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        setJMenuBar(menuBar); // Use setJMenuBar() to add the menu bar to the JFrame
        // Input Panel

        JPanel ProjectSetup = new JPanel(new GridLayout(0, 2, 10, 10));
        ProjectSetup.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ProjectTracking = new JPanel(new GridLayout(0, 2, 10, 10));
        ProjectTracking.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        addLabelAndField(
            ProjectTracking,
            "<html>Date: <br><i>(YYYY-MM-DD)</i></html>",
            dateField
        );
        dateField.setText(LocalDate.now().toString());

        addLabelAndField(
            ProjectSetup,
            "<html>Date: <br><i>(YYYY-MM-DD)</i></html>",
            dateField
        );
        String[] studentOptions = loadOptionsFromFile(
            "students.json",
            "students"
        );
        String[] teacherOptions = loadOptionsFromFile(
            "teachers.json",
            "teachers"
        );
        String[] tviOptions = loadOptionsFromFile("tvis.json", "tvis");
        String[] mediaTypeOptions = { "Braille", "Large Print", "Audio" };
        dateField.setText(LocalDate.now().toString());
        addLabelAndField(
            ProjectSetup,
            "<html>Student <br> <i>Select Initials from dropdown list</i></html>",
            studentField = new JComboBox<>(studentOptions)
        );
        String[] subjectOptions = loadOptionsFromFile(
            "subjects.json",
            "subjects"
        );
        subjectField = new JComboBox<>(subjectOptions);
        addLabelAndField(
            ProjectSetup,
            "<html>Academic Subject<br><i>Select Subject from Dropdown List</i></html>",
            subjectField
        );
        String[] schoolOptions = loadOptionsFromFile("schools.json", "schools");
        schoolField = new JComboBox<>(
            loadOptionsFromFile("schools.json", "schools")
        );

        addLabelAndField(ProjectSetup, "School", schoolField);

        ProjectTracking = new JPanel(new GridLayout(0, 2, 10, 10));
        ProjectTracking.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
        System.out.println("Initializing Proof Status field...");
        if (proofStatusField.getItemCount() == 0) {
            System.err.println(
                "Error: Proof Status field is empty after loading from proof_status.json."
            );
        } else {
            System.out.println(
                "Proof Status field loaded successfully with " +
                proofStatusField.getItemCount() +
                " items."
            );
        }
        addLabelAndField(ProjectTracking, "Proof Status", proofStatusField);
        //addLabelAndField(inputPanel, "Delivery Mode", deliveryModeField);

        System.out.println("Initializing Teachers field...");
        teacherField = new JList<>(new String[] { "Teacher 1", "Teacher 2" });
        if (teacherField == null) {
            System.err.println("Error: Teachers field is null.");
        }
        addLabelAndField(ProjectSetup, "Teachers", teacherField);
        teacherField.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );

        System.out.println("Initializing TVIs field...");
        tviField = new JList<>(new String[] { "TVI 1", "TVI 2" });
        if (tviField == null) {
            System.err.println("Error: TVIs field is null.");
        }
        addLabelAndField(ProjectSetup, "TVIs", tviField);
        tviField.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );
        tviField.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );
        System.out.println("Initializing Media Type field...");
        if (mediaTypeField == null) {
            System.err.println("Error: Media Type field is null.");
        }
        mediaTypeField = new JComboBox<>(
            loadOptionsFromFile("media_type.json", "media_types")
        );
        // Logging for mediaTypeField moved to constructor
        addLabelAndField(ProjectSetup, "Media Type", mediaTypeField);
        System.out.println("Initializing Project Type field...");
        projectField = new JComboBox<>(getProjects());
        if (projectField == null) {
            System.err.println("Error: Project Type field is null.");
        }
        addLabelAndField(ProjectSetup, "Project Type", projectField);
        System.out.println("Initializing Time field...");
        timeField = new JTextField();
        if (timeField == null) {
            System.err.println("Error: Time field is null.");
        }
        addLabelAndField(
            ProjectTracking,
            "<html>Time: <br>(Rounded UP to Nearest .25 hr fter 5 min)</html>",
            timeField
        );
        System.out.println("Initializing Updated field...");
        System.out.println("Updated field...");
        System.out.println("Initializing Updated field...");
        if (updatedField == null) {
            System.err.println("Error: Updated field is null.");
        }
        addLabelAndField(
            ProjectTracking,
            "<html>Updated: <br>(YYYY-MM-DD)</html>",
            updatedField
        );
        ProjectTracking.add(new JLabel("Updated Field")); // Add updatedField to ProjectTracking
        ProjectTracking.add(updatedField);
        addLabelAndField(ProjectTracking, "Proof Status", proofStatusField);
        addLabelAndField(ProjectSetup, "Delivery Mode", deliveryModeField);
        JLabel completeLabel = new JLabel("Complete:");
        completeCheckBox = new JCheckBox();
        completeCheckBox.setEnabled(true); // Activate the checkbox
        completeLabel.setLabelFor(completeCheckBox);
        ProjectTracking.add(completeLabel);
        ProjectTracking.add(completeCheckBox);
        completeCheckBox.setMnemonic(KeyEvent.VK_C);
        completeCheckBox
            .getAccessibleContext()
            .setAccessibleDescription("Check if the task is complete");
        submitButton = new JButton("Submit");
        submitButton.setMnemonic(KeyEvent.VK_S);
        submitButton.addActionListener(e -> submitData());
        submitButton
            .getAccessibleContext()
            .setAccessibleDescription(
                "Submit the entered data to the database"
            );
        generatePdfButton = new JButton("Generate PDF Report");
        generatePdfButton.setMnemonic(KeyEvent.VK_G);
        generatePdfButton.addActionListener(e -> showDateRangeDialog());
        JPanel projectTrackingPanel = new JPanel(new BorderLayout());
        projectTrackingPanel.add(generatePdfButton, BorderLayout.SOUTH);
        generatePdfButton
            .getAccessibleContext()
            .setAccessibleDescription(
                "Generate a PDF report for the project tracking data"
            );
        generatePdfButton
            .getAccessibleContext()
            .setAccessibleDescription(
                "Generate a PDF report for a specified date range"
            );

        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 10, 0)
        );
        buttonPanel.add(submitButton);
        buttonPanel.add(generatePdfButton);
        ProjectSetup.add(buttonPanel);

        JPanel projectSetupPanel = new JPanel(new BorderLayout());
        projectSetupPanel.add(ProjectSetup, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new String[] {
                "Date",
                "Student",
                "Subject",
                "School",
                "Project",
                "Time",
            },
            0
        );
        dataTable = new JTable(tableModel);
        dataTable
            .getAccessibleContext()
            .setAccessibleDescription("Table showing ledger entries");
        dataTable.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        projectSetupPanel.add(scrollPane, BorderLayout.SOUTH);

        JPanel filePickerPanel = new JPanel(new GridLayout(0, 2, 10, 50));
        String[] fileTypes = {
            "originals",
            "graphics",
            "ebraille",
            "DAISY",
            "braille",
            "accessibleDocument",
            "3dprint",
        };
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
        tabbedPane.addTab("Project Setup", projectSetupPanel);
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
        DefaultTableModel trackingTableModel = new DefaultTableModel(
            new String[] {
                "Date",
                "Student",
                "Subject",
                "School",
                "Project",
                "Time",
            },
            0
        );
        JTable trackingDataTable = new JTable(trackingTableModel);
        trackingDataTable
            .getAccessibleContext()
            .setAccessibleDescription("Table showing tracking entries");
        trackingDataTable.setEnabled(false);
        JScrollPane trackingScrollPane = new JScrollPane(trackingDataTable);
        projectTrackingPanel.add(ProjectTracking, BorderLayout.NORTH);
        projectTrackingPanel.add(ProjectTracking, BorderLayout.NORTH);

        tabbedPane.addTab("Project Tracking", projectTrackingPanel);
        addLabelAndField(
            ProjectTracking,
            "Project Name",
            projectNameField = new JComboBox<>(getProjectNames())
        );
        addLabelAndField(
            ProjectTracking,
            "Element of Project",
            projectElementField = new JComboBox<>(
                new String[] {
                    "Braille",
                    "Graphics",
                    "Formatting",
                    "Proofreading",
                    "Embossing",
                }
            )
        );
        addLabelAndField(
            ProjectTracking,
            "Time (HH:MM)",
            projectTimeField = new JTextField()
        );

        JButton addTrackingButton = new JButton("Add Tracking Info");
        addTrackingButton.addActionListener(e -> addTrackingInfo());
        ProjectTracking.add(addTrackingButton);

        projectTrackingPanel.add(ProjectTracking, BorderLayout.NORTH);
        tabbedPane.addTab("Project Tracking", projectTrackingPanel);

        // Set up focus traversal
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        setFocusCycleRoot(true);
    }

    /**
     * Adds a label and a field component to the specified panel.
     *
     * @param panel   the panel to add the label and field to
     * @param labelText the text for the label
     * @param field   the field component to add
     */
    private void addLabelAndField(
        JPanel panel,
        String labelText,
        JComponent field
    ) {
        JLabel label = new JLabel(labelText);
        if (field == null) {
            throw new IllegalArgumentException(
                "Field component cannot be null"
            );
        }
        label.setLabelFor(field);
        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.EAST);
        if (field.getAccessibleContext() != null) {
            field
                .getAccessibleContext()
                .setAccessibleDescription("Enter " + labelText.toLowerCase());
        }
    }

    /**
     * Cleans the input by removing any characters that might cause issues with SQLite.
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
            BufferedReader reader = new BufferedReader(new FileReader(fileName))
        ) {
            System.out.println(
                "Attempting to load options from file: " + fileName
            );
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            System.out.println(
                "File content loaded: " + jsonContent.toString()
            );
            JSONObject jsonObject = new JSONObject(jsonContent.toString());
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            System.out.println("Extracting options using key: " + key);
            for (int i = 0; i < jsonArray.length(); i++) {
                options.add(jsonArray.getString(i));
            }
            System.out.println("Options loaded successfully: " + options);
        } catch (IOException | JSONException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading options from file: " + fileName
            );
            System.err.println(
                "Exception occurred while loading options: " + e.getMessage()
            );
        }
        return options.toArray(new String[0]);
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
    /**
     * Displays a dialog for selecting a date range to generate a report.
     */
    private void showDateRangeDialog() {
        JTextField startDateField = new JTextField(getPreviousMonth16th(), 10);
        JTextField endDateField = new JTextField(getCurrentMonth15th(), 10);
        String[] studentOptions = loadOptionsFromFile(
            "students.json",
            "students"
        );
        JComboBox studentMaterial = new JComboBox<>(studentOptions);
        // Create checkboxes for project options
        String[] projectOptions = {
            "UEB Literary Transcription",
            "UEB Technical Transcription",
            "Tactile Graphics Generation",
            "Large Print Generation",
            "3D Print Rendering",
            "3D Print Production",
        };
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
        // Set the look and feel to the system look and feel
        String defaultTheme = "Cyan Light"; // or any other theme name from the map
        try {
            UIManager.setLookAndFeel(
                INTELLIJ_THEMES.get(defaultTheme)
                    .getDeclaredConstructor()
                    .newInstance()
            );
            UIManager.put(
                "defaultFont",
                new FontUIResource(
                    new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 24)
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

        SwingUtilities.invokeLater(() -> {
            new LedgerGUI().setVisible(true);
        });
    }

    /**
     * Sets the specified IntelliJ IDEA-inspired theme for the application.
     *
     * @param themeName the name of the theme to set
     */
    /**
     * Sets the IntelliJ IDEA-inspired theme for the application.
     *
     * @param themeName The name of the theme to apply.
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
                        new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 24)
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
     * Initializes the SQLite database used by the application.
     */
    /**
     * Initializes the database connection and ensures the database is ready for use.
     */
    private void initializeDatabase() {
        loadDataFromFiles();
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement()
        ) {
            // Database initialization logic here
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
        return new String[] {
            "UEB Literary Transcription",
            "UEB Technical Transcription",
            "Tactile Graphics Generation",
            "Large Print Generation",
            "3D Print Rendering",
            "3D Print Production",
        };
    }

    /**
     * Loads data from JSON files into GUI components.
     */
    /**
     * Loads data from external files into the application.
     */
    private void loadDataFromFiles() {
        try {
            String[] students = loadOptionsFromFile(
                "students.json",
                "students"
            );
            studentField.setModel(new DefaultComboBoxModel<>(students));

            String[] teachers = loadOptionsFromFile(
                "teachers.json",
                "teachers"
            );
            teacherField.setListData(teachers);

            String[] tvis = loadOptionsFromFile("tvis.json", "tvis");
            tviField.setListData(tvis);

            String[] subjects = loadOptionsFromFile(
                "subjects.json",
                "subjects"
            );
            subjectField.setModel(new DefaultComboBoxModel<>(subjects));

            String[] schools = loadOptionsFromFile("schools.json", "schools");
            schoolField.setModel(new DefaultComboBoxModel<>(schools));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading data: " + e.getMessage()
            );
        }
    }

    private String[] getProjectNames() {
        return new String[] { "Project A", "Project B", "Project C" };
    }

    private int getProjectId(String projectName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT project_name_id FROM Project_Name WHERE name = ?"
            )
        ) {
            pstmt.setString(1, projectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("project_name_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching project ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getStudentId(String studentName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT student_id FROM Students WHERE name = ?"
            )
        ) {
            pstmt.setString(1, studentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("student_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getTeacherId(String teacherName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT teacher_id FROM Teachers WHERE name = ?"
            )
        ) {
            pstmt.setString(1, teacherName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("teacher_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching teacher ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getSchoolId(String schoolName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT school_id FROM Schools WHERE name = ?"
            )
        ) {
            pstmt.setString(1, schoolName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("school_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching school ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getSubjectId(String subjectName) {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT subject_id FROM Subjects WHERE name = ?"
            )
        ) {
            pstmt.setString(1, subjectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("subject_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subject ID: " + e.getMessage());
        }
        return -1; // Return -1 if not found
    }

    private int getTviId(String tviName) {
        // Stub implementation
        return 1;
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
        // Stub implementation
        return new Object[] { new PdfPTable(1), 0.0 };
    }

    private void initializeDatabaseTables() {
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement()
        ) {
            String sqlProjectTracking =
                "CREATE TABLE IF NOT EXISTS Project_Tracking (" +
                "tracking_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_name TEXT NOT NULL, " +
                "element TEXT NOT NULL, " +
                "time TEXT NOT NULL, " +
                "date TEXT NOT NULL)";
            try {
                stmt.execute(sqlProjectTracking);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Project_Tracking table: " + e.getMessage()
                );
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
            String sqlProjectName =
                "CREATE TABLE IF NOT EXISTS Project_Name (" +
                "project_name_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "date TEXT NOT NULL)";
            try {
                stmt.execute(sqlProjectName);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Project_Name table: " + e.getMessage()
                );
            }
            String sqlSchools =
                "CREATE TABLE IF NOT EXISTS Schools (" +
                "school_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL)";
            try {
                stmt.execute(sqlSchools);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Schools table: " + e.getMessage()
                );
            }
            String sqlTeachers =
                "CREATE TABLE IF NOT EXISTS Teachers (" +
                "teacher_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "school_id INTEGER, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE SET NULL)";
            try {
                stmt.execute(sqlTeachers);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Teachers table: " + e.getMessage()
                );
            }
            String sqlSubjects =
                "CREATE TABLE IF NOT EXISTS Subjects (" +
                "subject_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "teacher_id INTEGER NOT NULL, " +
                "school_id INTEGER NOT NULL, " +
                "FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE CASCADE)";
            try {
                stmt.execute(sqlSubjects);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Subjects table: " + e.getMessage()
                );
            }
            String sqlTVIs =
                "CREATE TABLE IF NOT EXISTS TVIs (" +
                "tvi_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
                "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT, " +
                "school_id INTEGER NOT NULL, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE SET NULL)";
            try {
                stmt.execute(sqlStudents);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Students table: " + e.getMessage()
                );
            }
            String sqlStudentTeachers =
                "CREATE TABLE IF NOT EXISTS Student_Teachers (" +
                "student_id INTEGER NOT NULL, " +
                "teacher_id INTEGER NOT NULL, " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE CASCADE)";
            try {
                stmt.execute(sqlStudentTeachers);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Student_Teachers table: " + e.getMessage()
                );
            }
            String sqlStudentTVIs =
                "CREATE TABLE IF NOT EXISTS Student_TVIs (" +
                "student_id INTEGER NOT NULL, " +
                "tvi_id INTEGER NOT NULL, " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (tvi_id) REFERENCES TVIs(tvi_id) ON DELETE CASCADE)";
            try {
                stmt.execute(sqlStudentTVIs);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Student_TVIs table: " + e.getMessage()
                );
            }
            String sqlMediaTypes =
                "CREATE TABLE IF NOT EXISTS Media_Types (" +
                "media_type_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
                "detail_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_name_id INTEGER, " +
                "student_id INTEGER, " +
                "teacher_id INTEGER, " +
                "subject_id INTEGER, " +
                "school_id INTEGER, " +
                "minutes_spent INTEGER DEFAULT 0, " +
                "proof_status TEXT CHECK(proof_status IN ('no', 'in progress', 'revising', 'done')), " +
                "complete BOOLEAN DEFAULT 0, " +
                "delivered BOOLEAN DEFAULT 0, " +
                "delivery_mode TEXT CHECK(delivery_mode IN ('email', 'pick up', 'drop off')), " +
                "subject TEXT CHECK(subject IN ('art', 'history', 'math', 'ela', 'creative writing', 'coding', 'computer science', 'health', 'social studies', 'music')), " +
                "time TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "notes TEXT, " +
                "FOREIGN KEY (project_name_id) REFERENCES Project_Name(project_name_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (school_id) REFERENCES Schools(school_id) ON DELETE SET NULL)";
            try {
                stmt.execute(sqlProjectDetails);
            } catch (SQLException e) {
                System.err.println(
                    "Error creating Project_Details table: " + e.getMessage()
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error initializing database: " + e.getMessage()
            );
        }
    }

    /**
     * Loads the existing ledger data from the database and populates the data table.
     */
    /**
     * Loads data from the database into the application.
     */
    private JPanel ProjectTracking;

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT pd.date, s.name AS student, sub.name AS subject, sch.name AS school, pn.name AS project, t.name AS teacher, pd.time " +
                "FROM Project_Details pd " +
                "LEFT JOIN Students s ON pd.student_id = s.student_id " +
                "LEFT JOIN Subjects sub ON pd.subject_id = sub.subject_id " +
                "LEFT JOIN Schools sch ON pd.school_id = sch.school_id " +
                "LEFT JOIN Project_Name pn ON pd.project_name_id = pn.project_name_id " +
                "LEFT JOIN Teachers t ON pd.teacher_id = t.teacher_id"
            )
        ) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("date"),
                    rs.getString("student"),
                    rs.getString("subject"),
                    rs.getString("school"),
                    rs.getString("project"),
                    rs.getString("time"),
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            System.err.println(
                "Error executing query: SELECT date, student, subject, school, project, time FROM Project_Details"
            );
            System.err.println("SQL Error: " + e.getMessage());
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
        String projectName = projectNameField.getSelectedItem().toString();
        String projectElement = projectElementField
            .getSelectedItem()
            .toString();
        String projectTime = projectTimeField.getText();

        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Project_Tracking (project_name, element, time) VALUES (?, ?, ?)"
            )
        ) {
            pstmt.setString(1, projectName);
            pstmt.setString(2, projectElement);
            pstmt.setString(3, projectTime);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(
                this,
                "Tracking info added successfully."
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error adding tracking info: " + e.getMessage()
            );
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
        String date = dateField.getText();
        int schoolId = getSchoolId(schoolField.getSelectedItem().toString());
        int studentId = getStudentId(studentField.getSelectedItem().toString());
        String subject = subjectField.getSelectedItem().toString();
        String notes = "";
        int projectNameId = getProjectId(
            projectField.getSelectedItem().toString()
        );
        String time = timeField.getText();
        String updated = updatedField.getText();
        boolean complete = completeCheckBox.isSelected();
        int teacherId = getTeacherId(
            teacherField.getSelectedValue().toString()
        );
        String tvi = tviField.getSelectedValue().toString();
        String mediaType = mediaTypeField.getSelectedItem().toString();
        int subjectId = getSubjectId(subject);
        String proofStatus = proofStatusField.getSelectedItem().toString();
        String deliveryMode = deliveryModeField.getSelectedItem().toString();

        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO Project_Details (project_name_id, student_id, proof_status, complete, delivered, delivery_mode, subject, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            )
        ) {
            pstmt.setInt(
                1,
                getProjectId(projectField.getSelectedItem().toString())
            );
            pstmt.setInt(
                2,
                getStudentId(studentField.getSelectedItem().toString())
            );
            pstmt.setString(3, proofStatus);
            pstmt.setBoolean(4, complete);
            pstmt.setBoolean(5, false); // Default delivered status
            pstmt.setString(6, deliveryMode);
            pstmt.setString(7, subject);
            String sqlNotes = notes.replace("\n", "\\n");
            pstmt.setString(8, sqlNotes);
            pstmt.executeUpdate();

            // Insert into Student_Teachers
            PreparedStatement teacherStmt = conn.prepareStatement(
                "INSERT INTO Student_Teachers (student_id, teacher_id) VALUES (?, ?)"
            );
            teacherStmt.setInt(
                1,
                getStudentId(studentField.getSelectedItem().toString())
            );
            teacherStmt.setInt(
                2,
                getTeacherId(teacherField.getSelectedValue().toString())
            );
            teacherStmt.executeUpdate();

            // Insert into Student_TVIs
            PreparedStatement tviStmt = conn.prepareStatement(
                "INSERT INTO Student_TVIs (student_id, tvi_id) VALUES (?, ?)"
            );
            tviStmt.setInt(
                1,
                getStudentId(studentField.getSelectedItem().toString())
            );
            tviStmt.setInt(2, getTviId(tvi));
            tviStmt.executeUpdate();

            // Insert into Media_Types
            PreparedStatement mediaStmt = conn.prepareStatement(
                "INSERT INTO Media_Types (type, project_name_id) VALUES (?, ?)"
            );
            mediaStmt.setString(1, mediaType);
            mediaStmt.setInt(
                2,
                getProjectId(projectField.getSelectedItem().toString())
            );
            mediaStmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error saving data: " + e.getMessage()
            );
            return;
        }

        loadDataFromDatabase();
        JOptionPane.showMessageDialog(this, "Data submitted successfully.");
        clearInputFields();
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

        completeCheckBox.setSelected(false);
    }

    /**
     * Generates a PDF report for the specified date range and selected projects.
     *
     * @param startDate         the start date for the report
     * @param endDate           the end date for the report
     * @param selectedProjects  the list of selected projects to include in the report
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
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2, 2, 2, 2, 2, 1 });

        // Add table headers
        for (String header : new String[] {
            "Date",
            "Student",
            "Subject",
            "School",
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
            for (int j = 0; j < 6; j++) {
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

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            t = writer.getDirectContent().createTemplate(30, 16);
            try {
                total = com.itextpdf.text.Image.getInstance(t);
                total.setRole(PdfName.ARTIFACT);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
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

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
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
                "SELECT project_name, element, time FROM Project_Tracking WHERE project_name IN (" +
                String.join(
                    ",",
                    Collections.nCopies(selectedProjects.size(), "?")
                ) +
                ") AND date BETWEEN ? AND ? ORDER BY project_name"
            )
        ) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);

            // Set project parameters
            for (int i = 0; i < selectedProjects.size(); i++) {
                pstmt.setString(i + 3, selectedProjects.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                    rs.getString("project_name"),
                    rs.getString("element"),
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
        return trackingData;
    }
}
